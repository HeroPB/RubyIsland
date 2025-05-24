package me.herohd.rubyisland;

import me.herohd.rubyisland.commands.*;
import me.herohd.rubyisland.listener.PlayerListener;
import me.herohd.rubyisland.manager.IslandManager;
import me.herohd.rubyisland.manager.MySQLManager;
import me.herohd.rubyisland.manager.SchematicManager;
import me.herohd.rubyisland.utils.Config;
import me.kr1s_d.commandframework.CommandManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class RubyIsland extends JavaPlugin {

    private static RubyIsland instance;

    private MySQLManager mySQLManager;
    private IslandManager islandManager;

    private Config config;

    @Override
    public void onEnable() {
        instance = this;
        this.config = new Config(this, "config");
        SchematicManager.load();

        this.mySQLManager = new MySQLManager(config);
        this.islandManager = new IslandManager();
        new PlayerListener();

        CommandManager manager = new CommandManager("rubyisland", "RubyIsland » ");
        manager.register(new IslandMainCommand());
        manager.register(new IslandVisitCommand());
        manager.register(new IslandAddCommand());
        manager.register(new IslandBanCommand());
        manager.register(new IslandTrustCommand());
        manager.register(new IslandUnbanCommand());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static RubyIsland getInstance() {
        return instance;
    }

    public Config getConfigYML() {
        return config;
    }

    public MySQLManager getMySQLManager() {
        return mySQLManager;
    }

    public IslandManager getIslandManager() {
        return islandManager;
    }
}
