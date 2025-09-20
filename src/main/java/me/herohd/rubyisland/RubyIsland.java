package me.herohd.rubyisland;

import me.herohd.rubyisland.boosters.BoosterManager;
import me.herohd.rubyisland.collectors.CollectorManager;
import me.herohd.rubyisland.commands.*;
import me.herohd.rubyisland.commands.boosters.BoosterGiveCommand;
import me.herohd.rubyisland.commands.boosters.BoosterInfoCommand;
import me.herohd.rubyisland.commands.boosters.BoosterRemoveCommand;
import me.herohd.rubyisland.commands.collector.CollectorGiveCommand;
import me.herohd.rubyisland.commands.collector.CollectorRemoveCommand;
import me.herohd.rubyisland.commands.collector.CollectorSetLevelCommand;
import me.herohd.rubyisland.commands.plants.PlantGiveCommand;
import me.herohd.rubyisland.commands.plants.PlantsRemoveCommand;
import me.herohd.rubyisland.commands.points.PointsAddCommand;
import me.herohd.rubyisland.commands.points.PointsCheckCommand;
import me.herohd.rubyisland.commands.points.PointsTakeCommand;
import me.herohd.rubyisland.commands.quest.QuestMainCommand;
import me.herohd.rubyisland.commands.quest.QuestOpenCommand;
import me.herohd.rubyisland.commands.quest.QuestProgressCommand;
import me.herohd.rubyisland.commands.quest.QuestSetCommand;
import me.herohd.rubyisland.listener.*;
import me.herohd.rubyisland.manager.IslandManager;
import me.herohd.rubyisland.manager.MySQLManager;
import me.herohd.rubyisland.manager.NPC.NPCManager;
import me.herohd.rubyisland.manager.NPC.NpcFollowManager;
import me.herohd.rubyisland.manager.PlayerPointsManager;
import me.herohd.rubyisland.manager.SchematicManager;
import me.herohd.rubyisland.plants.TropicalPlantManager;
import me.herohd.rubyisland.plants.PlacedPlant;
import me.herohd.rubyisland.quests.QuestManager;
import me.herohd.rubyisland.quests.handlers.*;
import me.herohd.rubyisland.runnable.PlantGrowthTask;
import me.herohd.rubyisland.runnable.QuestAutoSaver;
import me.herohd.rubyisland.utils.Config;
import me.herohd.rubyisland.utils.hook.Placeholder;
import me.kr1s_d.commandframework.CommandManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class RubyIsland extends JavaPlugin {

    private static RubyIsland instance;

    private MySQLManager mySQLManager;
    private IslandManager islandManager;
    private NPCManager npcManager;
    private NpcFollowManager followManager;
    private QuestManager questManager;
    private TropicalPlantManager tropicalPlantManager;
    private CollectorManager collectorManager;
    private PlayerPointsManager playerPointsManager;
    private BoosterManager boosterManager;

    private BukkitTask plantGrowthTask;

    private Config config;
    private static Economy econ = null;

    @Override
    public void onEnable() {
        instance = this;
        this.config = new Config(this, "config");
        SchematicManager.load();
        setupEconomy();

        this.followManager = new NpcFollowManager(this);
        this.mySQLManager = new MySQLManager(config);
        this.islandManager = new IslandManager();
        npcManager = new NPCManager();
        new PlayerListener();
        new NpcClickListener(this);

        boosterManager = new BoosterManager(this);

        this.playerPointsManager = new PlayerPointsManager(this);
        this.playerPointsManager.loadPointTypes();

        this.collectorManager = new CollectorManager(this);
        this.collectorManager.loadCollectorTypes();
        getServer().getPluginManager().registerEvents(new CollectorListener(this.collectorManager), this);

        this.questManager = new QuestManager(this, this.mySQLManager);
        this.questManager.loadQuests();

        // TUTTTE LE QUEST
        getServer().getPluginManager().registerEvents(new QuestGlobalListener(this.questManager), this);
        questManager.registerQuestHandler(new MineQuestHandler());
        questManager.registerQuestHandler(new BlockPlaceQuestHandler());
        questManager.registerQuestHandler(new CratesQuestHandler());
        questManager.registerQuestHandler(new MineEnchantQuestHandler());
        questManager.registerQuestHandler(new CollectorBreakQuestHandler());
        questManager.registerQuestHandler(new CollectorFillQuestHandler());
        questManager.registerQuestHandler(new EconomyGainQuestHandler());
        questManager.registerQuestHandler(new UpgradePlantQuestHandler());
        questManager.registerQuestHandler(new BlockBreakQuestHandler());
        questManager.registerQuestHandler(new BlockBreakTwoQuestHandler());

        //
        getServer().getPluginManager().registerEvents(new QuestListener(), this);

        this.tropicalPlantManager = new TropicalPlantManager(this);
        this.tropicalPlantManager.loadPlantTypes();

        getServer().getPluginManager().registerEvents(new PlantListener(this.tropicalPlantManager), this);
        getServer().getPluginManager().registerEvents(new PlayerCacheListener(this.tropicalPlantManager, this.collectorManager, playerPointsManager), this);


        long interval = 300 * 20L;
        new QuestAutoSaver(questManager, mySQLManager).runTaskTimerAsynchronously(this, interval, interval);

        this.plantGrowthTask = new PlantGrowthTask(this, this.tropicalPlantManager, this.mySQLManager).runTaskTimer(this, 20L, 20L);


        CommandManager manager = new CommandManager("rubyisland", "RubyIsland » ", "is", "island");
        manager.register(new IslandMainCommand());
        manager.register(new IslandVisitCommand());
        manager.register(new IslandAddCommand());
        manager.register(new IslandBanCommand());
        manager.register(new IslandTrustCommand());
        manager.register(new IslandUnbanCommand());
        manager.register(new IslandSetStatusCommand());
        manager.register(new IslandSpawnPointCommand());

        CommandManager questCommand = new CommandManager("rubyislandquest", "RubyIsland » ", "islandquest", "riq", "questisland");
        questCommand.register(new QuestMainCommand());
        questCommand.register(new QuestProgressCommand());
        questCommand.register(new QuestSetCommand());
        questCommand.register(new QuestOpenCommand());

        CommandManager plantsCommand = new CommandManager("rubyislandplants", "RubyIsland » ", "islandplants", "rip");
        plantsCommand.register(new PlantGiveCommand());
        plantsCommand.register(new PlantsRemoveCommand());

        CommandManager collectorCommand = new CommandManager("rubyislandcollector", "RubyIsland » ", "rubycollector","collector", "ric");
        collectorCommand.register(new CollectorGiveCommand());
        collectorCommand.register(new CollectorRemoveCommand());
        collectorCommand.register(new CollectorSetLevelCommand());


        CommandManager pointsCommand = new CommandManager("rubyislandpoints", "RubyIsland » ", "punti", "pointsadmin");
        pointsCommand.register(new PointsAddCommand());
        pointsCommand.register(new PointsTakeCommand());
        pointsCommand.register(new PointsCheckCommand());

        CommandManager boosterCommand = new CommandManager("rubyislandbooster", "RubyIsland » ", "risbooster", "boosteradmin");
        boosterCommand.register(new BoosterGiveCommand());
        boosterCommand.register(new BoosterRemoveCommand());
        boosterCommand.register(new BoosterInfoCommand());

        final Placeholder placeholder = new Placeholder(this);
        placeholder.register();
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabilitazione di RubyIsland in corso...");

        if (this.plantGrowthTask != null && !this.plantGrowthTask.isCancelled()) {
            this.plantGrowthTask.cancel();
            getLogger().info("Task di crescita delle piante interrotto.");
        }

        // 1. Salvataggio tutti i dati delle piante attualmente caricate in memoria
        if (this.tropicalPlantManager != null && this.mySQLManager != null) {
            getLogger().info("Salvataggio dei dati delle piante tropicali...");
            int savedCount = 0;
            for (PlacedPlant plant : this.tropicalPlantManager.getActivePlants().values()) {
                this.mySQLManager.saveOrUpdatePlant(plant);
                savedCount++;
            }
            getLogger().info("Salvataggio completato. " + savedCount + " piante sono state salvate nel database.");
        }

        // 3. Salvataggio dati delle quest dei giocatori online
        if (this.questManager != null) {
            getLogger().info("Salvataggio dei dati delle quest...");
            this.questManager.saveAllOnlinePlayersData();
            getLogger().info("Dati delle quest salvati.");
        }


        if (this.playerPointsManager != null) {
            getLogger().info("Salvataggio dei dati dei punti...");
            this.playerPointsManager.saveAllOnlinePlayerData();
            getLogger().info("Dati dei punti salvati.");
        }

        getLogger().info("RubyIsland disabilitato correttamente.");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
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

    public NPCManager getNpcManager() {
        return npcManager;
    }

    public NpcFollowManager getFollowManager() {
        return this.followManager;
    }

    public QuestManager getQuestManager() {
        return questManager;
    }

    public TropicalPlantManager getTropicalPlantManager() {
        return tropicalPlantManager;
    }

    public static Economy getEconomy() {
        return econ;
    }

    public CollectorManager getCollectorManager() {
        return collectorManager;
    }


    public PlayerPointsManager getPlayerPointsManager() {
        return playerPointsManager;
    }
    public BoosterManager getBoosterManager() {
        return boosterManager;
    }
}
