package com.wiilink24.bot.commands.misc;

import com.wiilink24.bot.WiiLinkBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


import java.io.IOException;

public class RiiTag
{
    private final String URL = "https://tag.rc24.xyz/%s/tag.max.png?randomizer=%f";

    private final OkHttpClient httpClient = WiiLinkBot.getInstance().getHttpClient();

    public void riiTag(SlashCommandInteractionEvent event)
    {
        User user = event.getUser();
        if (!event.getOptionsByName("user").isEmpty()) {
            user = event.getOptionsByName("user").get(0).getAsUser();
        }

        event.deferReply().queue();
        Request request = new Request.Builder().url(String.format(URL, user.getId(), 0D)).build();
        User finalUser = user;

            httpClient.newCall(request).enqueue(new Callback()
            {
                @Override
                public void onFailure(Call call, IOException e)
                {
                    event.getHook().sendMessage("WOW! RiiTag has timed out! What a surprise, indeed " +
                            "||it's not a surprise, at all||.\n Complain to support, I guess.").queue();
                }

                @Override
                public void onResponse(Call call, Response response)
                {
                    if(response.code() == 404)
                    {
                        event.getHook().sendMessage("**" + finalUser.getAsTag() + "** does not have a RiiTag!").queue();
                        return;
                    }

                    if(!(response.isSuccessful()))
                    {
                        onFailure(call, new IOException("Server error: HTTP Code " + response.code()));
                        return;
                    }

                    displayTag(event, finalUser);
                }
            });
    }

    private void displayTag(SlashCommandInteractionEvent event, User user)
    {
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setAuthor(user.getAsTag() + "'s RiiTag", null, user.getEffectiveAvatarUrl())
                .setColor(0x00FF00)
                .setImage(String.format(URL, user.getId(), Math.random()));

        event.getHook().sendMessageEmbeds(embedBuilder.build()).queue();
    }
}
