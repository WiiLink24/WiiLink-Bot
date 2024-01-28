package com.wiilink24.bot.events;

import com.wiilink24.bot.utils.AFKStatus;
import com.wiilink24.bot.Bot;
import com.wiilink24.bot.Database;
import com.wiilink24.bot.utils.MessageCache;
import io.sentry.Sentry;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.events.user.UserTypingEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.format.DateTimeFormatter;

public class Listener implements EventListener {
    private final MessageCache cache;

    private final Bot bot;

    public Listener(Bot bot) {
        this.cache = new MessageCache();
        this.bot = bot;
    }

    @Override
    public void onEvent(@NotNull GenericEvent event) {
        if (event instanceof MessageReceivedEvent)
        {
            Message message = ((MessageReceivedEvent)event).getMessage();

            // Check if this was in a DM
            if (!message.isFromGuild())
                return;

            if (message.getGuild().getId().equals(bot.mainServerId()))
                if(!message.getAuthor().isBot())
                    this.cache.putMessage(message);
        }
        else if (event instanceof MessageDeleteEvent delete) {
            if (delete.getGuild().getId().equals(bot.mainServerId())) {
                MessageCache.CachedMessage message = this.cache.pullMessage(delete.getGuild(), delete.getMessageIdLong());

                if (message != null) {
                    String oldMessage = message.getContentRaw() + "\n";
                    oldMessage = message.getAttachments().stream().map(attachment -> attachment.getUrl() + "\n").reduce(oldMessage, String::concat);
                    EmbedBuilder embed = new EmbedBuilder()
                            .setColor(Color.RED)
                            .setDescription(oldMessage);

                    String topMessage = bot.timestamp()
                            + " :x: **"
                            + message.getUsername()
                            + "**#"
                            + message.getDiscriminator()
                            + " (ID:"
                            + message.getAuthorId()
                            + ")'s message was deleted from "
                            + delete.getChannel().getAsMention()
                            + ":";
                    sendMessage(event, topMessage, embed);
                }
            }
        }
        else if (event instanceof MessageUpdateEvent) {
            Message message = ((MessageUpdateEvent)event).getMessage();

            if (message.getGuild().getId().equals(bot.mainServerId())) {
                if (!message.getAuthor().isBot()) {
                    // Store old message in a variable then log the new message
                    MessageCache.CachedMessage old = this.cache.putMessage(message);

                    if (old != null) {
                        String oldMessage = old.getContentRaw() + "\n";
                        oldMessage = old.getAttachments().stream().map(attachment -> attachment.getUrl() + "\n").reduce(oldMessage, String::concat);

                        String newMessage = message.getContentRaw() + "\n";
                        newMessage = message.getAttachments().stream().map(attachment -> attachment.getUrl() + "\n").reduce(newMessage, String::concat);

                        EmbedBuilder embed = new EmbedBuilder()
                                .setColor(Color.YELLOW)
                                .addField("From: ", oldMessage, false)
                                .addField("To: ", newMessage, false);

                        String topMessage = bot.timestamp()
                                + " :warning: **"
                                + old.getUsername()
                                + "**#"
                                + old.getDiscriminator()
                                + " (ID:"
                                + old.getAuthorId()
                                + ") edited a message in <#"
                                + message.getChannel().getId()
                                + ">:";
                        sendMessage(event, topMessage, embed);
                    }
                }
            }
        }
        else if (event instanceof GuildMemberJoinEvent member) {
            if (member.getGuild().getId().equals(bot.mainServerId())) {
                // Send message to server logs
                String message = bot.timestamp()
                        + " :inbox_tray: **"
                        + member.getUser().getName()
                        + "**#"
                        + member.getUser().getDiscriminator()
                        + " (ID:"
                        + member.getUser().getId()
                        + ") joined the server.\nCreation: "
                        + member.getUser().getTimeCreated().format(DateTimeFormatter.RFC_1123_DATE_TIME);

                event.getJDA().getTextChannelById(bot.serverLog()).sendMessage(message).queue();
            }
        }
        else if (event instanceof GuildMemberRemoveEvent member) {
            if (member.getGuild().getId().equals(bot.mainServerId())) {
                String message = bot.timestamp()
                        + " :outbox_tray: **"
                        + member.getUser().getName()
                        + "**#"
                        + member.getUser().getDiscriminator()
                        + " (ID:"
                        + member.getUser().getId()
                        + ") has been kicked or left the server.\nCreation: "
                        + member.getUser().getTimeCreated().format(DateTimeFormatter.RFC_1123_DATE_TIME);

                event.getJDA().getTextChannelById(bot.serverLog()).sendMessage(message).queue();
            }
        }
    }

    /**
     * Sends the embed to our log channel
     * @param event
     * @param topMessage
     * @param embed
     */
    private void sendMessage(GenericEvent event, String topMessage, EmbedBuilder embed) {
        event.getJDA().getTextChannelById(bot.serverLog()).sendMessage(topMessage)
                .setEmbeds(embed.build()).queue();
    }
}
