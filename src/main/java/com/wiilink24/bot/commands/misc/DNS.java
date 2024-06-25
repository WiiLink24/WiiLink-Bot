package com.wiilink24.bot.commands.misc;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class DNS {
    private static final String PRIMARY_DNS = "167.235.229.36";
    private static final String SECONDARY_DNS = "1.1.1.1";

    public void dns(SlashCommandInteractionEvent event) {
        event.replyFormat("`%s` should be your primary DNS.\n`%s` should be your secondary DNS.", PRIMARY_DNS, SECONDARY_DNS).queue();
    }
}
