package com.wiilink24.bot.commands.moderation;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.wiilink24.bot.Bot;
import com.wiilink24.bot.commands.Categories;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;

/**
 * Kick command
 *
 * @author Sketch
 */

public class Kick extends Command {
    private final Bot bot;

    public Kick(Bot bot) {
        this.bot = bot;
        this.name = "kick";
        this.userPermissions = new Permission[]{Permission.BAN_MEMBERS};
        this.category = Categories.MODERATION;
    }

    @Override
    protected void execute(CommandEvent event) {
        String[] args = event.getArgs().split("\\s", 2);
        Member member = event.getGuild().retrieveMemberById(args[0]).complete();
        String message = bot.timestamp()
                + " :boot: **"
                + event.getAuthor().getName()
                + "**#"
                + event.getAuthor().getDiscriminator()
                + " kicked **"
                + member.getUser().getName()
                + "**#"
                + member.getUser().getDiscriminator()
                + ".\nReason: `"
                + args[1]
                + "`";

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(":boot: Successfully kicked " + member.getUser().getName() + "#" + member.getUser().getDiscriminator())
                .setDescription("Reason: " + args[1] + "\nBy: " + event.getAuthor().getAsMention());
        event.reply(embed.build());

        bot.sendDM(member.getUser(), "You were kicked from WiiLink for `" + args[1] + "`").complete();
        event.getGuild().kick(member).complete();
        event.getJDA().getTextChannelById(bot.modLog()).sendMessage(message).complete();
    }
}
