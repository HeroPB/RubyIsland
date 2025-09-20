package me.herohd.rubyisland.event;

import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.List;

public class CollectorFillEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final String player;
    private final String collectorId;
    private final Location location;
    private final int amountFilled;

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public CollectorFillEvent(String player, String collectorId, Location location, int amountFilled) {
        this.player = player;
        this.collectorId = collectorId;
        this.location = location;
        this.amountFilled = amountFilled;
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

    public int getAmountFilled() {
        return amountFilled;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public String getPlayerName() {
        return this.player;
    }

}
