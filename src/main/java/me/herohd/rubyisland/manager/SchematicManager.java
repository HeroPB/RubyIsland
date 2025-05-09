package me.herohd.rubyisland.manager;


import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.schematic.SchematicFormat;
import com.sk89q.worldedit.world.World;
import me.herohd.rubyisland.RubyIsland;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;

public class SchematicManager {

    private static File file = null;

    public static void load() {
        file = new File(RubyIsland.getInstance().getDataFolder() + "/schematics/" + RubyIsland.getInstance().getConfigYML().getString("general.schematic-name") + ".schematic");

        if (!file.exists()) {
            Bukkit.getPluginManager().disablePlugin(RubyIsland.getInstance());
        }
    }

    public static void pasteSchematic(Location spawn) {
        if(file == null) return;
        try {
            SchematicFormat format = SchematicFormat.getFormat(file);
            CuboidClipboard clipboard = format.load(file);

            Vector pasteAt = BukkitUtil.toVector(spawn);//.subtract(clipboard.getOffset());

            World world = BukkitUtil.getLocalWorld(spawn.getWorld());
            EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(world, 999999);
            clipboard.paste(editSession, pasteAt, false);
            Bukkit.getLogger().info("Pasting at: " + pasteAt);

            editSession.flushQueue();
            Bukkit.getLogger().info("Schematic pasted successfully at spawn point.");


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
