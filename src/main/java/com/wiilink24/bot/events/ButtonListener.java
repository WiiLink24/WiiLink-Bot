package com.wiilink24.bot.events;

import com.wiilink24.bot.Bot;
import com.wiilink24.bot.Database;
import com.wiilink24.bot.utils.WadUtil;
import io.sentry.Sentry;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ButtonListener extends ListenerAdapter {
    private Database database;
    private WadUtil wad;

    private final HashMap<String, String> buttonMappings = new HashMap<>(){{
        put("discord_updates", "785986443846615112");
        put("video_updates", "785986569445310485");
        put("content_updates", "790467855404892180");
        put("wiiroom_updates", "999534637602832504");
        put("digicam_updates", "999534692074262578");
        put("demae_updates", "1055876031304708156");
    }};

    public ButtonListener(Bot bot) {
        this.database = new Database();
        this.wad = new WadUtil(bot);
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String passedId = event.getComponentId();
        final Category ticketCategory = event.getGuild().getCategoryById("905822898424520725");
        
        if (passedId.startsWith("patchdl_")) {
            // This may take a while. Let Discord know we received the event.
            event.deferReply()
                    .setEphemeral(true)
                    .queue();

            try {
                // We now know we're dealing with patches.
                // Trim off the first 8 characters (`patchdl_`).
                String patchIdStr = passedId.substring(8);
                int patchId = Integer.parseInt(patchIdStr);
                String userId = event.getUser().getId();

                // First, check to see if we've already generated a patch for this user.
                String url = database.getWadUrl(patchId, userId);
                if (url.equals("")) {
                    // Generate a new wad for our user.
                    String filename = database.getWadFilename(patchId);
                    url = wad.uploadWad(filename, userId);

                    // Cache it in our database.
                    database.setWadUrl(patchId, userId, url);
                }

                event.getHook()
                        .sendMessage("Please visit the following to download: " + url)
                        .queue();
            } catch (Throwable e) {
                Sentry.captureException(e);
                event.getHook()
                        .sendMessage("A database error occurred. Please contact a developer.")
                        .queue();
            }
        }
        else if (passedId.startsWith("news_")) {
            try {
                // Trim off the first 5 characters (`news_`).
                String news = passedId.substring(5);
                String userId = news.substring(2);
                news = news.replace("_" + userId, "");

                int newsId = Integer.parseInt(news);

                if (!event.getUser().getId().equals(userId)) {
                    event.reply("You cannot change the page if you didn't invoke the command!").setEphemeral(true).queue();
                } else {
                    List<Button> buttons = new ArrayList<>();
                    buttons.add( Button.primary("news_" + (newsId - 1) + "_" + userId, "Previous"));
                    buttons.add(Button.primary("news_" + (newsId + 1) + "_" + userId, "Next"));

                    Document doc = Jsoup.connect("https://www.wiilink24.com/news").get();
                    Element div = doc.select("div").get(3 + newsId);
                    Element date = div.select("h1").first();
                    Element title = div.select("h3").first();
                    Element body = div.select("p").first();
                    Element authorNode = div.select("p").last();

                    String author = authorNode.text().replace("—", "");
                    author = author.replaceAll("\\s", "");

                    // Now we determine if there are news articles before or after the current one
                    if (doc.select("div").get(4 + newsId).select("h3").first() == null) {
                        buttons.remove(1);
                    }
                    if (newsId == 0) {
                        buttons.remove(0);
                    }

                    EmbedBuilder embed = new EmbedBuilder()
                            .setTitle(title.text())
                            .setDescription(body.text() + "\n\nTo read the full news article, go to https://www.wiilink24.com/news")
                            .setFooter("Author: " + author + " | " + date.text(), null);

                    event.deferEdit().queue();
                    event.getMessage().editMessageEmbeds(embed.build()).setActionRow(
                            buttons
                    ).queue();
                }
            } catch (IOException e) {
                Sentry.captureException(e);
            }
        }
        else if (passedId.startsWith("ticket")) {
            Member member = event.getMember();

            // This may take a while. Let Discord know we received the event.
            event.deferReply()
                    .setEphemeral(true)
                    .queue();

            try {
                boolean hasTicket = database.checkTicketUser(member.getId());
                if (hasTicket) {
                    event.getHook().sendMessage("You already have an open ticket!").setEphemeral(true).queue();
                    return;
                }
            } catch (SQLException e) {
                event.getHook().sendMessage("Failed to check database").setEphemeral(true).queue();
                Sentry.captureException(e);
                return;
            }

            // Insert ticket into the database then get the ticket id
            int ticketId;
            try {
                ticketId = database.insertTicket(member.getId());
            } catch (SQLException e) {
                event.getHook().sendMessage("Unable to insert ticket in database.").setEphemeral(true).queue();
                Sentry.captureException(e);
                return;
            }

            event.getGuild().createTextChannel("ticket-" + ticketId, ticketCategory).queue(
                success -> {
                    // We will now give view and write permissions to the user
                    success.upsertPermissionOverride(member)
                            .setAllowed(Permission.VIEW_CHANNEL)
                            .queue();

                    event.getHook()
                            .sendMessage("Successfully created a ticket! Your ticket ID is " + ticketId + ". Please wait for a staff member in <#" + success.getId() + "> to answer your question.")
                            .queue();

                    // Now send a message to admins and moderators regarding the new ticket
                    success.sendMessage("<@&750596106400038932> <@&901928638520369223> a new ticket was created by <@" + member.getId() + ">.").queue();
                }
            );
        }

        // Handle role buttons.
        if (buttonMappings.containsKey(passedId)) {
            String roleId = buttonMappings.get(passedId);

            // Determine role name
            Role currentRole = event.getGuild().getRoleById(roleId);
            if (currentRole == null) {
                Sentry.captureMessage("Unable to find role with ID " + roleId);
                return;
            }
            String roleName = currentRole.getName();

            Member member = event.getMember();
            if (member == null) {
                return;
            }

            // Remove the role if the user has it
            if (member.getRoles().contains(currentRole)) {
                event.getGuild().removeRoleFromMember(member, currentRole).queue();
                event.reply("Successfully removed the " + roleName + " role.").setEphemeral(true).queue();
            } else {
                event.getGuild().addRoleToMember(member, currentRole).queue();
                event.reply("Successfully added the " + roleName + " role.").setEphemeral(true).queue();
            }
        }
    }
}
