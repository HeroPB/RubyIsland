package me.herohd.rubyisland.event;

import me.herohd.rubyisland.plants.PlacedPlant;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class UpgradePlantEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final String player;
    private final PlacedPlant plant;

    public UpgradePlantEvent(String player, PlacedPlant plant) {
        this.player = player;
        this.plant = plant;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }


    public String getPlayer() {
        return player;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public String getPlayerName() {
        return this.player;
    }

    public PlacedPlant getPlant() {
        return plant;
    }
}