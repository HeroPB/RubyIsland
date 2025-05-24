package me.herohd.rubyisland.manager;

import me.herohd.rubyisland.RubyIsland;
import me.herohd.rubyisland.objects.Island;
import me.herohd.rubyisland.utils.Config;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.sql.*;
import java.util.*;

public class MySQLManager {
    private final String url;
    private final String user;
    private final String password;
    private Connection connection;

    public MySQLManager(Config config) {

        String host = config.getString("mysql.host");
        String port = config.getString("mysql.port");
        String database = config.getString("mysql.database");
        user = config.getString("mysql.user");
        password = config.getString("mysql.password");

        this.url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&autoReconnect=true";
        connect();
        createTables();
    }

    /**
     * Connessione al database MySQL
     */
    private void connect() {
        try {
            if (connection != null && !connection.isClosed()) return;
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("[MySQL] Connessione stabilita.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createTables() {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                "CREATE TABLE IF NOT EXISTS `islands` (" +
                        "  `id` int(11) NOT NULL AUTO_INCREMENT," +
                        "  `spawn_x` float NOT NULL DEFAULT '0'," +
                        "  `spawn_y` float NOT NULL DEFAULT '0'," +
                        "  `spawn_z` float NOT NULL DEFAULT '0'," +
                        "  `spawn_yaw` float NOT NULL DEFAULT '0'," +
                        "  `spawn_pitch` float NOT NULL DEFAULT '0'," +
                        "  `uuid` varchar(255) NOT NULL," +
                        "PRIMARY KEY (id)" +
                        ")"
        )) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getLogger().info("Qualcosa è andato storto " + e);
        }
    }

    public Island getIslandIdByUUID(String playerUUID) {
        String islandQuery = "SELECT * FROM islands WHERE uuid = ?";
        String playersQuery = "SELECT players_uuid, type FROM players WHERE island_id = ?";

        try (PreparedStatement islandStmt = getConnection().prepareStatement(islandQuery)) {
            islandStmt.setString(1, playerUUID);

            try (ResultSet islandRs = islandStmt.executeQuery()) {
                if (islandRs.next()) {
                    int islandId = islandRs.getInt("id");
                    boolean closed = islandRs.getBoolean("closed");
                    String ownerName = Bukkit.getOfflinePlayer(UUID.fromString(islandRs.getString("uuid"))).getName();
                    Location spawn = new Location(
                            Bukkit.getWorld(RubyIsland.getInstance().getConfigYML().getString("general.world-name")),
                            islandRs.getFloat("rs"),
                            islandRs.getFloat("spawn_y"),
                            islandRs.getFloat("spawn_z"),
                            islandRs.getFloat("spawn_yaw"),
                            islandRs.getFloat("spawn_pitch")
                    );

                    Map<String, String> playersMap = new HashMap<>();

                    try (PreparedStatement playersStmt = getConnection().prepareStatement(playersQuery)) {
                        playersStmt.setInt(1, islandId);

                        try (ResultSet playersRs = playersStmt.executeQuery()) {
                            while (playersRs.next()) {
                                String uuid = playersRs.getString("players_uuid");
                                String type = playersRs.getString("type");
                                playersMap.put(uuid, type);
                            }
                        }
                    }

                    return new Island(islandId, ownerName, spawn, playersMap, closed);
                }
            }
        } catch (SQLException e) {
            Bukkit.getLogger().warning("Errore nel recuperare l'isola per UUID: " + e.getMessage());
        }

        return null;
    }

    public int addIsland(String uuid) {
        try (PreparedStatement stmt = getConnection().prepareStatement(
                "INSERT INTO islands(uuid) VALUES(?);",
                Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, uuid);
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating island failed, no ID obtained.");
                }
            }

        } catch (SQLException e) {
            Bukkit.getLogger().info("Something went wrong. " + e);
        }

        return -1;
    }


    public void save(Island island) {

        try (PreparedStatement stmt = getConnection().prepareStatement(
                "UPDATE islands SET spawn_x = ?, spawn_y = ?, spawn_z = ?, spawn_yaw = ?, spawn_pitch = ? WHERE id = ?;"
        )) {
            stmt.setFloat(1, island.getSpawn().getBlockX());
            stmt.setFloat(2, island.getSpawn().getBlockY());
            stmt.setFloat(3, island.getSpawn().getBlockZ());
            stmt.setFloat(4, island.getSpawn().getYaw());
            stmt.setFloat(5, island.getSpawn().getPitch());
            stmt.setInt(6, island.getId());
            stmt.executeUpdate();

        } catch (SQLException e) {
            Bukkit.getLogger().info("Something went wrong. " + e);
        }

    }
    public void closeIsland(Island island) {

        try (PreparedStatement stmt = getConnection().prepareStatement(
                "UPDATE islands SET closed = ? WHERE id = ?;"
        )) {
            stmt.setBoolean(1, island.isClosed());
            stmt.setInt(2, island.getId());
            stmt.executeUpdate();

        } catch (SQLException e) {
            Bukkit.getLogger().info("Something went wrong. " + e);
        }

    }

    public Connection getConnection() {
        connect();
        return connection;
    }

    public void addOrUpdatePlayer(int islandId, String playerUUID, String type) {
        String query = "INSERT INTO players (island_id, players_uuid, type) " +
                "VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE type = VALUES(type)";

        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setInt(1, islandId);
            stmt.setString(2, playerUUID);
            stmt.setString(3, type.toUpperCase()); // deve essere uno tra 'ADD', 'TRUST', 'BAN'
            stmt.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getLogger().warning("Errore durante l'inserimento/aggiornamento del player: " + e.getMessage());
        }
    }

    public void removePlayer(int islandId, String playerUUID) {
        String query = "DELETE FROM players WHERE island_id = ? AND players_uuid = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setInt(1, islandId);
            stmt.setString(2, playerUUID);
            stmt.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getLogger().warning("Errore durante la rimozione del player dalla tabella: " + e.getMessage());
        }
    }
}
