package com.wiilink24.bot.commands.moderation;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.wiilink24.bot.Bot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;

public class Unban extends Command {
    Bot bot = new Bot();

    public Unban() {
        this.name = "unban";
        this.userPermissions = new Permission[]{Permission.BAN_MEMBERS};
    }

    @Override
    protected void execute(CommandEvent event) {
        String[] args = event.getArgs().split("\\s");
        StringBuilder unbanReason = new StringBuilder();
        User user = event.getJDA().retrieveUserById(args[0]).complete();

        // Build unban reason
        if (args.length == 1) {
            // No reason was provided
            unbanReason.append("No reason provided");
        } else {
            for (int i = 1; i < args.length; i++) {
                unbanReason.append(args[i]).append(" ");
            }
        }

        event.getGuild().unban(user).queue(
                success -> {
                    EmbedBuilder embed = new EmbedBuilder()
                            .setTitle(":hammer: Successfully unbanned " + user.getName() + "#" + user.getDiscriminator())
                            .setDescription("Reason: " + unbanReason + "\nBy: " + event.getAuthor().getAsMention());
                    event.reply(embed.build());

                    // Send to logs
                    String topMessage = bot.timestamp()
                            + " :hammer: **"
                            + event.getAuthor().getName()
                            + "**#"
                            + event.getAuthor().getDiscriminator()
                            + " unbanned **"
                            + user.getName()
                            + "**#"
                            + user.getDiscriminator()
                            + "(ID:"
                            + user.getId()
                            + ")\nReason: "
                            + "`"
                            + unbanReason
                            + "`";

                    event.getJDA().getTextChannelById(bot.modLog()).sendMessage(topMessage).queue();
                }, failure -> {
                    event.replyError("Failed to unban the requested user.");
                }
        );
    }
}
