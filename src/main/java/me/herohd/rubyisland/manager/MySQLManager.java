package me.herohd.rubyisland.manager;

import me.herohd.rubyisland.RubyIsland;
import me.herohd.rubyisland.objects.Island;
import me.herohd.rubyisland.utils.Config;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.sql.*;
import java.util.UUID;

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
        String query = "SELECT * FROM islands WHERE uuid = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {

            stmt.setString(1, playerUUID);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Island(
                            rs.getInt("id"),
                            Bukkit.getOfflinePlayer(UUID.fromString(rs.getString("uuid"))).getName(),
                            new Location(Bukkit.getWorld(RubyIsland.getInstance().getConfigYML().getString("general.world-name")), rs.getFloat("rs"), rs.getFloat("spawn_y"), rs.getFloat("spawn_z"), rs.getFloat("spawn_yaw"), rs.getFloat("spawn_pitch")));

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

    public Connection getConnection() {
        connect();
        return connection;
    }
}
