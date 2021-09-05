package com.wiilink24.bot;

import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.wiilink24.bot.commands.misc.*;
import com.wiilink24.bot.commands.moderation.*;
import io.sentry.Sentry;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import com.wiilink24.bot.commands.owner.Bash;
import com.wiilink24.bot.commands.owner.Eval;
import com.wiilink24.bot.events.Listener;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.RestAction;

import javax.security.auth.login.LoginException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class Bot {
    Config config = new Config();

    void run() throws LoginException {
        // Start Sentry
        Sentry.init( sentryOptions -> {
                sentryOptions.setDsn("");
                sentryOptions.setTracesSampleRate(1.0);
            }
        );
        
        CommandClientBuilder client = new CommandClientBuilder()
                .setPrefix("/")
                .setOwnerId("829487301422743613")
                .addCommands(
                        /* Misc Commands */
                        new AFK(this),
                        new About(),
                        new Avatar(),
                        new Credits(),
                        new GameTDB(),
                        new Mii(),
                        new RiiTag(),
                        new Userinfo(),
                        new ServerInfo(),
                        new OCR(),
                        new Ping(),

                        /* Moderation Commands */
                        new Clear(this),
                        new Ban(),
                        new Kick(this),
                        new Mute(this),
                        new Unban(),
                        new Strike(this),

                        /* Owner Only Commands */
                        new Bash(),
                        new Eval(this)
                );

        JDABuilder builder = JDABuilder.createLight(config.getToken())
                .setStatus(OnlineStatus.ONLINE)
                .setActivity(Activity.playing("Ordering Demae Dominos"))
                .addEventListeners(client.build(), new Listener(this))
                .enableIntents(GatewayIntent.GUILD_MESSAGE_TYPING, GatewayIntent.GUILD_MEMBERS);

        builder.build();
    }

    public RestAction sendDM(User user, String message) {
        return user.openPrivateChannel()
                .flatMap(privateChannel -> privateChannel.sendMessage(message));
    }

    public String modLog() {
        return "755522585864962099";
    }

    public String timestamp() {
        return DateTimeFormatter.ofPattern("'`['HH:mm:ss']`'").withZone(ZoneOffset.UTC).format(Instant.now());
    }

    public String dbUser() {
        return config.getDatabaseCreds()[0];
    }

    public String dbPass() {
        return config.getDatabaseCreds()[1];
    }

    public String db() {
        return config.getDatabaseCreds()[2];
    }
}
