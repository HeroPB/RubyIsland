package me.herohd.rubyisland.gui;

import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import me.herohd.rubycrops.utils.Config;
import me.herohd.rubyisland.RubyIsland;
import me.herohd.rubyisland.quests.PlayerQuestData;
import me.herohd.rubyisland.quests.Quest;
import me.herohd.rubyisland.quests.QuestManager;
import me.herohd.rubyisland.utils.SkullCreator;
import me.herohd.rubyisland.utils.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.stream.Collectors;

public class QuestGui {

    private final Gui gui;
    private final Player player;
    private final Config config;
    private final RubyIsland plugin = RubyIsland.getInstance();
    private final QuestManager questManager = plugin.getQuestManager();
    private final PlayerQuestData playerData;

    public QuestGui(Player player) {
        this.player = player;
        this.playerData = questManager.getPlayerData(player);
        this.config = new Config(plugin, "quest-gui"); // Assumendo un metodo per caricare config specifiche

        String title = Utils.colora(config.getString("title"));
        this.gui = Gui.gui()
                .rows(config.getInt("size"))
                .title(Component.text(title))
                .disableAllInteractions()
                .create();

        build();
        open();
    }

    private void build() {
        String activeQuestId = playerData.getActiveQuestId();

        if (activeQuestId == null) {
            // Tutte le quest sono state completate
            buildCompletedItem();
        } else {
            // C'è una quest attiva
            buildActiveQuestItem(activeQuestId);
        }

        fillGui();
    }

    private void buildActiveQuestItem(String questId) {
        Quest quest = questManager.getQuestById(questId);
        if (quest == null) {
            buildCompletedItem(); // Fallback se la quest non viene trovata
            return;
        }

        ConfigurationSection section = config.getSection("quest");
        if (section == null) return;

        // Prendi l'item base dalla configurazione della quest
        ItemStack item = quest.getGuiItem().clone();
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            // Imposta nome e lore dalla config della quest, sostituendo i placeholder
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', quest.getGuiDisplayName()));
            List<String> lore = quest.getGuiLore().stream()
                    .map(line -> ChatColor.translateAlternateColorCodes('&', line)
                            .replace("%rubyisland_quest_progress%", String.valueOf(playerData.getProgress()))
                            .replace("%rubyisland_quest_require%", String.valueOf(quest.getRequireAmount()))
                    ).collect(Collectors.toList());
            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        GuiItem guiItem = new GuiItem(item);
        guiItem.setAction(event -> gui.close(player)); // Il click non fa nulla
        gui.setItem(section.getInt("slot"), guiItem);
    }

    private void buildCompletedItem() {
        ConfigurationSection section = config.getSection("all-quest-complete");
        if (section == null) return;

        ItemStack item = createItemFromConfig(section);
        if (item == null) return;

        GuiItem guiItem = new GuiItem(item);
        guiItem.setAction(event -> gui.close(player));
        gui.setItem(section.getInt("slot"), guiItem);
    }

    private ItemStack createItemFromConfig(ConfigurationSection section) {
        // Metodo di supporto per creare item dalla config della GUI (come per le piante)
        String materialString = section.getString("material");
        String[] parts = materialString.split(";", 2);
        Material material = Material.getMaterial(parts[0]);
        if (material == null) return null;

        short data = (parts.length > 1) ? Short.parseShort(parts[1]) : 0;
        ItemStack item = new ItemStack(material, 1, data);

        if ((material == Material.SKULL || material == Material.SKULL_ITEM) && data == 3 && section.contains("meta")) {
            item = SkullCreator.itemFromBase64(section.getString("meta"));
        }

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', section.getString("name", "")));
            List<String> lore = section.getStringList("lore").stream()
                    .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                    .collect(Collectors.toList());
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private void fillGui() {
        // ... (identico alla GUI delle piante) ...
    }

    private void open() {
        gui.open(player);
    }
}