package me.herohd.rubyisland.manager.NPC;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import me.herohd.rubyisland.RubyIsland;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NPCManager {


    private final Map<UUID, EntityPlayer> playerNpcs = new HashMap<>();

    public void spawnNpc(Player player, Location location) {
        if(playerNpcs.containsKey(player.getUniqueId())) {
            removeNpc(player);
        }
        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer world = ((CraftWorld) location.getWorld()).getHandle();
        GameProfile gameProfile = new GameProfile(UUID.randomUUID(), RubyIsland.getInstance().getConfigYML().getColoredString("general.npc-name")); // Nome visualizzato sopra la testa

        gameProfile.getProperties().put("textures", new Property("textures", "ewogICJ0aW1lc3RhbXAiIDogMTc0Mjc4NjkwNDU3MSwKICAicHJvZmlsZUlkIiA6ICJiMTM1MDRmMjMxOGI0OWNjYWFkZDcyYWVhYmMyNTQ1MCIsCiAgInByb2ZpbGVOYW1lIiA6ICJUeXBrZW4iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTRjN2U0ODg5OWE0MzY4MzcxMTIwMWU5MjlkYjI1YzcwNmFlYWY4YTVjOTM1NmZhNTIwNzg2MDMwMTQwMGMwNyIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9", "MbdLp6sR5xwdZhElQ8ynlb9kq/rrj4wHtEvs+c2gwxsInv9EfhNK+DuSnBrTykRfXbB6vMgAg+6vKA5utail3NI7wedpJTpNO4/uzTL2x1H6+YegeK6dNXCgGPMxAnR2fODYITvYuZ5unZ8qByXv7vJAjgNUhQEp1XN4GX9awR9xrekJ/zey3MyPrM//W9kG74vXJtTRhBGykScV2XbqvvDbGWdAiCYV80ltBtmmoYDQIvJymDoiVlOeyx9NYFY5fWTzqPRJII7lQfAMGK+PDCReW9KnbOiu1Px2owyQ44e611AaWjByhzzuRJbY1NkUXcKSq1dgs51fZKnZCRT+EFJQ/B0Nz7HFNyxi2E93VinFoR88OKw1y4huY/0mptwDh2dpfwMT2P0tcQU1BgDVXMRmVRcPkt1gEVwpbL/bwFxT3kCIB3/g2M4nPrrt2q8dg49hFwZXAnrbSE37hY/XN6SazvL6rRWPS1gZAxYr/YpLaHy4zlcOK3grqObPH3vJ1idwcmP65Pu39u2ERXMYJukr/ONxV6R5uDWK7wpWGa7ZVZ44dtjbuY62EUJ84GYlkjNcNtm3zT4cDIsD9bOmkD8ytSOGQr+TcIqMpTVBol4ExVa3QTTFkz069897Q8Q3kbu7rdG0TXU0ZdkPplztpkWcz1Y/e9jOLR3E0/UgWh8="));

        EntityPlayer npc = new EntityPlayer(server, world, gameProfile, new PlayerInteractManager(world));
        npc.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        playerNpcs.put(player.getUniqueId(), npc);

        sendNpcPackets(player, npc);
    }

    private void sendNpcPackets(Player player, EntityPlayer npc) {
        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;

        connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, npc));

        connection.sendPacket(new PacketPlayOutNamedEntitySpawn(npc));
        updateNpcRotationAlternative(npc, player);

        Bukkit.getScheduler().runTaskLater(RubyIsland.getInstance(), () -> {
            connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, npc));
        }, 60); // 3 secondi
        Bukkit.getScheduler().runTaskLaterAsynchronously(RubyIsland.getInstance(), () -> {
            RubyIsland.getInstance().getFollowManager().startFollowing(npc, player);
        }, 20); // 1 secondo
    }

    public EntityPlayer getNpcForPlayer(Player player) {
        return playerNpcs.get(player.getUniqueId());
    }

    public void removeNpc(Player player) {
        EntityPlayer npc = playerNpcs.remove(player.getUniqueId());
        if (npc != null) {
            PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
            connection.sendPacket(new PacketPlayOutEntityDestroy(npc.getId()));
            RubyIsland.getInstance().getFollowManager().stopFollowing(npc.getId());
        }
    }

    public void updateNpcRotationAlternative(EntityPlayer npc, Player player) {
        Location playerLocation = player.getEyeLocation();

        double deltaX = playerLocation.getX() - npc.locX;
        double deltaY = playerLocation.getY() - (npc.locY + 1.62); // Altezza occhi
        double deltaZ = playerLocation.getZ() - npc.locZ;
        double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        // Calcola yaw e pitch
        float yaw = (float) Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90;
        float pitch = (float) -Math.toDegrees(Math.atan2(deltaY, distance));

        npc.yaw = yaw;
        PacketPlayOutEntityTeleport teleportPacket = new PacketPlayOutEntityTeleport(npc);

        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
        connection.sendPacket(teleportPacket);
    }


}