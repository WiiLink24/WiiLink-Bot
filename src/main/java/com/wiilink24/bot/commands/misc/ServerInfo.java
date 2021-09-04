package com.wiilink24.bot.commands.misc;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.wiilink24.bot.commands.Categories;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.requests.RestAction;

import java.time.format.DateTimeFormatter;

public class ServerInfo extends Command {
    public ServerInfo() {
        this.name = "serverinfo";
        this.category = Categories.MISC;
    }

    @Override
    protected void execute(CommandEvent event) {
        Guild guild = event.getGuild();

        String features = "";
        features = guild.getFeatures().stream().map(feature -> " " + feature + ",").reduce(features, String::concat);

        // For reasons unknown to me, "net.dv8tion.jda.api.entities.Guild.getOwner()" always returns null.
        // I assume lazy loading is the fault as "net.dv8tion.jda.api.entities.Guild.retrieveOwner()" works
        RestAction<String> owner =  guild.retrieveOwner().map(Member::getAsMention);

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Server Information")
                .setThumbnail(guild.getIconUrl())
                .setImage(guild.getSplashUrl() + "?size=1024")
                .addField(String.format("**%s**", guild.getName()),
                        ":white_small_square: ID: **" + guild.getId()
                                + "**\n:white_small_square: Owner: **" + owner.complete()
                                + "**\n:white_small_square: Location: **" + guild.getLocale().getCountry()
                                + "**\n:white_small_square: Creation: **" +  guild.getTimeCreated().format(DateTimeFormatter.RFC_1123_DATE_TIME)
                                + "**\n:white_small_square: Members: **" + guild.getMemberCount()
                                + "**\n:white_small_square: Channels: " + String.format("**%d** Channels; **%d** Text, **%d** Voice, **%d** Categories",
                                guild.getChannels().size(), guild.getTextChannels().size(), guild.getVoiceChannels().size(), guild.getCategories().size())
                                + "\n:white_small_square: Verification: **" + guild.getVerificationLevel()
                                + "**\n:white_small_square: Features: **" + features + "**",
                        false);

        event.reply(embed.build());
    }
}

