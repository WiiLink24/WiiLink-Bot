package com.wiilink24.bot.commands.misc;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/**
 * Bot credits
 *
 * @author Sketch
 */

public class Credits  {
    public Credits() {}

    public void credits(SlashCommandInteractionEvent event) {
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(0x00FF00)
                .setTitle("Credits", null)
                .setAuthor("WiiLink Bot", null, event.getUser().getAvatarUrl())
                .addField("People", "<@239809536012058625>: Giving me the opportunity to be a developer as well as hosting the bot on cacti\n<@667563245107937297>: Creator and developer of WiiLink24 Bot\nJDA: Discord API wrapper made in Java\njagrosh: I used the MessageCache from Vortex,\n", false);

        event.replyEmbeds(embed.build()).queue();
    }
}
