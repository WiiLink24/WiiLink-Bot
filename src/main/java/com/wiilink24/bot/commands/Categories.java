package com.wiilink24.bot.commands;

import com.jagrosh.jdautilities.command.Command.Category;


/**
 * Command categories
 *
 * @author Sketch
 */

public class Categories {
    public static final Category MISC = new Category("Misc Commands");
    public static final Category OWNER = new Category("Commands only the owner of the bot can run");
    public static final Category MODERATION = new Category("Commands only moderators can run");
    public static final Category DEVELOPER = new Category("Commands only developers can run");
}
