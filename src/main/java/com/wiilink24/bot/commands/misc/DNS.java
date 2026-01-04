package com.wiilink24.bot.commands.misc;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class DNS {
    private static final String WIIMMFI_DNS = "167.235.229.36";
    private static final String WWFC_DNS = "5.161.56.11";
    private static final String SECONDARY_DNS = "1.1.1.1";

    public void dns(SlashCommandInteractionEvent event) {
        event.replyFormat("### WiiLink / Wiimmfi\n`%s` should be your primary DNS.\n### WiiLink WFC\n`%s` should be your primary DNS.\n\nOn DS, your secondary DNS should match your primary. On Wii, you can use any public DNS server for your secondary, i.e.`%s`", WIIMMFI_DNS, WWFC_DNS, SECONDARY_DNS).queue();
    }
}
