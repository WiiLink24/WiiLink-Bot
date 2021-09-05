package com.wiilink24.bot.commands.misc;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.wiilink24.bot.commands.Categories;


public class Ping extends Command
{
    public Ping()
    {
        this.name = "ping";
        this.help = "Checks the connection to Discord's servers";
        this.category = Categories.MISC;
    }

    @Override
    protected void execute(CommandEvent event)
    {
        event.getJDA().getRestPing().queue(ping -> event.replyFormatted("Gateway Ping: %dms, Discord API Ping: %dms", event.getJDA().getGatewayPing(), ping));
    }
}