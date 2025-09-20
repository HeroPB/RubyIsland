package me.herohd.rubyisland.listener;

import me.herohd.rubyisland.collectors.CollectorManager;
import me.herohd.rubyisland.gui.CollectorRemoveGui;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;


import me.herohd.rubyisland.collectors.CollectorType;
import me.herohd.rubyisland.collectors.PlacedCollector;
import me.herohd.rubyisland.utils.IslandUtils;
import me.herohd.rubyisland.utils.NBTEditor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import static com.comphenix.protocol.events.ListenerPriority.HIGHEST;

public class CollectorListener implements Listener {
    private final CollectorManager collectorManager;

    public CollectorListener(CollectorManager collectorManager) {
        this.collectorManager = collectorManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCollectorPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        ItemStack itemInHand = event.getItemInHand();

        // 1. Controlla se l'item è un collector
        String collectorId = NBTEditor.getNBT(itemInHand, "collector-id");
        if (collectorId == null || collectorId.isEmpty()) {
            return;
        }

        // --- NUOVA LOGICA DI OWNERSHIP ---
        // 2. Controlla se l'item ha un proprietario designato
        String ownerUuid = NBTEditor.getNBT(itemInHand, "collector-owner");
        if (ownerUuid == null || ownerUuid.isEmpty()) {
            event.setCancelled(true);
            player.sendMessage("§cQuesto item collector è corrotto e non ha un proprietario. Contatta un admin.");
            return;
        }

        // 3. Controlla se il giocatore che piazza è il proprietario dell'item
        if (!player.getUniqueId().toString().equals(ownerUuid)) {
            event.setCancelled(true);
            player.sendMessage("§fNon sei il proprietario di questo §ccollector §fe non puoi piazzarlo.");
            return;
        }
        // --- FINE LOGICA OWNERSHIP ---

        // 4. Controlla se il giocatore è nella sua isola
        if (!IslandUtils.isInHisIsland(player, event.getBlock().getLocation())) {
            event.setCancelled(true);
            player.sendMessage("§fPuoi piazzare i collector solo nella tua §cisola§f!");
            return;
        }

        // Tutti i controlli sono passati, procedi con il piazzamento custom

        CollectorType type = collectorManager.getCollectorTypes().get(collectorId);
        if (type == null) {
            player.sendMessage("§cTipo di collector non valido. Contatta un admin.");
            return;
        }

        if(type.getPermission() != null && !player.hasPermission(type.getPermission())) {
            player.sendMessage("§cNon puoi utilizzare questo collector!");
            return;
        }

        // Leggi la quantità iniziale dall'NBT (se presente)
        double initialAmount = NBTEditor.getNBTDouble(itemInHand, "collector-amount");

        collectorManager.placeCollector(player, event.getBlock().getLocation(), type, initialAmount);
    }

    @EventHandler(ignoreCancelled = true)
    public void onCollectorInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null) {
            return;
        }

        Location location = event.getClickedBlock().getLocation();
        PlacedCollector collector = collectorManager.getActiveCollectors().get(location);
        if (collector == null) return;

        event.setCancelled(true);
        Player player = event.getPlayer();

        if (!collector.getOwnerUuid().equals(player.getUniqueId())) {
            player.sendMessage("§cPuoi interagire solo con i tuoi collector!");
            return;
        }

        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        ItemStack fillableItem = collector.getCollectorType().getFillableBlock();

        // CASO 1: Il giocatore sta cercando di RIEMPIRE il collector
        if (itemInHand != null && itemInHand.isSimilar(fillableItem)) {
            collectorManager.fillCollector(player, collector, player.isSneaking());
        }
        // CASO 2: Il giocatore vuole RIMUOVERE il collector (mano vuota, non sneak)
        else if ((itemInHand == null || itemInHand.getType() == Material.AIR) && !player.isSneaking()) {
            new CollectorRemoveGui(player, collector);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onCollectorBreak(BlockBreakEvent event) {
        Location location = event.getBlock().getLocation();
        PlacedCollector collector = collectorManager.getActiveCollectors().get(location);
        if (collector == null) return;

        event.setCancelled(true);
        Player player = event.getPlayer();

        // Controlla se il giocatore è il proprietario
        if (!collector.getOwnerUuid().equals(player.getUniqueId())) {
            player.sendMessage("§e§lISOLOTTO§8: §fPuoi rompere solo i tuoi collector!");
            return;
        }

        collectorManager.breakCollector(player, collector);
    }
}