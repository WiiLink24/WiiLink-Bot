package com.wiilink24.bot.commands.misc;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;


public class Help {
    public Help() {}

    public void help(SlashCommandInteractionEvent event) {
        SelectMenu.Builder selectionMenu = SelectMenu.create("help")
                .addOption("Miscellaneous", "misc")
                .addOption("Music", "music");

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("WiiLink Bot Help")
                .setDescription("Read through the command categories and use the corresponding selection menu")
                .addField("Miscellaneous Commands", "General commands that all users can use", false)
                .addField("Music Commands", "Commands to control the music functionality of WiiLink Bot. Can be used in all servers.", false)
                .setFooter("Invoked by: " + event.getUser().getName(), event.getUser().getEffectiveAvatarUrl());

        if (event.getMember().getPermissions().contains(Permission.BAN_MEMBERS)) {
            embed.addField("Moderation Commands", "Commands only moderators can use. Only available in the WiiLink Server", false);
            selectionMenu.addOption("Moderation", "mod");
        }

        event.replyEmbeds(embed.build()).addActionRow(
            selectionMenu.build()
        ).setEphemeral(true).queue();
    }
}
