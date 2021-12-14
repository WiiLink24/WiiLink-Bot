package com.wiilink24.bot.events;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;

import javax.annotation.Nonnull;

public class SelectionBoxListener extends ListenerAdapter {
    public SelectionBoxListener() {}

    public void onSelectionMenu(@Nonnull SelectionMenuEvent event) {
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
            } else if (event.getValues().get(0).equals("music")) {
                embed.setTitle("Music Commands")
                        .setDescription("Commands that control the music player function of the bot.")
                        .addField("Play", "Plays audio from the specified URL or mp3 file. Usage: `/play [url/file]`", false)
                        .addField("Pause", "Pauses the current track. Usage: `/pause`", false)
                        .addField("Stop", "Stops playing the current track. Usage: `/stop`", false)
                        .addField("Seek", "Moves the track to the specified point in M:S format. Usage: `/seek [time]`", false)
                        .addField("Now Playing", "Shows the data and state of the current track. Usage: `/np`", false)
                        .addField("Restart", "Restarts the current track. Usage: `/restart`", false)
                        .addField("Next", "Plays the next track in queue. Usage: `/next`", false)
                        .addField("Previous", "Plays the previous track again. Usage: `/previous`", false)
                        .addField("Queue", "Shows the upcoming tracks as well as the currently playing track. Usage: `/queue`", false)
                        .addField("Shuffle", "Shuffles the songs in the queue. Usage: `/shuffle`", false)
                        .addField("Connect", "Connects the bot the the voice channel you are in. Usage: `/connect`", false)
                        .addField("Disconnect", "Disconnects the bot from the voice channel and clears the queue. Usage: `/disconnect`", false)
                        .addField("Volume", "Adjusts the player's volume. Usage: `/volume [level]`", false)
                        .addField("EQ", "Adjusts the equalizer for the current track. Usage: `/eq [preset]`", false);

                event.editMessageEmbeds(embed.build()).queue();
            }
        }
    }
}
