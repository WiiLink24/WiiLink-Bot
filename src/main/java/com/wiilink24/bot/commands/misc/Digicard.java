package com.wiilink24.bot.commands.misc;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import okhttp3.*;

import java.io.IOException;

public class Digicard  {
    private final OkHttpClient httpClient;
    private final String URL = "https://card.wiilink24.com/cards/%s.jpg?randomizer=%f";

    public Digicard()
    {
        this.httpClient = new OkHttpClient();
    }

    public void card(SlashCommandEvent event) {
        User user = event.getUser();
        if (!event.getOptionsByName("user").isEmpty()) {
            user = event.getOptionsByName("user").get(0).getAsUser();
        }

        Request request = new Request.Builder().url(String.format(URL, user.getId(), 0D)).build();
        User finalUser = user;
        httpClient.newCall(request).enqueue(new Callback()
        {
            @Override
            public void onFailure(Call call, IOException e)
            {
                event.reply("Digicard has timed out. Please contact Sketch.").setEphemeral(true).queue();
            }

            @Override
            public void onResponse(Call call, Response response)
            {
                if(response.code() == 404)
                {
                    event.reply("**" + finalUser.getAsTag() + "** does not have a Digicard!").setEphemeral(true).queue();
                    response.close();
                    return;
                }

                if(!(response.isSuccessful()))
                {
                    onFailure(call, new IOException("Server error: HTTP Code " + response.code()));
                    return;
                }

                EmbedBuilder embed = new EmbedBuilder()
                        .setAuthor(finalUser.getAsTag() + "'s Digicard", null, finalUser.getEffectiveAvatarUrl())
                        .setColor(0x00FF00)
                        .setImage(String.format(URL, finalUser.getId(), Math.random()));

                event.replyEmbeds(embed.build()).queue();
                response.close();
            }
        });
    }
}
