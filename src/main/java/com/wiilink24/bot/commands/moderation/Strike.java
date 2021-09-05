package com.wiilink24.bot.commands.moderation;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.wiilink24.bot.Bot;
import com.wiilink24.bot.Database;
import com.wiilink24.bot.commands.Categories;
import io.sentry.Sentry;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Strike command
 *
 * @author Sketch
 */

public class Strike extends Command {
    private final Database database;
    private final Bot bot;
    private final Timer timer;
    private final String timestamp;

    public Strike(Bot bot) {
        this.bot = bot;
        this.database = new Database();
        this.name = "strike";
        this.timer = new Timer();
        this.timestamp = bot.timestamp();
        this.category = Categories.MODERATION;
        this.userPermissions = new Permission[]{Permission.BAN_MEMBERS};
    }

    @Override
    protected void execute(CommandEvent event) {
        try (Connection con = DriverManager.getConnection(bot.db(), bot.dbUser(), bot.dbPass())) {
            String[] args = event.getArgs().split("\\s", 3);
            String strikeReason;
            String message;
            Role mutedRole = event.getGuild().getRoleById("770836633419120650");
            User user = event.getGuild().retrieveMemberById(args[0]).complete().getUser();

            // Build strike reason
            if (args.length == 1) {
                strikeReason = "No reason provided";
            } else {
                strikeReason = args[2];
            }

            boolean exists = database.doesExist(con, user.getId());
            // The user is not in the database, add them
            if (!exists) {
                database.createUser(con, user.getId());
            }

            // Query to see how many strikes the user has before our new strikes
            ResultSet result = database.fullQuery(con, user.getId());
            int strikes = 0;
            int oldStrikes = 0;

            while (result.next()) {
                oldStrikes = result.getInt(2);
                strikes = oldStrikes + Integer.parseInt(args[1]);
            }

            event.reply("Successfully gave " + args[1] + " to **" + user.getName() + "**#" + user.getDiscriminator());
            // Now we can strike them and send the user a DM
            database.updateStrike(con, user.getId(), strikes);
            bot.sendDM(user, "You were given " + args[1] + " strikes in WiiLink for `" + strikeReason + "`").queue();
            event.getJDA().getTextChannelById(bot.modLog()).sendMessage(
                    timestamp
                    + " :triangular_flag_on_post: **"
                    + event.getAuthor().getName()
                    + "**#"
                    + event.getAuthor().getDiscriminator()
                    + " gave **"
                    + user.getName()
                    + "**#"
                    + user.getDiscriminator()
                    + " (`"
                    + oldStrikes
                    + " -> "
                    + strikes
                    + "`)\n"
                    + "Reason: `"
                    + strikeReason
                    + "`"
            ).complete();

            // Basic unmute message
            String unmuteText = timestamp
                    + " :loud_sound: **"
                    + user.getName()
                    + "**#"
                    + user.getDiscriminator()
                    + " was unmuted because their mute time expired.";

            switch (strikes) {
                case 1 -> {
                }
                case 2 -> {
                    message = timestamp
                            + " :mute: Due to having 2 strikes, **"
                            + user.getName()
                            + "**#"
                            + user.getDiscriminator()
                            + " was muted for 2 hours.";
                    bot.sendDM(user, "Due to having 2 strikes, you have been muted for 10 minutes in WiiLink.").queue();
                    event.getJDA().getTextChannelById(bot.modLog()).sendMessage(message).complete();

                    // Add muted role, then set a 10-minute timer
                    event.getGuild().addRoleToMember(user.getId(), mutedRole).complete();
                    timer.schedule(
                            new TimerTask() {
                                @Override
                                public void run() {
                                    event.getGuild().removeRoleFromMember(user.getId(), mutedRole).complete();
                                    event.getJDA().getTextChannelById(bot.modLog()).sendMessage(unmuteText).complete();
                                }
                            }, 120 * 1000
                    );
                }
                case 3 -> {
                    message = timestamp
                            + " :mute: Due to having 3 strikes, **"
                            + user.getName()
                            + "**#"
                            + user.getDiscriminator()
                            + " was muted for 2 hours.";
                    bot.sendDM(user, "Due to having 3 strikes, you have been muted for 2 hours in WiiLink.").queue();
                    event.getJDA().getTextChannelById(bot.modLog()).sendMessage(message).complete();

                    // Add muted role, then set a 2-hour timer
                    event.getGuild().addRoleToMember(user.getId(), mutedRole).complete();
                    timer.schedule(
                            new TimerTask() {
                                @Override
                                public void run() {
                                    event.getGuild().removeRoleFromMember(user.getId(), mutedRole).complete();
                                    event.getJDA().getTextChannelById(bot.modLog()).sendMessage(unmuteText).complete();
                                }
                            }, 7200 * 1000
                    );
                }
                case 4 -> {
                    message = timestamp
                            + " :boot: Due to having 4 strikes, **"
                            + user.getName()
                            + "**#"
                            + user.getDiscriminator()
                            + " was kicked.";
                    bot.sendDM(user, "Due to having 4 strikes, you were kicked from WiiLink.").complete();
                    event.getGuild().kick(event.getGuild().retrieveMemberById(args[0]).complete()).complete();
                    event.getJDA().getTextChannelById(bot.modLog()).sendMessage(message).complete();
                }
                default -> {
                    // Anything higher than 4 will be a ban
                    message = timestamp
                        + " :hammer: Due to having 5 or more strikes, **"
                        + user.getName()
                        + "**#"
                        + user.getDiscriminator()
                        + " was banned.";
                    bot.sendDM(user, "Due to having 5 or more strikes, you were banned from WiiLink.").complete();
                    event.getGuild().ban(event.getGuild().retrieveMemberById(args[0]).complete(), 1, strikeReason).complete();
                    event.getJDA().getTextChannelById(bot.modLog()).sendMessage(message).complete();
                }
            }

        } catch (SQLException e) {
            Sentry.captureException(e);
        }
    }


}
