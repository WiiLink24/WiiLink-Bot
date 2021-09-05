package com.wiilink24.bot.commands.misc;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.wiilink24.bot.commands.Categories;
import io.sentry.Sentry;
import net.dv8tion.jda.api.EmbedBuilder;
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

public class GameTDB extends Command {
    private final OkHttpClient httpClient;

    public GameTDB() {
        this.name = "gametdb";
        this.category = Categories.MISC;
        this.arguments = "[console] [titleID]";
        this.help = "Grabs game data from the GameTDB website. Enter the console and titleID to get started.";
        this.httpClient = new OkHttpClient();
    }

    @Override
    protected void execute(CommandEvent event) {
        event.async(() -> {
            String[] args = event.getArgs().split("\\s");
            String link = String.format("https://www.gametdb.com/%s/%s", args[0], args[1]);

            Request request = new Request.Builder().url(link).build();
            httpClient.newCall(request).enqueue(new Callback()
            {
                @Override
                public void onFailure(Call call, IOException e)
                {
                    event.replyError("There is a server error on GameTDB's end.");
                }

                @Override
                public void onResponse(Call call, Response response)
                {
                    if(response.code() == 404)
                    {
                        event.replyError("The requested game does not exist.");
                        return;
                    }

                    if(!(response.isSuccessful()))
                    {
                        onFailure(call, new IOException("Server error: HTTP Code " + response.code()));
                        return;
                    }

                    displayGame(event, link);
                }
            });
        });
    }

    private void displayGame(CommandEvent event, String link) {
        try {
            String[] args = event.getArgs().split("\\s");
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
                    .setImage(getImage(args[0].toLowerCase(), args[1]))
                    .setColor(0x00FF00);

            event.reply(embed.build());
        } catch (IOException e) {
            Sentry.captureException(e);
        }
    }

    String getImage(String system, String game_id) {
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
