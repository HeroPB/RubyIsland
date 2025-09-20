package me.herohd.rubyisland.utils.hook;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.herohd.rubyisland.RubyIsland;
import me.herohd.rubyisland.collectors.CollectorManager;
import me.herohd.rubyisland.collectors.PlacedCollector;
import me.herohd.rubyisland.manager.PlayerPointsManager;
import me.herohd.rubyisland.quests.PlayerQuestData;
import me.herohd.rubyisland.quests.Quest;
import me.herohd.rubyisland.quests.QuestManager;
import me.herohd.rubyisland.utils.Formatter;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class Placeholder extends PlaceholderExpansion {

    private final RubyIsland plugin;

    public Placeholder(RubyIsland plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "rubyisland"; // Questo sarà il prefisso, es. %rubyisland_...%
    }

    @Override
    public @NotNull String getAuthor() {
        return "H3r0HD_";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true; // Mantiene l'espansione registrata
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
        if (offlinePlayer == null) {
            return null;
        }

        // --- PLACEHOLDER PER PUNTI ---
        // Uso: %rubyisland_points_<tipo>% es. %rubyisland_points_economy%
        if (params.startsWith("points_")) {
            Player player = offlinePlayer.getPlayer();
            if (player == null) return "0"; // I punti sono gestiti in cache solo per giocatori online

            PlayerPointsManager pointsManager = plugin.getPlayerPointsManager();
            String pointType = params.substring("points_".length()).toUpperCase();

            if (pointsManager.getValidPointTypes().contains(pointType)) {
                double points = pointsManager.getPoints(player, pointType);
                return Formatter.format(points);
            }
        }

        // Uso: %rubyisland_collectors_blocks_<id_collector>%
        if (params.startsWith("collectors_blocks_")) {
            Player player = offlinePlayer.getPlayer();
            if (player == null) return "0"; // Funziona solo per giocatori online

            CollectorManager collectorManager = plugin.getCollectorManager();
            String collectorId = params.substring("collectors_blocks_".length());
            long totalAmount = 0;

            for (PlacedCollector collector : collectorManager.activeCollectors.values()) {
                if (collector.getOwnerUuid().equals(player.getUniqueId()) && collector.getCollectorType().getId().equalsIgnoreCase(collectorId)) {
                    totalAmount += collector.getAmount();
                }
            }
            return Formatter.format(totalAmount);
        }

        // --- PLACEHOLDER PER QUEST ---
        if (params.startsWith("quest_")) {
            Player player = offlinePlayer.getPlayer();
            if (player == null) return "N/D"; // Le quest sono gestite solo per giocatori online

            QuestManager questManager = plugin.getQuestManager();
            PlayerQuestData data = questManager.getPlayerData(player);
            if (data == null || data.getActiveQuestId() == null) {
                return "Nessuna";
            }
            Quest activeQuest = questManager.getQuestById(data.getActiveQuestId());
            if (activeQuest == null) {
                return "Nessuna";
            }

            switch (params) {
                case "quest_name":
                    return ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', activeQuest.getGuiDisplayName()));
                case "quest_progress":
                    return String.valueOf(data.getProgress());
                case "quest_required":
                    return String.valueOf(activeQuest.getRequireAmount());
                case "quest_progress_formatted":
                    return Formatter.format(data.getProgress()) + "/" + Formatter.format(activeQuest.getRequireAmount());
            }
        }

        return null; // Placeholder non trovato
    }
}