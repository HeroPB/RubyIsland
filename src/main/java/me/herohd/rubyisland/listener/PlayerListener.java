package me.herohd.rubyisland.listener;

import me.herohd.rubyisland.RubyIsland;
import me.herohd.rubyisland.objects.Island;
import me.herohd.rubyisland.utils.IslandUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.*;

public class PlayerListener implements Listener {

    public PlayerListener() {
        Bukkit.getServer().getPluginManager().registerEvents(this, RubyIsland.getInstance());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        final Location location = e.getBlock().getLocation();
        if (!IslandUtils.isInIslandWorld(location)) return;
        if (!IslandUtils.isInHisIsland(e.getPlayer(), location)) {
            final Island visitor = RubyIsland.getInstance().getIslandManager().isVisitor(e.getPlayer().getName());
            if(visitor == null) return;
            if(IslandUtils.isInIsland(e.getPlayer(), visitor, location)) {
                if(visitor.isTrusted(e.getPlayer().getUniqueId().toString())) return;
                if(visitor.isAdded(e.getPlayer().getUniqueId().toString())) {
                    if(Bukkit.getPlayer(visitor.getPlayer()) != null) return;
                }
            }
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        final Location location = e.getBlock().getLocation();
        if (!IslandUtils.isInIslandWorld(location)) return;
        if (!IslandUtils.isInHisIsland(e.getPlayer(), location)) {
            final Island visitor = RubyIsland.getInstance().getIslandManager().isVisitor(e.getPlayer().getName());
            if(visitor == null) return;
            if(IslandUtils.isInIsland(e.getPlayer(), visitor, location)) {
                if(visitor.isTrusted(e.getPlayer().getUniqueId().toString())) return;
                if(visitor.isAdded(e.getPlayer().getUniqueId().toString())) {
                    if(Bukkit.getPlayer(visitor.getPlayer()) != null) return;
                }
            }
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getClickedBlock() == null) return;
        final Location location = e.getClickedBlock().getLocation();
        if (!IslandUtils.isInIslandWorld(location)) return;
        if (!IslandUtils.isInHisIsland(e.getPlayer(), location)) {
            final Island visitor = RubyIsland.getInstance().getIslandManager().isVisitor(e.getPlayer().getName());
            if(visitor == null) return;
            if(IslandUtils.isInIsland(e.getPlayer(), visitor, location)) {
                if(visitor.isTrusted(e.getPlayer().getUniqueId().toString())) return;
                if(visitor.isAdded(e.getPlayer().getUniqueId().toString())) {
                    if(Bukkit.getPlayer(visitor.getPlayer()) != null) return;
                }
            }
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent e) {
        final Location location = e.getBlockClicked().getLocation();
        if (!IslandUtils.isInIslandWorld(location)) return;
        if (!IslandUtils.isInHisIsland(e.getPlayer(), location)) {
            final Island visitor = RubyIsland.getInstance().getIslandManager().isVisitor(e.getPlayer().getName());
            if(visitor == null) return;
            if(IslandUtils.isInIsland(e.getPlayer(), visitor, location)) {
                if(visitor.isTrusted(e.getPlayer().getUniqueId().toString())) return;
                if(visitor.isAdded(e.getPlayer().getUniqueId().toString())) {
                    if(Bukkit.getPlayer(visitor.getPlayer()) != null) return;
                }
            }
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent e) {
        final Location location = e.getBlockClicked().getLocation();
        if (!IslandUtils.isInIslandWorld(location)) return;
        if (!IslandUtils.isInHisIsland(e.getPlayer(), location)) {
            final Island visitor = RubyIsland.getInstance().getIslandManager().isVisitor(e.getPlayer().getName());
            if(visitor == null) return;
            if(IslandUtils.isInIsland(e.getPlayer(), visitor, location)) {
                if(visitor.isTrusted(e.getPlayer().getUniqueId().toString())) return;
                if(visitor.isAdded(e.getPlayer().getUniqueId().toString())) {
                    if(Bukkit.getPlayer(visitor.getPlayer()) != null) return;
                }
            }
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
        final Location location = e.getRightClicked().getLocation();
        if (!IslandUtils.isInIslandWorld(location)) return;
        if (!IslandUtils.isInHisIsland(e.getPlayer(), location)) {
            final Island visitor = RubyIsland.getInstance().getIslandManager().isVisitor(e.getPlayer().getName());
            if(visitor == null) return;
            if(IslandUtils.isInIsland(e.getPlayer(), visitor, location)) {
                if(visitor.isTrusted(e.getPlayer().getUniqueId().toString())) return;
                if(visitor.isAdded(e.getPlayer().getUniqueId().toString())) {
                    if(Bukkit.getPlayer(visitor.getPlayer()) != null) return;
                }
            }
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onArmorStandManipulate(PlayerArmorStandManipulateEvent e) {
        final Location location = e.getRightClicked().getLocation();
        if (!IslandUtils.isInIslandWorld(location)) return;
        if (!IslandUtils.isInHisIsland(e.getPlayer(), location)) {
            final Island visitor = RubyIsland.getInstance().getIslandManager().isVisitor(e.getPlayer().getName());
            if(visitor == null) return;
            if(IslandUtils.isInIsland(e.getPlayer(), visitor, location)) {
                if(visitor.isTrusted(e.getPlayer().getUniqueId().toString())) return;
                if(visitor.isAdded(e.getPlayer().getUniqueId().toString())) {
                    if(Bukkit.getPlayer(visitor.getPlayer()) != null) return;
                }
            }
            e.setCancelled(true);
        }
    }
}
