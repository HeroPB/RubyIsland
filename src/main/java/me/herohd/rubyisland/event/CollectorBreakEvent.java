package me.herohd.rubyisland.event;

import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.List;

public class CollectorBreakEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final String player;
    private final String collectorId;
    private final Location location;
    private final int amountBreaked;
    private final List<String> commands;

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public CollectorBreakEvent(String player, String collectorId, Location location, int amountBreaked, List<String> commands) {
        this.player = player;
        this.collectorId = collectorId;
        this.location = location;
        this.amountBreaked = amountBreaked;
        this.commands = commands;
    }

    public String getPlayer() {
        return player;
    }

    public String getCollectorId() {
        return collectorId;
    }

    public Location getLocation() {
        return location;
    }

    public int getAmountBreaked() {
        return amountBreaked;
    }

    public List<String> getCommands() {
        return commands;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public String getPlayerName() {
        return this.player;
    }

}