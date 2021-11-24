package com.wiilink24.bot.commands.misc;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.awt.*;
import java.time.format.DateTimeFormatter;

/**
 * User Info command
 *
 * @author Sketch
 */

public class Userinfo {
    public Userinfo() {}

    public void userInfo(SlashCommandEvent event) {
        Member member = event.getMember();
        User user = event.getUser();

        if (!event.getOptionsByName("user").isEmpty()) {
            member = event.getOptionsByName("user").get(0).getAsMember();
            user = event.getOptionsByName("user").get(0).getAsUser();
        }

        Color color = Color.GREEN;
        String joinDate = "Not in server";
        int numOfRoles = 0;

        String roles = "Not in server";
        String nick = "None";
        if (member != null) {
            roles = "";
            roles = member.getRoles().stream().map(role -> " "+role.getAsMention()).reduce(roles, String::concat);

            if (member.getNickname() != null) {
                nick = member.getNickname();
            }

            color = member.getColor();
            joinDate = member.getTimeJoined().format(DateTimeFormatter.RFC_1123_DATE_TIME);
            numOfRoles = member.getRoles().toArray().length;
        }

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(String.format("Info about %s #%s", user.getName(), user.getDiscriminator()))
                .setColor(color)
                .setThumbnail(user.getEffectiveAvatarUrl())
                .addField("User ID", user.getId(), false)
                .addField("Nickname", nick, false)
                .addField("Account Creation", user.getTimeCreated().format(DateTimeFormatter.RFC_1123_DATE_TIME), false)
                .addField("Join Date", joinDate, false)
                .addField(String.format("Roles [%s]", numOfRoles), roles, false);

        event.replyEmbeds(embed.build()).queue();
    }
}
