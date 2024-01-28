package com.wiilink24.bot;

import com.wiilink24.bot.commands.testing.UploadWad;
import com.wiilink24.bot.events.ButtonListener;
import com.wiilink24.bot.events.SelectionBoxListener;
import com.wiilink24.bot.events.SlashCommandListener;
import io.sentry.Sentry;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import okhttp3.OkHttpClient;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.security.auth.login.LoginException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class Bot {
    static BasicDataSource connectionPool;
    static BasicDataSource dominosPool;
    private final OkHttpClient httpClient = new OkHttpClient();
    Config config = new Config();

    void run() throws LoginException {
        // Create database pool
        connectionPool = new BasicDataSource();
        connectionPool.setDriverClassName("org.postgresql.Driver");
        connectionPool.setUsername(dbUser());
        connectionPool.setPassword(dbPass());
        connectionPool.setUrl(dbUrl());
        connectionPool.setInitialSize(3);

        dominosPool = new BasicDataSource();
        dominosPool.setDriverClassName("org.postgresql.Driver");
        dominosPool.setUsername(dominosDbUser());
        dominosPool.setPassword(dominosDbPass());
        dominosPool.setUrl(dominosDbUrl());
        dominosPool.setInitialSize(3);

        // Start Sentry
        Sentry.init( sentryOptions -> {
                    sentryOptions.setDsn(config.getSentryDSN());
                    sentryOptions.setTracesSampleRate(1.0);
                }
        );

        JDABuilder builder = JDABuilder.createLight(config.getToken())
                .setStatus(OnlineStatus.ONLINE)
                .setActivity(Activity.playing("Ordering Demae Dominos"))
                .enableCache(CacheFlag.ROLE_TAGS)
                .addEventListeners(new UploadWad(), new ButtonListener(this), new SlashCommandListener(this), new SelectionBoxListener());

        builder.build();
    }

    // We return a RestAction because depending on use we can complete or queue
    public static RestAction<Message> sendDM(User user, String message) {
        return user.openPrivateChannel()
                .flatMap(privateChannel -> privateChannel.sendMessage(message));
    }

    public String modLog() {
        return config.modLog;
    }

    public String serverLog() {
        return config.serverLog;
    }

    public static String patchesChannel() {
        return "894316256788893706";
    }

    public static String developerRoleId() {
        return "750591972044963850";
    }

    public String timestamp() {
        return DateTimeFormatter.ofPattern("'`['HH:mm:ss']`'").withZone(ZoneOffset.UTC).format(Instant.now());
    }

    public String dbUser() {
        return config.databaseCreds[0];
    }

    public String dbPass() {
        return config.databaseCreds[1];
    }

    public String dbUrl() {
        return config.databaseCreds[2];
    }

    public String dominosDbUser() {
        return config.dominosCreds[0];
    }

    public String dominosDbPass() {
        return config.dominosCreds[1];
    }

    public String dominosDbUrl() {
        return config.dominosCreds[2];
    }

    public String wadPath() {
        return config.wadsDir;
    }

    public String owoToken() {
        return config.owoToken;
    }

    public String mainServerId() {
        return config.mainServer;
    }

    public OkHttpClient getHttpClient() {
        return this.httpClient;
    }
}