package com.wiilink24.bot.commands.misc;

import io.sentry.Sentry;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import okhttp3.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Arrays;

/**
 * Command that grabs game data from
 * the GameTDB website
 *
 * @author Sketch
 */

public class GameTDB {
    private final OkHttpClient httpClient;

    public GameTDB() {
        this.httpClient = new OkHttpClient();
    }

    public void gameTDB(SlashCommandEvent event) {
        String console = event.getOptionsByName("console").get(0).getAsString();
        String gameId = event.getOptionsByName("gameid").get(0).getAsString();
        String link = String.format("https://www.gametdb.com/%s/%s", console, gameId);

        event.deferReply().queue();

        Request request = new Request.Builder().url(link).build();
        try {
            Response response = httpClient.newCall(request).execute();

            switch (response.code()) {
                case 200 -> {
                    if (displayGame(event, link) != null) {
                        event.replyEmbeds(displayGame(event, link).build()).queue();
                    }
                }
                case 404 -> event.reply("The requested game does not exist.").setEphemeral(true).queue();
                default -> event.reply("There is a server error on GameTDB's end.").setEphemeral(true).queue();
            }
            response.close();
        } catch (IOException e) {
            Sentry.captureException(e);
        }
    }
    
    private EmbedBuilder displayGame(SlashCommandEvent event, String link) {
        try {
            String console = event.getOptionsByName("console").get(0).getAsString();
            String gameID = event.getOptionsByName("gameid").get(0).getAsString();
            Document doc = Jsoup.connect(link).get();
            Element table = doc.select("table.GameData").first();
            Elements rows = table.select("td.notranslate");

            String[] data = {};

            for (Element i : rows) {
                data = Arrays.copyOf(data, data.length + 1);
                data[data.length - 1] = i.text();
            }

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle(data[2])
                    .setDescription(data[4])
                    .setImage(getImage(console.toLowerCase(), gameID))
                    .setColor(0x00FF00);

            return embed;
        } catch (IOException e) {
            Sentry.captureException(e);
        }
        return null;
    }

    private String getImage(String system, String game_id) {
        String image_url = "";
        char[] region = game_id.toCharArray();
        String image = "https://art.gametdb.com/%s/cover/%s/%s.png";
        switch (region[3]) {
            case 'E' -> image_url = String.format(image, system, "US", game_id);
            case 'P' -> image_url = String.format(image, system, "EN", game_id);
            case 'J' -> image_url = String.format(image, system, "JA", game_id);
        }

        return image_url;
    }
}
