package me.herohd.rubyisland.manager;

import me.herohd.rubycrops.RubyCrops;
import me.herohd.rubycrops.objects.PlayerProfile;
import me.herohd.rubyisland.RubyIsland;
import me.herohd.rubyisland.objects.Island;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

public class IslandManager {
    public HashMap<String, Island> islands;
    public HashMap<String, Island> visitors;
    public int ISLAND_SIZE;

    public IslandManager() {
        this.islands = new HashMap<>();
        this.visitors = new HashMap<>();
        this.ISLAND_SIZE = RubyIsland.getInstance().getConfigYML().getInt("general.island-size");
    }

    public HashMap<String, Island> getVisitors() {
        return visitors;
    }

    public Island isVisitor(String player) {
        return visitors.getOrDefault(player, null);
    }


    public void addVisitor(String player, Island island) {
        visitors.put(player, island);
    }

    public Island create(Player owner) {
        int id = RubyIsland.getInstance().getMySQLManager().addIsland(owner.getUniqueId().toString());
        Location location = IslandChunkManager.getCenterFromId(id);
        Location spawn = new Location(Bukkit.getWorld(RubyIsland.getInstance().getConfigYML().getString("general.world-name")), location.getBlockX(), RubyIsland.getInstance().getConfigYML().getInt("general.world-y"), location.getBlockZ() + 2);

        Island island = new Island(id, owner.getName(), spawn);
        islands.put(owner.getUniqueId().toString(), island);

        island.setSpawn(spawn);
        island.save();


        SchematicManager.pasteSchematic(spawn);

        new BukkitRunnable() {
            @Override
            public void run() {
                owner.sendMessage("\n§fA causa di un §eproblema tecnico §fla tua isola è stata resettata! Parla con Luca per riscattare i premi di scuse\n§f \n");
                //owner.sendTitle("§c§lMALEDIZIONE DELL'ISOLA", "§fTi sono stati resettati i germogli", 20, 60, 20);
                //final PlayerProfile profile = RubyCrops.getInstance().getCropsManager().getPlayerProfile(owner.getName());
                //if(profile != null) profile.setCrops(0);
            }
        }.runTaskLater(RubyIsland.getInstance(), 40l);

        return island;
    }

    public Island getIsland(String uuid) {
        if(islands.containsKey(uuid)) return islands.get(uuid);
        Island is = RubyIsland.getInstance().getMySQLManager().getIslandIdByUUID(uuid);
        Player p = Bukkit.getPlayer(UUID.fromString(uuid));
        if(is == null && p != null) return create(p);
        islands.put(uuid, is);
        return is;
    }

    public Island getIslandTemp(String uuid) {
        if(islands.containsKey(uuid)) return islands.get(uuid);
        return RubyIsland.getInstance().getMySQLManager().getIslandIdByUUID(uuid);
    }

    public Island getIslandOrNull(String uuid) {
        if(islands.containsKey(uuid)) return islands.get(uuid);
        Island island = RubyIsland.getInstance().getMySQLManager().getIslandIdByUUID(uuid);
        if(island == null) return null;
        islands.put(uuid, island);
        return island;
    }

    public void saveIsland(String uuid, Island island) {
        islands.put(uuid, island);
    }

    public int getISLAND_SIZE() {
        return ISLAND_SIZE;
    }
}
