package me.herohd.rubyisland.collectors;

import me.herohd.rubyisland.utils.PercentElementList;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.TreeMap;

public class CollectorType {
    private final String id;
    private ItemStack collectorItem;
    private ItemStack fillableBlock;
    private List<String> hologramLines;
    private PercentElementList<String> weightedCommands;
    private final TreeMap<Integer, CollectorLevel> levels = new TreeMap<>(); // TreeMap per tenere i livelli ordinati

    private String itemName;
    private List<String> itemLore;

    private String permission = null;

    public CollectorType(String id) {
        this.id = id;
        this.weightedCommands = new PercentElementList<>();
    }

    // Getters e Setters
    public String getId() {
        return id;
    }

    public ItemStack getCollectorItem() {
        return collectorItem;
    }

    public void setCollectorItem(ItemStack collectorItem) {
        this.collectorItem = collectorItem;
    }

    public ItemStack getFillableBlock() {
        return fillableBlock;
    }

    public void setFillableBlock(ItemStack fillableBlock) {
        this.fillableBlock = fillableBlock;
    }

    public List<String> getHologramLines() {
        return hologramLines;
    }

    public void setHologramLines(List<String> hologramLines) {
        this.hologramLines = hologramLines;
    }

    public PercentElementList<String> getWeightedCommands() {
        return weightedCommands;
    }

    public void addWeightCommand(String a, int b) {
        weightedCommands.add(a, b);
    }

    public String getRandomCommand() {
        return weightedCommands.next();
    }

    public TreeMap<Integer, CollectorLevel> getLevels() {
        return levels;
    }

    public void addLevel(int level, CollectorLevel levelData) {
        this.levels.put(level, levelData);
    }
    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public List<String> getItemLore() {
        return itemLore;
    }

    public void setItemLore(List<String> itemLore) {
        this.itemLore = itemLore;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public String getPermission() {
        return permission;
    }
}
