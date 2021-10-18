package com.wiilink24.bot.commands.misc;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.wiilink24.bot.commands.Categories;
import com.wiilink24.bot.utils.SearcherUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;

import java.time.format.DateTimeFormatter;

/**
 * User Info command
 *
 * @author Sketch
 */

public class Userinfo extends Command {
    public Userinfo() {
        this.name = "userinfo";
        this.category = Categories.MISC;
        this.help = "Get information for a user";
        this.arguments = "[user]";

    }

    @Override
    protected void execute(CommandEvent event) {
        event.async(() -> {
            Member member = SearcherUtil.findMember(event, event.getArgs());
            if(member == null)
                return;

            getInfo(event, member);
        });
    }

    private void getInfo(CommandEvent event, Member member) {
        String roles = "";
        roles = member.getRoles().stream().map(role -> " "+role.getAsMention()).reduce(roles, String::concat);

        String nick = "None";
        if (!member.getNickname().isEmpty()) {
            nick = member.getNickname();
        }
        
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(String.format("Info about %s #%s", member.getUser().getName(), member.getUser().getDiscriminator()))
                .setColor(member.getColor())
                .setThumbnail(member.getUser().getEffectiveAvatarUrl())
                .addField("User ID", member.getId(), false)
                .addField("Nickname", nick, false)
                .addField("Account Creation", member.getTimeCreated().format(DateTimeFormatter.RFC_1123_DATE_TIME), false)
                .addField("Join Date", member.getTimeJoined().format(DateTimeFormatter.RFC_1123_DATE_TIME), false)
                .addField(String.format("Roles [%s]", member.getRoles().toArray().length), roles, false);

        event.reply(embed.build());
    }
}
