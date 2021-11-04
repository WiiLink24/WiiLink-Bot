package com.wiilink24.bot.commands.moderation;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.wiilink24.bot.Database;
import com.wiilink24.bot.commands.Categories;
import io.sentry.Sentry;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import java.sql.SQLException;

public class Ticket extends Command {
    private final Database database;

    public Ticket() {
        this.database = new Database();
        this.name = "ticket";
        this.category = Categories.MODERATION;
        this.userPermissions = new Permission[]{Permission.BAN_MEMBERS};
    }

    @Override
    protected void execute(CommandEvent event) {
        String[] args = event.getArgs().split("\\s", 3);
        if (args[0].equals("close")) {
            try {
                database.closeTicket(Integer.parseInt(args[1]));
                event.reply("Successfully removed ticket.");

                // Now that it is gone from the database, remove the user's access.
                // The mods/admins can do whatever they want with the channel afterwards
                TextChannel ticketChannel = event.getGuild().getTextChannelsByName("ticket-" + args[1], false).get(0);
                Member member = event.getGuild().retrieveMemberById(args[2]).complete();
                ticketChannel.createPermissionOverride(member).setDeny(Permission.VIEW_CHANNEL).queue();
            } catch (SQLException e) {
                event.replyError("Failed to remove ticket");
                Sentry.captureException(e);
            }
        }
    }
}
