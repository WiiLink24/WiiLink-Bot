package com.wiilink24.bot;

import io.github.cdimascio.dotenv.Dotenv;

public class Config {
    Dotenv dotenv = Dotenv.load();

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

    public String getSentryDSN() {
        return getString("SENTRY_DSN") == null ? "" : getString("SENTRY_DSN");
    }

    public String getDeeplCreds() {
        return getString("DEEPL_TOKEN");
    }

    public String getOwoCreds() {
        return getString("OWO_TOKEN");
    }

    public String getWadsDirectory() {
        return getString("WADS_DIR");
    }
}
