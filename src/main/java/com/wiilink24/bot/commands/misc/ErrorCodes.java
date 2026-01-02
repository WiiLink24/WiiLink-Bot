/*
 * MIT License
 *
 * Copyright (c) 2017-2020 RiiConnect24 and its contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.wiilink24.bot.commands.misc;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.wiilink24.bot.WiiLinkBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.awt.*;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Looks up errors using the Wiimmfi API.
 *
 * @author Spotlight, Artuto, Gamebuster
 */

public class ErrorCodes {
    private static final OkHttpClient httpClient = WiiLinkBot.getInstance().getHttpClient();
    private static final Pattern CHANNEL = Pattern.compile("(NEWS|FORE)0{4}\\d{2}", Pattern.CASE_INSENSITIVE);
    private static final Pattern CHANNEL_CODE = Pattern.compile("0{4}\\d{2}");
    private static final Pattern CODE = Pattern.compile("\\d{1,6}");
    private static final Gson gson = new Gson();

    public void errorCode(SlashCommandInteractionEvent event) {

        String code = event.getOption("code").getAsString();
        Matcher channelCheck = CHANNEL.matcher(code);

        // Check for Fore/News
        if (channelCheck.find()) {

            // First match will be the type, then second our actual code.
            int codeNum;
            try {

                // Make sure the code's actually a code.
                Matcher codeCheck = CHANNEL_CODE.matcher(channelCheck.group());
                if (!(codeCheck.find())) {
                    event.reply("Invalid error code provided").setEphemeral(true).queue();
                    return;
                }

                codeNum = Integer.parseInt(codeCheck.group(0));

                if (channelErrors.get(codeNum) == null) {
                    event.reply("Invalid error code provided").setEphemeral(true).queue();
                    return;
                }

            } catch (NumberFormatException e) {
                event.reply("Could not find the specified app error code.").setEphemeral(true).queue();
                return;
            }

            String title = "Here's information about your error:";
            String description = channelErrors.get(codeNum);
            String footer = "All information provided by WiiLink Developers.";

            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle(title);
            builder.setDescription(description);
            builder.setColor(0xD32F2F);
            builder.setFooter(footer, null);
            event.replyEmbeds(builder.build()).queue();

        } else {

            int codeNum;
            try {

                // Validate if it is a number.
                Matcher codeCheck = CODE.matcher(code);
                if (!(codeCheck.find())) {
                    event.reply("Invalid error code provided").setEphemeral(true).queue();
                    return;
                }

                codeNum = Integer.parseInt(codeCheck.group(0));
                if (codeNum == 0) {
                    // 0 returns an empty array (see https://forum.wii-homebrew.com/index.php/Thread/57051-Wiimmfi-Error-API-has-an-error/?postID=680936)
                    // We'll just treat it as an error.
                    event.reply("Invalid error code provided").setEphemeral(true).queue();
                    return;
                }

            } catch (NumberFormatException e) {
                event.reply("Could not find the specified app error code.").setEphemeral(true).queue();
                return;
            }

            // Get method
            String method = "e=" + code;
            String url = "https://wiimmfi.de/error?" + method + "&m=json";

            Request request = new Request.Builder().url(url).build();
            httpClient.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    event.reply("Hm, something went wrong on our end. Check Wiimmfi's website is up?").setEphemeral(true).queue();}

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) {

                    if (!(response.isSuccessful())) {
                        onFailure(call, new IOException("Not success response code: " + response.code()));
                        response.close();
                        return;
                    }

                    try (response) {

                        String description;
                        JSONFormat json = gson.fromJson(new InputStreamReader(response.body().byteStream()), JSONFormat[].class)[0];
                        boolean success = json.found == 1;
                        EmbedBuilder embed = new EmbedBuilder();

                        if (!success) {
                            embed.setColor(Color.RED);
                            embed.setDescription("Could not find the specified error from Wiimmfi.");
                            return;
                        } else {

                            StringBuilder infoBuilder = new StringBuilder();

                            for (InfoListFormat format : json.infolists) {

                                String htmlToMarkdown = format.info;
                                Document infoSegment = Jsoup.parseBodyFragment(htmlToMarkdown);

                                // Replace links with Markdown format
                                for (Element hRef : infoSegment.select("a[href]")) {
                                    // So, we have to transform &amp; back to &.
                                    // It's funny, the same issue happened with Nokogiri and Ruby.
                                    String realOuterHTML = hRef.outerHtml();
                                    realOuterHTML = realOuterHTML.replace("&amp;", "&");
                                    htmlToMarkdown = htmlToMarkdown.replace(realOuterHTML, "[" + hRef.text() + "](" + hRef.attr("href") + ")");
                                }

                                // Parse again to handle updates
                                infoSegment = Jsoup.parseBodyFragment(htmlToMarkdown);
                                for (Element bold : infoSegment.select("b"))
                                    htmlToMarkdown = htmlToMarkdown.replace(bold.outerHtml(), "**" + bold.text() + "**");

                                // ...and parse, once more.
                                infoSegment = Jsoup.parseBodyFragment(htmlToMarkdown);
                                for (Element italics : infoSegment.select("i"))
                                    htmlToMarkdown = htmlToMarkdown.replace(italics.outerHtml(), "*" + italics.text() + "*");

                                infoBuilder.append(format.type).append(" for error ").append(format.name).append(": ").append(htmlToMarkdown).append("\n");
                            }

                            // Check for dev note
                            if (codeNotes.containsKey(json.error))
                                infoBuilder.append("Note from WiiLink: ").append(codeNotes.get(json.error));

                            String title = "Here's information about your error:";
                            description = infoBuilder.toString();
                            String footer = "All information is from Wiimmfi unless noted.";
                            embed.setTitle(title);
                            embed.setDescription(description);
                            embed.setColor(Color.decode("#D32F2F"));
                            embed.setFooter(footer, null);
                            event.replyEmbeds(embed.build()).queue();
                        }

                    } finally {
                        response.close();
                    }

                    response.close();
                }
            });
        }
    }

    private static final class JSONFormat {
        @SerializedName("error")
        int error;
        @SerializedName("found")
        int found;
        @SerializedName("infolist")
        InfoListFormat[] infolists;
    }

    private static final class InfoListFormat {
        @SerializedName("type")
        String type;
        @SerializedName("name")
        String name;
        @SerializedName("info")
        String info;
    }

    private static final Map<Integer, String> channelErrors = new HashMap<>() {{
        put(1, "Can't open the VFF.");
        put(2, "Seems to happen when there is a problem with one of the files on the NAND. " + "If you're getting it after fixing NEWS/FORE000006, do a connection test to fix it.");
        put(3, "VFF file corrupted.");
        put(4, "This error probably doesn't exist.");
        put(5, "Seems to happen when there is a problem with the VFF. If you're getting this on the Wii," + "follow https://wii.guide/deleting-vffs to fix it." + "If you're getting this on Dolphin, make sure you're using the VFF Downloader and make sure that it's working.");
        put(6, "Follow https://wii.hacks.guide/wiiconnect24#updating-rtc-clock and try again.");
        put(99, "Other error.");
    }};

    private static final Map<Integer, String> codeNotes = new HashMap<>() {{
        put(107245, "Make sure you've installed the latest patched WAD for the relevant channel.");
        put(107304, "Try again. If it still doesn't work, it might be a problem with your internet connection.");
        put(107305, "Try again. If it still doesn't work, it might be a problem with your internet connection.");
        put(117410, "If you are receiving this in the News Channel, follow https://wiilink.ca/guide/update to update it.");
        put(231000, "Restart the Channel or your Wii then try again.");
        put(231401, "You are not using the patched WAD for the Everybody Votes Channel. Please follow https://wiilink.ca/guide/install.");
        put(231409, "You are not using the patched WAD for the Everybody Votes Channel. Please follow https://wiilink.ca/guide/install.");
        put(239001, "This is a server-side issue that does not affect functionality, just press OK.");
        put(258302, "Follow https://wiilink.ca/guide/update to update the Check Mii Out Channel.");
        put(260400, "If you are receiving this in the Nintendo Channel, follow https://wiilink.ca/guide/update to update it.");
        put(33020, "If you are receiving this in the Check Mii Out Channel, follow https://wiilink.ca/guide/update to update it.");
        put(51330, "Try changing your router's settings to use 802.11 b/g/n. If that doesn't work, try the suggestions found on Nintendo's site. https://bit.ly/2OoC0c2");
        put(51331, "Try changing your router's settings to use 802.11 b/g/n. If that doesn't work, try the suggestions found on Nintendo's site. https://bit.ly/2OoC0c2");
        put(51332, "Try changing your router's settings to use 802.11 b/g/n. If that doesn't work, try the suggestions found on Nintendo's site. https://bit.ly/2OoC0c2");
        put(52030, "Try changing your router's settings to use 802.11 b/g/n. If that doesn't work, try the suggestions found on Nintendo's site. https://bit.ly/2OoC0c2");
        put(52031, "Try changing your router's settings to use 802.11 b/g/n. If that doesn't work, try the suggestions found on Nintendo's site. https://bit.ly/2OoC0c2");
        put(52032, "Try changing your router's settings to use 802.11 b/g/n. If that doesn't work, try the suggestions found on Nintendo's site. https://bit.ly/2OoC0c2");
    }};

}