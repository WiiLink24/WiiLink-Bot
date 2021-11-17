package com.wiilink24.bot.events;

import com.wiilink24.bot.commands.moderation.*;
import io.sentry.Sentry;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

public class SlashCommandListener extends ListenerAdapter {
    public SlashCommandListener() {}

    @Override
    public void onSlashCommand(@NotNull SlashCommandEvent event) {
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
    }
}
