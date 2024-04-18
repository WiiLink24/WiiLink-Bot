package com.wiilink24.bot;

import com.google.common.cache.Cache;
import com.wiilink24.bot.events.ButtonListener;
import com.wiilink24.bot.events.SelectionBoxListener;
import com.wiilink24.bot.events.SlashCommandListener;
import com.wiilink24.bot.utils.CodeType;
import com.wiilink24.bot.utils.SimpleCacheBuilder;
import io.sentry.Sentry;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import okhttp3.OkHttpClient;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.security.auth.login.LoginException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class Bot {
    static BasicDataSource connectionPool;
    static BasicDataSource dominosPool;

    static BasicDataSource mailPool;

    private final OkHttpClient httpClient = new OkHttpClient();

    private final Cache<Long, Map<CodeType, Map<String, String>>> codeCache = new SimpleCacheBuilder<>(3).build();
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

        mailPool = new BasicDataSource();
        mailPool.setDriverClassName("org.postgresql.Driver");
        mailPool.setUsername(mailDbUser());
        mailPool.setPassword(mailDbPass());
        mailPool.setUrl(mailDbUrl());
        mailPool.setInitialSize(3);

        // Start Sentry
        Sentry.init( sentryOptions -> {
                    sentryOptions.setDsn(config.getSentryDSN());
                    sentryOptions.setTracesSampleRate(1.0);
                }
        );

        JDA jda = JDABuilder.createLight(config.getToken())
                .setStatus(OnlineStatus.ONLINE)
                .setActivity(Activity.playing("Ordering Demae Dominos"))
                .enableCache(CacheFlag.ROLE_TAGS)
                .addEventListeners(new ButtonListener(), new SlashCommandListener(), new SelectionBoxListener())
                .build();
    }

    public static String patchesChannel() {
        return "1191513827104264352";
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

    public String mailDbUser() {
        return config.mailCreds[0];
    }

    public String mailDbPass() {
        return config.mailCreds[1];
    }

    public String mailDbUrl() {
        return config.mailCreds[2];
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

    public BasicDataSource getConnectionPool() {
        return connectionPool;
    }

    public Map<CodeType, Map<String, String>> getAllCodes(long user)
    {
        try
        {
            Database db = new Database();
            return codeCache.get(user, () -> db.getAllCodes(user));
        }
        catch(ExecutionException e)
        {
            return Collections.emptyMap();
        }
    }

    public Map<String, String> getCodesForType(CodeType type, long user)
    {
        try
        {
            Database db = new Database();
            Map<CodeType, Map<String, String>> allCodes = codeCache.get(user, () -> db.getAllCodes(user));
            return allCodes == null ? Collections.emptyMap() : (allCodes.get(type) == null ? Collections.emptyMap() : allCodes.get(type));
        }
        catch(ExecutionException e)
        {
            return Collections.emptyMap();
        }
    }

    public void updateCodeCache(CodeType type, long id, Map<String, String> map)
    {
        getAllCodes(id).put(type, map);
    }
}