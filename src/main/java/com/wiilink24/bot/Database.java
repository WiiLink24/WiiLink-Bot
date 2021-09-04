package com.wiilink24.bot;

import java.sql.*;

// For common Database functions
public class Database {
    public boolean doesExist(Connection con, String userID) throws SQLException {
        PreparedStatement query = con.prepareStatement("SELECT userid FROM userinfo WHERE userid = ?", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
        query.setObject(1, userID);

        return query.executeQuery().first();
    }

    public void updateAFK(Connection con, boolean isAFK, String reason, String userID) throws SQLException {
        PreparedStatement pst = con.prepareStatement("UPDATE userinfo SET afk = ?, afk_reason = ? WHERE userid = ?");

        pst.setBoolean(1, isAFK);
        pst.setString(2, reason);
        pst.setObject(3, userID);
        pst.executeUpdate();
    }

    public void createUser(Connection con, String userID) throws SQLException {
        PreparedStatement pst = con.prepareStatement("INSERT INTO userinfo (userid) VALUES (?)");

        pst.setString(1, userID);
        pst.executeUpdate();
    }

    public ResultSet fullQuery(Connection con, String userID) throws SQLException {
        PreparedStatement query = con.prepareStatement("SELECT * FROM userinfo WHERE userid = ?", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
        query.setObject(1, userID);

        return query.executeQuery();
    }
}
