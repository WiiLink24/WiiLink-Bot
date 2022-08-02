package com.wiilink24.bot;

import com.wiilink24.bot.commands.moderation.Ticket;
import com.wiilink24.bot.commands.testing.UploadWad;
import com.wiilink24.bot.events.ButtonListener;
import com.wiilink24.bot.events.SelectionBoxListener;
import com.wiilink24.bot.events.SlashCommandListener;
import io.sentry.Sentry;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import com.wiilink24.bot.events.Listener;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.security.auth.login.LoginException;
import java.io.*;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Bot {
    static BasicDataSource connectionPool;
    public static List<String> antiSwearWords = new ArrayList<>();
    Config config = new Config();

    void run() throws LoginException {
        // Create database pool
        connectionPool = new BasicDataSource();
        connectionPool.setDriverClassName("org.postgresql.Driver");
        connectionPool.setUsername(dbUser());
        connectionPool.setPassword(dbPass());
        connectionPool.setUrl(dbUrl());
        connectionPool.setInitialSize(3);

        // Start Sentry
        Sentry.init( sentryOptions -> {
                    sentryOptions.setDsn(config.getSentryDSN());
                    sentryOptions.setTracesSampleRate(1.0);
                }
        );

        // Load the Anti-Swear
        getAntiSwear();

        JDABuilder builder = JDABuilder.createLight(config.getToken())
                .setStatus(OnlineStatus.ONLINE)
                .setActivity(Activity.playing("Ordering Demae Dominos"))
                .enableCache(CacheFlag.VOICE_STATE, CacheFlag.ROLE_TAGS)
                .addEventListeners(new UploadWad(), new Ticket(), new Listener(), new ButtonListener(this), new SlashCommandListener(this), new SelectionBoxListener())
                .enableIntents(GatewayIntent.GUILD_MESSAGE_TYPING, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_VOICE_STATES);

        builder.build();
    }

    private static void getAntiSwear() {
        // Load the Anti-Swear words
        try {
            File file = new File("./censor.txt");
            BufferedReader reader = new BufferedReader(new FileReader(file));

            String st;
            while ((st = reader.readLine()) != null) {
                antiSwearWords.add(st);
            }
            System.out.println(antiSwearWords);
        } catch (IOException e) {
            // Should never occur in production
            Sentry.captureException(e);
        }
    }

    // We return a RestAction because depending on use we can complete or queue
    public static RestAction<Message> sendDM(User user, String message) {
        return user.openPrivateChannel()
                .flatMap(privateChannel -> privateChannel.sendMessage(message));
    }

    public static RestAction<Message> sendDM(User user, MessageEmbed embed) {
        return user.openPrivateChannel()
                .flatMap(privateChannel -> privateChannel.sendMessageEmbeds(embed));
    }

    public static String modLog() {
        return "755522585864962099";
    }

    public static String introductions() {
        return "1004162613904674946";
    }

    public static String serverLog() {
        return "913181713977458718";
    }

    public static String patchesChannel() {
        return "894316256788893706";
    }

    public static String developerRoleId() {
        return "750591972044963850";
    }

    public static String wiiLinkServerId() {
        return "750581992223146074";
    }

    public static String mutedRoleId() {
        return "770836633419120650";
    }

    public static String timestamp() {
        return DateTimeFormatter.ofPattern("'`['HH:mm:ss']`'").withZone(ZoneOffset.UTC).format(Instant.now());
    }

    public String dbUser() {
        return config.getDatabaseCreds()[0];
    }

    public String dbPass() {
        return config.getDatabaseCreds()[1];
    }

    public String dbUrl() {
        return config.getDatabaseCreds()[2];
    }

    public String deepl() {
        return config.getDeeplCreds();
    }

    public String wadPath() {
        return config.getWadsDirectory();
    }

    public String owoToken() {
        return config.getOwoCreds();
    }
}