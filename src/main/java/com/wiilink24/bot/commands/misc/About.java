package com.wiilink24.bot.commands.misc;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.wiilink24.bot.commands.Categories;
import net.dv8tion.jda.api.EmbedBuilder;

/**
 * About command
 *
 * @author Sketch
 */

public class About extends Command {
    public About() {
        this.name = "about";
        this.category = Categories.MISC;
        this.help = "About WiiLink Bot";
    }

    @Override
    protected void execute(CommandEvent event) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("<:wiilink:844609429239234640> About WiiLink24 Bot")
                .setColor(0x00FF00)
                .setThumbnail(event.getGuild().getIconUrl())
                .addField("Created by:", "SketchMaster2001", false)
                .addField("Created for:", "The WiiLink Discord server", false)
                .addField("More Info:", "The prefix is `/`.\nWiiLink Bot is written in Java using JDA.\nIf you need help, run `/help` or contact Sketch.", false);

        event.reply(embed.build());
    }
}
