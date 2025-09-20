package me.herohd.rubyisland.manager;

import me.herohd.rubyisland.RubyIsland;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class IslandChunkManager {

    public static Location getCenterFromId(int id) {
        int x = id*769;

        int z = (int) ((double) (x / 384500) *769);

        return new Location(Bukkit.getWorld("world"), x, RubyIsland.getInstance().getConfigYML().getInt("general.world-y"), z);
        //y 100
    }
}
