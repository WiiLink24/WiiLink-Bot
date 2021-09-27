package com.wiilink24.bot.events;

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
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.events.user.UserTypingEvent;
import net.dv8tion.jda.api.hooks.EventListener;

import java.awt.*;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Listener implements EventListener {
    private final Bot bot;
    private final String modLog;
    private final MessageCache cache;
    private final String timestamp;
    private final Database database;

    public Listener(Bot bot) {
        this.bot = bot;
        this.modLog = bot.modLog();
        this.cache = new MessageCache();
        this.timestamp = bot.timestamp();
        this.database = new Database();
    }

    @Override
    public void onEvent(GenericEvent event) {
        if (event instanceof GuildMessageReceivedEvent)
        {
            Message message = ((GuildMessageReceivedEvent)event).getMessage();

            if(!message.getAuthor().isBot())
            {
                this.cache.putMessage(message);
            }

            // The below is for the AFK command
            List<Member> queried_members = message.getMentionedMembers();

            // If there are no mentioned members, don't bother querying
            if (queried_members.size() != 0) {
                try (Connection con = DriverManager.getConnection(bot.db(), bot.dbUser(), bot.dbPass())) {
                    for (Member queried_member : queried_members) {
                        User user = queried_member.getUser();

                        // Query to see if the user exists and grab AFK status
                        ResultSet result = database.fullQuery(con, user.getId());

                        if (result.first()) {
                            if (result.getBoolean(3)) {
                                EmbedBuilder embed = new EmbedBuilder()
                                        .setAuthor(user.getName() + " is AFK", null, user.getEffectiveAvatarUrl())
                                        .addField("Reason:", result.getString(4), false);

                                message.getChannel().sendMessage(embed.build()).queue();

                                // Now send a DM to the AFK user with the mentioned message
                                EmbedBuilder newEmbed = new EmbedBuilder()
                                        .setAuthor(message.getAuthor().getName(), null, message.getAuthor().getEffectiveAvatarUrl())
                                        .addField("You were mentioned in a message in WiiLink", message.getContentRaw(), false)
                                        .setFooter("#" + message.getChannel().getName(), message.getGuild().getIconUrl());

                                user.openPrivateChannel()
                                        .flatMap(channel -> channel.sendMessage(newEmbed.build())).complete();
                            }
                        }
                    }

                } catch (SQLException e) {
                    Sentry.captureException(e);
                }
            }
        }
        // For the AFK command
        else if (event instanceof UserTypingEvent)
        {
            UserTypingEvent typing = (UserTypingEvent) event;

            User user = typing.getUser();

            // Query the database and find if the user is AFK
            try (Connection con = DriverManager.getConnection(bot.db(), bot.dbUser(), bot.dbPass())) {
                ResultSet result = database.fullQuery(con, user.getId());

                if (result.first()) {
                    if (result.getBoolean(3)) {
                        database.updateAFK(con, false, null, user.getId());

                        // Now DM the member telling them that the AFK status is removed
                        user.openPrivateChannel()
                                .flatMap(privateChannel -> privateChannel.sendMessage("Your AFK status has been removed.")).complete();
                    }
                }

            } catch (SQLException e) {
                Sentry.captureException(e);
            }
        }
        else if (event instanceof GuildMessageDeleteEvent)
        {
            GuildMessageDeleteEvent delete = (GuildMessageDeleteEvent) event;
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
        else if (event instanceof GuildMessageUpdateEvent) {
            Message message = ((GuildMessageUpdateEvent)event).getMessage();

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
        else if (event instanceof GuildMemberJoinEvent) {
            GuildMemberJoinEvent member = (GuildMemberJoinEvent) event;
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
        }
        else if (event instanceof GuildMemberRemoveEvent) {
            GuildMemberRemoveEvent member = (GuildMemberRemoveEvent) event;
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

    /* Sends the embed to our log channel */
    private void sendMessage(GenericEvent event, String topMessage, EmbedBuilder embed) {
        event.getJDA().getTextChannelById(modLog).sendMessage(topMessage)
                .embed(embed.build()).queue();
    }
}
