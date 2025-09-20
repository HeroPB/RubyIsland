package me.herohd.rubyisland.quests; // Assicurati che il package sia corretto

import me.herohd.rubyisland.RubyIsland;
import me.herohd.rubyisland.manager.MySQLManager;
import me.herohd.rubyisland.utils.SkullCreator;
import me.herohd.rubyisland.utils.Utils;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;

public class QuestManager {

    private final RubyIsland plugin;
    private final MySQLManager mysqlManager; // Gestore del database

    private final Map<String, Quest> loadedQuests = new HashMap<>();
    private final List<String> questOrder = new ArrayList<>();
    private final Map<UUID, PlayerQuestData> playerData = new HashMap<>(); // Cache in memoria dei dati dei giocatori online
    private final Map<String, IQuestHandler> questHandlers = new HashMap<>();

    public QuestManager(RubyIsland plugin, MySQLManager mysqlManager) {
        this.plugin = plugin;
        this.mysqlManager = mysqlManager;
    }

    /**
     * Carica tutte le configurazioni delle quest dai file .yml.
     */
    public void loadQuests() {
        loadedQuests.clear();
        questOrder.clear();
        questOrder.addAll(plugin.getConfig().getStringList("quest-system.order"));
        if (questOrder.isEmpty()) {
            plugin.getLogger().warning("Nessun ordine di quest trovato in config.yml!");
            return;
        }

        File questsFolder = new File(plugin.getDataFolder(), "quests");
        if (!questsFolder.exists()) {
            questsFolder.mkdirs();
        }

        File[] questFiles = questsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (questFiles == null) return;
        for (File questFile : questFiles) {
            String fileId = questFile.getName().replace(".yml", "");
            FileConfiguration questConfig = YamlConfiguration.loadConfiguration(questFile);
            try {
                Quest quest = new Quest(fileId);
                quest.setQuestName(questConfig.getString("QUEST_NAME", "Quest senza nome"));
                quest.setQuestType(questConfig.getString("QUEST_TYPE", "").toUpperCase());
                quest.setGuiDisplayName(questConfig.getString("GUI.QUEST_DISPLAYNAME"));
                quest.setGuiLore(questConfig.getStringList("GUI.QUEST_LORE"));
                quest.setMessageComplete(questConfig.getString("MESSAGE.QUEST_COMPLETE"));
                quest.setMessageStarted(questConfig.getString("MESSAGE.QUEST_STARTED"));
                quest.setMessageProgress(questConfig.getString("MESSAGE.PROGRESS_UPDATE"));
                quest.setRequireTarget(questConfig.getStringList("REQUIRE_TARGET"));
                quest.setRequireAmount(questConfig.getInt("REQUIRE_AMOUNT", 1));
                quest.setRewards(questConfig.getStringList("REWARD"));
                // --- NUOVA LOGICA PER CARICARE L'ITEM ---
                String itemString = questConfig.getString("GUI.QUEST_ITEM", "STONE");
                String[] parts = itemString.split(";", 2);
                Material material = Material.getMaterial(parts[0].toUpperCase());
                short data = (parts.length > 1) ? Short.parseShort(parts[1]) : 0;

                ItemStack guiItem = new ItemStack(material, 1, data);
                if ((material == Material.SKULL || material == Material.SKULL_ITEM) && data == 3 && questConfig.contains("GUI.QUEST_META")) {
                    guiItem = SkullCreator.itemFromBase64(questConfig.getString("GUI.QUEST_META"));
                }
                quest.setGuiItem(guiItem);
                // --- FINE NUOVA LOGICA ---
                loadedQuests.put(fileId, quest);
            } catch (Exception e) {
                plugin.getLogger().severe("Errore caricando la quest: " + questFile.getName());
                e.printStackTrace();
            }
        }
    }

    /**
     * Registra un handler per un tipo di quest, permettendo l'estensione tramite API.
     */
    public void registerQuestHandler(IQuestHandler handler) {
        if (handler == null || handler.getQuestType() == null || handler.getQuestType().isEmpty()) {
            plugin.getLogger().warning("Tentativo di registrare un handler di quest non valido.");
            return;
        }
        String type = handler.getQuestType().toUpperCase();
        if (questHandlers.containsKey(type)) {
            plugin.getLogger().warning("Handler per quest '" + type + "' già registrato. Ignorando.");
            return;
        }
        questHandlers.put(type, handler);
    }

    /**
     * Delega un evento all'handler corretto per la quest attiva del giocatore.
     */
    public void handleEvent(Event event, Player player) {
        PlayerQuestData data = getPlayerData(player);
        if (data == null || data.getActiveQuestId() == null) return;
        Quest activeQuest = getQuestById(data.getActiveQuestId());
        if (activeQuest == null || activeQuest.getQuestType().isEmpty()) return;
        IQuestHandler handler = questHandlers.get(activeQuest.getQuestType().toUpperCase());
        if (handler != null) {
            handler.handleEvent(event, player, activeQuest, data, this);
        }
    }

    /**
     * Incrementa il progresso di una quest per un giocatore.
     * Non salva più su DB a ogni chiamata per ottimizzare le performance.
     */
    public void progressQuest(Player player, double amount) {
        PlayerQuestData data = getPlayerData(player);
        if (data == null || data.getActiveQuestId() == null) return;
        Quest activeQuest = getQuestById(data.getActiveQuestId());
        if (activeQuest == null) return;

        data.incrementProgress(amount);

        if (data.getProgress() >= activeQuest.getRequireAmount()) {
            completeQuest(player);
        }
    }

    /**
     * Completa la quest attiva di un giocatore, assegna la successiva e salva i dati.
     */
    public void completeQuest(Player player) {
        PlayerQuestData data = getPlayerData(player);
        if (data == null || data.getActiveQuestId() == null) return;

        Quest completedQuest = getQuestById(data.getActiveQuestId());
        if (completedQuest == null) return;

        player.sendMessage(Utils.colora(completedQuest.getMessageComplete().replace("%rubyisland_quest_name%", completedQuest.getQuestName())));
        completedQuest.getRewards().forEach(command ->
                plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command.replace("%player%", player.getName()))
        );

        // 1. Aggiungi la quest allo storico del giocatore (in memoria e su DB)
        data.addCompletedQuest(completedQuest.getFileId());
        mysqlManager.addCompletedQuestToHistory(player.getUniqueId(), completedQuest.getFileId());

        // 2. Determina la prossima quest
        int currentIndex = questOrder.indexOf(data.getActiveQuestId());
        if (currentIndex != -1 && currentIndex + 1 < questOrder.size()) {
            String nextQuestId = questOrder.get(currentIndex + 1);
            data.setActiveQuestId(nextQuestId);
            data.setProgress(0);
            Quest newQuest = getQuestById(nextQuestId);
            if (newQuest != null) {
                player.sendMessage(Utils.colora(newQuest.getMessageStarted().replace("%rubyisland_quest_name%", newQuest.getQuestName())));
            }
        } else {
            data.setActiveQuestId(null);
            data.setProgress(0);
            player.sendMessage("Hai completato tutte le quest disponibili!");
        }

        // 3. Salva il nuovo stato del giocatore (nuova quest attiva, progresso a 0)
        mysqlManager.savePlayerQuestStatus(data);
    }

    // --- METODI DI GESTIONE DATI ---

    /**
     * Carica i dati di un giocatore dal DB quando entra nel server.
     * Se non esistono, crea e salva un nuovo profilo.
     */
    public void loadPlayerData(Player player) {
        if(playerData.containsKey(player.getUniqueId())) return;
        PlayerQuestData loadedData = mysqlManager.loadPlayerQuestData(player.getUniqueId());
        if (loadedData != null) {
            playerData.put(player.getUniqueId(), loadedData);
        } else {
            PlayerQuestData newData = createNewPlayerData(player.getUniqueId());
            playerData.put(player.getUniqueId(), newData);
            mysqlManager.savePlayerQuestStatus(newData);
        }
    }

    /**
     * Salva i dati di un giocatore su DB quando esce dal server e li rimuove dalla cache.
     */
    public void unloadPlayerData(Player player) {
        PlayerQuestData dataToSave = playerData.remove(player.getUniqueId());
        if (dataToSave != null) {
            mysqlManager.savePlayerQuestStatus(dataToSave);
        }
    }

    /**
     * Ottiene i dati di un giocatore dalla cache in memoria.
     * Usato internamente per i giocatori online.
     */
    public PlayerQuestData getPlayerData(Player player) {
        return playerData.get(player.getUniqueId());
    }

    /**
     * Crea un nuovo oggetto PlayerQuestData per un nuovo giocatore.
     */
    private PlayerQuestData createNewPlayerData(UUID uuid) {
        String firstQuestId = questOrder.isEmpty() ? null : questOrder.get(0);
        return new PlayerQuestData(uuid, firstQuestId);
    }

    /**
     * Ottiene un oggetto Quest dalla sua configurazione caricata.
     */
    public Quest getQuestById(String id) {
        return loadedQuests.get(id);
    }

    /**
     * Restituisce la mappa dei dati dei giocatori online. Utile per l'auto-saver.
     * @return Una collezione non modificabile dei dati dei giocatori.
     */
    public Collection<PlayerQuestData> getOnlinePlayerData() {
        return Collections.unmodifiableCollection(playerData.values());
    }

    public void saveAllOnlinePlayersData() {
        if (playerData.isEmpty()) {
            return; // Nessun giocatore online, nessuna azione richiesta.
        }

        plugin.getLogger().info("Salvataggio dei dati delle quest per " + playerData.size() + " giocatori online...");
        for (PlayerQuestData data : playerData.values()) {
            mysqlManager.savePlayerQuestStatus(data);
        }
        plugin.getLogger().info("Salvataggio dati quest completato.");
    }


    public Map<String, Quest> getLoadedQuests() {
        return loadedQuests;
    }
}