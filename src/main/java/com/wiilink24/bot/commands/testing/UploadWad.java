package com.wiilink24.bot.commands.testing;

import com.wiilink24.bot.Bot;
import com.wiilink24.bot.Database;
import io.sentry.Sentry;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.sql.SQLException;

/**
 * Generates a download URL for a tester.
 *
 * @author Sketch
 */

public class UploadWad {
    private final Database database;

    public UploadWad() {
        this.database = new Database();
    }

    public void uploadWad(SlashCommandInteractionEvent event) {
        String filename = event.getOption("filename").getAsString();
        String title = event.getOption("title").getAsString();
        String description = event.getOption("description").getAsString();

        // Sanitize names
        filename = filename.trim();
        title = title.trim();
        description = description.trim();

        event.deferReply().queue();

        int interactionId;
        try {
            interactionId = database.insertWad(filename, title);
        } catch (SQLException e) {
            event.getHook().sendMessage("Unable to insert WAD to database.").queue();
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

        event.getHook().sendMessage("Successfully uploaded WAD").queue();
    }
}

