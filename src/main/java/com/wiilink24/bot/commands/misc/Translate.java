package com.wiilink24.bot.commands.misc;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.wiilink24.bot.Bot;
import com.wiilink24.bot.commands.Categories;
import io.sentry.Sentry;
import net.dv8tion.jda.api.EmbedBuilder;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class Translate extends Command {
    private final Bot bot;

    public Translate(Bot bot) {
        this.name = "translate";
        this.bot = bot;
        this.category = Categories.MISC;
        this.arguments = "[language] [text]";
        this.help = "Translates text into the specified language. NOTE: Only languages DeepL support can be used ";
    }

    @Override
    protected void execute(CommandEvent event) {
        event.async(() -> {
            // Hashtable of all the languages DeepL supports
            Hashtable<String, String> langTable = new Hashtable<>(
                    Map.ofEntries(
                            Map.entry("bulgarian", "BG"),
                            Map.entry("czech", "CZ"),
                            Map.entry("english", "EN"),
                            Map.entry("danish", "DA"),
                            Map.entry("german", "DE"),
                            Map.entry("greek", "EL"),
                            Map.entry("spanish", "ES"),
                            Map.entry("estonian", "ET"),
                            Map.entry("finnish", "FI"),
                            Map.entry("french", "FR"),
                            Map.entry("hungarian", "HU"),
                            Map.entry("italian", "IT"),
                            Map.entry("japanese", "JA"),
                            Map.entry("lithuanian", "LT"),
                            Map.entry("latvian", "LV"),
                            Map.entry("dutch", "NL"),
                            Map.entry("polish", "PL"),
                            Map.entry("portuguese", "PT"),
                            Map.entry("romanian", "RO"),
                            Map.entry("russian", "RU"),
                            Map.entry("slovak", "SK"),
                            Map.entry("slovenian", "SL"),
                            Map.entry("swedish", "SV"),
                            Map.entry("chinese", "ZH")
                    )
            );

            String[] args = event.getArgs().split("\\s", 2);
            HttpClient httpClient = HttpClients.createDefault();
            HttpPost post = new HttpPost("https://api-free.deepl.com/v2/translate");

            List<NameValuePair> params = new ArrayList<>(1);
            params.add(new BasicNameValuePair("auth_key", bot.deepl()));
            params.add(new BasicNameValuePair("Text", args[1]));
            params.add(new BasicNameValuePair("target_lang", langTable.get(args[0].toLowerCase())));

            try {
                post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                // This should never fail
                e.printStackTrace();
            }

            try {
                HttpResponse response = httpClient.execute(post);
                HttpEntity entity = response.getEntity();

                if (entity != null) {
                    // Now we parse the response which is a JSON
                    String jsonString = EntityUtils.toString(entity, "UTF-8");
                    JSONObject jsonObject = new JSONObject(jsonString);

                    // DeepL is decided to nest our data inside an array.
                    // I am assuming this is if we add multiple text to translate
                    JSONArray array = jsonObject.getJSONArray("translations");
                    String text = array.getJSONObject(0).getString("text");

                    EmbedBuilder embed = new EmbedBuilder()
                            .setTitle("Translation")
                            .setFooter("All translations are provided by DeepL", null)
                            .setDescription("```" + text + "```")
                            .setColor(0xADD8E6);

                    event.reply(embed.build());
                }
            } catch (IOException e) {
                Sentry.captureException(e);
            }
        });
    }
}
