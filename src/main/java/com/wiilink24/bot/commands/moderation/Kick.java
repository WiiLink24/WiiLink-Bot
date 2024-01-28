package com.wiilink24.bot.commands.moderation;

import com.wiilink24.bot.Bot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/**
 * Kick command
 *
 * @author Sketch
 */

public class Kick {
    private final Bot bot;
    public Kick(Bot bot) {
        this.bot = bot;
    }

    public void kick(SlashCommandInteractionEvent event) {
        // Member is a required field
        Member member = event.getOptionsByName("member").get(0).getAsMember();

        // Don't kick moderators or admins
        if (member.getPermissions().contains(Permission.BAN_MEMBERS)) {
            event.reply("You cannot ban a moderator/admin!").queue();
            return;
        }

        // Reason is optional
        String reason = "No reason provided.";
        if (!event.getOptionsByName("reason").isEmpty()) {
            reason = event.getOptionsByName("reason").get(0).getAsString();
        }

        String message = bot.timestamp()
                + " :boot: **"
                + event.getUser().getName()
                + "**#"
                + event.getUser().getDiscriminator()
                + " kicked **"
                + member.getUser().getName()
                + "**#"
                + member.getUser().getDiscriminator()
                + ".\nReason: `"
                + reason
                + "`";

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(":boot: Successfully kicked " + member.getUser().getName() + "#" + member.getUser().getDiscriminator())
                .setDescription("Reason: " + reason + "\nBy: " + event.getUser().getAsMention());
        event.replyEmbeds(embed.build()).queue();

        Bot.sendDM(member.getUser(), "You were kicked from WiiLink for `" + reason + "`").complete();
        event.getGuild().kick(member).complete();

        event.getJDA().getTextChannelById(bot.modLog()).sendMessage(message).complete();
    }
}
