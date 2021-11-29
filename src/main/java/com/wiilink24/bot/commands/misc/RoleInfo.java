package com.wiilink24.bot.commands.misc;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.utils.concurrent.Task;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class RoleInfo {
    public RoleInfo() {}

    public void roleInfo(SlashCommandEvent event) {
        event.deferReply().queue();
        Role role = event.getOptionsByName("role").get(0).getAsRole();

        Task<List<Member>> task = event.getGuild().findMembers(
                member -> member.getRoles().contains(role)
        );

        task.onSuccess(
                list -> {
                    String members = "";
                    String perms = "";
                    members = list.stream().map(member -> member.getAsMention() + " ").reduce(members, String::concat);
                    perms = role.getPermissions().stream().map(perm -> perm.getName() + " ").reduce(perms, String::concat);
                    EmbedBuilder embed = new EmbedBuilder()
                            .setColor(role.getColor())
                            .addField("Info about **" + role.getName() + "**", ":white_small_square: ID: **" + role.getId()
                                    + "**\n:white_small_square: Creation: **" + role.getTimeCreated().format(DateTimeFormatter.RFC_1123_DATE_TIME)
                                    + "**\n:white_small_square: Position: **" + role.getPosition()
                                    + "**\n:white_small_square: Color: **#" + (role.getColor()==null ? "000000" : Integer.toHexString(role.getColor().getRGB()).toUpperCase().substring(2))
                                    + "**\n:white_small_square: Mentionable: **" + role.isMentionable()
                                    + "**\n:white_small_square: Hoisted: **" + role.isHoisted()
                                    + "**\n:white_small_square: Managed: **" + role.isManaged()
                                    + "**\n:white_small_square: Permissions: **" + perms
                                    + "**", false)
                            .addField("Members [" + list.size() + "]", members, false);

                    event.getHook().sendMessageEmbeds(embed.build()).queue();
                }
        );
    }
}
