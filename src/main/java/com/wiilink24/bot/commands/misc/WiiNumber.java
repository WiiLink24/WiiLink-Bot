package com.wiilink24.bot.commands.misc;

import com.wiilink24.bot.Database;
import io.sentry.Sentry;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.sql.SQLException;

public class WiiNumber {

    private final Database database;

    public WiiNumber() {
        this.database = new Database();
    }

    public void deleteNumber(SlashCommandInteractionEvent event) {
        String num = event.getOption("wiino").getAsString();
        num = num.replaceAll("[^0-9]", "");
        if (num.length() != 16) {
            event.reply("Incorrect length! Wii Numbers must be 16 digits long.").queue();
            return;
        }
        
        try {
            boolean registered = database.getWiiNumberRegistration(num);
            if (!registered) {
                event.reply("Wii Number is not registered with WiiLink! Did you type it correctly?").queue();
                return;
            }
            database.deleteWiiNumber(num);

            String fancyNum = num.substring(0, 4) + "-"
                + num.substring(4, 8) + "-"
                + num.substring(8, 12) + "-"
                + num.substring(12, 16);

            event.reply("Successfully deleted Wii Number " + fancyNum).queue();
        } catch (SQLException e) {
            event.reply("Unable to delete Wii Number.").queue();
            System.out.println(e.getMessage());
            Sentry.captureException(e);
        }
    }
}
