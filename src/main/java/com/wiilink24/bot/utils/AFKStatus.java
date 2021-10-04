package com.wiilink24.bot.utils;

public class AFKStatus {
    public AFKStatus(boolean isAFK, String reason) {
        this.isAFK = isAFK;
        this.reason = reason;
    }

    public boolean isAFK;
    public String reason;
}
