package com.wiilink24.bot.commands.moderation;

import com.wiilink24.bot.Database;
import io.sentry.Sentry;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.sql.SQLException;

public class Ticket extends ListenerAdapter {
    private final Database database;

    public Ticket() {
        this.database = new Database();
    }

    public void onGuildMessageReceived(GuildMessageReceivedEvent event)  {
        String[] args = event.getMessage().getContentRaw().split(" ");
        if (args[0].equalsIgnoreCase("/ticket")) {
            if (!event.getMember().getPermissions().contains(Permission.BAN_MEMBERS)) {
                event.getChannel().sendMessage("You do not have permission to run this command!").queue();
            } else {
                if (args[1].equals("close")) {
                    try {
                        database.closeTicket(Integer.parseInt(args[2]));
                        event.getChannel().sendMessage("Successfully removed ticket.").queue();

                        // Now that it is gone from the database, remove the user's access.
                        // The mods/admins can do whatever they want with the channel afterwards
                        TextChannel ticketChannel = event.getGuild().getTextChannelsByName("ticket-" + args[2], false).get(0);
                        Member member = event.getGuild().retrieveMemberById(args[3]).complete();
                        ticketChannel.createPermissionOverride(member).setDeny(Permission.VIEW_CHANNEL).queue();
                    } catch (SQLException e) {
                        event.getChannel().sendMessage("Failed to remove ticket").queue();
                        Sentry.captureException(e);
                    }
                }
            }
        }
    }
}
