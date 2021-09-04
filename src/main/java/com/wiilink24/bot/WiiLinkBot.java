package com.wiilink24.bot;

import javax.security.auth.login.LoginException;

/**
 * Bot entry point
 *
 * @author Sketch
 */

public class WiiLinkBot {
    public static void main(String[] args) throws LoginException {
        System.out.println("Starting WiiLink Bot");

        new Bot().run();
    }
}