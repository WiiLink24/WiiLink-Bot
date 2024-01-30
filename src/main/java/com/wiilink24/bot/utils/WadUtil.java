package com.wiilink24.bot.utils;

import com.wiilink24.bot.WiiLinkBot;
import me.bramhaag.owo.OwO;

import java.nio.file.Files;
import java.nio.file.Paths;

public class WadUtil {
    private final OwO owo;

    public WadUtil() {
        this.owo = new OwO.Builder()
                .setKey(WiiLinkBot.getInstance().owoToken())
                .setUploadUrl("https://wiilink.is-pretty.cool/")
                .build();
    }

    public String uploadWad(String wadFilename, String userId) throws Throwable {
        byte[] wadData = Files.readAllBytes(Paths.get(WiiLinkBot.getInstance().wadPath(), wadFilename));
        int written = 0;
        int position = wadData.length - 64;

        for (char c: userId.toCharArray()) {
            wadData[position] = (byte)c;
            written++;
            position++;
        }

        for (int i = 64 - written; i >= 64; i++) {
            wadData[position] = 0;
            position++;
        }

        return owo.uploadAssociated(wadData, "dummy.wad", "application/x-wii-wad")
                .executeSync()
                .getFullUrl();
    }
}
