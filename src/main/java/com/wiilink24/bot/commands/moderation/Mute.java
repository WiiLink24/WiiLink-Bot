package com.wiilink24.bot.commands.moderation;

import com.wiilink24.bot.Bot;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Mute command
 *
 * @author Sketch
 */

public class Mute {
    public Mute() {}

    public void mute(SlashCommandInteractionEvent event) {
        Role mutedRole = event.getGuild().getRoleById(Bot.mutedRoleId());

        // Member is a required field
        Member member = event.getOptionsByName("member").get(0).getAsMember();

        // Don't kick moderators or admins
        if (member.getPermissions().contains(Permission.BAN_MEMBERS)) {
            event.reply("You cannot mute a moderator/admin!").queue();
            return;
        }

        // Now handle optionals
        String reason = "No reason provided.";
        if (!event.getOptionsByName("reason").isEmpty()) {
            reason = event.getOptionsByName("reason").get(0).getAsString();
        }

        // Default to a 1-day mute
        int time = 86400;
        String timeString = "1d";
        if (!event.getOptionsByName("time").isEmpty()) {
            timeString = event.getOptionsByName("time").get(0).getAsString();
            time = timeMuted(timeString);
        }

        String unmuteText = Bot.timestamp()
                + " :loud_sound: **"
                + event.getUser().getName()
                + "**#"
                + event.getUser().getDiscriminator()
                + " was unmuted because their mute time expired.";

        String message = Bot.timestamp()
                + " :mute: **"
                + event.getUser().getName()
                + "**#"
                + event.getUser().getDiscriminator()
                + " muted **"
                + member.getUser().getName()
                + "**#"
                + member.getUser().getDiscriminator()
                + " for "
                + timeString
                + ".\nReason: "
                + reason;

        // Add muted role, then set timer
        int finalTime = time;
        String finalTimeString = timeString;
        event.getGuild().addRoleToMember(member.getUser(), mutedRole).queue(
                success -> {
                    event.reply("Successfully muted" + " **" + member.getUser().getName() + "**#" + member.getUser().getDiscriminator() + " for " + finalTimeString + ".").queue();
                    Bot.sendDM(member.getUser(), "You have been muted for " + finalTimeString + " in WiiLink.").queue();
                    event.getJDA().getTextChannelById(Bot.serverLog()).sendMessage(message).queue();

                    new Timer().schedule(
                            new TimerTask() {
                                @Override
                                public void run() {
                                    event.getGuild().removeRoleFromMember(member.getUser(), mutedRole).queue();
                                    event.getJDA().getTextChannelById(Bot.serverLog()).sendMessage(unmuteText).queue();
                                }
                            }, finalTime
                    );
                }, failure -> {
                    event.reply("Failed to mute the specified member.").queue();
                }
        );
    }

    private int timeMuted(String time) {
        // We make a hashtable to get the time values we need based on the argument
        Hashtable<String, Integer> timeTable = new Hashtable<>();
        timeTable.put("m", 60);
        timeTable.put("h", 3600);
        timeTable.put("d", 86400);

        String[] timeNum = time.split("h|m|d");
        String[] timeKey = time.split("([0-9]{1,3})");

        Integer milliSeconds = timeTable.get(timeKey[1]);

        return Integer.parseInt(timeNum[0]) * milliSeconds * 1000;
    }
}
