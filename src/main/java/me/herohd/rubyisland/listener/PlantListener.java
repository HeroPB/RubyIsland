package me.herohd.rubyisland.listener;


import com.vk2gpz.tokenenchant.api.TokenEnchantAPI;
import me.clip.autosell.events.SellEvent;
import me.herohd.rubyisland.RubyIsland;
import me.herohd.rubyisland.boosters.BoosterManager;
import me.herohd.rubyisland.boosters.BoosterType;
import me.herohd.rubyisland.gui.MainGui;
import me.herohd.rubyisland.plants.PlacedPlant;
import me.herohd.rubyisland.plants.PlantType;
import me.herohd.rubyisland.plants.TropicalPlantManager;
import me.herohd.rubyisland.utils.Formatter;
import me.herohd.rubyisland.utils.IslandUtils;
import me.herohd.rubyisland.utils.NBTEditor;
import me.herohd.rubyisland.utils.Utils;
import me.kr1s_d.rubycrates.api.events.CrateOpenEvent;
import me.kr1s_d.rubycrates.api.objects.crate.ICrateReward;
import me.kr1s_d.rubyenchantmanager.events.WEnchantGainObjectEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class PlantListener implements Listener {
    private final TropicalPlantManager plantManager;

    public PlantListener(TropicalPlantManager plantManager) {
        this.plantManager = plantManager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlantPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        if (block.getType() != Material.SKULL) return;

        // Controlla che il blocco sia una testa

        ItemStack hand = event.getItemInHand();
        String plantId = NBTEditor.getNBT(hand, "type");

        // Se non ha l'NBT, non è una delle nostre piante
        if (plantId == null || plantId.isEmpty()) {
            return;
        }

        // Controlla che il mondo sia quello corretto
        if (!IslandUtils.isInIslandWorld(block.getLocation())) {
            event.setCancelled(true);
            player.sendMessage("§cPuoi piazzare le piante solo nella tua isola!");
            return;
        }

        // --- Logica di controllo dell'isola ---
        if (!IslandUtils.isInHisIsland(player, block.getLocation())) {
            event.setCancelled(true);
            player.sendMessage("§cPuoi piazzare le piante solo nella tua isola!");
            return;
        }


        PlantType type = plantManager.getPlantTypes().get(plantId);
        if (type == null) {
            player.sendMessage("§cErrore: Tipo di pianta non valido. Contatta un admin.");
            Bukkit.getLogger().severe("Il giocatore " + player.getName() + " ha provato a piazzare una pianta con un ID invalido: " + plantId);
            return;
        }

        int upgradeLevel = NBTEditor.getNBTInt(hand, "upgrade_level");
        if (upgradeLevel == 0) {
            upgradeLevel = 1; // Se il tag non esiste, getNBTInt restituisce 0
        }

        // Chiama il manager per piazzare la pianta
        plantManager.placePlant(player, event.getBlockPlaced().getLocation(), type, upgradeLevel);
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlantHarvest(BlockBreakEvent event) {
        Location location = event.getBlock().getLocation();
        PlacedPlant plant = plantManager.getActivePlants().get(location);

        // Se non c'è una pianta in questa location, esci
        if (plant == null) return;

        // L'evento di rottura viene sempre cancellato se è una pianta
        event.setCancelled(true);
        Player player = event.getPlayer();

        // Controlla che il giocatore sia il proprietario della pianta
        if (!plant.getOwnerUuid().equals(player.getUniqueId())) {
            player.sendMessage("§cNon puoi raccogliere una pianta che non è tua!");
            return;
        }

        // Chiama il metodo per il raccolto
        plantManager.harvestPlant(player, location);
    }


    @EventHandler(ignoreCancelled = true)
    public void onPlantInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null || !event.getClickedBlock().getType().equals(Material.SKULL)) {
            return;
        }

        Location location = event.getClickedBlock().getLocation();
        PlacedPlant plant = plantManager.getActivePlants().get(location);

        if (plant == null) return;

        event.setCancelled(true);
        Player player = event.getPlayer();

        if (!plant.getOwnerUuid().equals(player.getUniqueId())) {
            player.sendMessage("§cNon puoi interagire con una pianta che non è tua!");
            return;
        }

        // --- APRI LA NUOVA GUI ---
        new MainGui(player, plant);
    }

    @EventHandler
    public void onSellEvent(SellEvent event) {
        final OfflinePlayer offline = event.getPlayer();
        if (!offline.isOnline()) return;

        final Player player = offline.getPlayer();
        if (player == null) return;

        double originalGained = event.getGained();

        BoosterManager boosterManager = RubyIsland.getInstance().getBoosterManager();
        double multiplier = boosterManager.getBoosterValue(player.getUniqueId(), BoosterType.MINE_MULTIPLIER);

        double finalGained = originalGained;

        if (multiplier > 0) {
            // Calcola il bonus come percentuale del guadagno originale
            double bonusAmount = originalGained * multiplier;
            finalGained = bonusAmount;
            RubyIsland.getEconomy().depositPlayer(player, finalGained);
        }

        // Deposita nel conto del giocatore il valore finale calcolato
    }

    @EventHandler
    public void onEnchantGaine(WEnchantGainObjectEvent event) {
        final Player player = event.getPlayer();
        final double gained = event.getGained();

        BoosterManager boosterManager = RubyIsland.getInstance().getBoosterManager();
        double multiplier = boosterManager.getBoosterValue(player.getUniqueId(), BoosterType.ENCHANT_MULTIPLIER);


        switch (event.getType().toUpperCase()) {
            case "CHARITY":
            case "MIDASAURA_MONEY":
                RubyIsland.getEconomy().depositPlayer(player, gained*multiplier);
                break;
            default:
                TokenEnchantAPI.getInstance().addTokens(player, gained*multiplier);
        }
    }

}