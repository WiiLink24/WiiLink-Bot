package com.wiilink24.bot;

import com.google.gson.Gson;
import com.wiilink24.bot.utils.CodeType;

import java.sql.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

// For common Database functions
public class Database {
    private final Gson gson = new Gson();

    public void deleteWiiNumber(String num) throws SQLException {
        try (Connection con = Bot.mailPool.getConnection()) {
            PreparedStatement pst = con.prepareStatement("""
                            DELETE FROM accounts WHERE mlid = ?
                            """);

            pst.setString(1, num);
            pst.executeUpdate();
        }
    }

    /**
     * Inserts a WAD into the database.
     *
     * @param filename The filename of the WAD on disk, usable once retrieved later.
     * @param title The title to use when referring to this WAD, such as "Beta v13".
     * @return The ID of the inserted WAD, usable for interaction callbacks.
     * @throws SQLException Should the execution fail.
     */
    public int insertWad(String filename, String title) throws SQLException {
        try (Connection con = Bot.connectionPool.getConnection()) {
            PreparedStatement pst = con.prepareStatement("INSERT INTO wads (filename, readable_name) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
            pst.setString(1, filename);
            pst.setString(2, title);
            pst.executeUpdate();

            ResultSet keys = pst.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt(1);
            } else {
                return 0;
            }
        }
    }

    /**
     * Returns the stored filename for a given WAD ID.
     *
     * @param patchId The WAD ID to look up.
     * @return The filename registered for the WAD.
     * @throws SQLException Should the execution fail.
     */
    public String getWadFilename(int patchId) throws SQLException {
        try (Connection con = Bot.connectionPool.getConnection()) {
            PreparedStatement query = con.prepareStatement("SELECT filename FROM wads WHERE file_id = ?", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            query.setInt(1, patchId);

            ResultSet set = query.executeQuery();
            if (set.next()) {
                return set.getString(1);
            } else {
                return "";
            }
        }
    }

    /**
     * Gets a cached WAD url for the given WAD and user ID.
     *
     * @param patchId The WAD ID to look up.
     * @param userId The user ID to look up.
     * @return The cached WAD URL, or "" if none.
     * @throws SQLException Should the execution fail.
     */
    public String getWadUrl(int patchId, String userId) throws SQLException {
        try (Connection con = Bot.connectionPool.getConnection()) {
            PreparedStatement query = con.prepareStatement("SELECT wad_url FROM wad_urls WHERE wad_id = ? AND user_id = ?", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            query.setInt(1, patchId);
            query.setString(2, userId);

            ResultSet set = query.executeQuery();
            if (set.next()) {
                return set.getString(1);
            } else {
                return "";
            }
        }
    }

    /**
     * Sets the WAD URL for the given pair of IDs for caching.
     *
     * @param patchId The WAD ID to use.
     * @param userId The user ID to associate against.
     * @param uploadedUrl The URL to cache.
     * @throws SQLException Should the execution fail.
     */
    public void setWadUrl(int patchId, String userId, String uploadedUrl) throws SQLException {
        try (Connection con = Bot.connectionPool.getConnection()) {
            PreparedStatement pst = con.prepareStatement("INSERT INTO wad_urls (wad_id, user_id, wad_url) VALUES (?, ?, ?)");

            pst.setInt(1, patchId);
            pst.setString(2, userId);
            pst.setString(3, uploadedUrl);
            pst.executeUpdate();
        }
    }

    public Map<CodeType, Map<String, String>> getAllCodes(long user) throws SQLException {
        try (Connection con = Bot.connectionPool.getConnection()) {
            PreparedStatement query = con.prepareStatement("SELECT * FROM codes WHERE user_id = ?", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            query.setLong(1, user);

            ResultSet row = query.executeQuery();
            Map<CodeType, Map<String, String>> map = new HashMap<>();
            while (row.next()) {
                for(CodeType type : CodeType.values())
                {
                    if(type == null)
                        continue;

                    Map<String, String> typeMap = gson.fromJson(row.getString(type.getColumn()), Map.class);
                    if(typeMap == null)
                        typeMap = new HashMap<>();

                    map.put(type, typeMap);
                }
            }

            return map;
        }
    }

    public Map<String, String> getCodesForType(CodeType type, long user) throws SQLException {
        try (Connection con = Bot.connectionPool.getConnection()) {
            PreparedStatement query = con.prepareStatement("SELECT * FROM codes WHERE user_id = ?", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            query.setLong(1, user);

            ResultSet row = query.executeQuery();
            if (row.next()) {
                return gson.fromJson(row.getString(type.getColumn()), Map.class);
            }

            return Collections.emptyMap();
        }
    }

    public void addCode(CodeType type, long id, String code, String name) throws SQLException {
        try (Connection con = Bot.connectionPool.getConnection()) {
            PreparedStatement pst = con.prepareStatement("INSERT INTO codes (user_id, " + type.getColumn() + ") " +
                    "VALUES(?, ?) ON CONFLICT(user_id) DO UPDATE SET " + type.getColumn() + " = ?");

            String json = gson.toJson(updateCodeCache(type, id, code, name));

            pst.setLong(1, id);
            pst.setString(2, json);
            pst.setString(3, json);
            pst.executeUpdate();
        }
    }

    private Map<String, String> updateCodeCache(CodeType type, long id, String code, String name) {
        Map<String, String> currentCodes = WiiLinkBot.getInstance().getCodesForType(type, id);
        if(currentCodes == null || currentCodes.getClass().getSimpleName().equals("EmptyMap"))
            currentCodes = new HashMap<>();

        currentCodes.put(name, code);
        WiiLinkBot.getInstance().updateCodeCache(type, id, currentCodes);

        return currentCodes;
    }

    public void editCode(CodeType type, long id, String code, String name) throws SQLException {
        try (Connection con = Bot.connectionPool.getConnection()) {
            PreparedStatement pst = con.prepareStatement("UPDATE codes SET " + type.getColumn() + " = ? " +
                    "WHERE user_id = ?");

            pst.setString(1, gson.toJson(updateCodeCache(type, id, code, name)));
            pst.setLong(2, id);
            pst.executeUpdate();
        }
    }

    public void removeCode(CodeType type, long id, String name) throws SQLException {
        Map<String, String> map = removeFromCodeCache(type, id, name);
        if(map == null)
            return;

        try (Connection con = Bot.connectionPool.getConnection()) {
            PreparedStatement pst = con.prepareStatement("UPDATE codes SET " + type.getColumn() + " = ? " +
                    "WHERE user_id = ?");

            pst.setString(1, gson.toJson(map));
            pst.setLong(2, id);
            pst.executeUpdate();
        }
    }

    private Map<String, String> removeFromCodeCache(CodeType type, long id, String name)
    {
        Map<String, String> currentCodes = WiiLinkBot.getInstance().getCodesForType(type, id);
        if(currentCodes == null || currentCodes.getClass().getSimpleName().equals("EmptyMap"))
            return null;

        currentCodes.remove(name);
        WiiLinkBot.getInstance().updateCodeCache(type, id, currentCodes);

        return currentCodes;
    }
}
