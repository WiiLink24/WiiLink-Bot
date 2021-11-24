package com.wiilink24.bot.commands.moderation;

import com.wiilink24.bot.Bot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

/**
 * Ban command
 *
 * @author Sketch
 */

public class Ban {
    public Ban() {}

    public void ban(SlashCommandEvent event) {
        // User is a required field
        User user = event.getOptionsByName("user").get(0).getAsUser();

        if (event.getOptionsByName("user").get(0).getAsMember() != null) {
            // Don't ban moderators or admins
            if (event.getOptionsByName("user").get(0).getAsMember().getPermissions().contains(Permission.BAN_MEMBERS)) {
                event.reply("You cannot ban a moderator/admin!").queue();
                return;
            }
        }

        // Reason is optional
        String reason = "No reason provided.";
        if (!event.getOptionsByName("reason").isEmpty()) {
            reason = event.getOptionsByName("reason").get(0).getAsString();
        }

        String finalReason = reason;
        event.getGuild().ban(user, 1, reason).queue(
                success -> {
                    EmbedBuilder embed = new EmbedBuilder()
                            .setTitle(":hammer: Successfully banned " + user.getName() + "#" + user.getDiscriminator())
                            .setDescription("Reason: " + finalReason + "\nBy: " + event.getMember().getAsMention());

                    event.replyEmbeds(embed.build()).queue();

                    // Send to logs
                    String topMessage = Bot.timestamp()
                            + " :hammer: **"
                            + event.getUser().getName()
                            + "**#"
                            + event.getUser().getDiscriminator()
                            + " banned **"
                            + user.getName()
                            + "**#"
                            + user.getDiscriminator()
                            + "(ID:"
                            + user.getId()
                            + ")\nReason: "
                            + "`"
                            + finalReason
                            + "`";

                    Bot.sendDM(user, "You were banned in WiiLink for **`" + finalReason + "`**").queue();
                    event.getJDA().getTextChannelById(Bot.serverLog()).sendMessage(topMessage).queue();
                }, failure -> {
                    event.reply("Failed to ban the requested user.").queue();
                }
        );
    }
}
