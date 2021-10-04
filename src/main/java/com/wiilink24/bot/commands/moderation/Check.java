package com.wiilink24.bot.commands.moderation;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.wiilink24.bot.Bot;
import com.wiilink24.bot.Database;
import com.wiilink24.bot.commands.Categories;
import com.wiilink24.bot.utils.SearcherUtil;
import io.sentry.Sentry;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class Check extends Command {
    private final Database database;

    public Check(Bot bot) {
        this.database = new Database();
        this.name = "check";
        this.category = Categories.MODERATION;
        this.userPermissions = new Permission[]{Permission.BAN_MEMBERS};
    }

    @Override
    protected void execute(CommandEvent event) {
        event.async(() -> {
            try {
                String muted = "**No**";
                Integer strikes = 0;
                Member member = SearcherUtil.findMember(event, event.getArgs());
                User user;

                if (member == null) {
                    // The requested user is not in the server
                    user = event.getJDA().retrieveUserById(event.getArgs()).complete();
                    muted = "**Not in Server**";
                } else {
                    user = member.getUser();
                    ArrayList<String> roleArray = member.getRoles().stream().map(Role::getId).collect(Collectors.toCollection(ArrayList::new));

                    if (roleArray.contains("770836633419120650")) {
                        muted = "**Yes**";
                    }
                }

                ResultSet result = database.fullQuery(user.getId());

                if (result.first()) {
                    strikes = result.getInt(2);
                }

                String finalMuted = muted;
                Integer finalStrikes = strikes;
                event.getGuild().retrieveBan(user).queue(
                            success -> {
                                String banned = "**Yes** (`" + success.getReason() + "`)";

                                event.reply(":white_check_mark: Moderation Information for **"
                                        + user.getName()
                                        + "**#"
                                        + user.getDiscriminator()
                                        + " (ID:"
                                        + user.getId()
                                        + "):\n"
                                        + ":triangular_flag_on_post: Strikes: **"
                                        + finalStrikes
                                        + "**\n:mute: Muted: "
                                        + finalMuted
                                        + "\n:hammer: Banned: "
                                        + banned
                                );
                            }, failure -> {

                                event.reply(":white_check_mark: Moderation Information for **"
                                        + user.getName()
                                        + "**#"
                                        + user.getDiscriminator()
                                        + " (ID:"
                                        + user.getId()
                                        + "):\n"
                                        + ":triangular_flag_on_post: Strikes: **"
                                        + finalStrikes
                                        + "**\n:mute: Muted: "
                                        + finalMuted
                                        + "\n:hammer: Banned: **No**"
                                );
                            }
                    );
            } catch (SQLException e) {
                Sentry.captureException(e);
            }
        });
    }
}
