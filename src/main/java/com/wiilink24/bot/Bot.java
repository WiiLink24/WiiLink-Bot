package com.wiilink24.bot;

import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.wiilink24.bot.commands.misc.*;
import com.wiilink24.bot.commands.moderation.*;
import com.wiilink24.bot.commands.testing.UploadWad;
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
import org.apache.commons.dbcp2.BasicDataSource;

import javax.security.auth.login.LoginException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class Bot {
    static BasicDataSource connectionPool;
    Config config = new Config();

    void run() throws LoginException {
        // Create database pool
        connectionPool = new BasicDataSource();
        connectionPool.setDriverClassName("org.postgresql.Driver");
        String[] credentials = config.getDatabaseCreds();
        connectionPool.setUsername(credentials[0]);
        connectionPool.setPassword(credentials[1]);
        connectionPool.setUrl(credentials[2]);
        connectionPool.setInitialSize(3);

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
                        new AFK(),
                        new About(),
                        new Avatar(),
                        new Credits(),
                        new Digicard(),
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
                        new Check(this),
                        new Translate(this),
                        new Kick(this),
                        new Mute(this),
                        new Unban(),
                        new Strike(this),

                        /* Developer commands */
                        new UploadWad(),

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

    public String patchesChannel() {
        return "894316256788893706";
    }

    public static String developerRoleId() {
        return "750591972044963850";
    }

    public static String testerRoleId() {
        return "751858257307631697";
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

    public String deepl() {return config.getDeeplCreds();}
}
