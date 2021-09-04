package com.wiilink24.bot.commands.misc;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.wiilink24.bot.utils.SearcherUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

/**
 * Avatar command
 *
 * @author Sketch
 */

public class Avatar extends Command {
    public Avatar() {
        this.name = "avatar";
        this.aliases = new String[]{"avy"};
    }

    @Override
    protected void execute(CommandEvent event) {
        event.async(() ->
        {
            Member member = SearcherUtil.findMember(event, event.getArgs());
            if(member == null)
                return;

            User user = member.getUser();

            display_avatar(event, user);
        });
    }

    private void display_avatar(CommandEvent event, User user) {
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(0x00FF00)
                .setAuthor(user.getName(), null, user.getEffectiveAvatarUrl())
                .setImage(user.getEffectiveAvatarUrl());

        event.reply(embed.build());
    }
}
