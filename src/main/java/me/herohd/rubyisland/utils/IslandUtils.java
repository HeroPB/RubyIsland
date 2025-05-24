package me.herohd.rubyisland.utils;

import me.herohd.rubyisland.RubyIsland;
import me.herohd.rubyisland.manager.IslandChunkManager;
import me.herohd.rubyisland.manager.IslandManager;
import me.herohd.rubyisland.objects.Island;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class IslandUtils {

    public static boolean isInHisIsland(Player player, Location loc) {
        final IslandManager manager = RubyIsland.getInstance().getIslandManager();
        Island island = manager.getIslandOrNull(player.getUniqueId().toString());
        if (island == null) return false;

        final Location center = IslandChunkManager.getCenterFromId(island.getId());
        Location centerd = new Location(loc.getWorld(), center.getX(), center.getY(), center.getZ());
        final int radius = RubyIsland.getInstance().getIslandManager().ISLAND_SIZE/2; // oppure island.getRadius() se dinamico

        return isLocationInSquareRange(loc, centerd, radius);
    }
    public static boolean isInIsland(Player player, Island island, Location loc) {
        if (island == null) return false;

        final Location center = IslandChunkManager.getCenterFromId(island.getId());
        Location centerd = new Location(player.getWorld(), center.getX(), center.getY(), center.getZ());
        final int radius = RubyIsland.getInstance().getIslandManager().ISLAND_SIZE/2; // oppure island.getRadius() se dinamico

        return isLocationInSquareRange(loc, centerd, radius);
    }

    public static boolean isLocationInSquareRange(Location check, Location center, int radius) {

        int minX = center.getBlockX() - radius;
        int maxX = center.getBlockX() + radius;
        int minZ = center.getBlockZ() - radius;
        int maxZ = center.getBlockZ() + radius;

        int x = check.getBlockX();
        int z = check.getBlockZ();

        return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
    }

    public static boolean isInIslandWorld(Location location) {
        return location.getWorld().getName().equalsIgnoreCase(RubyIsland.getInstance().getConfigYML().getString("general.world-name"));
    }
}
