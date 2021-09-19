package com.wiilink24.bot.commands.misc;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.wiilink24.bot.commands.Categories;
import com.wiilink24.bot.utils.SearcherUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import okhttp3.*;

import java.io.IOException;

public class Digicard extends Command {
    private final OkHttpClient httpClient;
    private final String URL = "https://card.wiilink24.com/cards/%s.jpg?randomizer=%f";

    public Digicard()
    {
        this.name = "digicard";
        this.aliases = new String[]{"card"};
        this.help = "Gets a user's Digicard";
        this.arguments = "[user]";
        this.category = Categories.MISC;
        this.httpClient = new OkHttpClient();
    }

    @Override
    protected void execute(CommandEvent event)
    {
        event.async(() ->
        {
            Member member = SearcherUtil.findMember(event, event.getArgs());
            if(member == null)
                return;

            User user = member.getUser();
            Request request = new Request.Builder().url(String.format(URL, user.getId(), 0D)).build();
            httpClient.newCall(request).enqueue(new Callback()
            {
                @Override
                public void onFailure(Call call, IOException e)
                {
                    event.replyError("Digicard has timed out. Please contact Sketch or Snoot.");
                }

                @Override
                public void onResponse(Call call, Response response)
                {
                    if(response.code() == 404)
                    {
                        event.replyError("**" + user.getAsTag() + "** does not have a Digicard!");
                        return;
                    }

                    if(!(response.isSuccessful()))
                    {
                        onFailure(call, new IOException("Server error: HTTP Code " + response.code()));
                        return;
                    }

                    displayTag(event, user);
                }
            });
        });
    }

    private void displayTag(CommandEvent event, User user)
    {
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setAuthor(user.getAsTag() + "'s Digicard", null, user.getEffectiveAvatarUrl())
                .setColor(0x00FF00)
                .setImage(String.format(URL, user.getId(), Math.random()));

        event.reply(embedBuilder.build());
    }
}
