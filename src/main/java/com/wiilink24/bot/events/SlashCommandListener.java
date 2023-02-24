package com.wiilink24.bot.events;

import com.wiilink24.bot.Bot;
import com.wiilink24.bot.commands.misc.*;
import com.wiilink24.bot.commands.moderation.*;
import io.sentry.Sentry;
import io.sentry.protocol.App;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

public class SlashCommandListener extends ListenerAdapter {
    private final Bot bot;
    public SlashCommandListener(Bot bot) {this.bot = bot;}

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        /*
        Moderation Listeners
         */
        if (event.getName().equals("strike")) {
            if (event.getMember().getPermissions().contains(Permission.BAN_MEMBERS)) {
                try {
                    new Strike().strike(event);
                } catch (SQLException e) {
                    Sentry.captureException(e);
                    event.reply("An error has occurred. Contact Sketch.").queue();
                }
            } else {
                event.reply("You don't have permission to run this command!").queue();
            }
        }
        else if (event.getName().equals("clear")) {
            if (event.getMember().getPermissions().contains(Permission.BAN_MEMBERS)) {
                new Clear().clear(event);
            } else {
                event.reply("You don't have permission to run this command!").queue();
            }
        }
        else if (event.getName().equals("check")) {
            if (event.getMember().getPermissions().contains(Permission.BAN_MEMBERS)) {
                try {
                    new Check().check(event);
                } catch (SQLException e) {
                    Sentry.captureException(e);
                    event.reply("An error has occurred. Contact Sketch.").queue();
                }
            } else {
                event.reply("You don't have permission to run this command!").queue();
            }
        }
        else if (event.getName().equals("ban")) {
            if (event.getMember().getPermissions().contains(Permission.BAN_MEMBERS)) {
                new Ban().ban(event);
            } else {
                event.reply("You don't have permission to run this command!").queue();
            }
        }
        else if (event.getName().equals("unban")) {
            if (event.getMember().getPermissions().contains(Permission.BAN_MEMBERS)) {
                new Unban().unban(event);
            } else {
                event.reply("You don't have permission to run this command!").queue();
            }
        }
        else if (event.getName().equals("kick")) {
            if (event.getMember().getPermissions().contains(Permission.BAN_MEMBERS)) {
                new Kick().kick(event);
            } else {
                event.reply("You don't have permission to run this command!").queue();
            }
        }
        else if (event.getName().equals("mute")) {
            if (event.getMember().getPermissions().contains(Permission.BAN_MEMBERS)) {
                new Mute().mute(event);
            } else {
                event.reply("You don't have permission to run this command!").queue();
            }
        }
        /*
        General Listeners
         */
        else if (event.getName().equals("about")) {
            new About().about(event);
        }
        else if (event.getName().equals("afk")) {
            new AFK().afk(event);
        }
        else if (event.getName().equals("apply")) {
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
        else if (event.getName().equals("dominos")) {
            new Dominos().dominos(event);
        }
        else if (event.getName().equals("gametdb")) {
            new GameTDB().gameTDB(event);
        }
        else if (event.getName().equals("mii")) {
            new Mii().mii(event);
        }
        else if (event.getName().equals("ocr")) {
            new OCR().ocr(event);
        }
        else if (event.getName().equals("ping")) {
            new Ping().ping(event);
        }
        else if (event.getName().equals("riitag")) {
            new RiiTag().riiTag(event);
        }
        else if (event.getName().equals("serverinfo")) {
            new ServerInfo().serverInfo(event);
        }
        else if (event.getName().equals("translate")) {
            new Translate(this.bot).translate(event);
        }
        else if (event.getName().equals("userinfo")) {
            new Userinfo().userInfo(event);
        }
        else if (event.getName().equals("news")) {
            new News().news(event);
        }
        else if (event.getName().equals("help")) {
            new Help().help(event);
        }
        else if (event.getName().equals("roleinfo")) {
            new RoleInfo().roleInfo(event);
        }
    }
}
