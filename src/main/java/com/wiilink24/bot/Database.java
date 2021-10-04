package com.wiilink24.bot;

import java.sql.*;

// For common Database functions
public class Database {
    public boolean doesExist(String userID) throws SQLException {
        try (Connection con = Bot.connectionPool.getConnection()) {
            PreparedStatement query = con.prepareStatement("SELECT userid FROM userinfo WHERE userid = ?", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            query.setObject(1, userID);

            return query.executeQuery().first();
        }
    }

    public void updateAFK(boolean isAFK, String reason, String userID) throws SQLException {
        try (Connection con = Bot.connectionPool.getConnection()) {
            PreparedStatement pst = con.prepareStatement("UPDATE userinfo SET afk = ?, afk_reason = ? WHERE userid = ?");

            pst.setBoolean(1, isAFK);
            pst.setString(2, reason);
            pst.setObject(3, userID);
            pst.executeUpdate();
        }
    }

    public void createUser(String userID) throws SQLException {
        try (Connection con = Bot.connectionPool.getConnection()) {
            PreparedStatement pst = con.prepareStatement("INSERT INTO userinfo (userid) VALUES (?)");

            pst.setString(1, userID);
            pst.executeUpdate();
        }
    }

    public ResultSet fullQuery(String userID) throws SQLException {
        try (Connection con = Bot.connectionPool.getConnection()) {
            PreparedStatement query = con.prepareStatement("SELECT * FROM userinfo WHERE userid = ?", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            query.setObject(1, userID);

            return query.executeQuery();
        }
    }

    public void updateStrike(String userID, Integer strikes) throws SQLException {
        try (Connection con = Bot.connectionPool.getConnection()) {
            PreparedStatement pst = con.prepareStatement("UPDATE userinfo SET strikes = ? WHERE userid = ?");

            pst.setInt(1, strikes);
            pst.setObject(2, userID);
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
}
