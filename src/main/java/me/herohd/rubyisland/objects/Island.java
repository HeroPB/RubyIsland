package me.herohd.rubyisland.objects;


import me.herohd.rubyisland.RubyIsland;
import me.herohd.rubyisland.manager.IslandChunkManager;
import net.minecraft.server.v1_12_R1.PacketPlayOutWorldBorder;
import net.minecraft.server.v1_12_R1.WorldBorder;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Island {
    private int id;
    private String player;
    private Location spawn;
    private List<String> bannedPlayers;
    private List<String> trustedPlayers;
    private List<String> addedPlayers;

    public Island(int id, String player, Location spawn) {
        this.id = id;
        this.player = player;
        this.spawn = spawn;
        this.bannedPlayers = new ArrayList<>();
        this.trustedPlayers = new ArrayList<>();
        this.addedPlayers = new ArrayList<>();
    }

    public Island(int id, String player, Location spawn, List<String> bp, List<String> ap, List<String> tp) {
        this.id = id;
        this.player = player;
        this.spawn = spawn;
        this.bannedPlayers = bp;
        this.trustedPlayers = tp;
        this.addedPlayers = ap;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    public Location getSpawn() {
        return spawn;
    }

    public void setSpawn(Location spawn) {
        this.spawn = spawn;
    }

    public void teleport(Player player) {
        player.teleport(spawn);
        setBorder(player);
    }

    public void save() {
        RubyIsland.getInstance().getMySQLManager().save(this);
    }

    public void setBorder(Player player) {
        Location center = IslandChunkManager.getCenterFromId(id);
        WorldBorder worldBorder = new WorldBorder();
        worldBorder.world = ((CraftWorld) spawn.getWorld()).getHandle();
        worldBorder.setCenter(center.getX(), center.getZ());
        worldBorder.setSize(RubyIsland.getInstance().getConfigYML().getInt("general.island-size"));

        PacketPlayOutWorldBorder packet = new PacketPlayOutWorldBorder(worldBorder, PacketPlayOutWorldBorder.EnumWorldBorderAction.INITIALIZE);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }
}
