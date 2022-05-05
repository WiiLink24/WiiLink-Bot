package com.wiilink24.bot.commands.misc;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class Ping {
    public Ping() {}

    public void ping(SlashCommandInteractionEvent event)
    {
        event.getJDA().getRestPing().queue(ping -> event.reply("Gateway Ping: " + event.getJDA().getGatewayPing() + "ms\nDiscord API Ping: " + ping + "ms").queue());
    }
}