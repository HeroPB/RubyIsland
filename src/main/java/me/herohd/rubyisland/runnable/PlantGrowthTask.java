package me.herohd.rubyisland.runnable;

import me.herohd.rubyisland.RubyIsland;
import me.herohd.rubyisland.manager.MySQLManager;
import me.herohd.rubyisland.plants.TropicalPlantManager;
import me.herohd.rubyisland.plants.PlacedPlant;
import me.herohd.rubyisland.plants.PlantPhase;
import me.herohd.rubyisland.utils.SkullCreator;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.TimeUnit;

public class PlantGrowthTask extends BukkitRunnable {
    private final RubyIsland plugin;
    private final TropicalPlantManager plantManager;
    private final MySQLManager mySQLManager;

    public PlantGrowthTask(RubyIsland plugin, TropicalPlantManager plantManager, MySQLManager mySQLManager) {
        this.plugin = plugin;
        this.plantManager = plantManager;
        this.mySQLManager = mySQLManager;
    }

    @Override
    public void run() {
        long now = System.currentTimeMillis();
        for (PlacedPlant plant : plantManager.getActivePlants().values()) {

            if (plant.getHologram() == null) continue;

            if (plant.isReadyToHarvest()) {
                // --- NUOVA LOGICA DI AUTOMAZIONE ---
                if (plant.isAutomated()) {
                    Player owner = Bukkit.getPlayer(plant.getOwnerUuid());
                    // Raccogli solo se il proprietario è online per dargli le ricompense
                    if (owner != null && owner.isOnline()) {
                        // Esegui la raccolta nel thread principale per sicurezza API
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            plantManager.harvestPlant(owner, plant.getLocation());
                        });
                    }
                } else {
                    // Comportamento standard: aggiorna solo l'ologramma per la raccolta manuale
                    plant.updateHologram();
                }
                continue; // Passa alla pianta successiva
            }

            if (now >= plant.getNextGrowthTimestamp()) {
                // --- LOGICA DI CRESCITA (INVARIATA) ---
                int nextPhaseIndex = plant.getCurrentPhaseIndex() + 1;
                PlantPhase nextPhase = plant.getPlantType().getPhase(nextPhaseIndex);

                if (nextPhase == null) continue;

                plant.setCurrentPhaseIndex(nextPhaseIndex);

                Block block = plant.getLocation().getBlock();
                Bukkit.getScheduler().runTask(plugin, () -> {
                    SkullCreator.blockWithBase64(block, nextPhase.getTextureMeta());
                });

                if (!plant.isReadyToHarvest()) {
                    PlantPhase futurePhase = plant.getPlantType().getPhase(nextPhaseIndex);
                    if (futurePhase != null && futurePhase.getType().equalsIgnoreCase("TIME") && futurePhase.getRequire() > 0) {
                        long growthTimeMillis = TimeUnit.MINUTES.toMillis(futurePhase.getRequire());
                        plant.setNextGrowthTimestamp(System.currentTimeMillis() + growthTimeMillis);
                    } else {
                        plant.setNextGrowthTimestamp(Long.MAX_VALUE);
                    }
                } else {
                    plant.setNextGrowthTimestamp(0);
                    Player owner = Bukkit.getPlayer(plant.getOwnerUuid());
                    if(owner != null && owner.isOnline())
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            owner.sendMessage("§fUna tua pianta sull'isola §8(§c" + plant.getPlantType().getId() + "§8) §fè cresciuta");
                        });
                }

                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    mySQLManager.saveOrUpdatePlant(plant);
                });

                plant.updateHologram();

            } else {
                plant.updateHologram();
            }
        }
    }
}