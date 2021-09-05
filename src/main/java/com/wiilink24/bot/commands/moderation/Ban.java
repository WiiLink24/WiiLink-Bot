package com.wiilink24.bot.commands.moderation;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.wiilink24.bot.Bot;
import com.wiilink24.bot.commands.Categories;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

/**
 * Ban command
 *
 * @author Sketch
 */

public class Ban extends Command {
    private final Bot bot;

    public Ban() {
        this.bot = new Bot();
        this.name = "ban";
        this.category = Categories.MODERATION;
        this.userPermissions = new Permission[]{Permission.BAN_MEMBERS};
    }

    @Override
    protected void execute(CommandEvent event) {
        String[] args = event.getArgs().split("\\s", 2);
        String banReason;
        Member member = event.getGuild().retrieveMemberById(args[0]).complete();
        User user = member.getUser();

        // Build ban reason
        if (args.length == 1) {
            // No reason was provided
            banReason = "No reason provided";
        } else {
            banReason = args[1];
        }

        bot.sendDM(user, "You were banned in WiiLink24 for **`" + banReason + "`**").complete();

        event.getGuild().ban(member, 1, banReason).queue(
                success -> {
                    EmbedBuilder embed = new EmbedBuilder()
                            .setTitle(":hammer: Successfully banned " + user.getName() + "#" + user.getDiscriminator())
                            .setDescription("Reason: " + banReason + "\nBy: " + event.getAuthor().getAsMention());
                    event.reply(embed.build());

                    // Send to logs
                    String topMessage = bot.timestamp()
                            + " :hammer: **"
                            + event.getAuthor().getName()
                            + "**#"
                            + event.getAuthor().getDiscriminator()
                            + " banned **"
                            + user.getName()
                            + "**#"
                            + user.getDiscriminator()
                            + "(ID:"
                            + user.getId()
                            + ")\nReason: "
                            + "`"
                            + banReason
                            + "`";

                    event.getJDA().getTextChannelById(bot.modLog()).sendMessage(topMessage).queue();
                }, failure -> {
                    event.replyError("Failed to ban the requested user.");
                }
        );
    }
}
