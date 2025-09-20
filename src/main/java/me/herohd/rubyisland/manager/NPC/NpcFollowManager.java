package me.herohd.rubyisland.manager.NPC;

import me.herohd.rubyisland.RubyIsland;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class NpcFollowManager {

    private final RubyIsland plugin;
    private final Map<Integer, EntityPlayer> activeNpcs = new ConcurrentHashMap<>();
    private final Map<Integer, UUID> targetPlayers = new ConcurrentHashMap<>();

    private static final double MAX_DISTANCE_SQUARED = 3600; // 16 blocchi (16*16)

    public NpcFollowManager(RubyIsland plugin) {
        this.plugin = plugin;
        startFollowTask();
    }

    /**
     * Fa in modo che un NPC inizi a guardare un giocatore.
     * @param npc L'NPC che deve seguire con lo sguardo.
     * @param target Il giocatore da seguire.
     */
    public void startFollowing(EntityPlayer npc, Player target) {
        activeNpcs.put(npc.getId(), npc);
        targetPlayers.put(npc.getId(), target.getUniqueId());
    }

    /**
     * Fa smettere a un NPC di guardare un giocatore.
     * @param npcId L'ID dell'NPC.
     */
    public void stopFollowing(int npcId) {
        activeNpcs.remove(npcId);
        targetPlayers.remove(npcId);
    }

    private void startFollowTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (targetPlayers.isEmpty()) {
                    return;
                }

                for (Map.Entry<Integer, UUID> entry : targetPlayers.entrySet()) {
                    int npcId = entry.getKey();
                    UUID targetUuid = entry.getValue();

                    EntityPlayer npc = activeNpcs.get(npcId);
                    Player target = Bukkit.getPlayer(targetUuid);

                    if (target == null || !target.isOnline() || npc == null) {
                        stopFollowing(npcId);
                        continue;
                    }

                    if (target.getWorld().equals(npc.getBukkitEntity().getWorld())) {
                        double distanceSquared = target.getLocation().distanceSquared(npc.getBukkitEntity().getLocation());
                        if(distanceSquared > MAX_DISTANCE_SQUARED) {
                            stopFollowing(npcId);
                            continue;
                        }
                    } else {
                        stopFollowing(npcId);
                        continue;
                    }
                    updateNpcRotation(npc, target);
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0L, 2L);
    }

    private void updateNpcRotation(EntityPlayer npc, Player target) {
        Location playerLocation = target.getEyeLocation();

        double deltaX = playerLocation.getX() - npc.locX;
        double deltaY = playerLocation.getY() - (npc.locY + 1.62); // Altezza occhi
        double deltaZ = playerLocation.getZ() - npc.locZ;
        double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        float yaw = (float) Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90;
        float pitch = (float) -Math.toDegrees(Math.atan2(deltaY, distance));

        byte yawByte = (byte) (yaw * 256 / 360);
        byte pitchByte = (byte) (pitch * 256 / 360);

        PacketPlayOutEntity.PacketPlayOutEntityLook bodyLookPacket = new PacketPlayOutEntity.PacketPlayOutEntityLook(npc.getId(), yawByte, pitchByte, true);

        PacketPlayOutEntityHeadRotation headLookPacket = new PacketPlayOutEntityHeadRotation(npc, yawByte);

        PlayerConnection connection = ((CraftPlayer) target).getHandle().playerConnection;
        connection.sendPacket(bodyLookPacket);
        connection.sendPacket(headLookPacket);
    }
}