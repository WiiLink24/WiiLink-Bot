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
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Listener implements EventListener {
    private final String modLog;
    private final String offTopic;
    private final MessageCache cache;
    private final String timestamp;
    private final Database database;

    public Listener() {
        this.modLog = Bot.modLog();
        this.offTopic = Bot.offTopic();
        this.cache = new MessageCache();
        this.timestamp = Bot.timestamp();
        this.database = new Database();
    }

    @Override
    public void onEvent(@NotNull GenericEvent event) {
        if (event instanceof MessageReceivedEvent)
        {
            Message message = ((MessageReceivedEvent)event).getMessage();

            // Check whether this message should be deleted under the anti-swear list.
            if (shouldBeCensored(message)) {
                message.delete().queue();
                return;
            }

            if (message.getGuild().getId().equals(Bot.wiiLinkServerId())) {
                if(!message.getAuthor().isBot())
                {
                    this.cache.putMessage(message);
                }

                // The below is for the AFK command
                List<Member> queried_members = message.getMentions().getMembers();

                // If there are no mentioned members, don't bother querying
                if (queried_members.size() != 0) {
                    try {
                        for (Member queried_member : queried_members) {
                            User user = queried_member.getUser();

                            // Query to see if the user exists and grab AFK status
                            AFKStatus status = database.getAFKStatus(user.getId());
                            if (status.isAFK) {
                                EmbedBuilder embed = new EmbedBuilder()
                                        .setAuthor(user.getName() + " is AFK", null, user.getEffectiveAvatarUrl())
                                        .addField("Reason:", status.reason, false);

                                message.getChannel().sendMessageEmbeds(embed.build()).queue();

                                // Now send a DM to the AFK user with the mentioned message
                                EmbedBuilder newEmbed = new EmbedBuilder()
                                        .setAuthor(message.getAuthor().getName(), null, message.getAuthor().getEffectiveAvatarUrl())
                                        .addField("You were mentioned in a message in WiiLink", message.getContentRaw(), false)
                                        .setFooter("#" + message.getChannel().getName(), message.getGuild().getIconUrl());

                                user.openPrivateChannel()
                                        .flatMap(channel -> channel.sendMessageEmbeds(newEmbed.build())).complete();
                            }

                        }

                    } catch (SQLException e) {
                        Sentry.captureException(e);
                    }
                }
            }
        }
        // For the AFK command
        else if (event instanceof UserTypingEvent typing)
        {
            User user = typing.getUser();

            // Query the database and find if the user is AFK
            try {
                AFKStatus status = database.getAFKStatus(user.getId());
                if (status.isAFK) {
                    database.updateAFK(false, null, user.getId());

                    // Now DM the member telling them that the AFK status is removed
                    user.openPrivateChannel()
                            .flatMap(privateChannel -> privateChannel.sendMessage("Your AFK status has been removed.")).complete();
                }

            } catch (SQLException e) {
                Sentry.captureException(e);
            }
        }
        else if (event instanceof MessageDeleteEvent delete) {
            if (delete.getGuild().getId().equals(Bot.wiiLinkServerId())) {
                MessageCache.CachedMessage message = this.cache.pullMessage(delete.getGuild(), delete.getMessageIdLong());

                if (message != null) {
                    String oldMessage = message.getContentRaw() + "\n";
                    oldMessage = message.getAttachments().stream().map(attachment -> attachment.getUrl() + "\n").reduce(oldMessage, String::concat);
                    EmbedBuilder embed = new EmbedBuilder()
                            .setColor(Color.RED)
                            .setDescription(oldMessage);

                    String topMessage = timestamp
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

            // Check whether this message should be deleted under the anti-swear list.
            if (shouldBeCensored(message)) {
                // Ensure the message's new content is cached.
                this.cache.putMessage(message);
                message.delete().queue();
                return;
            }

            if (message.getGuild().getId().equals(Bot.wiiLinkServerId())) {
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

                        String topMessage = timestamp
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
            if (member.getGuild().getId().equals(Bot.wiiLinkServerId())) {
                // Send message to server logs
                String message = timestamp
                        + " :inbox_tray: **"
                        + member.getUser().getName()
                        + "**#"
                        + member.getUser().getDiscriminator()
                        + " (ID:"
                        + member.getUser().getId()
                        + ") joined the server.\nCreation: "
                        + member.getUser().getTimeCreated().format(DateTimeFormatter.RFC_1123_DATE_TIME);

                event.getJDA().getTextChannelById(modLog).sendMessage(message).queue();

                // Now make embed to send to off-topic
                EmbedBuilder embed = new EmbedBuilder()
                        .setColor(0x00FF00)
                        .setTitle("Welcome to WiiLink, " + member.getUser().getName() + "!")
                        .setDescription(
                                "- Be sure to install WiiLink by following the guide here: https://wii.guide/wiilink\n" +
                                "- Get some roles to stay up to date with the service: <#785983938089713664>\n" +
                                "- Get assistance for Wii Room at <#998024180559782020>\n" +
                                "- Get assistance for Digicam at <#998026252529192981>\n"
                        );

                event.getJDA().getTextChannelById(offTopic).sendMessage("<@" + member.getUser().getId() + ">").setEmbeds(embed.build()).queue();
            }
        }
        else if (event instanceof GuildMemberRemoveEvent member) {
            if (member.getGuild().getId().equals(Bot.wiiLinkServerId())) {
                String message = timestamp
                        + " :outbox_tray: **"
                        + member.getUser().getName()
                        + "**#"
                        + member.getUser().getDiscriminator()
                        + " (ID:"
                        + member.getUser().getId()
                        + ") has been kicked or left the server.\nCreation: "
                        + member.getUser().getTimeCreated().format(DateTimeFormatter.RFC_1123_DATE_TIME);

                event.getJDA().getTextChannelById(modLog).sendMessage(message).queue();
            }
        }
    }

    /**
     * Checks whether the current message contains a word on the anti-swear list.
     * @param message The message to search.
     * @return Whether a word within the message should be censored.
     */
    private boolean shouldBeCensored(Message message) {
        for (String str : Bot.antiSwearWords) {
            Pattern pattern = Pattern.compile("(?i)" + str, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(message.getContentRaw());
            boolean match = matcher.find();

            if (match) {
                return true;
            }
        }

        return false;
    }

    /**
     * Sends the embed to our log channel
     * @param event
     * @param topMessage
     * @param embed
     */
    private void sendMessage(GenericEvent event, String topMessage, EmbedBuilder embed) {
        event.getJDA().getTextChannelById(modLog).sendMessage(topMessage)
                .setEmbeds(embed.build()).queue();
    }
}