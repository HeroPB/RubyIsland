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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Island {
    private int id;
    private String player;
    private Location spawn;
    private boolean closed;
    private Map<String, String> players;

    public Island(int id, String player, Location spawn) {
        this.id = id;
        this.player = player;
        this.spawn = spawn;
        this.closed = false;
        this.players = new ConcurrentHashMap<>();
    }

    public Island(int id, String player, Location spawn, Map<String, String> players, boolean closed) {
        this.id = id;
        this.player = player;
        this.spawn = spawn;
        this.players = players;
        this.closed = closed;
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
        RubyIsland.getInstance().getIslandManager().addVisitor(player.getName(), this);
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

    public Map<String, String> getPlayers() {
        return players;
    }

    public boolean banPlayer(String uuid) {
        final String type = players.getOrDefault(uuid, null);
        if(type == null || !type.equalsIgnoreCase("BAN")) {
            players.put(uuid, "BAN");
            RubyIsland.getInstance().getMySQLManager().addOrUpdatePlayer(id, uuid, "BAN");
            return true;
        }
        return false;
    }
    public boolean addPlayer(String uuid) {
        final String type = players.getOrDefault(uuid, null);
        if(type == null || !type.equalsIgnoreCase("ADD")) {
            players.put(uuid, "ADD");
            RubyIsland.getInstance().getMySQLManager().addOrUpdatePlayer(id, uuid, "ADD");
            return true;
        }
        return false;
    }

    public boolean trustPlayer(String uuid) {
        final String type = players.getOrDefault(uuid, null);
        if(type == null || !type.equalsIgnoreCase("TRUST")) {
            players.put(uuid, "TRUST");
            RubyIsland.getInstance().getMySQLManager().addOrUpdatePlayer(id, uuid, "TRUST");
            return true;
        }
        return false;
    }

    public boolean unbanPlayer(String uuid) {
        final String type = players.getOrDefault(uuid, null);
        if(type != null && type.equalsIgnoreCase("BAN")) {
            players.remove(uuid);
            RubyIsland.getInstance().getMySQLManager().removePlayer(id, uuid);
            return true;
        }
        return false;
    }

    public boolean isTrusted(String uuid) {
        String type = players.getOrDefault(uuid, null);
        if(type == null) return false;
        return type.equalsIgnoreCase("trust");
    }

    public boolean isAdded(String uuid) {
        String type = players.getOrDefault(uuid, null);
        if(type == null) return false;
        return type.equalsIgnoreCase("add");
    }

    public boolean isBanned(String uuid) {
        String type = players.getOrDefault(uuid, null);
        if(type == null) return false;
        return type.equalsIgnoreCase("ban");
    }

    public boolean isClosed() {
        return closed;
    }

    public boolean canJoin(String uuid) {
        return !closed || isAdded(uuid) || isTrusted(uuid);
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
        RubyIsland.getInstance().getMySQLManager().closeIsland(this);
    }
}
