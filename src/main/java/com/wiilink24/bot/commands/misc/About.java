package com.wiilink24.bot.commands.misc;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/**
 * About command
 *
 * @author Sketch
 */

public class About  {
    public About() {}

    public void about(SlashCommandInteractionEvent event) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("<:wiilink:844609429239234640> About WiiLink Bot")
                .setColor(0x00FF00)
                .setThumbnail(event.getGuild().getIconUrl())
                .addField("Created by:", "Sketch", false)
                .addField("Created for:", "The WiiLink Discord server", false)
                .addField("More Info:", "The prefix is `/`.\nWiiLink Bot is written in Java using JDA.\nIf you need help, run `/help` or contact Sketch.", false);

        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
    }
}
