package com.wiilink24.bot.commands.testing;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.wiilink24.bot.Bot;
import com.wiilink24.bot.Database;
import com.wiilink24.bot.commands.Categories;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.interactions.components.Button;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

enum FlagType {
    NONE,
    FILENAME,
    TITLE,
    DESCRIPTION
}

/**
 * Generates a download URL for a tester.
 *
 * @author Spotlight
 */
public class UploadWad extends Command {
    private Bot bot;
    private Database database;


    public UploadWad(Bot bot) {
        this.bot = bot;
        this.name = "uploadwad";
        this.help = "Register a WAD available for download.";
        this.category = Categories.DEVELOPER;
        this.database = new Database();
    }

    @Override
    protected void execute(CommandEvent event) {
        // Ensure the running user has the needed developer ID.
        String neededId = Bot.developerRoleId();
        if (!event.isFromType(ChannelType.TEXT) || event.getMember().getRoles().stream().noneMatch(r -> r.getId().equalsIgnoreCase(neededId))) {
            event.replyError("You must have the Developer role to use that!");
            return;
        }

        if (event.getArgs().length() < 6) {
            event.replyError("No WAD filepath, title and name provided!");
            event.replyError("Usage: `/uploadwad filename: test.wad title: Test v1 description: This is v1 of Test.`");
            return;
        }
        String[] arguments = event.getArgs().split(" ");

        String filename = "";
        String title = "";
        String description = "";

        // Will be reset upon first encounter.
        FlagType current = FlagType.FILENAME;
        for (String argument: arguments) {
            switch (argument) {
                case "filename:":
                    current = FlagType.FILENAME;
                    break;

                case "title:":
                    current = FlagType.TITLE;
                    break;

                case "description:":
                    current = FlagType.DESCRIPTION;
                    break;

                default:
                    // This is an argument to add to a type's string.
                    switch (current) {
                        case FILENAME -> {
                            filename += argument;
                        }

                        case TITLE -> {
                            title += argument;
                        }

                        case DESCRIPTION -> {
                            description += argument;
                        }
                    }
            }
        }

        int interaction_id;
        try (Connection con = DriverManager.getConnection(bot.db(), bot.dbUser(), bot.dbPass())) {
            // TODO
            interaction_id = database.insertWad(con, filename, title);
            event.reply("ID: " + interaction_id);
        } catch (SQLException throwables) {
            event.replyError("Unable to insert WAD to database.");
            throwables.printStackTrace();
            return;
        }

        // Finally, send an embed informing of a new patch.
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("New release: " + title)
                .setDescription(description);

        event.getChannel()
                .sendMessage(embed.build())
                .setActionRow(
                        Button.success("patchdl_" + interaction_id, "Download")
                )
                .queue();
    }
}

