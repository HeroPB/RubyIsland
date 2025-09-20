package me.herohd.rubyisland.quests.handlers;

import me.clip.autosell.events.SellEvent;
import me.herohd.rubyisland.event.CollectorBreakEvent;
import me.herohd.rubyisland.event.CollectorFillEvent;
import me.herohd.rubyisland.event.UpgradePlantEvent;
import me.herohd.rubyisland.quests.QuestManager;
import me.kr1s_d.rubycrates.api.events.CrateBulkOpenEvent;
import me.kr1s_d.rubycrates.api.events.CrateOpenEvent;
import me.kr1s_d.rubyenchantmanager.events.WEnchantGainObjectEvent;
import me.kr1s_d.rubyenchantmanager.events.WMultipleBlockBreakEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class QuestGlobalListener implements Listener {

    private final QuestManager questManager;

    public QuestGlobalListener(QuestManager questManager) {
        this.questManager = questManager;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onWBlockBreak(WMultipleBlockBreakEvent event) {
        Player player = event.getPlayer();
        questManager.handleEvent(event, player);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        questManager.handleEvent(event, player);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onCratesOpen(CrateOpenEvent event) {
        Player player = event.getOpener();
        questManager.handleEvent(event, player);
    }
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onCratesBulkOpen(CrateBulkOpenEvent event) {
        Player player = event.getOpener();
        questManager.handleEvent(event, player);
    }
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onBlockBlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        questManager.handleEvent(event, player);
    }
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onBreakCollector(CollectorBreakEvent event) {
        Player player = Bukkit.getPlayer(event.getPlayer());
        questManager.handleEvent(event, player);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onFillCollector(CollectorFillEvent event) {
        Player player = Bukkit.getPlayer(event.getPlayer());
        questManager.handleEvent(event, player);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onEconomyGain(WEnchantGainObjectEvent event) {
        Player player = event.getPlayer();
        questManager.handleEvent(event, player);
    }
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onUpgrade(UpgradePlantEvent event) {
        Player player = Bukkit.getPlayer(event.getPlayerName());
        if(player == null) return;
        questManager.handleEvent(event, player);
    }
}