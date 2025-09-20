package me.herohd.rubyisland.manager;

import me.herohd.rubyisland.RubyIsland;
import me.herohd.rubyisland.collectors.CollectorManager;
import me.herohd.rubyisland.collectors.CollectorType;
import me.herohd.rubyisland.collectors.PlacedCollector;
import me.herohd.rubyisland.objects.Island;
import me.herohd.rubyisland.plants.PlacedPlant;
import me.herohd.rubyisland.plants.PlantType;
import me.herohd.rubyisland.plants.TropicalPlantManager;
import me.herohd.rubyisland.quests.PlayerQuestData;
import me.herohd.rubyisland.utils.Config;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

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
                            islandRs.getFloat("spawn_x"),
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
    public PlayerQuestData loadPlayerQuestData(UUID playerUUID) {
        PlayerQuestData playerQuestData = null;

        // 1. Carica lo stato attuale
        String statusQuery = "SELECT * FROM player_quests_status WHERE player_uuid = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(statusQuery)) {
            stmt.setString(1, playerUUID.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String activeQuestId = rs.getString("active_quest_id");
                double progress = rs.getDouble("quest_progress");
                playerQuestData = new PlayerQuestData(playerUUID, activeQuestId);
                playerQuestData.setProgress(progress);
            }
        } catch (SQLException e) {
            Bukkit.getLogger().warning("Errore caricando lo stato quest per " + playerUUID + ": " + e.getMessage());
            return null;
        }

        // Se il giocatore non ha uno stato, non ha senso cercare lo storico
        if (playerQuestData == null) {
            return null;
        }

        // 2. Carica lo storico
        String historyQuery = "SELECT completed_quest_id FROM player_quests_history WHERE player_uuid = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(historyQuery)) {
            stmt.setString(1, playerUUID.toString());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                playerQuestData.addCompletedQuest(rs.getString("completed_quest_id"));
            }
        } catch (SQLException e) {
            Bukkit.getLogger().warning("Errore caricando lo storico quest per " + playerUUID + ": " + e.getMessage());
        }

        return playerQuestData;
    }

    /**
     * Salva lo stato corrente della quest di un giocatore.
     */
    public void savePlayerQuestStatus(PlayerQuestData data) {
        String query = "INSERT INTO player_quests_status (player_uuid, active_quest_id, quest_progress) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE active_quest_id = VALUES(active_quest_id), quest_progress = VALUES(quest_progress);";

        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setString(1, data.getPlayerId().toString());
            stmt.setString(2, data.getActiveQuestId());
            stmt.setDouble(3, data.getProgress());
            stmt.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getLogger().warning("Errore salvando lo stato quest per " + data.getPlayerId() + ": " + e.getMessage());
        }
    }

    /**
     * Aggiunge una quest allo storico di un giocatore.
     */
    public void addCompletedQuestToHistory(UUID playerUUID, String completedQuestId) {
        String query = "INSERT IGNORE INTO player_quests_history (player_uuid, completed_quest_id) VALUES (?, ?)";

        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setString(1, playerUUID.toString());
            stmt.setString(2, completedQuestId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getLogger().warning("Errore aggiungendo quest allo storico per " + playerUUID + ": " + e.getMessage());
        }
    }

    public int countPlayerPlants(UUID playerUuid) {
        String query = "SELECT COUNT(*) FROM placed_tropical_plants WHERE owner_uuid = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setString(1, playerUuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            Bukkit.getLogger().warning("Errore nel contare le piante del giocatore: " + e.getMessage());
        }
        return 0;
    }

    public void removePlant(Location location) {
        String query = "DELETE FROM placed_tropical_plants WHERE world = ? AND x = ? AND y = ? AND z = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setString(1, location.getWorld().getName());
            stmt.setInt(2, location.getBlockX());
            stmt.setInt(3, location.getBlockY());
            stmt.setInt(4, location.getBlockZ());
            stmt.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getLogger().warning("Errore durante la rimozione della pianta tropicale: " + e.getMessage());
        }
    }
    public void saveOrUpdatePlant(PlacedPlant plant) {
        // Query aggiornata per includere 'is_automated'
        String query = "INSERT INTO placed_tropical_plants (world, x, y, z, owner_uuid, plant_type_id, upgrade_level, current_phase_index, next_growth_timestamp, is_automated) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "owner_uuid = VALUES(owner_uuid), " +
                "plant_type_id = VALUES(plant_type_id), " +
                "upgrade_level = VALUES(upgrade_level), " +
                "current_phase_index = VALUES(current_phase_index), " +
                "next_growth_timestamp = VALUES(next_growth_timestamp), " +
                "is_automated = VALUES(is_automated);"; // Aggiorna anche questo campo

        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            Location loc = plant.getLocation();
            stmt.setString(1, loc.getWorld().getName());
            stmt.setInt(2, loc.getBlockX());
            stmt.setInt(3, loc.getBlockY());
            stmt.setInt(4, loc.getBlockZ());
            stmt.setString(5, plant.getOwnerUuid().toString());
            stmt.setString(6, plant.getPlantType().getId());
            stmt.setInt(7, plant.getUpgradeLevel());
            stmt.setInt(8, plant.getCurrentPhaseIndex());
            stmt.setLong(9, plant.getNextGrowthTimestamp());
            stmt.setBoolean(10, plant.isAutomated()); // NUOVO: Imposta il valore del campo automazione
            stmt.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getLogger().warning("Errore durante il salvataggio della pianta tropicale: " + e.getMessage());
        }
    }

    /**
     * MODIFICATO: Aggiornata la logica per caricare anche lo stato di automazione.
     */
    public Map<Location, PlacedPlant> loadPlantsForPlayer(UUID playerUuid, TropicalPlantManager plantManager) {
        Map<Location, PlacedPlant> loadedPlants = new HashMap<>();
        String query = "SELECT * FROM placed_tropical_plants WHERE owner_uuid = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setString(1, playerUuid.toString());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                World world = Bukkit.getWorld(rs.getString("world"));
                if (world == null) {
                    Bukkit.getLogger().warning("Mondo '" + rs.getString("world") + "' non trovato per una pianta, skip.");
                    continue;
                }
                Location loc = new Location(world, rs.getInt("x"), rs.getInt("y"), rs.getInt("z"));

                String plantTypeId = rs.getString("plant_type_id");
                PlantType type = plantManager.getPlantTypes().get(plantTypeId);
                if (type == null) {
                    Bukkit.getLogger().warning("Tipo di pianta '" + plantTypeId + "' non trovato, skip caricamento pianta a " + loc);
                    continue;
                }

                PlacedPlant plant = new PlacedPlant(loc, playerUuid, type);
                plant.setUpgradeLevel(rs.getInt("upgrade_level"));
                plant.setCurrentPhaseIndex(rs.getInt("current_phase_index"));
                plant.setNextGrowthTimestamp(rs.getLong("next_growth_timestamp"));
                plant.setAutomated(rs.getBoolean("is_automated")); // NUOVO: Carica lo stato di automazione

                loadedPlants.put(loc, plant);
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe("Errore durante il caricamento delle piante per " + playerUuid + ": " + e.getMessage());
        }
        return loadedPlants;
    }

    // --- METODI PER I COLLECTOR ---

    /**
     * Salva un nuovo collector o aggiorna uno esistente nel database.
     * @param collector L'oggetto PlacedCollector da salvare.
     */
    public void saveOrUpdateCollector(PlacedCollector collector) {
        String query = "INSERT INTO placed_collectors (world, x, y, z, owner_uuid, collector_type_id, amount) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "owner_uuid = VALUES(owner_uuid), " +
                "collector_type_id = VALUES(collector_type_id), " +
                "amount = VALUES(amount);";

        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            Location loc = collector.getLocation();
            stmt.setString(1, loc.getWorld().getName());
            stmt.setInt(2, loc.getBlockX());
            stmt.setInt(3, loc.getBlockY());
            stmt.setInt(4, loc.getBlockZ());
            stmt.setString(5, collector.getOwnerUuid().toString());
            stmt.setString(6, collector.getCollectorType().getId());
            stmt.setDouble(7, collector.getAmount());
            stmt.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getLogger().warning("Errore durante il salvataggio del collector: " + e.getMessage());
        }
    }

    /**
     * Rimuove un collector dal database usando la sua posizione.
     * @param location La posizione del collector da rimuovere.
     */
    public void removeCollector(Location location) {
        String query = "DELETE FROM placed_collectors WHERE world = ? AND x = ? AND y = ? AND z = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setString(1, location.getWorld().getName());
            stmt.setInt(2, location.getBlockX());
            stmt.setInt(3, location.getBlockY());
            stmt.setInt(4, location.getBlockZ());
            stmt.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getLogger().warning("Errore durante la rimozione del collector: " + e.getMessage());
        }
    }


    public int countPlayerCollectors(UUID playerUuid) {
        String query = "SELECT COUNT(*) FROM placed_collectors WHERE owner_uuid = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setString(1, playerUuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            Bukkit.getLogger().warning("Errore nel contare i collector del giocatore: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Carica tutti i collector di un giocatore specifico dal database.
     * @param playerUuid L'UUID del giocatore di cui caricare i collector.
     * @param collectorManager Il manager dei collector per ottenere i CollectorType.
     * @return Una mappa dei collector caricati per quel giocatore.
     */
    public Map<Location, PlacedCollector> loadCollectorsForPlayer(UUID playerUuid, CollectorManager collectorManager) {
        Map<Location, PlacedCollector> loadedCollectors = new HashMap<>();
        String query = "SELECT * FROM placed_collectors WHERE owner_uuid = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setString(1, playerUuid.toString());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                World world = Bukkit.getWorld(rs.getString("world"));
                if (world == null) {
                    Bukkit.getLogger().warning("Mondo '" + rs.getString("world") + "' non trovato per un collector, skip.");
                    continue;
                }
                Location loc = new Location(world, rs.getInt("x"), rs.getInt("y"), rs.getInt("z"));

                String collectorTypeId = rs.getString("collector_type_id");
                CollectorType type = collectorManager.getCollectorTypes().get(collectorTypeId);
                if (type == null) {
                    Bukkit.getLogger().warning("Tipo di collector '" + collectorTypeId + "' non trovato, skip caricamento collector a " + loc);
                    continue;
                }

                long amount = rs.getLong("amount");
                PlacedCollector collector = new PlacedCollector(loc, playerUuid, type, amount);
                loadedCollectors.put(loc, collector);
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe("Errore durante il caricamento dei collector per " + playerUuid + ": " + e.getMessage());
        }
        return loadedCollectors;
    }

    /**
     * Salva o aggiorna il punteggio di un giocatore per un tipo specifico.
     */
    public void saveOrUpdatePlayerPoints(UUID uuid, String pointType, double amount) {
        String query = "INSERT INTO player_points (player_uuid, point_type, amount) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE amount = VALUES(amount);";
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, pointType);
            stmt.setDouble(3, amount);
            stmt.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getLogger().warning("Errore salvando i punti per " + uuid + ": " + e.getMessage());
        }
    }

    /**
     * Carica tutti i punti di un giocatore dal database.
     * @return Una mappa con Tipo di Punto -> Quantità.
     */
    public Map<String, Double> loadPlayerPoints(UUID uuid) {
        Map<String, Double> points = new HashMap<>();
        String query = "SELECT point_type, amount FROM player_points WHERE player_uuid = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                points.put(rs.getString("point_type"), rs.getDouble("amount"));
            }
        } catch (SQLException e) {
            Bukkit.getLogger().warning("Errore caricando i punti per " + uuid + ": " + e.getMessage());
        }
        return points;
    }



}
