package com.wiilink24.bot.commands.misc;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.wiilink24.bot.Bot;
import com.wiilink24.bot.Database;
import com.wiilink24.bot.commands.Categories;
import io.sentry.Sentry;

import java.sql.*;

/**
 * AFK command
 *
 * @author Sketch
 */

public class AFK extends Command {
    private final Database database;

    public AFK()  {
        this.name = "afk";
        this.arguments = "[reason]";
        this.help = "Run the AFK command so members that ping you know you are AFK. You will also get a DM with all the messages you were mentioned in while AFK.";
        this.category = Categories.MISC;
        this.database = new Database();
    }

    @Override
    protected void execute(CommandEvent event) {
        try {
            String userID = event.getAuthor().getId();
            boolean exists = database.doesExist(userID);

            // The user is not in the database, add them
            if (!exists) {
                database.createUser(userID);
            }

            database.updateAFK(true, event.getArgs(), userID);

            event.reply("**" + event.getAuthor().getName() + "**#" + event.getAuthor().getDiscriminator() + " is now AFK.");
        } catch (SQLException e) {
            Sentry.captureException(e);
        }
    }
}
