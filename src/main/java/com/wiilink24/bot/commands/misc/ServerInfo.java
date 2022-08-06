package com.wiilink24.bot.commands.misc;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.requests.RestAction;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Server info command
 *
 * @author Sketch
 */

public class ServerInfo {
    public ServerInfo() {}

    public void serverInfo(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();

        String features = "";
        features = guild.getFeatures().stream().map(feature -> " " + feature + ",").reduce(features, String::concat);

        // For reasons unknown to me, "net.dv8tion.jda.api.entities.Guild.getOwner()" always returns null even though I cache owners.
        // I assume lazy loading is the fault as "net.dv8tion.jda.api.entities.Guild.retrieveOwner()" works
        RestAction<String> owner =  guild.retrieveOwner().map(Member::getAsMention);

        Locale serverLocale = Locale.forLanguageTag(guild.getLocale().getLocale());

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Server Information")
                .setThumbnail(guild.getIconUrl())
                .addField(String.format("**%s**", guild.getName()),
                        ":white_small_square: ID: **" + guild.getId()
                                + "**\n:white_small_square: Owner: **" + owner.complete()
                                + "**\n:white_small_square: Location: **" + serverLocale.getCountry()
                                + "**\n:white_small_square: Creation: **" +  guild.getTimeCreated().format(DateTimeFormatter.RFC_1123_DATE_TIME)
                                + "**\n:white_small_square: Members: **" + guild.getMemberCount()
                                + "**\n:white_small_square: Channels: " + String.format("**%d** Channels; **%d** Text, **%d** Voice, **%d** Categories",
                                guild.getChannels().size(), guild.getTextChannels().size(), guild.getVoiceChannels().size(), guild.getCategories().size())
                                + "\n:white_small_square: Verification: **" + guild.getVerificationLevel()
                                + "**\n:white_small_square: Features: **" + features + "**",
                        false);

        if (guild.getSplashUrl() != null) {
            embed.setImage(guild.getSplashUrl() + "?size=1024");
        }

        event.replyEmbeds(embed.build()).queue();
    }
}

