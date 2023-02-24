package com.wiilink24.bot.commands.misc;

import com.wiilink24.bot.Database;
import io.sentry.Sentry;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.sql.SQLException;

public class Dominos {
    private final Database database;
    public Dominos() { this.database = new Database(); }

    public void dominos(SlashCommandInteractionEvent event) {
        Integer wiiId = event.getOption("console_id").getAsInt();

        try {
            database.insertWiiId(event.getInteraction().getUser().getId(), wiiId);
        } catch (SQLException e) {
            event.reply("Unable to insert Wii ID into database.").queue();
            System.out.println(e.getMessage());
            Sentry.captureException(e);
            return;
        }

        event.reply("Successfully inserted into database. Enjoy Domino's on your Wii!").setEphemeral(true).queue();
    }
}
