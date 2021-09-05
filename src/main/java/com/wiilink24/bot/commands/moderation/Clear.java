package com.wiilink24.bot.commands.moderation;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.wiilink24.bot.Bot;
import com.wiilink24.bot.commands.Categories;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;

import java.util.List;

/**
 * Clear command
 *
 * @author Sketch
 */

public class Clear extends Command {
    private final String modLog;

    public Clear(Bot bot) {
        this.modLog = bot.modLog();
        this.name = "clear";
        this.category = Categories.MODERATION;
        this.userPermissions = new Permission[]{Permission.BAN_MEMBERS};
    }

    @Override
    protected void execute(CommandEvent event) {
        event.async(() -> {
            MessageHistory history = new MessageHistory(event.getChannel());
            List<Message> messages = history.retrievePast(Integer.parseInt(event.getArgs())).complete();

            event.getChannel().purgeMessages(messages);

            try {
                Thread.sleep(1000);
                event.reply("I have cleared " + event.getArgs() + " messages.");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Log to server logs
            EmbedBuilder embed = new EmbedBuilder().setTitle(event.getAuthor().getName() + "#" + event.getAuthor().getDiscriminator() + " cleared " + event.getArgs() + " messages");
            event.getJDA().getTextChannelById(modLog).sendMessage(embed.build()).complete();
        });
    }
}
