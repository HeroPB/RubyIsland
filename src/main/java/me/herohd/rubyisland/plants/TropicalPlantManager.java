package me.herohd.rubyisland.plants;

import com.vk2gpz.tokenenchant.api.TokenEnchantAPI;
import me.herohd.rubycrops.RubyCrops;
import me.herohd.rubyisland.RubyIsland;
import me.herohd.rubyisland.boosters.BoosterType;
import me.herohd.rubyisland.event.UpgradePlantEvent;
import me.herohd.rubyisland.manager.MySQLManager;
import me.herohd.rubyisland.manager.PlayerPointsManager;
import me.herohd.rubyisland.plants.*;
import me.herohd.rubyisland.quests.PlayerQuestData;
import me.herohd.rubyisland.quests.QuestManager;
import me.herohd.rubyisland.utils.Formatter;
import me.herohd.rubyisland.utils.Messages;
import me.herohd.rubyisland.utils.NBTEditor;
import me.herohd.rubyisland.utils.NumberUtils;
import me.herohd.rubyisland.utils.SkullCreator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class TropicalPlantManager {
    private final RubyIsland plugin;
    private final MySQLManager mySQLManager;

    // Mappa dei tipi di pianta caricati dai file .yml <ID, Config>
    private final Map<String, PlantType> plantTypes = new HashMap<>();
    // Mappa delle piante attualmente piazzate nel mondo <Location, OggettoPianta>
    private final Map<Location, PlacedPlant> activePlants = new ConcurrentHashMap<>();
    private final Set<UUID> playersLoading = Collections.synchronizedSet(new HashSet<>());


    public TropicalPlantManager(RubyIsland plugin) {
        this.plugin = plugin;
        this.mySQLManager = plugin.getMySQLManager();
    }

    /**
     * Carica tutti i tipi di pianta dai file di configurazione .yml
     * presenti nella cartella /plugins/RubyIsland/plants/
     */
//    public void loadPlantTypes() {
//        plantTypes.clear(); // Pulisce la mappa per un ricaricamento
//        File plantsFolder = new File(plugin.getDataFolder(), "plants");
//
//        if (!plantsFolder.exists()) {
//            plantsFolder.mkdirs();
//            Bukkit.getLogger().info("[RubyIsland] Creata cartella 'plants', inserisci qui i file .yml delle piante.");
//            return;
//        }
//
//        File[] plantFiles = plantsFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".yml"));
//
//        if (plantFiles == null || plantFiles.length == 0) {
//            Bukkit.getLogger().info("[RubyIsland] Nessun file .yml trovato nella cartella 'plants'.");
//            return;
//        }
//
//        for (File plantFile : plantFiles) {
//            try {
//                String plantId = plantFile.getName().replace(".yml", "");
//                FileConfiguration config = YamlConfiguration.loadConfiguration(plantFile);
//                PlantType plantType = new PlantType(plantId);
//
//                // 1. Carica l'item fisico usando SkullCreator
//                String base64Texture = config.getString("item.meta");
//                ItemStack item = SkullCreator.itemFromBase64(base64Texture);
//
//                ItemMeta meta = item.getItemMeta();
//                if (meta != null) {
//                    meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', config.getString("item.name")));
//                    List<String> lore = config.getStringList("item.lore").stream()
//                            .map(line -> ChatColor.translateAlternateColorCodes('&', line))
//                            .collect(Collectors.toList());
//                    meta.setLore(lore);
//                    item.setItemMeta(meta);
//                }
//
//                item = NBTEditor.addNBT(item, "type", plantId);
//                plantType.setPlantItem(item);
//
//                // 2. Carica le linee dell'hologramma
//                plantType.setHologramLines(config.getStringList("hologram"));
//
//                // 3. Carica il prezzo di vendita (sell-plants)
//                plantType.setSellPrice(config.getInt("sell-plants", 0));
//
//                // 4. Carica le fasi di crescita
//                Map<Integer, PlantPhase> phases = new HashMap<>();
//                ConfigurationSection phasesSection = config.getConfigurationSection("phases");
//                if (phasesSection != null) {
//                    for (String key : phasesSection.getKeys(false)) {
//                        int phaseIndex = Integer.parseInt(key);
//                        String path = "phases." + key;
//                        PlantPhase phase = new PlantPhase(
//                                config.getString(path + ".meta"), // Texture base64 per la fase
//                                config.getString(path + ".message"),
//                                config.getString(path + ".type"),
//                                config.getInt(path + ".require") // Es. minuti
//                        );
//                        phases.put(phaseIndex, phase);
//                    }
//                }
//                plantType.setPhases(phases);
//
//                // 5. Carica gli upgrades
//                Map<Integer, PlantUpgrade> upgrades = new HashMap<>();
//                ConfigurationSection upgradesSection = config.getConfigurationSection("upgrades");
//                if (upgradesSection != null) {
//                    for (String key : upgradesSection.getKeys(false)) {
//                        int upgradeLevel = Integer.parseInt(key);
//                        String path = "upgrades." + key;
//                        PlantUpgrade upgrade = new PlantUpgrade(
//                                config.getInt(path + ".cost"),
//                                config.getInt(path + ".sell"), // 'sell' è il reward
//                                config.getString(path + ".sell_type"),
//                                config.getString(path + ".require_quest_id", null) // Campo opzionale
//                        );
//                        upgrades.put(upgradeLevel, upgrade);
//                    }
//                }
//                plantType.setUpgrades(upgrades);
//
//                // Aggiungi la pianta caricata alla mappa
//                plantTypes.put(plantId, plantType);
//                Bukkit.getLogger().info("[RubyIsland] Caricata pianta tropicale: " + plantId);
//
//            } catch (Exception e) {
//                Bukkit.getLogger().severe("[RubyIsland] Errore durante il caricamento del file della pianta: " + plantFile.getName());
//                e.printStackTrace();
//            }
//        }
//    }

    /**
     * Avvia il caricamento asincrono delle piante per un giocatore.
     * @param player Il giocatore che è entrato nel server.
     */
    public void loadPlantsForPlayer(Player player) {
        UUID playerUuid = player.getUniqueId();
        playersLoading.add(playerUuid);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            // Carica i dati dal DB in un thread separato
            Map<Location, PlacedPlant> playerPlants = mySQLManager.loadPlantsForPlayer(playerUuid, this);

            Bukkit.getScheduler().runTask(plugin, () -> {
                // Esegui la logica di aggiunta nel thread principale
                if (!player.isOnline()) {
                    // Se il giocatore ha fatto logout nel frattempo, non fare nulla.
                    playersLoading.remove(playerUuid);
                    return;
                }

                activePlants.putAll(playerPlants);
                playersLoading.remove(playerUuid);

                Bukkit.getLogger().info("Caricate " + playerPlants.size() + " piante per " + player.getName());
            });
        });
    }

    /**
     * Rimuove le piante di un giocatore dalla memoria attiva.
     * @param player Il giocatore che ha lasciato il server.
     */
    public void unloadPlantsForPlayer(Player player) {
        UUID playerUuid = player.getUniqueId();

        // Se il giocatore sta ancora caricando, il processo di caricamento si annullerà da solo.
        // Aspettare potrebbe causare problemi. Rimuoviamo subito quelle già caricate.
        if (playersLoading.contains(playerUuid)) {
            Bukkit.getLogger().warning("Il giocatore " + player.getName() + " ha quittato durante il caricamento delle sue piante.");
        }

        Iterator<Map.Entry<Location, PlacedPlant>> iterator = activePlants.entrySet().iterator();
        int count = 0;
        while (iterator.hasNext()) {
            Map.Entry<Location, PlacedPlant> entry = iterator.next();
            if (entry.getValue().getOwnerUuid().equals(playerUuid)) {
                entry.getValue().getHologram().delete();
                iterator.remove();
                count++;
            }
        }
        if (count > 0) {
            Bukkit.getLogger().info("Rimosse " + count + " piante dalla memoria per " + player.getName());
        }
    }

    // ... altri metodi (placePlant, harvestPlant, etc.) rimangono invariati ...


    public void loadPlantTypes() {
        plantTypes.clear(); // Pulisce la mappa per un ricaricamento
        File plantsFolder = new File(plugin.getDataFolder(), "plants");

        if (!plantsFolder.exists()) {
            plantsFolder.mkdirs();
            Bukkit.getLogger().info("[RubyIsland] Creata cartella 'plants', inserisci qui i file .yml delle piante.");
            return;
        }

        File[] plantFiles = plantsFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".yml"));

        if (plantFiles == null || plantFiles.length == 0) {
            Bukkit.getLogger().info("[RubyIsland] Nessun file .yml trovato nella cartella 'plants'.");
            return;
        }

        for (File plantFile : plantFiles) {
            try {
                String plantId = plantFile.getName().replace(".yml", "");
                FileConfiguration config = YamlConfiguration.loadConfiguration(plantFile);
                PlantType plantType = new PlantType(plantId);

                // 1. Carica l'item fisico usando SkullCreator
                String base64Texture = config.getString("item.meta");
                ItemStack item = SkullCreator.itemFromBase64(base64Texture);

                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', config.getString("item.name")));
                    List<String> lore = config.getStringList("item.lore").stream()
                            .map(line -> ChatColor.translateAlternateColorCodes('&', line)
                            )
                            .collect(Collectors.toList());
                    meta.setLore(lore);
                    item.setItemMeta(meta);
                }

                item = NBTEditor.addNBT(item, "type", plantId);
                plantType.setPlantItem(item);

                // 2. Carica le linee dell'hologramma
                plantType.setHologramLines(config.getStringList("hologram"));

                // 3. Carica il prezzo di vendita (sell-plants)
                plantType.setSellPrice(config.getInt("sell-plants", 0));

                // 4. Carica le fasi di crescita
                Map<Integer, PlantPhase> phases = new HashMap<>();
                ConfigurationSection phasesSection = config.getConfigurationSection("phases");
                if (phasesSection != null) {
                    for (String key : phasesSection.getKeys(false)) {
                        int phaseIndex = Integer.parseInt(key);
                        String path = "phases." + key;
                        PlantPhase phase = new PlantPhase(
                                config.getString(path + ".meta"), // Texture base64 per la fase
                                config.getString(path + ".message"),
                                config.getString(path + ".type"),
                                config.getInt(path + ".require") // Es. minuti
                        );
                        phases.put(phaseIndex, phase);
                    }
                }
                plantType.setPhases(phases);

                // 5. Carica gli upgrades
                Map<Integer, PlantUpgrade> upgrades = new HashMap<>();
                ConfigurationSection upgradesSection = config.getConfigurationSection("upgrades");
                if (upgradesSection != null) {
                    for (String key : upgradesSection.getKeys(false)) {
                        int upgradeLevel = Integer.parseInt(key);
                        String path = "upgrades." + key;

                        // Leggi la nuova lista di requisiti
                        List<String> requirements = config.getStringList(path + ".require");

                        String sellString = config.getString(path + ".sell", "0");
                        // Converti la stringa in un numero grande usando NumberUtils
                        double sellAmount = NumberUtils.parseBigNumber(sellString);

                        PlantUpgrade upgrade = new PlantUpgrade(
                                sellAmount,
                                config.getString(path + ".sell_type"),
                                requirements
                        );
                        upgrades.put(upgradeLevel, upgrade);
                    }
                }
                plantType.setUpgrades(upgrades);

                // Aggiungi la pianta caricata alla mappa
                plantTypes.put(plantId, plantType);
                Bukkit.getLogger().info("[RubyIsland] Caricata pianta tropicale: " + plantId);

            } catch (Exception e) {
                Bukkit.getLogger().severe("[RubyIsland] Errore durante il caricamento del file della pianta: " + plantFile.getName());
                e.printStackTrace();
            }
        }
    }
    public void placePlant(Player player, Location location, PlantType type, int initialUpgradeLevel) {
        // 1. Controlla il limite massimo di piante
        int maxPlants = plugin.getConfigYML().getInt("plants.max-per-player");
        int currentPlants = mySQLManager.countPlayerPlants(player.getUniqueId());

        if (currentPlants >= maxPlants) {
            player.sendMessage(Messages.PLANTS_MAX_REACHED.getAsString());
            return;
        }

        // 2. Crea l'oggetto PlacedPlant (lo stato interno è già fase 1)
        PlacedPlant plant = new PlacedPlant(location, player.getUniqueId(), type);
        // IMPOSTA IL LIVELLO DI UPGRADE LETTO DALL'ITEM
        plant.setUpgradeLevel(initialUpgradeLevel);
        PlantPhase firstPhase = type.getPhase(1);
        if (firstPhase == null) {
            Bukkit.getLogger().severe("La pianta '" + type.getId() + "' non ha una Fase 1 definita!");
            player.sendMessage(ChatColor.RED + "Errore di configurazione per questa pianta. Contatta un admin.");
            return;
        }

        // 3. IMPOSTA LA TEXTURE INIZIALE (FIX VISIVO)
        Block block = location.getBlock();
        Bukkit.getScheduler().runTask(plugin, () -> {
            SkullCreator.blockWithBase64(block, firstPhase.getTextureMeta());
        });

        // 4. Calcola il primo timestamp di crescita
        if (firstPhase.getType().equalsIgnoreCase("TIME") && firstPhase.getRequire() > 0) {
            long growthTimeMillis = TimeUnit.MINUTES.toMillis(firstPhase.getRequire());
            plant.setNextGrowthTimestamp(System.currentTimeMillis() + growthTimeMillis);
        } else {
            plant.setNextGrowthTimestamp(Long.MAX_VALUE); // Non cresce col tempo
        }

        // 6. Aggiungi la pianta alla mappa delle piante attive
        activePlants.put(location, plant);

        // 7. Aggiorna l'hologramma con i dati iniziali
        plant.updateHologram();

        // 8. Salva la nuova pianta su MySQL
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            mySQLManager.saveOrUpdatePlant(plant);
        });

        int amount = player.getInventory().getItemInMainHand().getAmount();
        // 9. Rimuovi l'item dalla mano del giocatore
        player.getInventory().getItemInMainHand().setAmount(amount-1);
    }

    public void harvestPlant(Player player, Location location) {
        PlacedPlant plant = activePlants.get(location);
        if (plant == null || !plant.isReadyToHarvest()) {
            return;
        }

        // 1. Dai la ricompensa
        PlantUpgrade currentUpgrade = plant.getPlantType().getUpgrade(plant.getUpgradeLevel());
        if (currentUpgrade == null) {
            Bukkit.getLogger().severe("Errore critico: Impossibile trovare l'upgrade " + plant.getUpgradeLevel() + " per la pianta " + plant.getPlantType().getId());
            player.sendMessage(ChatColor.RED + "Si è verificato un errore, contatta un amministratore.");
            return;
        }
        player.sendMessage(Messages.PLANTS_HARVESTED.getAsString());
        giveReward(player, currentUpgrade.getSellReward(), currentUpgrade.getSellType());

        // 2. Resetta la pianta alla fase 1
        plant.setCurrentPhaseIndex(1);

        // 3. Calcola e imposta il nuovo timestamp di crescita
        PlantPhase firstPhase = plant.getPlantType().getPhase(1);
        if (firstPhase != null && firstPhase.getType().equalsIgnoreCase("TIME") && firstPhase.getRequire() > 0) {
            long growthTimeMillis = TimeUnit.MINUTES.toMillis(firstPhase.getRequire());
            plant.setNextGrowthTimestamp(System.currentTimeMillis() + growthTimeMillis);
        } else {
            plant.setNextGrowthTimestamp(Long.MAX_VALUE); // Evita crescita se non configurato
        }

        // 4. Cambia la texture del blocco a quella della prima fase
        Block block = location.getBlock();
        Bukkit.getScheduler().runTask(plugin, () -> {
            SkullCreator.blockWithBase64(block, firstPhase.getTextureMeta());
        });

        // 5. Aggiorna la pianta su MySQL
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            mySQLManager.saveOrUpdatePlant(plant);
        });

        // 6. Aggiorna l'hologramma
        plant.updateHologram();
    }

    /**
     * Fornisce una ricompensa al giocatore in base al tipo specificato.
     * @param player Il giocatore che riceve la ricompensa.
     * @param amount La quantità della ricompensa.
     * @param type Il tipo di ricompensa (es. "TOKEN", "SOLDI").
     */
    private void giveReward(Player player, double amount, String type) {
        if (type == null) {
            Bukkit.getLogger().warning("Tipo di ricompensa nullo per la pianta raccolta da " + player.getName());
            return;
        }

        try {
            BoosterType boosterType = BoosterType.valueOf(type.toUpperCase());
            // Se la conversione riesce, è un booster.
            // 'amount' è il valore (es. moltiplicatore)
            // La durata è fissa a 10 minuti per ora.
            plugin.getBoosterManager().activateBooster(player, boosterType, amount, 10);

        } catch (IllegalArgumentException e) {
            // Se la conversione fallisce, non è un BoosterType valido.
            // Gestisci i vecchi tipi di ricompensa (TOKEN, SOLDI).
            switch (type.toUpperCase()) {
                case "TOKEN":
                    TokenEnchantAPI.getInstance().addTokens(player, amount);
                    player.sendMessage( "§f\t§a+ §2" + Formatter.format(amount) + " §fTokens");
                    break;
                case "SOLDI":
                case "MONEY":
                    RubyIsland.getEconomy().depositPlayer(player, amount);
                    player.sendMessage( "§f\t§a+ §2" + Formatter.format(amount) + " §fSoldi");
                    break;
                default:
                    Bukkit.getLogger().warning("Tipo di ricompensa non riconosciuto: '" + type + "'");
                    break;
            }
        }

        // La logica dei germogli rimane invariata
        RubyCrops.getInstance().getCropsManager().getPlayerProfile(player.getName()).addCrops(1);
        player.sendMessage("§d+1 §fGermoglio!");
    }

    public int getMaxAutomatedPlants(Player player) {
        for (int i = 5; i > 0; i--) {
            if (player.hasPermission("rubyisland.auto.plants." + i)) {
                return i;
            }
        }
        return 0; // Nessun permesso trovato
    }
    public int getActiveAutomatedPlantsCount(UUID playerUuid) {
        int count = 0;
        for (PlacedPlant plant : activePlants.values()) {
            if (plant.getOwnerUuid().equals(playerUuid) && plant.isAutomated()) {
                count++;
            }
        }
        return count;
    }


    public void upgradePlant(Player player, Location location) {
        PlacedPlant plant = activePlants.get(location);
        if (plant == null) return;

        int nextLevel = plant.getUpgradeLevel() + 1;
        PlantUpgrade nextUpgrade = plant.getPlantType().getUpgrade(nextLevel);
        if (nextUpgrade == null) {
            player.sendMessage("§cQuesta pianta è già al livello massimo!");
            return;
        }

        // 1. Controlla se il giocatore soddisfa tutti i requisiti
        if (!canAffordUpgrade(player, nextUpgrade)) {
            return;
        }

        // 2. Esegui l'upgrade e prendi le risorse
        takeRequirements(player, nextUpgrade);
        plant.setUpgradeLevel(nextLevel);

        UpgradePlantEvent event = new UpgradePlantEvent(player.getName(), plant);
        Bukkit.getPluginManager().callEvent(event);

        // 3. Salva e aggiorna
        mySQLManager.saveOrUpdatePlant(plant);
        plant.updateHologram();
        player.sendMessage("§fPianta potenziata al livello §a" + nextLevel + "§f!");
    }

    /**
     * Controlla se un giocatore ha le risorse per un upgrade.
     * Invia un messaggio al giocatore se un requisito non è soddisfatto.
     * @return true se il giocatore può effettuare l'upgrade, altrimenti false.
     */
    private boolean canAffordUpgrade(Player player, PlantUpgrade upgrade) {
        for (String req : upgrade.getRequirements()) {
            String[] parts = req.split(";", 2);
            if (parts.length < 2) continue;

            String type = parts[0].toUpperCase();
            String value = parts[1];

            switch (type) {
                case "MONEY":
                    double originalMoney = NumberUtils.parseBigNumber(value);
                    double requiredMoney = getDiscountedCost(player, originalMoney); // Applica lo sconto
                    double playerMoney = RubyIsland.getEconomy().getBalance(player); // Placeholder
                    if (playerMoney < requiredMoney) {
                        player.sendMessage("§fNon hai abbastanza soldi! Richiesti: §a" + requiredMoney);
                        return false;
                    }
                    break;
                case "CROPS":
                    int originalCrops = (int) NumberUtils.parseBigNumber(value);
                    int requiredCrops = (int) getDiscountedCost(player, originalCrops); // Applica lo sconto
                    int playerCrops = RubyCrops.getInstance().getCropsManager().getPlayerProfile(player.getName()).getCrops(); // Placeholder
                    if (playerCrops < requiredCrops) {
                        player.sendMessage("§fNon hai abbastanza germogli! Richiesti: §c" + requiredCrops);
                        return false;
                    }
                    break;
                case "QUEST":
                    QuestManager questManager = plugin.getQuestManager();
                    PlayerQuestData questData = questManager.getPlayerData(player);
                    if (questData == null || !questData.hasCompletedQuest(value)) {
                        player.sendMessage("§fDevi prima completare la quest: §e" + value);
                        return false;
                    }
                    break;
                default:
                    break;
            }
        }
        return true;
    }

    /**
     * Scala le risorse a un giocatore dopo un upgrade.
     */
    private void takeRequirements(Player player, PlantUpgrade upgrade) {
        for (String req : upgrade.getRequirements()) {
            String[] parts = req.split(";", 2);
            if (parts.length < 2) continue;

            String type = parts[0].toUpperCase();
            String value = parts[1];

            switch (type) {
                case "MONEY":
                    double cost = NumberUtils.parseBigNumber(value);
                    double finalMoneyCost = getDiscountedCost(player, cost); // Applica lo sconto
                    RubyIsland.getEconomy().withdrawPlayer(player, finalMoneyCost);
                    break;
                case "CROPS":
                    int cropCost = (int) NumberUtils.parseBigNumber(value);
                    int finalCropCost = (int) getDiscountedCost(player, cropCost); // Applica lo sconto
                    RubyCrops.getInstance().getCropsManager().getPlayerProfile(player.getName()).removeCrops(finalCropCost);
                    break;
                case "QUEST":
                default:
                    break;
            }
        }
    }

    private double getDiscountedCost(Player player, double originalCost) {
        PlayerPointsManager pointsManager = plugin.getPlayerPointsManager();
        // Ottieni i punti sconto del giocatore
        double discountPoints = pointsManager.getPoints(player, "SCONTO_PIANTE");

        // Calcola la percentuale di sconto (1 punto = 1%), con un massimo del 100%
        double discountPercentage = Math.min(discountPoints, 100.0) / 100.0;

        // Applica lo sconto
        return originalCost * (1.0 - discountPercentage);
    }




    public Map<String, PlantType> getPlantTypes() {
        return plantTypes;
    }

    public Map<Location, PlacedPlant> getActivePlants() {
        return activePlants;
    }
}
