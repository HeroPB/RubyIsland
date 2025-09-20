package me.herohd.rubyisland.runnable;

import me.herohd.rubyisland.manager.MySQLManager;
import me.herohd.rubyisland.quests.PlayerQuestData;
import me.herohd.rubyisland.quests.QuestManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class QuestAutoSaver extends BukkitRunnable {

    private final QuestManager questManager;
    private final MySQLManager mysqlManager;

    public QuestAutoSaver(QuestManager questManager, MySQLManager mysqlManager) {
        this.questManager = questManager;
        this.mysqlManager = mysqlManager;
    }

    @Override
    public void run() {
        // Questo codice viene eseguito su un thread separato
        int savedCount = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerQuestData data = questManager.getPlayerData(player);
            if (data != null) {
                mysqlManager.savePlayerQuestStatus(data);
                savedCount++;
            }
        }

        if (savedCount > 0) {
            Bukkit.getLogger().info("[QuestAutoSaver] Salvataggio automatico completato per " + savedCount + " giocatori.");
        }
    }
}