package me.herohd.rubyisland.listener;

import me.herohd.rubyisland.collectors.CollectorManager;
import me.herohd.rubyisland.manager.PlayerPointsManager;
import me.herohd.rubyisland.plants.TropicalPlantManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerCacheListener implements Listener {

    private final TropicalPlantManager plantManager;
    private final CollectorManager collectorManager;
    private final PlayerPointsManager pointsManager; // AGGIUNTO

    public PlayerCacheListener(TropicalPlantManager plantManager, CollectorManager collectorManager, PlayerPointsManager playerPointsManager) {
        this.plantManager = plantManager;
        this.collectorManager = collectorManager;
        this.pointsManager = playerPointsManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // Carica sia le piante che i collector
        plantManager.loadPlantsForPlayer(player);
        collectorManager.loadCollectorsForPlayer(player);
        pointsManager.loadPlayerData(player); // AGGIUNTO
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        // Scarica sia le piante che i collector
        plantManager.unloadPlantsForPlayer(player);
        collectorManager.unloadCollectorsForPlayer(player);
        pointsManager.saveAndUnloadPlayerData(player); // AGGIUNTO
    }
}