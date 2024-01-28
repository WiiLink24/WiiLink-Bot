package com.wiilink24.bot;

import javax.security.auth.login.LoginException;

/**
 * Bot entry point
 *
 * @author Sketch
 */

public class WiiLinkBot {
    private static Bot instance;

    public static void main(String[] args) throws LoginException {
        final String GREEN = "\033[1;32m";
        final String RESET = "\033[0m";

        System.out.println(GREEN + "Starting WiiLink Bot...");
        System.out.println("Started!" + RESET);

        instance = new Bot();
        instance.run();
    }

    public static Bot getInstance() {
        if (instance == null) throw new IllegalStateException("The bot is not initialized!");
        return instance;
    }
}