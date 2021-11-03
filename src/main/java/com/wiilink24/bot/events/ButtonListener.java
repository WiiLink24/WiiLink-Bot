package com.wiilink24.bot.events;

import com.wiilink24.bot.Bot;
import com.wiilink24.bot.Database;
import com.wiilink24.bot.utils.WadUtil;
import io.sentry.Sentry;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ButtonListener extends ListenerAdapter {
    private Database database;
    private WadUtil wad;

    public ButtonListener(Bot bot) {
        this.database = new Database();
        this.wad = new WadUtil(bot);
    }

    @Override
    public void onButtonClick(ButtonClickEvent event) {
        String passedId = event.getComponentId();
        if (passedId.startsWith("patchdl_")) {
            // This may take a while. Let Discord know we received the event.
            event.deferReply()
                    .setEphemeral(true)
                    .queue();

            try {
                // We now know we're dealing with patches.
                // Trim off the first 8 characters (`patchdl_`).
                String patchIdStr = passedId.substring(8);
                int patchId = Integer.parseInt(patchIdStr);
                String userId = event.getUser().getId();

                // First, check to see if we've already generated a patch for this user.
                String url = database.getWadUrl(patchId, userId);
                if (url.equals("")) {
                    // Generate a new wad for our user.
                    String filename = database.getWadFilename(patchId);
                    url = wad.uploadWad(filename, userId);

                    // Cache it in our database.
                    database.setWadUrl(patchId, userId, url);
                }

                event.getHook()
                        .sendMessage("Please visit the following to download: " + url)
                        .queue();
            } catch (Throwable e) {
                Sentry.captureException(e);
                event.getHook()
                        .sendMessage("A database error occurred. Please contact a developer.")
                        .queue();
            }
        }
    }
}
