package com.wiilink24.bot.commands.misc;

import io.sentry.Sentry;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;

/**
 * Grabs the first News article from
 * the WiiLink website
 *
 * @author Sketch
 */

public class News {
    public News() {}

    public void news(SlashCommandInteractionEvent event) {
        try {
            Document doc = Jsoup.connect("https://www.wiilink24.com/news").get();
            Element div = doc.select("div").get(3);
            Element date = div.select("h1").first();
            Element title = div.select("h3").first();
            Element body = div.select("p").first();
            Element authorNode = div.select("p").last();

            String author = authorNode.text().replace("—", "");
            author = author.replaceAll("\\s", "");

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle(title.text())
                    .setDescription(body.text() + "\n\nTo read the full news article, go to https://www.wiilink24.com/news")
                    .setFooter("Author: " + author + " | " + date.text(), null);

            event.replyEmbeds(embed.build()).addActionRow(
                    Button.primary("news_1_" + event.getUser().getId(), "Next")
            ).queue();
        } catch (IOException e) {
            Sentry.captureException(e);
        }
    }
}
