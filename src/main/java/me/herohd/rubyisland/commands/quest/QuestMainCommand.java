package me.herohd.rubyisland.commands.quest;

import me.herohd.rubyisland.RubyIsland;
import me.herohd.rubyisland.manager.MySQLManager;
import me.herohd.rubyisland.quests.PlayerQuestData;
import me.herohd.rubyisland.quests.Quest;
import me.herohd.rubyisland.quests.QuestManager;
import me.kr1s_d.commandframework.objects.BaseCommand;
import me.kr1s_d.commandframework.objects.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@BaseCommand
public class QuestMainCommand implements SubCommand {
    @Override
    public String getSubCommandId() {
        return "check";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // Uso: /questadmin check <player>
        String playerName = args[1];
        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);

        // Controlla se il giocatore è mai entrato nel server
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage( "§cGiocatore non trovato.");
            return;
        }

        MySQLManager mysqlManager = RubyIsland.getInstance().getMySQLManager();
        QuestManager questManager = RubyIsland.getInstance().getQuestManager();

        // Carichiamo i dati direttamente dal database, dato che il giocatore potrebbe essere offline
        PlayerQuestData data = mysqlManager.loadPlayerQuestData(target.getUniqueId());

        if (data == null) {
            sender.sendMessage("§fIl giocatore '§e" + target.getName() + "' §fnon ha ancora iniziato il percorso delle quest.");
            return;
        }

        sender.sendMessage( "§f--- §6Stato Quest di §e" + target.getName() + " §f---");

        if (data.getActiveQuestId() == null) {
            sender.sendMessage("§fQuest Attiva: §eNESSUNA (Tutte completate)");
        } else {
            Quest activeQuest = questManager.getQuestById(data.getActiveQuestId());
            if (activeQuest == null) {
                sender.sendMessage("§fID Quest Attiva: §e" + data.getActiveQuestId() + " (ATTENZIONE: Quest non più esistente!)");
            } else {
                sender.sendMessage("§fQuest Attiva: §e" + activeQuest.getQuestName() + " (" + activeQuest.getFileId() + ")");
                sender.sendMessage("§fProgresso: §e" +data.getProgress() + " / " + activeQuest.getRequireAmount());
                sender.sendMessage("§fTipo: §e" + activeQuest.getQuestType());
            }
        }

        sender.sendMessage( "§8» §eStorico:");
        sender.sendMessage("§fQuest Completate: §e" + data.getCompletedQuests().size());
        if (!data.getCompletedQuests().isEmpty()) {
            // Mostra le ultime 5 quest completate per non spammare la chat
            String history = data.getCompletedQuests().stream().limit(5).collect(Collectors.joining(", "));
            sender.sendMessage("§fUltime completate: §e" + history);
        }

        sender.sendMessage( "§f ");
    }

    @Override
    public String getPermission() {
        return "rubyisland.quest.admin";
    }

    @Override
    public int minArgs() {
        return 2; // Richiede /questadmin check <player>
    }

    @Override
    public Map<Integer, List<String>> getTabCompleter(CommandSender sender, Command command, String label, String[] args) {
        return null;
    }

    @Override
    public boolean allowedConsole() {
        return true;
    }
}