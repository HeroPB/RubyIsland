package me.herohd.rubyisland.collectors;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import me.herohd.rubyisland.RubyIsland;
import me.herohd.rubyisland.collectors.CollectorType;
import me.herohd.rubyisland.collectors.PlacedCollector;
import me.herohd.rubyisland.event.CollectorBreakEvent;
import me.herohd.rubyisland.event.CollectorFillEvent;
import me.herohd.rubyisland.manager.MySQLManager;
import me.herohd.rubyisland.utils.NBTEditor;
import me.herohd.rubyisland.utils.SkullCreator;
import me.herohd.rubyisland.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class CollectorManager {
    private final RubyIsland plugin;
    private final MySQLManager mySQLManager;
    private final Map<String, CollectorType> collectorTypes = new HashMap<>();
    public final Map<Location, PlacedCollector> activeCollectors = new ConcurrentHashMap<>();
    private final Set<UUID> playersLoadingCollectors = ConcurrentHashMap.newKeySet();

    // Cache per i livelli dei giocatori, si resetta ogni 10 minuti per aggiornare i permessi
    private final Cache<UUID, Map<String, Integer>> playerLevelCache = CacheBuilder.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build();

    public CollectorManager(RubyIsland plugin) {
        this.plugin = plugin;
        this.mySQLManager = plugin.getMySQLManager();
    }

    public void loadCollectorsForPlayer(Player player) {
        UUID playerUuid = player.getUniqueId();
        playersLoadingCollectors.add(playerUuid);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Map<Location, PlacedCollector> playerCollectors = mySQLManager.loadCollectorsForPlayer(playerUuid, this);

            Bukkit.getScheduler().runTask(plugin, () -> {
                // Se il giocatore ha fatto logout nel frattempo, non fare nulla.
                if (!player.isOnline()) {
                    playersLoadingCollectors.remove(playerUuid);
                    return;
                }

                // Aggiungi i collector alla mappa attiva e crea i loro ologrammi
                for (PlacedCollector collector : playerCollectors.values()) {
                    collector.setHologram(DHAPI.createHologram(UUID.randomUUID().toString(), Utils.getBlockCenter(collector.getLocation(), 3)));
                    collector.updateHologram(this);
                    activeCollectors.put(collector.getLocation(), collector);
                }

                playersLoadingCollectors.remove(playerUuid);
                Bukkit.getLogger().info("Caricati " + playerCollectors.size() + " collector per " + player.getName());
            });
        });
    }

    /**
     * Forza l'aggiornamento del livello di un giocatore nella cache.
     * Utile quando un permesso viene aggiunto o rimosso manualmente.
     * @param uuid L'UUID del giocatore.
     * @param collectorId L'ID del collector.
     * @param level Il nuovo livello da memorizzare nella cache.
     */
    public void forceUpdatePlayerLevelCache(UUID uuid, String collectorId, int level) {
        Map<String, Integer> playerLevels = playerLevelCache.getIfPresent(uuid);
        if (playerLevels == null) {
            playerLevels = new HashMap<>();
        }
        playerLevels.put(collectorId, level);
        playerLevelCache.put(uuid, playerLevels);
    }

    /**
     * Rimuove i collector di un giocatore dalla memoria attiva quando esce.
     * @param player Il giocatore che ha lasciato il server.
     */
    public void unloadCollectorsForPlayer(Player player) {
        UUID playerUuid = player.getUniqueId();

        if (playersLoadingCollectors.contains(playerUuid)) {
            Bukkit.getLogger().warning("Il giocatore " + player.getName() + " ha quittato durante il caricamento dei suoi collector.");
        }

        Iterator<Map.Entry<Location, PlacedCollector>> iterator = activeCollectors.entrySet().iterator();
        int count = 0;
        while (iterator.hasNext()) {
            Map.Entry<Location, PlacedCollector> entry = iterator.next();
            if (entry.getValue().getOwnerUuid().equals(playerUuid)) {
                // Rimuovi l'hologramma dal mondo
                if (entry.getValue().getHologram() != null) {
                    entry.getValue().getHologram().delete();
                }
                iterator.remove();
                count++;
            }
        }
        if (count > 0) {
            Bukkit.getLogger().info("Rimossi " + count + " collector dalla memoria per " + player.getName());
        }
    }

    public void placeCollector(Player player, Location location, CollectorType type, double initialAmount) {
        PlacedCollector collector = new PlacedCollector(location, player.getUniqueId(), type, initialAmount);

        activeCollectors.put(location, collector);
        collector.setHologram(DHAPI.createHologram(UUID.randomUUID().toString(), Utils.getBlockCenter(location, 3)));
        collector.updateHologram(this); // Passa il manager per ottenere il livello

        mySQLManager.saveOrUpdateCollector(collector);
        player.getInventory().setItemInMainHand(null);
        player.sendMessage("§e§lISOLOTTO§8: §fCollector piazzato correttamente");
    }

    public void fillCollector(Player player, PlacedCollector collector, boolean fillAll) {
        ItemStack fillableItem = collector.getCollectorType().getFillableBlock();
        int playerLevel = getPlayerLevel(player, collector.getCollectorType().getId());
        CollectorLevel levelData = collector.getCollectorType().getLevels().get(playerLevel);
        long maxCapacity = levelData.getMaxCapacity();
        double spaceLeft = maxCapacity - collector.getAmount();

        if (spaceLeft <= 0) {
            player.sendMessage("§e§lISOLOTTO§8: §fQuesto collector è pieno");
            return;
        }

        int amountToAdd = 0;
        if (fillAll) {
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && item.isSimilar(fillableItem)) {
                    amountToAdd += item.getAmount();
                }
            }
        } else {
            ItemStack itemInHand = player.getInventory().getItemInMainHand();
            if (itemInHand != null && itemInHand.isSimilar(fillableItem)) {
                amountToAdd = itemInHand.getAmount();
            } else {
                player.sendMessage("§e§lISOLOTTO§8: §fTieni in mano il blocco corretto!");
                return;
            }
        }

        if (amountToAdd == 0) {
            player.sendMessage("§e§lISOLOTTO§8: §fNon hai niente nel tuo inventario ");
            return;
        }

        int finalAmount = (int) Math.min(amountToAdd, spaceLeft);

        player.getInventory().removeItem(new ItemStack(fillableItem.getType(), finalAmount, fillableItem.getDurability()));
        collector.addAmount(finalAmount);


        CollectorFillEvent collectorFillEvent = new CollectorFillEvent(player.getName(), collector.getCollectorType().getId(), collector.getLocation(), finalAmount);
        Bukkit.getPluginManager().callEvent(collectorFillEvent);

        collector.updateHologram(this);
        mySQLManager.saveOrUpdateCollector(collector);
        player.sendMessage("§e§lISOLOTTO§8: §fHai aggiunto §e" + finalAmount + " §fblocchi al collector.");
    }

    public void breakCollector(Player player, PlacedCollector collector) {

        if (collector.getAmount() <= 0) {
            player.sendMessage("§e§lISOLOTTO§8: §fQuesto collector è vuoto");
            return;
        }

        int playerLevel = getPlayerLevel(player, collector.getCollectorType().getId());
        CollectorLevel levelData = collector.getCollectorType().getLevels().get(playerLevel);
        int maxBreakAmount = levelData.getBreakAmount();

        int actualBreakAmount = (int) Math.min(collector.getAmount(), maxBreakAmount);

        // Riduci la quantità nel collector
        collector.addAmount(-actualBreakAmount);

        // Esegui comandi
        List<String> commands = new ArrayList<>();
        for (int i = 0; i < actualBreakAmount; i++) {
            String command = collector.getCollectorType().getRandomCommand();
            commands.add(command);
            if (command != null) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.getName()));
            }
        }

        CollectorBreakEvent collectorBreakEvent = new CollectorBreakEvent(player.getName(), collector.getCollectorType().getId(), collector.getLocation(), actualBreakAmount, commands);
        Bukkit.getPluginManager().callEvent(collectorBreakEvent);


        collector.updateHologram(this);
        mySQLManager.saveOrUpdateCollector(collector);
        player.sendMessage("§e§lISOLOTTO§8: §fHai rotto §e" + actualBreakAmount + " §fblocchi dal collector!");
    }

    public void loadCollectorTypes() {
        collectorTypes.clear();
        File collectorsFolder = new File(plugin.getDataFolder(), "collectors");

        if (!collectorsFolder.exists()) {
            collectorsFolder.mkdirs();
            Bukkit.getLogger().info("[RubyIsland] Creata cartella 'collectors', inserisci qui i file .yml dei container.");
            return;
        }

        File[] collectorFiles = collectorsFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".yml"));
        if (collectorFiles == null || collectorFiles.length == 0) {
            Bukkit.getLogger().info("[RubyIsland] Nessun file .yml trovato nella cartella 'collectors'.");
            return;
        }

        for (File collectorFile : collectorFiles) {
            try {
                String id = collectorFile.getName().replace(".yml", "");
                FileConfiguration config = YamlConfiguration.loadConfiguration(collectorFile);
                CollectorType type = new CollectorType(id);

                ConfigurationSection itemSection = config.getConfigurationSection("item");
                if (itemSection != null) {
                    // Carica l'item di base (materiale e meta per le teste)
                    ItemStack baseItem = createItemFromConfigSection(itemSection);
                    type.setCollectorItem(baseItem);
                    // Salva nome e lore per usarli dinamicamente
                    type.setItemName(itemSection.getString("name"));
                    type.setItemLore(itemSection.getStringList("lore"));
                }

                // 2. Carica il blocco che può essere inserito
                type.setFillableBlock(createItemFromString(config.getString("fillable-block")));

                // 3. Carica le linee dell'hologramma
                type.setHologramLines(config.getStringList("hologram"));

                if(config.contains("permission") && config.getString("permission") != null) {
                    type.setPermission(config.getString("permission"));
                }

                // 4. Carica i comandi pesati
                for (String line : config.getStringList("command-on-break")) {
                    try {
                        int weight = Integer.parseInt(line.substring(line.indexOf('[') + 1, line.indexOf(']')));
                        String command = line.substring(line.indexOf(']') + 2);
                        type.addWeightCommand(command, weight);
                    } catch (Exception e) {
                        Bukkit.getLogger().warning("Formato comando non valido nel file " + id + ".yml: " + line);
                    }
                }

                // 5. Carica i livelli
                for (String levelKey : config.getConfigurationSection("level").getKeys(false)) {
                    int level = Integer.parseInt(levelKey);
                    String[] levelData = config.getString("level." + levelKey).split(";", 2);
                    long maxCapacity = Long.parseLong(levelData[0]);
                    int breakAmount = Integer.parseInt(levelData[1]);
                    type.addLevel(level, new CollectorLevel(maxCapacity, breakAmount));
                }

                collectorTypes.put(id, type);
                Bukkit.getLogger().info("[RubyIsland] Caricato collector: " + id);

            } catch (Exception e) {
                Bukkit.getLogger().severe("Errore durante il caricamento del collector: " + collectorFile.getName());
                e.printStackTrace();
            }
        }
    }


    private ItemStack createItemFromString(String itemString) {
        if (itemString == null || itemString.isEmpty()) return new ItemStack(Material.AIR);
        String[] parts = itemString.split(";", 2);
        Material material = Material.getMaterial(parts[0].toUpperCase());
        short data = (parts.length > 1) ? Short.parseShort(parts[1]) : 0;
        return new ItemStack(material != null ? material : Material.STONE, 1, data);
    }

    private ItemStack createItemFromConfigSection(ConfigurationSection section) {
        String materialString = section.getString("material", "STONE");
        String[] parts = materialString.split(";", 2);
        Material material = Material.getMaterial(parts[0].toUpperCase());
        short data = (parts.length > 1) ? Short.parseShort(parts[1]) : 0;

        ItemStack item = new ItemStack(material != null ? material : Material.STONE, 1, data);

        if ((material == Material.SKULL || material == Material.SKULL_ITEM) && data == 3 && section.contains("meta")) {
            item = SkullCreator.itemFromBase64(section.getString("meta"));
        }
        return item;
    }

    public int getPlayerLevel(Player player, String collectorId) {
        Map<String, Integer> playerLevels = playerLevelCache.getIfPresent(player.getUniqueId());
        if (playerLevels != null && playerLevels.containsKey(collectorId)) {
            return playerLevels.get(collectorId);
        }

        CollectorType type = collectorTypes.get(collectorId);
        if (type == null) return 1;

        // Itera al contrario per trovare il permesso più alto
        for (int level : type.getLevels().descendingKeySet()) {
            if (player.hasPermission("rubyisland.container." + collectorId + "." + level)) {
                cachePlayerLevel(player.getUniqueId(), collectorId, level);
                return level;
            }
        }

        cachePlayerLevel(player.getUniqueId(), collectorId, 1);
        return 1; // Livello di default
    }



    private void cachePlayerLevel(UUID uuid, String collectorId, int level) {
        forceUpdatePlayerLevelCache(uuid, collectorId, level);
    }

    // Metodi per la logica di piazzamento, riempimento e rottura

    // Getters
    public Map<String, CollectorType> getCollectorTypes() { return collectorTypes; }
    public Map<Location, PlacedCollector> getActiveCollectors() { return activeCollectors; }
}