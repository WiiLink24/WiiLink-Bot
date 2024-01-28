package com.wiilink24.bot.events;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;

public class SelectionBoxListener extends ListenerAdapter {
    public SelectionBoxListener() {}

    public void onSelectMenuInteraction(@Nonnull SelectMenuInteractionEvent event) {
        if (event.getComponentId().equals("help")) {
            // I hate you JDA for making the selection like this
            EmbedBuilder embed = new EmbedBuilder();
            if (event.getValues().get(0).equals("misc")) {
                embed.setTitle("Miscellaneous Commands")
                        .setDescription("General commands all users can run. All of these commands are slash commands. Arguments wrapped in `<>` are optional while `[]` is required.")
                        .addField("About", "About WiiLink Bot. Takes no arguments", false)
                        .addField("AFK", "Sets your status to AFK. Whenever you are mentioned the person who mentioned will be alerted of your AFK status and reason. You will get a DM with the message content and author. Usage: `/afk <reason>`", false)
                        .addField("Avatar", "Gets your avatar or the specified user's avatar. Usage: `/avatar <user>`", false)
                        .addField("Credits", "Credits for WiiLink Bot. Takes no arguments", false)
                        .addField("Digicard", "Gets your Digicard or the specified user's Digicard. Usage: `/card <user>`", false)
                        .addField("GameTDB", "Gets information about the specified game. Usage: `/gametdb [console] [gameid]`", false)
                        .addField("Help", "Load the help screen. Takes no arguments", false)
                        .addField("Mii", "Gets the photo of a Mii from the Check Mii Out Channel website. Usage: `/mii [entry-number/name]`", false)
                        .addField("News", "Gets all WiiLink news ever written. Takes no arguments", false)
                        .addField("OCR", "Reads text from an image using Google Cloud. Usage: `/ocr [url]`", false)
                        .addField("Ping", "Gets the gateway ping. Takes no arguments", false)
                        .addField("RiiTag", "Gets your RiiTag or the specified user's RiiTag. Usage: `/riitag <user>`", false)
                        .addField("Server Info", "Gets info about the current server. Takes no arguments", false)
                        .addField("Translate", "Translates text into the specified language by using DeepL. Usage: `/translate [text]`", false)
                        .addField("User Info", "Gets information about you or the specified user. Usage: `/userinfo <user>`", false);


                event.editMessageEmbeds(embed.build()).queue();
            } else if (event.getValues().get(0).equals("mod")) {
                embed.setTitle("Moderation Commands")
                        .setDescription("Commands that only mods can use. Use wisely. Arguments wrapped in `<>` are optional while `[]` is required")
                        .addField("Ban", "Bans a user from the server. Usage: `/ban [user] <reason>`", false)
                        .addField("Check", "Checks moderation information about a user. Usage: `/check [user]`", false)
                        .addField("Clear", "Clears a specified amount of messages. Usage: `/clear [num]`", false)
                        .addField("Kick", "Kicks a user from the server. Usage: `/kick [user] <reason>`", false)
                        .addField("Timeout", "Times out a user in the server. Usage: `/timeout [user] [time] <reason>`", false)
                        .addField("Unban", "Unbans a user from the server. Usage: `/unban [user]`", false);


                event.editMessageEmbeds(embed.build()).queue();
            }
        }
    }
}
