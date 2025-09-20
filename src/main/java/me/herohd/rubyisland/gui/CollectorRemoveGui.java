package me.herohd.rubyisland.gui;

import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import me.herohd.rubyisland.RubyIsland;
import me.herohd.rubyisland.collectors.PlacedCollector;
import me.herohd.rubyisland.utils.Config;
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

public class CollectorRemoveGui {

    private final Gui gui;
    private final Player player;
    private final PlacedCollector collector;
    private final Config config;
    private final RubyIsland plugin = RubyIsland.getInstance();

    public CollectorRemoveGui(Player player, PlacedCollector collector) {
        this.player = player;
        this.collector = collector;
        this.config = new Config(plugin, "collector-gui");

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
        createAndSetItem("confirm-remove");
        fillGui();
    }

    private void createAndSetItem(String key) {
        ConfigurationSection section = config.getSection(key);
        if (section == null) return;

        ItemStack item = createItemFromConfig(section);
        if (item == null) return;

        GuiItem guiItem = new GuiItem(item);

        // Imposta l'azione del click
        guiItem.setAction(event -> {
            if (key.equals("confirm-remove")) {
                if(collector.isPicked()) return;
                collector.pickUp(player);
                gui.close(player);
            }
        });

        gui.setItem(section.getInt("slot"), guiItem);
    }

    private ItemStack createItemFromConfig(ConfigurationSection section) {
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
        String[] fillerData = config.getString("filler-item").split(";");
        Material material = Material.getMaterial(fillerData[0]);
        if (material == null) material = Material.STAINED_GLASS_PANE;
        short data = (fillerData.length > 1) ? Short.parseShort(fillerData[1]) : 15;
        gui.getFiller().fill(new GuiItem(new ItemStack(material, 1, data)));
    }

    private void open() {
        gui.open(player);
    }
}
