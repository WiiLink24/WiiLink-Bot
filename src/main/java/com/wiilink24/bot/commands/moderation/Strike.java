package com.wiilink24.bot.commands.moderation;

import com.wiilink24.bot.Bot;
import com.wiilink24.bot.Database;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.sql.SQLException;

/**
 * Strike command
 *
 * @author Sketch
 */

public class Strike {
    private final Database database;
    private final Bot bot;

    public Strike(Bot bot) {
        this.database = new Database();
        this.bot = bot;
    }

    public void strike(SlashCommandInteractionEvent event) throws SQLException {
        // User is a required field
        User user = event.getOptionsByName("member").get(0).getAsUser();

        // Don't strike moderators or admins
        if (event.getOptionsByName("member").get(0).getAsMember().getPermissions().contains(Permission.BAN_MEMBERS)) {
            event.reply("You cannot strike a moderator/admin!").queue();
            return;
        }

        // Now we handle the optionals
        int strikes = 1;
        if (!event.getOptionsByName("strikes").isEmpty()) {
            strikes = Integer.parseInt(event.getOptionsByName("strikes").get(0).getAsString());
        }

        String reason = "No reason provided.";
        if (!event.getOptionsByName("reason").isEmpty()) {
            reason = event.getOptionsByName("reason").get(0).getAsString();
        }

        // Now we deal with the striking
        boolean exists = database.doesExist(user.getId());
        // The user is not in the database, add them
        if (!exists) {
            database.createUser(user.getId());
        }

        // Query to see how many strikes the user has before our new strikes
        int oldStrikes = database.getStrikes(user.getId());
        int newStrikes = strikes + oldStrikes;

        // Now we can strike them and send the user a DM
        database.updateStrike(user.getId(), newStrikes);

        event.reply("Successfully gave " + strikes + " strikes to **" + user.getName() + "**#" + user.getDiscriminator()).queue();
        Bot.sendDM(user, "You were given " + strikes + " strikes in WiiLink for `" + reason + "`").queue();
        event.getJDA().getTextChannelById(bot.modLog()).sendMessage(
                bot.timestamp()
                        + " :triangular_flag_on_post: **"
                        + event.getUser().getName()
                        + "**#"
                        + event.getUser().getDiscriminator()
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
                        + reason
                        + "`"
        ).complete();
    }
}
