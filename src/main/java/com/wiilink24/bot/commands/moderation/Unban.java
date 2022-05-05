package com.wiilink24.bot.commands.moderation;

import com.wiilink24.bot.Bot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/**
 * Unban command
 *
 * @author Sketch
 */

public class Unban {
    public Unban() {}

    public void unban(SlashCommandInteractionEvent event) {
        // User is a required field
        User user = event.getOptionsByName("user").get(0).getAsUser();

        event.getGuild().unban(user).queue(
                success -> {
                    EmbedBuilder embed = new EmbedBuilder()
                            .setTitle(":hammer: Successfully unbanned " + user.getName() + "#" + user.getDiscriminator());
                    event.replyEmbeds(embed.build()).queue();

                    // Send to logs
                    String topMessage = Bot.timestamp()
                            + " :hammer: **"
                            + event.getUser().getName()
                            + "**#"
                            + event.getUser().getDiscriminator()
                            + " unbanned **"
                            + user.getName()
                            + "**#"
                            + user.getDiscriminator()
                            + " (ID:"
                            + user.getId()
                            + ")\n";

                    event.getJDA().getTextChannelById(Bot.serverLog()).sendMessage(topMessage).queue();
                }, failure -> {
                    event.reply("Failed to unban the requested user.").queue();
                }
        );
    }
}
