/* Was too lazy to make my own, this is a modified version of RC24 Bot's command */

package com.wiilink24.bot.commands.misc;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.wiilink24.bot.commands.Categories;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.wiilink24.bot.utils.SearcherUtil;


import java.io.IOException;

public class RiiTag extends Command
{
    private final OkHttpClient httpClient;
    private final String URL = "https://tag.rc24.xyz/%s/tag.max.png?randomizer=%f";

    public RiiTag()
    {
        this.name = "riitag";
        this.help = "Gets a user's RiiTag";
        this.arguments = "[user]";
        this.aliases = new String[]{"tag"};
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
                    event.replyError("WOW! RiiTag has timed out! What a surprise, indeed " +
                            "||it's not a surprise, at all||.\n Complain to Larsenv, I guess.");
                }

                @Override
                public void onResponse(Call call, Response response)
                {
                    if(response.code() == 404)
                    {
                        event.replyError("**" + user.getAsTag() + "** does not have a RiiTag!");
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
                .setAuthor(user.getAsTag() + "'s RiiTag", null, user.getEffectiveAvatarUrl())
                .setColor(0x00FF00)
                .setImage(String.format(URL, user.getId(), Math.random()));

        event.reply(embedBuilder.build());
    }
}
