package me.herohd.rubyisland.gui;

import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import me.herohd.rubyisland.RubyIsland;
import me.herohd.rubyisland.manager.PlayerPointsManager;
import me.herohd.rubyisland.plants.PlacedPlant;
import me.herohd.rubyisland.plants.PlantUpgrade;
import me.herohd.rubyisland.plants.TropicalPlantManager;
import me.herohd.rubyisland.utils.Config;
import me.herohd.rubyisland.utils.Formatter;
import me.herohd.rubyisland.utils.NumberUtils;
import me.herohd.rubyisland.utils.SkullCreator;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MainGui implements IGui {

    private final Gui gui;
    private final Player player;
    private final PlacedPlant plant;
    private final Config config;
    private final RubyIsland plugin = RubyIsland.getInstance();

    public MainGui(Player player, PlacedPlant plant) {
        this.player = player;
        this.plant = plant;
        this.config = new Config(plugin, "plant-gui");

        String title = ChatColor.translateAlternateColorCodes('&', config.getString("title"));
        this.gui = Gui.gui()
                .rows(config.getInt("size"))
                .title(Component.text(title))
                .disableAllInteractions()
                .create();

        build();
        open();
    }

    public void build() {
        // --- Bottone Upgrade / Completo ---
        PlantUpgrade nextUpgrade = plant.getPlantType().getUpgrade(plant.getUpgradeLevel() + 1);
        if (nextUpgrade == null) {
            createAndSetItem("complete", null);
        } else {
            createAndSetItem("upgrade", nextUpgrade);
        }

        // --- Bottone Rimuovi ---
        createAndSetItem("remove", null);

        // --- NUOVO: Bottone Automazione ---
        buildAutomationButton();

        fillGui();
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    private void createAndSetItem(String key, PlantUpgrade nextUpgrade) {
        ConfigurationSection section = config.getSection(key);
        if (section == null) return;

        ItemStack item = createItemFromConfig(section);
        if (item == null) return;

        ItemMeta meta = item.getItemMeta();
        // --- FIX QUI ---
        // Il controllo 'meta.hasLore()' è stato rimosso.
        // Se l'item ha un meta, procediamo a costruire e impostare la lore.
        if (meta != null) {
            List<String> newLore;
            if (key.equals("upgrade")) {
                // Logica speciale per la lore dell'upgrade
                newLore = new ArrayList<>();
                newLore.addAll(replacePlaceholders(section.getStringList("lore-header"), nextUpgrade));
                newLore.addAll(generateRequirementsLore(nextUpgrade));
                newLore.addAll(replacePlaceholders(section.getStringList("lore-footer"), nextUpgrade));
            } else {
                // Logica per tutti gli altri bottoni
                newLore = replacePlaceholders(section.getStringList("lore"), nextUpgrade);
            }
            meta.setLore(newLore);
            item.setItemMeta(meta);
        }

        GuiItem guiItem = new GuiItem(item);

        // Imposta l'azione del click
        guiItem.setAction(event -> {
            switch (key) {
                case "upgrade":
                    plugin.getTropicalPlantManager().upgradePlant(player, plant.getLocation());
                    gui.close(player);
                    break;
                case "remove":
                    plant.pickUp(player);
                    // Il messaggio viene già inviato da pickUp()
                    gui.close(player);
                    break;
                case "complete":
                    gui.close(player);
                    break;
            }
        });

        gui.setItem(section.getInt("slot"), guiItem);
    }

    /**
     * NUOVO: Costruisce e imposta il bottone per l'automazione della pianta.
     */
    private void buildAutomationButton() {
        ConfigurationSection section = config.getSection("automation");
        if (section == null) {
            // Se la sezione 'automation' non esiste, non fare nulla.
            return;
        }

        TropicalPlantManager manager = plugin.getTropicalPlantManager();
        int maxAutomated = manager.getMaxAutomatedPlants(player);

        if (maxAutomated == 0) {
            return; // Il giocatore non ha permessi, non mostrare il bottone.
        }

        ItemStack item;
        GuiItem guiItem;

        // Determina quale sezione della config usare (on, off, unavailable)
        ConfigurationSection itemSection;
        if (plant.isAutomated()) {
            itemSection = section.getConfigurationSection("on-1");
        } else {
            int currentAutomated = manager.getActiveAutomatedPlantsCount(player.getUniqueId());
            if (currentAutomated < maxAutomated) {
                itemSection = section.getConfigurationSection("off-1");
            } else {
                itemSection = section.getConfigurationSection("unavailable");
            }
        }

        // Se la sottosezione non è stata trovata, logga un errore ed esci
        if (itemSection == null) {
            plugin.getLogger().warning("Impossibile trovare la sottosezione corretta (on/off/unavailable) in 'automation' nel file 'plant-gui.yml'.");
            return;
        }

        item = createItemFromConfig(itemSection);

        // Se l'item è null, logga un errore ed esci
        if (item == null) {
            plugin.getLogger().warning("Impossibile creare l'item dalla sezione '" + itemSection.getName() + "'. Controlla la config.");
            return;
        }

        guiItem = new GuiItem(item);

        // Imposta l'azione corretta in base allo stato
        if (plant.isAutomated()) {
            // AZIONE PER DISATTIVARE L'AUTOMAZIONE
            guiItem.setAction(event -> {
                plant.setAutomated(false);
                plugin.getMySQLManager().saveOrUpdatePlant(plant);
                player.sendMessage("§cAutomazione disattivata per questa pianta.");
                gui.close(player);
            });

        } else {
            int currentAutomated = manager.getActiveAutomatedPlantsCount(player.getUniqueId());
            if (currentAutomated < maxAutomated) {
                // AZIONE PER ATTIVARE L'AUTOMAZIONE
                guiItem.setAction(event -> {
                    plant.setAutomated(true);
                    plugin.getMySQLManager().saveOrUpdatePlant(plant);
                    player.sendMessage("§aAutomazione attivata per questa pianta!");
                    gui.close(player);
                });
            } else {
                // AZIONE PER IL BOTTONE "NON DISPONIBILE"
                // Aggiorna la lore per mostrare il conteggio corretto
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    List<String> newLore = meta.getLore().stream()
                            .map(line -> line.replace("%current%", String.valueOf(currentAutomated))
                                    .replace("%max%", String.valueOf(maxAutomated)))
                            .collect(Collectors.toList());
                    meta.setLore(newLore);
                    item.setItemMeta(meta);
                }
                // Messaggio informativo al click
                guiItem.setAction(event -> player.sendMessage("§cHai raggiunto il limite di piante automatizzabili."));
            }
        }
        gui.setItem(section.getInt("slot"), guiItem);
    }


    private List<String> generateRequirementsLore(PlantUpgrade upgrade) {
        List<String> requirementsLore = new ArrayList<>();
        requirementsLore.add("§8▶ §fRequisiti per l'upgrade§c:");

        PlayerPointsManager pointsManager = plugin.getPlayerPointsManager();
        double discountPoints = pointsManager.getPoints(player, "SCONTO_PIANTE");
        double discountPercentage = Math.min(discountPoints, 100.0);

        for (String req : upgrade.getRequirements()) {
            String[] parts = req.split(";", 2);
            if (parts.length < 2) continue;

            String type = parts[0].toUpperCase();
            String value = parts[1];
            String line = "§c- Requisito non riconosciuto";

            switch (type) {
                case "MONEY":
                    double money = NumberUtils.parseBigNumber(value);
                    double discountedCost = money * (1.0 - (discountPercentage / 100.0));
                    line = "  §8- §fSoldi: §c" + Formatter.format(discountedCost);
                    break;
                case "CROPS":
                    double crops = NumberUtils.parseBigNumber(value);
                    double discountedCrops = crops * (1.0 - (discountPercentage / 100.0));
                    line = "  §8- §fGermogli: §c" + Formatter.format(discountedCrops);
                    break;
                case "QUEST":
                    line = "  §8- §fQuest: §c" + RubyIsland.getInstance().getQuestManager().getQuestById(value).getQuestName();
                    break;
            }
            requirementsLore.add(line);
        }
        return requirementsLore;
    }

    private List<String> replacePlaceholders(List<String> lore, PlantUpgrade nextUpgrade) {
        if (lore == null) return new ArrayList<>();
        PlantUpgrade currentUpgrade = plant.getPlantType().getUpgrade(plant.getUpgradeLevel());
        if (currentUpgrade == null) return lore;

        return lore.stream().map(line -> ChatColor.translateAlternateColorCodes('&', line)
                .replace("%level%", String.valueOf(plant.getUpgradeLevel()))
                .replace("%max_level%", String.valueOf(plant.getPlantType().getUpgrades().size()))
                .replace("%earn%", Formatter.format(currentUpgrade.getSellReward()))
                .replace("%type%", currentUpgrade.getSellType())
                .replace("%next_level%", nextUpgrade != null ? String.valueOf(plant.getUpgradeLevel() + 1) : "MAX")
                .replace("%next_earn%", nextUpgrade != null ? Formatter.format(nextUpgrade.getSellReward()) : "MAX")
                .replace("%amount%", String.valueOf(plant.getPlantType().getSellPrice()))
        ).collect(Collectors.toList());
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

    public void open() {
        gui.open(player);
    }
}
