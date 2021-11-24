package com.wiilink24.bot.commands.misc;

import com.wiilink24.bot.Database;
import io.sentry.Sentry;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.sql.*;

/**
 * AFK command
 *
 * @author Sketch
 */

public class AFK {
    private final Database database;

    public AFK()  {
        this.database = new Database();
    }

    public void afk(SlashCommandEvent event) {
        try {
            String userID = event.getUser().getId();
            boolean exists = database.doesExist(userID);

            // The user is not in the database, add them
            if (!exists) {
                database.createUser(userID);
            }

            // Reason is optional
            String reason = "No reason provided.";
            if (!event.getOptionsByName("reason").isEmpty()) {
                reason = event.getOptionsByName("reason").get(0).getAsString();
            }

            database.updateAFK(true, reason, userID);

            event.reply("**" + event.getUser().getName() + "**#" + event.getUser().getDiscriminator() + " is now AFK.").queue();
        } catch (SQLException e) {
            Sentry.captureException(e);
        }
    }
}
