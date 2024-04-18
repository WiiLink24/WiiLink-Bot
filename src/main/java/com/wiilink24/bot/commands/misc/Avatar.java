package com.wiilink24.bot.commands.misc;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

/**
 * Avatar command
 *
 * @author Sketch
 */

public class Avatar {
    public Avatar() {}

    public void avatar(SlashCommandInteractionEvent event) {
        User user = event.getUser();
        if (!event.getOptionsByName("user").isEmpty()) {
            user = event.getOptionsByName("user").get(0).getAsUser();
        }

        EmbedBuilder embed = new EmbedBuilder()
                .setColor(0x00FF00)
                .setAuthor(user.getName(), null, user.getEffectiveAvatarUrl())
                .setImage(user.getEffectiveAvatarUrl());

        event.replyEmbeds(embed.build()).queue();
    }
}
