package com.wiilink24.bot.commands.moderation;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.wiilink24.bot.Bot;
import com.wiilink24.bot.commands.Categories;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Mute command
 *
 * @author Sketch
 */

public class Mute extends Command {
    private final Bot bot;

    public Mute(Bot bot) {
        this.bot = bot;
        this.name = "mute";
        this.category = Categories.MODERATION;
        this.userPermissions = new Permission[]{Permission.BAN_MEMBERS};
    }

    @Override
    protected void execute(CommandEvent event) {
        String[] args = event.getArgs().split("\\s", 3);
        User user = event.getGuild().retrieveMemberById(args[0]).complete().getUser();
        Role mutedRole = event.getGuild().getRoleById("770836633419120650");
        Integer time = timeMuted(args[1]);

        // Basic unmute message
        String unmuteText = bot.timestamp()
                + " :loud_sound: **"
                + user.getName()
                + "**#"
                + user.getDiscriminator()
                + " was unmuted because their mute time expired.";

        String message = bot.timestamp()
                + " :mute: **"
                + event.getAuthor().getName()
                + "**#"
                + event.getAuthor().getDiscriminator()
                + " muted **"
                + user.getName()
                + "**#"
                + user.getDiscriminator()
                + " for "
                + args[1]
                + ".\nReason: "
                + args[2];

        // Add muted role, then set timer
        event.getGuild().addRoleToMember(user.getId(), mutedRole).queue(
                success -> {
                    event.reply("Successfully muted" + " **" + user.getName() + "**#" + user.getDiscriminator() + " for " + args[1] + ".");
                    bot.sendDM(user, "You have been muted for " + args[1] + " in WiiLink.").queue();
                    event.getJDA().getTextChannelById(bot.modLog()).sendMessage(message).queue();

                    new Timer().schedule(
                            new TimerTask() {
                                @Override
                                public void run() {
                                    event.getGuild().removeRoleFromMember(user.getId(), mutedRole).queue();
                                    event.getJDA().getTextChannelById(bot.modLog()).sendMessage(unmuteText).queue();
                                }
                            }, time
                    );
                }, failure -> {
                    event.replyError("Failed to mute the specified member.");
                }
        );
    }

    private Integer timeMuted(String time) {
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
