package com.wiilink24.bot.commands.moderation;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.wiilink24.bot.Bot;
import com.wiilink24.bot.commands.Categories;
import com.wiilink24.bot.events.SlashCommandListener;
import io.sentry.Sentry;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Clear command
 *
 * @author Sketch
 */

public class Clear {
    public Clear() {}

    public void clear(SlashCommandEvent event) {
        String messagesToClear = event.getOptionsByName("amount").get(0).getAsString();
        MessageHistory history = new MessageHistory(event.getChannel());
        List<Message> messages = history.retrievePast(Integer.parseInt(messagesToClear)).complete();

        event.getChannel().purgeMessages(messages);

        try {
            Thread.sleep(1000);
            event.reply("I have cleared " + messagesToClear + " messages.").queue(
                    success -> {
                        try {
                            Thread.sleep(5);
                            success.deleteOriginal().queue();
                        } catch (InterruptedException e) {
                            Sentry.captureException(e);
                        }
                    }
            );
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Log to server logs
        EmbedBuilder embed = new EmbedBuilder().setTitle(event.getUser().getName() + "#" + event.getUser().getDiscriminator() + " cleared " + messagesToClear + " messages");
        event.getJDA().getTextChannelById(Bot.modLog()).sendMessageEmbeds(embed.build()).complete();
    }
}
