package me.herohd.rubyisland.gui;

import org.bukkit.entity.Player;

public interface IGui {
    void build();

    Player getPlayer();

    void open();
}