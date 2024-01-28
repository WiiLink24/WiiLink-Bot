package com.wiilink24.bot.commands.moderation;

import com.wiilink24.bot.Bot;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.threeten.bp.temporal.ChronoUnit;

import java.time.Duration;
import java.time.temporal.TemporalUnit;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Mute command
 *
 * @author Sketch
 */

public class Mute {
    private final Bot bot;
    public Mute(Bot bot) {
        this.bot = bot;
    }

    public void mute(SlashCommandInteractionEvent event) {
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

        String timeString = event.getOptionsByName("time").get(0).getAsString();
        int time = timeMuted(timeString);

        String message = bot.timestamp()
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
        String finalTimeString = timeString;
        event.getGuild().timeoutFor(member.getUser(), Duration.ofMillis(time)).reason(timeString).queue(
                success -> {
                    event.reply("Successfully timed out" + " **" + member.getUser().getName() + "** for " + finalTimeString + ".").queue();
                    Bot.sendDM(member.getUser(), "You have been muted for " + finalTimeString + " in WiiLink.").queue();
                    event.getJDA().getTextChannelById(bot.modLog()).sendMessage(message).queue();
                },
                failure -> event.reply("Failed to mute the specified member.").queue()
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
