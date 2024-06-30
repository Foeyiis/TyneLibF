package org.tynes.database;

import cn.nukkit.Player;
import org.tynes.TyneLibF;
import org.tynes.profiles.ProfileMode;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.sql.*;

public class PlayerDatabase {

    private final Connection connection;
    private final Connection profilesConnection;

    public PlayerDatabase(String path, String profileConnectionPath) throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Failed to load SQLite JDBC class", e);
        }
        connection = DriverManager.getConnection("jdbc:sqlite:" + path);
        profilesConnection = DriverManager.getConnection("jdbc:sqlite:" + profileConnectionPath);
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS players (" +
                    "uuid TEXT PRIMARY KEY, " +
                    "username TEXT NOT NULL, " +
                    "reputations INTEGER NOT NULL DEFAULT 100," +
                    "profileid TEXT DEFAULT NULL," +
                    "profilesnumber INT DEFAULT 0)");
        }
        try (Statement statement = profilesConnection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS profiles (" +
                    "profileuuid TEXT PRIMARY KEY, " +
                    "owner TEXT NOT NULL," +
                    "profilename TEXT NOT NULL," +
                    "inventorydata TEXT DEFAULT NULL," +
                    "explevel INTEGER DEFAULT 0)");
        }
        TyneLibF.getInstance().getLogger().info("§aAll SQLite Database Connected Properly.");
    }

    public void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
        if (profilesConnection != null && !profilesConnection.isClosed()) {
            profilesConnection.close();
        }
    }

    public void addPlayer(Player player) throws SQLException {
        //this should error if the player already exists
        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO players (uuid, username) VALUES (?, ?)")) {
            preparedStatement.setString(1, player.getUniqueId().toString());
            preparedStatement.setString(2, player.getName());
            preparedStatement.executeUpdate();
        }
    }

    public boolean playerExists(Player player) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM players WHERE uuid = ?")) {
            preparedStatement.setString(1, player.getUniqueId().toString());
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        }
    }

//    Reputations System

    public void updateReputations(Player p, int i) throws SQLException {

        if (!playerExists(p)) {
            addPlayer(p);
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE players SET reputations = ? WHERE uuid = ?")) {
            preparedStatement.setInt(1, i);
            preparedStatement.setString(2, p.getUniqueId().toString());
            preparedStatement.executeUpdate();
        }
    }

    public int getReputations(Player p) throws SQLException {

        if (!playerExists(p)) {
            addPlayer(p);
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT reputations FROM players WHERE uuid = ?")) {
            preparedStatement.setString(1, p.getUniqueId().toString());
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("reputations");
            } else {
                return 0;
            }
        }
    }

    public void addReputations(Player p, int i) throws SQLException {
        updateReputations(p, getReputations(p) + i);
    }

    public void removeReputations(Player p, int i) throws SQLException {
        updateReputations(p, getReputations(p) - i);
    }

    public void resetReputation(Player p) throws SQLException {
        updateReputations(p, 0);
    }

//    Player Profiles

    /*
     *
     * [0] = Player UUID
     * [1] = Player Profile
     * [2] = Profile Mode
     *
     */
    public String[] decryptProfileUUID(String uuid) {
        return uuid.split("/");
    }

    public boolean profileUUIDExists(String uuid) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM players WHERE uuid = ?")) {
            preparedStatement.setString(1, uuid);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        }
    }

    public void createProfile(Player player, int id, ProfileMode profileMode) throws SQLException, KeyAlreadyExistsException {
        String uuid = player.getUniqueId().toString() + "/Profile-" + id + "/" + profileMode.toString().toLowerCase();
        if (profileUUIDExists(uuid))
            throw new KeyAlreadyExistsException("Profile UUID \"" + uuid + "\" is exist, Remove the Profile first before create the new one");
        try (PreparedStatement preparedStatement = profilesConnection.prepareStatement("INSERT INTO profiles (profileuuid, owner) VALUES (?, ?)")) {
            preparedStatement.setString(1, player.getUniqueId().toString() + "/Profile-" + id + "/" + profileMode.toString().toLowerCase());
            preparedStatement.setString(2, player.getName());
            preparedStatement.executeUpdate();
        }
    }

    public void removeProfile(Player player, int id, ProfileMode profileMode) throws SQLException, NullPointerException {
        String uuid = player.getUniqueId().toString() + "/Profile-" + id + "/" + profileMode.toString().toLowerCase();
        if (!profileUUIDExists(uuid))
            throw new NullPointerException("Profile UUID \"" + uuid + "\" does not exist");
        try (PreparedStatement preparedStatement = profilesConnection.prepareStatement("DELETE FROM profiles WHERE profileuuid = ?")) {
            preparedStatement.setString(1, uuid);
            preparedStatement.executeUpdate();
        }
    }

    public String getProfileUUID(Player player) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT profileid FROM players WHERE uuid = ?")) {
            preparedStatement.setString(1, player.getUniqueId().toString());
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("profileid");
            }
            return null;
        }
    }

    public void switchProfile(Player player, String id) throws SQLException, NullPointerException {
        if (!profileUUIDExists(id)) {
            throw new NullPointerException("No such profile uuid");
        }
        try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE players SET profileid = ? WHERE uuid = ?")) {
            preparedStatement.setString(1, id);
            preparedStatement.setString(2, player.getUniqueId().toString());
            preparedStatement.executeUpdate();
        }
    }

    public void updateProfilesNumber(Player p, int number) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE players SET profilesnumber = ? WHERE uuid = ?")) {
            preparedStatement.setInt(1, number);
            preparedStatement.setString(2, p.getUniqueId().toString());
            preparedStatement.executeUpdate();
        }
    }

    public int getProfilesNumber(Player p) throws SQLException {

        if (!playerExists(p)) {
            addPlayer(p);
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT profilesnumber FROM players WHERE uuid = ?")) {
            preparedStatement.setString(1, p.getUniqueId().toString());
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("profilesnumber");
            } else {
                return 0;
            }
        }
    }

}
