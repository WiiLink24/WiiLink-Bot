package com.wiilink24.bot.events;

import com.wiilink24.bot.commands.misc.*;
import com.wiilink24.bot.commands.testing.UploadWad;
import io.sentry.Sentry;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

public class SlashCommandListener extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        /*
        General Listeners
         */
        if (event.getName().equals("apply")) {
            new Apply().apply(event);
        }
        else if (event.getName().equals("avatar")) {
            new Avatar().avatar(event);
        }
        else if (event.getName().equals("credits")) {
            new Credits().credits(event);
        }
        else if (event.getName().equals("card")) {
            new Digicard().card(event);
        }
        else if (event.getName().equals("code")) {
            try {
                new CodeHandler().codeHandler(event);
            } catch (SQLException e) {
                Sentry.captureException(e);
                event.reply("An error has occurred. Contact Sketch.").queue();
            }
        }
        else if (event.getName().equals("dns")) {
            new DNS().dns(event);
        }
        else if (event.getName().equals("error")) {
            new ErrorCodes().errorCode(event);
        }
        else if (event.getName().equals("gametdb")) {
            new GameTDB().gameTDB(event);
        }
        else if (event.getName().equals("mii")) {
            new Mii().mii(event);
        }
        else if (event.getName().equals("ping")) {
            new Ping().ping(event);
        }
        else if (event.getName().equals("linktag")) {
            new LinkTag().linkTag(event);
        }
        else if (event.getName().equals("serverinfo")) {
            new ServerInfo().serverInfo(event);
        }
        else if (event.getName().equals("userinfo")) {
            new Userinfo().userInfo(event);
        }
        else if (event.getName().equals("help")) {
            new Help().help(event);
        }
        else if (event.getName().equals("roleinfo")) {
            new RoleInfo().roleInfo(event);
        }
        else if (event.getName().equals("uploadwad")) {
            new UploadWad().uploadWad(event);
        }
        else if (event.getName().equals("wiinodel")) {
            new WiiNumber().deleteNumber(event);
        }
    }
}
