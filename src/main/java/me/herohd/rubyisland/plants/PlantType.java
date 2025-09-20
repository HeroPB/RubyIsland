package me.herohd.rubyisland.plants;

import org.bukkit.inventory.ItemStack;
import java.util.List;
import java.util.Map;

public class PlantType {
    private final String id;
    private ItemStack plantItem;
    private List<String> hologramLines;
    private Map<Integer, PlantPhase> phases;
    private Map<Integer, PlantUpgrade> upgrades;
    private int sellPrice; // Corrisponde a sell-plants

    public PlantType(String id) { this.id = id; }
    // Setter per popolare l'oggetto durante il caricamento dal file YML
    public void setPlantItem(ItemStack plantItem) { this.plantItem = plantItem; }
    public void setHologramLines(List<String> hologramLines) { this.hologramLines = hologramLines; }
    public void setPhases(Map<Integer, PlantPhase> phases) { this.phases = phases; }
    public void setUpgrades(Map<Integer, PlantUpgrade> upgrades) { this.upgrades = upgrades; }
    public void setSellPrice(int sellPrice) { this.sellPrice = sellPrice; }

    public PlantPhase getPhase(int index) { return phases.get(index); }
    public PlantUpgrade getUpgrade(int level) { return upgrades.get(level); }
    public boolean isFinalPhase(int index) { return index == phases.size(); }

    public String getId() {
        return id;
    }

    public ItemStack getPlantItem() {
        return plantItem;
    }

    public List<String> getHologramLines() {
        return hologramLines;
    }

    public Map<Integer, PlantPhase> getPhases() {
        return phases;
    }

    public Map<Integer, PlantUpgrade> getUpgrades() {
        return upgrades;
    }

    public int getSellPrice() {
        return sellPrice;
    }
}