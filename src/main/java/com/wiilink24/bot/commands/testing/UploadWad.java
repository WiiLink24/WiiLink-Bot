package com.wiilink24.bot.commands.testing;

import com.wiilink24.bot.Bot;
import com.wiilink24.bot.Database;
import io.sentry.Sentry;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.Button;

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

public class UploadWad extends ListenerAdapter {
    private final Database database;

    public UploadWad() {
        this.database = new Database();
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        String[] arguments = event.getMessage().getContentRaw().split(" ");
        if (arguments[0].equalsIgnoreCase("/uploadwad")) {
            // Ensure the running user has the needed developer ID.
            String neededId = Bot.developerRoleId();
            if (event.getMember().getRoles().stream().noneMatch(r -> r.getId().equalsIgnoreCase(neededId))) {
                event.getChannel().sendMessage("You must have the Developer role to use that!").queue();
                return;
            }

            if (arguments.length < 7) {
                event.getChannel().sendMessage("No WAD filepath, title and name provided!").queue();
                event.getChannel().sendMessage("\"Usage: `/uploadwad filename: test.wad title: Test v1 description: This is v1 of Test.`\"").queue();
            } else {
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
                                    filename += argument + " ";
                                }

                                case TITLE -> {
                                    title += argument + " ";
                                }

                                case DESCRIPTION -> {
                                    description += argument + " ";
                                }
                            }
                    }
                }

                // Sanitize names
                filename = filename.trim();
                title = title.trim();
                description = description.trim();

                int interactionId;
                try {
                    interactionId = database.insertWad(filename, title);
                } catch (SQLException e) {
                    event.getChannel().sendMessage("Unable to insert WAD to database.").queue();
                    Sentry.captureException(e);
                    return;
                }

                // Finally, send an embed informing of a new patch.
                EmbedBuilder embed = new EmbedBuilder()
                        .setTitle("New release: " + title)
                        .setDescription(description);

                event.getJDA()
                        .getTextChannelById(Bot.patchesChannel())
                        .sendMessageEmbeds(embed.build())
                        .setActionRow(
                                Button.success("patchdl_" + interactionId, "Download")
                        )
                        .queue();
            }
        }
    }
}

