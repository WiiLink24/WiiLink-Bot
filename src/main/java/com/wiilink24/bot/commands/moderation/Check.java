package com.wiilink24.bot.commands.moderation;

import com.wiilink24.bot.Database;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.stream.Collectors;


public class Check {
    private final Database database;

    public Check() {
        this.database = new Database();
    }

    public void check(SlashCommandEvent event) throws SQLException {
        String muted = "**No**";
        Member member = event.getOptionsByName("member").get(0).getAsMember();
        User user;

        if (member == null) {
            user = event.getOptionsByName("member").get(0).getAsUser();
            muted = "**Not in Server**";
        } else {
            user = member.getUser();
            ArrayList<String> roleArray = member.getRoles().stream().map(Role::getId).collect(Collectors.toCollection(ArrayList::new));

            if (roleArray.contains("770836633419120650")) {
                muted = "**Yes**";
            }
        }

        int strikes = database.getStrikes(user.getId());
        String finalMuted = muted;

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
                            + strikes
                            + "**\n:mute: Muted: "
                            + finalMuted
                            + "\n:hammer: Banned: "
                            + banned
                    ).queue();
                }, failure -> {

                    event.reply(":white_check_mark: Moderation Information for **"
                            + user.getName()
                            + "**#"
                            + user.getDiscriminator()
                            + " (ID:"
                            + user.getId()
                            + "):\n"
                            + ":triangular_flag_on_post: Strikes: **"
                            + strikes
                            + "**\n:mute: Muted: "
                            + finalMuted
                            + "\n:hammer: Banned: **No**"
                    ).queue();
                }
        );
    }
}
