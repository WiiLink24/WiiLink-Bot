package com.wiilink24.bot.commands.misc;

import io.sentry.Sentry;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Grabs a Mii from the
 * Check Mii Out Channel website
 *
 * @author Sketch
 */

public class Mii {
    public Mii() {}

    public void mii(SlashCommandEvent event) {
        String link;
        String entryNumber = event.getOptionsByName("argument").get(0).getAsString();
        // Check if the argument was a name or entry number
        Pattern pattern = Pattern.compile("[0-9]{12}", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(entryNumber.replace("-", ""));
        boolean match = matcher.find();

        if (match) {
            link = "https://miicontestp.wii.rc24.xyz/cgi-bin/htmlsearch.cgi?query=" + entryNumber;
        } else {
            link = "https://miicontestp.wii.rc24.xyz/cgi-bin/htmlmiisearch.cgi?query=" + entryNumber;
        }

        try {
            event.deferReply().queue();
            Document doc = Jsoup.connect(link).get();
            // Grab Mii image URL
            Element x = doc.select("a").first().select("img").first();
            String miiURL = x.attr("src");

            // Now we get the Mii Artisan and nickname
            Element table = doc.select("table").first();
            Elements rows = table.select("td");

            String[] data = {};

            for (Element i : rows) {
                data = Arrays.copyOf(data, data.length + 1);
                data[data.length - 1] = i.text();
            }

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle(data[1])
                    .setImage(miiURL)
                    .setFooter(String.format("Created by: %s", data[5]), "https://cdn.discordapp.com/emojis/420052317690396673.png?v=1")
                    .setColor(0x00FF00);


            event.getHook().sendMessageEmbeds(embed.build()).queue();
        } catch (IOException e) {
            Sentry.captureException(e);
        }
    }
}
