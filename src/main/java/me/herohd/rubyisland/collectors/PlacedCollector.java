package me.herohd.rubyisland.collectors;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import me.herohd.rubyisland.RubyIsland;
import me.herohd.rubyisland.manager.MySQLManager;
import me.herohd.rubyisland.utils.NBTEditor;
import me.herohd.rubyisland.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

public class PlacedCollector {
    private final Location location;
    private final UUID ownerUuid;
    private final CollectorType collectorType;
    private double amount;
    private Hologram hologram;
    private boolean picked = false;

    public PlacedCollector(Location location, UUID ownerUuid, CollectorType collectorType, double initialAmount) {
        this.location = location;
        this.ownerUuid = ownerUuid;
        this.collectorType = collectorType;
        this.amount = initialAmount;
    }

    public void updateHologram(CollectorManager manager) {
        if (hologram == null) return;

        int playerLevel = manager.getPlayerLevel(Bukkit.getPlayer(ownerUuid), collectorType.getId());
        CollectorLevel levelData = collectorType.getLevels().get(playerLevel);
        long maxCapacity = (levelData != null) ? levelData.getMaxCapacity() : 0;

        List<String> updatedLines = collectorType.getHologramLines().stream()
                .map(line -> line.replace("%amount%", NumberFormat.getInstance(Locale.US).format(this.amount))
                        .replace("%max_capacity%", NumberFormat.getInstance(Locale.US).format(maxCapacity)))
                .collect(Collectors.toList());

        DHAPI.setHologramLines(hologram, updatedLines);
    }

    public void pickUp(Player player) {
        picked = true;
        ItemStack collectorItem = this.collectorType.getCollectorItem().clone();
        ItemMeta meta = collectorItem.getItemMeta();

        if (meta != null) {
            // Imposta il nome
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', this.collectorType.getItemName()));

            // Imposta la lore, sostituendo i placeholder
            List<String> lore = this.collectorType.getItemLore().stream()
                    .map(line -> ChatColor.translateAlternateColorCodes('&', line)
                            .replace("%owner%", Bukkit.getOfflinePlayer(this.ownerUuid).getName())
                            .replace("%blocks%", NumberFormat.getInstance(Locale.US).format(this.amount)))
                    .collect(Collectors.toList());
            meta.setLore(lore);
            collectorItem.setItemMeta(meta);
        }

        collectorItem = NBTEditor.addNBT(collectorItem, "collector-id", this.collectorType.getId());
        collectorItem = NBTEditor.addNBT(collectorItem, "collector-owner", this.ownerUuid.toString());
        collectorItem = NBTEditor.addNBT(collectorItem, "collector-amount", this.amount);

        // Pulisci il mondo
        if (this.hologram != null) this.hologram.delete();
        RubyIsland.getInstance().getCollectorManager().activeCollectors.remove(this.location);
        MySQLManager mySQLManager = RubyIsland.getInstance().getMySQLManager();
        Bukkit.getScheduler().runTaskAsynchronously(RubyIsland.getInstance(), () -> mySQLManager.removeCollector(this.location));
        this.location.getBlock().setType(Material.AIR);

        // Dai l'item
        if (player.getInventory().firstEmpty() == -1) {
            player.getWorld().dropItemNaturally(player.getLocation(), collectorItem);
        } else {
            player.getInventory().addItem(collectorItem);
        }
    }

    // Getters e Setters
    public Location getLocation() { return location; }
    public UUID getOwnerUuid() { return ownerUuid; }
    public CollectorType getCollectorType() { return collectorType; }
    public double getAmount() { return amount; }
    public void setAmount(long amount) { this.amount = amount; }
    public void addAmount(long amountToAdd) { this.amount += amountToAdd; }
    public Hologram getHologram() { return hologram; }
    public void setHologram(Hologram hologram) { this.hologram = hologram; }

    public boolean isPicked() {
        return picked;
    }
}