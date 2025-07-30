package com.wiilink24.bot;

import io.github.cdimascio.dotenv.Dotenv;

public class Config {
    Dotenv dotenv = Dotenv.load();

    public String discordToken;
    public String[] databaseCreds;
    public String[] mailCreds;
    public String sentryDSN;
    public String owoToken;
    public String wadsDir;
    public String mainServer;

    public Config() {
        discordToken = getToken();
        databaseCreds = getDatabaseCreds();
        mailCreds = getMailDatabaseCreds();
        sentryDSN = getSentryDSN();
        owoToken = getOwoCreds();
        wadsDir = getWadsDirectory();
        mainServer = getMainServer();
    }

    private String getString(String key) {
        return dotenv.get(key);
    }

    public String getToken() {
        return getString("DISCORD_TOKEN");
    }

    public String[] getDatabaseCreds() {
        String user = getString("DB_USER");
        String password = getString("DB_PASS");
        String port = getString("DB_PORT") == null ? "5432" : getString("DB_PORT");

        // Change the IP and port if needed.
        String database = "jdbc:postgresql://localhost:" + port + "/" + getString("DB_NAME");

        return new String[]{user, password, database};
    }

    public String[] getMailDatabaseCreds() {
        String user = getString("MAIL_DB_USER");
        String password = getString("MAIL_DB_PASS");
        String port = getString("MAIL_DB_PORT") == null ? "5432" : getString("MAIL_DB_PORT");

        // Change the IP and port if needed.
        String database = "jdbc:postgresql://localhost:" + port + "/" + getString("MAIL_DB_NAME");

        return new String[]{user, password, database};
    }

    public String getSentryDSN() {
        return getString("SENTRY_DSN") == null ? "" : getString("SENTRY_DSN");
    }

    public String getOwoCreds() {
        return getString("OWO_TOKEN");
    }

    public String getWadsDirectory() {
        return getString("WADS_DIR");
    }

    public String getMainServer() {
        return getString("MAIN_SERVER_ID");
    }
}
