package me.herohd.rubyisland.plants;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import me.herohd.rubyisland.RubyIsland;
import me.herohd.rubyisland.manager.MySQLManager;
import me.herohd.rubyisland.utils.NBTEditor;
import me.herohd.rubyisland.utils.TimeUtils;
import me.herohd.rubyisland.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PlacedPlant {
    private final Location location;
    private final UUID ownerUuid;
    private final PlantType plantType;

    private int upgradeLevel;
    private int currentPhaseIndex;
    private long nextGrowthTimestamp;
    private Hologram hologram;
    private boolean isAutomated; // <-- NUOVO CAMPO

    public PlacedPlant(Location location, UUID ownerUuid, PlantType plantType) {
        this.location = location;
        this.ownerUuid = ownerUuid;
        this.plantType = plantType;
        this.upgradeLevel = 1;
        this.currentPhaseIndex = 1;
        this.isAutomated = false; // Di default non è automatizzata
        hologram = DHAPI.createHologram(UUID.randomUUID().toString(), Utils.getBlockCenter(location, 4));
    }

    // Getter e Setter per il nuovo campo
    public boolean isAutomated() {
        return isAutomated;
    }

    public void setAutomated(boolean automated) {
        isAutomated = automated;
    }

    /**
     * Aggiorna le linee dell'ologramma della pianta in base al suo stato attuale.
     */
    public void updateHologram() {
        if (hologram == null) {
            return;
        }

        List<String> originalLines = plantType.getHologramLines();

        String timeRemaining;
        if (isReadyToHarvest()) {
            timeRemaining = "§aPronta per il Raccolto!";
        } else if (nextGrowthTimestamp == Long.MAX_VALUE) {
            // FIX: Gestisce il caso in cui la crescita non è basata sul tempo
            timeRemaining = "§7N/D"; // Puoi cambiarlo in "In Attesa", etc.
        } else {
            long remainingMillis = nextGrowthTimestamp - System.currentTimeMillis();
            timeRemaining = TimeUtils.formatTime(remainingMillis);
        }

        // Sostituisce i placeholder con i valori attuali
        List<String> updatedLines = originalLines.stream()
                .map(line -> line
                        .replace("%upgrade%", String.valueOf(upgradeLevel))
                        .replace("%phase%", String.valueOf(currentPhaseIndex))
                        .replace("%max%", String.valueOf(plantType.getPhases().size()))
                        .replace("%grow%", timeRemaining))
                .collect(Collectors.toList());

        DHAPI.setHologramLines(hologram, updatedLines);
    }

    public void pickUp(Player player) {
        // 1. Crea l'item da dare al giocatore
        ItemStack plantItem = this.plantType.getPlantItem().clone();

        // 2. Salva il livello di upgrade corrente nell'NBT dell'item
        plantItem = NBTEditor.addNBT(plantItem, "upgrade_level", this.getUpgradeLevel());

        ItemMeta meta = plantItem.getItemMeta();
        List<String> firstLore = meta.getLore();
        List<String> result = new ArrayList<>();

        for (String s : firstLore) {
            result.add(s.replaceAll("%level%", String.valueOf(this.getUpgradeLevel())));
        }

        meta.setLore(result);
        plantItem.setItemMeta(meta);

        // 3. Pulisci il mondo
        // Rimuovi l'hologramma
        if (this.hologram != null) {
            this.hologram.delete();
        }
        // Rimuovi dalla mappa delle piante attive
        TropicalPlantManager plantManager = RubyIsland.getInstance().getTropicalPlantManager();
        if(plantManager.getActivePlants().getOrDefault(this.location, null) == null) return;
        plantManager.getActivePlants().remove(this.location);

        // Rimuovi dal database (asincrono)
        MySQLManager mySQLManager = RubyIsland.getInstance().getMySQLManager();
        Bukkit.getScheduler().runTaskAsynchronously(RubyIsland.getInstance(), () -> {
            mySQLManager.removePlant(this.location);
        });

        // Rimuovi il blocco fisico
        this.location.getBlock().setType(Material.AIR);

        // 4. Dai l'item al giocatore
        if (player.getInventory().firstEmpty() == -1) {
            // Se l'inventario è pieno, fai cadere l'item
            player.getWorld().dropItemNaturally(player.getLocation(), plantItem);
            player.sendMessage("§cIl tuo inventario è pieno! La pianta è stata lasciata a terra.");
        } else {
            player.getInventory().addItem(plantItem);
            player.sendMessage("§aHai raccolto la tua pianta.");
        }
    }


    public boolean isReadyToHarvest() {
        return plantType.isFinalPhase(currentPhaseIndex);
    }

    public Location getLocation() { return location; }
    public UUID getOwnerUuid() { return ownerUuid; }
    public PlantType getPlantType() { return plantType; }
    public int getUpgradeLevel() { return upgradeLevel; }
    public void setUpgradeLevel(int upgradeLevel) { this.upgradeLevel = upgradeLevel; }
    public int getCurrentPhaseIndex() { return currentPhaseIndex; }
    public void setCurrentPhaseIndex(int currentPhaseIndex) { this.currentPhaseIndex = currentPhaseIndex; }
    public long getNextGrowthTimestamp() { return nextGrowthTimestamp; }
    public void setNextGrowthTimestamp(long nextGrowthTimestamp) { this.nextGrowthTimestamp = nextGrowthTimestamp; }
    public Hologram getHologram() { return hologram; }
    public void setHologram(Hologram hologram) { this.hologram = hologram; }
}