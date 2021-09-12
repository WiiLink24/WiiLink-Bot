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
            Hashtable<String, String> langTable = new Hashtable<>();
            langTable.put("bulgarian", "BG");
            langTable.put("czech", "CZ");
            langTable.put("danish", "DA");
            langTable.put("german", "DE");
            langTable.put("greek", "EL");
            langTable.put("spanish", "ES");
            langTable.put("estonian", "ET");
            langTable.put("finnish", "FI");
            langTable.put("french", "FR");
            langTable.put("hungarian", "HU");
            langTable.put("italian", "IT");
            langTable.put("japanese", "JA");
            langTable.put("lithuanian", "LT");
            langTable.put("latvian", "LV");
            langTable.put("dutch", "NL");
            langTable.put("polish", "PL");
            langTable.put("portuguese", "PT");
            langTable.put("romanian", "RO");
            langTable.put("russian", "RU");
            langTable.put("slovak", "SK");
            langTable.put("slovenian", "SL");
            langTable.put("swedish", "SV");
            langTable.put("chinese", "ZH");

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
