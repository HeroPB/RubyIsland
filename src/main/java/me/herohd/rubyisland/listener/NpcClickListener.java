package me.herohd.rubyisland.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import me.herohd.rubyisland.RubyIsland;
import me.herohd.rubyisland.gui.QuestGui;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class NpcClickListener {

    private final RubyIsland plugin;

    public NpcClickListener(RubyIsland plugin) {
        this.plugin = plugin;
        registerPacketListener();
    }

    private void registerPacketListener() {
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        protocolManager.addPacketListener(new PacketAdapter(plugin, PacketType.Play.Client.USE_ENTITY) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                Player player = event.getPlayer();
                int entityId = packet.getIntegers().read(0);

                // Controlla se l'entità cliccata è l'NPC di quel giocatore
                EntityPlayer npc = RubyIsland.getInstance().getNpcManager().getNpcForPlayer(player);
                if (npc != null && npc.getId() == entityId) {
                    EnumWrappers.EntityUseAction action = packet.getEntityUseActions().read(0);

                    if (action == EnumWrappers.EntityUseAction.ATTACK) {
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            if(player.hasPermission("rubyfail.porcodio")) {
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "dioporcone " + player.getName());
                            }
                        }, 40L);
                    }
                    else if (action == EnumWrappers.EntityUseAction.INTERACT_AT) {
                        EnumWrappers.Hand hand = packet.getHands().read(0);
                        if (hand == EnumWrappers.Hand.MAIN_HAND) {
                            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                if(player.hasPermission("rubyfail.porcodio")) {
                                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "dioporcone " + player.getName());
                                }
                            }, 40L);
                        }
                    }
                }
            }
        });
    }
}