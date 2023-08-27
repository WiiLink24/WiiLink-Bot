package com.wiilink24.bot.commands.misc;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class Apply {

    private final String applyURL = "https://tripetto.app/run/HQBKZIND4A?userid=";

    public void apply(SlashCommandInteractionEvent event) {
        event.getInteraction().deferReply().queue();
        event.getHook().sendMessage("Click the link below to apply!\n" + applyURL + event.getUser().getId()).queue();
    }
}
