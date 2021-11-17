package com.wiilink24.bot.commands.owner;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.wiilink24.bot.Bot;
import com.wiilink24.bot.commands.Categories;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import org.codehaus.groovy.jsr223.GroovyScriptEngineFactory;


import javax.script.ScriptEngine;
import java.util.Arrays;
import java.util.List;

/**
 * @author Artuto, Spotlight, Sketch
 */

public class Eval extends Command
{
    private ScriptEngine engine;
    private List<String> imports;
    private Bot bot;

    public Eval(Bot bot)
    {
        this.bot = bot;
        this.name = "eval";
        this.help = "Executes Groovy code";
        this.category = Categories.OWNER;
        this.botPermissions = new Permission[]{Permission.MESSAGE_WRITE};
        this.userPermissions = new Permission[]{Permission.MESSAGE_WRITE};
        this.ownerCommand = true;

        engine = new GroovyScriptEngineFactory().getScriptEngine();
        imports = Arrays.asList("com.jagrosh.jdautilities.command",
                "com.jagrosh.jdautilities.command.impl",
                "com.jagrosh.jdautilities.commons",
                "com.jagrosh.jdautilities.commons.utils",
                "java.io", "java.lang", "java.util",
                "net.dv8tion.jda.api",
                "net.dv8tion.jda.api.entities",
                "net.dv8tion.jda.api.managers",
                "net.dv8tion.jda.api.utils",
                "net.dv8tion.jda.internal",
                "net.dv8tion.jda.internal.entities",
                "net.dv8tion.jda.internal.managers",
                "wiilink.bot");
    }

    @Override
    protected void execute(CommandEvent event)
    {
        String importString = "";
        String eval;

        try
        {
            engine.put("event", event);
            engine.put("jda", event.getJDA());
            engine.put("channel", event.getChannel());
            engine.put("message", event.getMessage());
            engine.put("bot", event.getSelfUser());
            engine.put("client", event.getClient());
            engine.put("author", event.getAuthor());
            engine.put("guild", event.getGuild());
            engine.put("bot", bot);
            if(event.isFromType(ChannelType.TEXT))
            {
                engine.put("member", event.getMember());
                engine.put("guild", event.getGuild());
                engine.put("tc", event.getTextChannel());
                engine.put("selfmember", event.getGuild().getSelfMember());
            }

            for(final String s : imports)
                importString += "import " + s + ".*;";

            eval = event.getArgs().replaceAll("getToken", "getSelfUser");
            Object out = engine.eval(importString + eval);

            if(out == null || String.valueOf(out).isEmpty())
                event.reactSuccess();
            else
                event.replySuccess("Done! Output:\n```java\n" + out.toString().replaceAll(event.getJDA().getToken(), "Nice try.") + " ```");
        }
        catch(Exception e2)
        {
            event.replyError("Error! Output:\n```java\n" + e2 + " ```");
        }
    }
}
