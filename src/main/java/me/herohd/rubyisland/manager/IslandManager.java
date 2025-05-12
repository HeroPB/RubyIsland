package me.herohd.rubyisland.manager;

import me.herohd.rubyisland.RubyIsland;
import me.herohd.rubyisland.objects.Island;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class IslandManager {
    public HashMap<String, Island> islands;
    public int ISLAND_SIZE;

    public IslandManager() {
        this.islands = new HashMap<>();
        this.ISLAND_SIZE = RubyIsland.getInstance().getConfigYML().getInt("general.island-size");
    }

    public Island create(Player owner) {
        int id = RubyIsland.getInstance().getMySQLManager().addIsland(owner.getUniqueId().toString());
        Location location = IslandChunkManager.getCenterFromId(id);
        Location spawn = new Location(Bukkit.getWorld(RubyIsland.getInstance().getConfigYML().getString("general.world-name")), location.getBlockX(), 15, location.getBlockZ() + 2);

        Island island = new Island(id, owner.getName(), spawn);
        islands.put(owner.getUniqueId().toString(), island);

        island.setSpawn(spawn);
        island.save();


        SchematicManager.pasteSchematic(spawn);
        return island;
    }

    public Island getIsland(String uuid) {
        if(islands.containsKey(uuid)) return islands.get(uuid);
        Island is = RubyIsland.getInstance().getMySQLManager().getIslandIdByUUID(uuid);
        Player p = Bukkit.getPlayer(UUID.fromString(uuid));
        if(is == null && p != null) return create(p);
        return islands.put(uuid, is);
    }

    public Island getIslandTemp(String uuid) {
        if(islands.containsKey(uuid)) return islands.get(uuid);
        return RubyIsland.getInstance().getMySQLManager().getIslandIdByUUID(uuid);
    }

    public Island getIslandOrNull(String uuid) {
        return islands.getOrDefault(uuid, null);
    }

    public int getISLAND_SIZE() {
        return ISLAND_SIZE;
    }
}
