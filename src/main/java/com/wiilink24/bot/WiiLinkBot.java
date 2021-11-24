package com.wiilink24.bot;

import javax.security.auth.login.LoginException;

/**
 * Bot entry point
 *
 * @author Sketch
 */

public class WiiLinkBot {
    public static void main(String[] args) throws LoginException {
        final String GREEN = "\033[1;32m";
        final String RESET = "\033[0m";

        System.out.println(GREEN + "Starting WiiLink Bot...");
        System.out.println("Started!" + RESET);

        new Bot().run();
    }
}