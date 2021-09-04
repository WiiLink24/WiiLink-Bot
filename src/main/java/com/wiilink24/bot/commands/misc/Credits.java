package com.wiilink24.bot.commands.misc;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import com.wiilink24.bot.commands.Categories;

/**
 * Bot credits
 *
 * @author Sketch
 */

public class Credits extends Command {
    public Credits() {
        this.name = "credits";
        this.category = Categories.MISC;
        this.help = "Displays Credits";
    }

    @Override
    protected void execute(CommandEvent event) {
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(0x00FF00)
                .setTitle("Credits", null)
                .setAuthor("WiiLink Bot", null, event.getSelfUser().getAvatarUrl())
                .addField("People", "<@239809536012058625>: Giving me the opportunity to be a developer as well as hosting the bot on cacti\n<@667563245107937297>: Creator and developer of WiiLink24 Bot\nJDA: Discord API wrapper made in Java\n", false);

        event.reply(embed.build());
    }
}
