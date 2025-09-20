package me.herohd.rubyisland.manager;

import me.herohd.rubyisland.RubyIsland;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerPointsManager {

    private final RubyIsland plugin;
    private final MySQLManager mySQLManager;
    private final Set<String> validPointTypes = new HashSet<>();
    private final Map<UUID, Map<String, Double>> playerPointsCache = new ConcurrentHashMap<>();

    public PlayerPointsManager(RubyIsland plugin) {
        this.plugin = plugin;
        this.mySQLManager = plugin.getMySQLManager();
    }

    /**
     * Carica i tipi di punti validi dal config.yml.
     */
    public void loadPointTypes() {
        validPointTypes.clear();
        validPointTypes.addAll(plugin.getConfigYML().getStringList("points-system.types"));
        Bukkit.getLogger().info("[RubyIsland] Caricati " + validPointTypes.size() + " tipi di punti.");
    }

    // --- GESTIONE CACHE ---

    public void loadPlayerData(Player player) {
        UUID uuid = player.getUniqueId();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Map<String, Double> points = mySQLManager.loadPlayerPoints(uuid);
            // Torna sul thread principale per modificare la cache
            Bukkit.getScheduler().runTask(plugin, () -> {
                playerPointsCache.put(uuid, new ConcurrentHashMap<>(points));
            });
        });
    }

    public void saveAndUnloadPlayerData(Player player) {
        UUID uuid = player.getUniqueId();
        Map<String, Double> points = playerPointsCache.remove(uuid);
        if (points != null) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                for (Map.Entry<String, Double> entry : points.entrySet()) {
                    mySQLManager.saveOrUpdatePlayerPoints(uuid, entry.getKey(), entry.getValue());
                }
            });
        }
    }

    public void saveAllOnlinePlayerData() {
        // Metodo sincrono per lo spegnimento
        for (Map.Entry<UUID, Map<String, Double>> playerData : playerPointsCache.entrySet()) {
            for (Map.Entry<String, Double> entry : playerData.getValue().entrySet()) {
                mySQLManager.saveOrUpdatePlayerPoints(playerData.getKey(), entry.getKey(), entry.getValue());
            }
        }
    }

    // --- API PUBBLICA ---

    /**
     * Ottiene i punti di un giocatore per un tipo specifico.
     * @return I punti del giocatore, o 0.0 se non ne ha.
     */
    public double getPoints(Player player, String pointType) {
        String type = pointType.toUpperCase();
        if (!validPointTypes.contains(type)) return 0.0;

        return playerPointsCache
                .getOrDefault(player.getUniqueId(), new ConcurrentHashMap<>())
                .getOrDefault(type, 0.0);
    }

    /**
     * Aggiunge punti a un giocatore.
     * @return Il nuovo totale di punti.
     */
    public double addPoints(Player player, String pointType, double amount) {
        String type = pointType.toUpperCase();
        if (!validPointTypes.contains(type) || amount <= 0) return getPoints(player, type);

        Map<String, Double> playerCache = playerPointsCache.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>());
        double newAmount = playerCache.getOrDefault(type, 0.0) + amount;
        playerCache.put(type, newAmount);
        return newAmount;
    }

    /**
     * Rimuove punti da un giocatore. Non va sotto lo zero.
     * @return Il nuovo totale di punti.
     */
    public double takePoints(Player player, String pointType, double amount) {
        String type = pointType.toUpperCase();
        if (!validPointTypes.contains(type) || amount <= 0) return getPoints(player, type);

        Map<String, Double> playerCache = playerPointsCache.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>());
        double currentAmount = playerCache.getOrDefault(type, 0.0);
        double newAmount = Math.max(0.0, currentAmount - amount);
        playerCache.put(type, newAmount);
        return newAmount;
    }

    /**
     * Imposta i punti di un giocatore a un valore specifico.
     */
    public void setPoints(Player player, String pointType, double amount) {
        String type = pointType.toUpperCase();
        if (!validPointTypes.contains(type)) return;

        Map<String, Double> playerCache = playerPointsCache.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>());
        playerCache.put(type, Math.max(0.0, amount));
    }


    public Set<String> getValidPointTypes() {
        return java.util.Collections.unmodifiableSet(validPointTypes);
    }
}
