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

        // Change the IP and port if needed.
        String database = "jdbc:postgresql://localhost:5432/" + getString("DB_NAME");

        return new String[]{user, password, database};
    }

    public String getDeeplCreds() {
        return getString("DEEPL_TOKEN");
    }

    public String getWadsDirectory() {
        return getString("WADS_DIR");
    }
}
