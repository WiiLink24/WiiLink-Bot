package com.wiilink24.bot.commands.misc;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import io.sentry.Sentry;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.wiilink24.bot.commands.Categories;
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

public class Mii extends Command {
    public Mii() {
        this.name = "mii";
        this.arguments = "[entry_number]";
        this.category = Categories.MISC;
        this.help = "Display's the requested Mii.";
    }

    @Override
    protected void execute(CommandEvent event) {
        String link;
        // Check if the argument was a name or entry number
        Pattern pattern = Pattern.compile("[0-9]{12}", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(event.getArgs().replace("-", ""));
        boolean match = matcher.find();

        if (match) {
            link = "https://miicontestp.wii.rc24.xyz/cgi-bin/htmlsearch.cgi?query=" + event.getArgs();
        } else {
            link = "https://miicontestp.wii.rc24.xyz/cgi-bin/htmlmiisearch.cgi?query=" + event.getArgs();
        }

        try {
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

            event.reply(embed.build());
        } catch (IOException e) {
            Sentry.captureException(e);
        }
    }
}
