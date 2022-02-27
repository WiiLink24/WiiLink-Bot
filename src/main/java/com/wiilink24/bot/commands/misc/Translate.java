package com.wiilink24.bot.commands.misc;

import com.wiilink24.bot.Bot;
import io.sentry.Sentry;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Translate {
    private final Bot bot;

    public Translate(Bot bot) {
        this.bot = bot;
    }

    public void translate(SlashCommandEvent event) {
        event.deferReply().queue();
        String language = event.getOptionsByName("language").get(0).getAsString();
        String text = event.getOptionsByName("text").get(0).getAsString();
        HttpClient httpClient = HttpClients.createDefault();
        HttpPost post = new HttpPost("https://api-free.deepl.com/v2/translate");

        List<NameValuePair> params = new ArrayList<>(1);
        params.add(new BasicNameValuePair("auth_key", bot.deepl()));
        params.add(new BasicNameValuePair("Text", text));
        params.add(new BasicNameValuePair("target_lang", language));

        post.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));

        try {
            HttpResponse response = httpClient.execute(post);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                // Now we parse the response which is a JSON
                String jsonString = EntityUtils.toString(entity, StandardCharsets.UTF_8);
                JSONObject jsonObject = new JSONObject(jsonString);

                // DeepL decided to nest our data inside an array.
                // I am assuming this is if we add multiple text to translate
                JSONArray array = jsonObject.getJSONArray("translations");
                String newText = array.getJSONObject(0).getString("text");
                String targetLanguage = array.getJSONObject(0).getString("detected_source_language");

                EmbedBuilder embed = new EmbedBuilder()
                        .setTitle("Translation")
                        .setFooter(targetLanguage + " -> " + language + " | All translations are made by DeepL", null)
                        .setDescription("```" + newText + "```")
                        .setColor(0xADD8E6);

                event.getHook().sendMessageEmbeds(embed.build()).queue();
            }
        } catch (IOException e) {
            Sentry.captureException(e);
        }
    }
}
