package me.herohd.rubyisland.commands.quest;

import me.herohd.rubyisland.RubyIsland;
import me.herohd.rubyisland.quests.PlayerQuestData;
import me.herohd.rubyisland.quests.QuestManager;
import me.kr1s_d.commandframework.objects.SubCommand;
import me.kr1s_d.commandframework.utils.CommandMapBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class QuestSetCommand implements SubCommand {
    @Override
    public String getSubCommandId() {
        return "set";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // Uso: /questadmin set <player> <quest_id>
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        String questId = args[2];
        QuestManager questManager = RubyIsland.getInstance().getQuestManager();

        if (questManager.getQuestById(questId) == null) {
            sender.sendMessage("§cQuest ID non valido.");
            return;
        }

        UUID targetUUID = target.getUniqueId();
        // Carica i dati del giocatore se è online, altrimenti crea un nuovo profilo per il salvataggio
        PlayerQuestData data = questManager.getPlayerData(target.getPlayer());
        if (data == null) {
            data = new PlayerQuestData(targetUUID, questId); // Crea un oggetto temporaneo per i giocatori offline
        } else {
            data.setActiveQuestId(questId);
            data.setProgress(0);
        }

        RubyIsland.getInstance().getMySQLManager().savePlayerQuestStatus(data);

        sender.sendMessage( "§fHai impostato la quest '§c" + questId + "§f' per §c" + target.getName() + "§f.");
        if (target.isOnline()) {
            ((Player)target).sendMessage("§fUn amministratore ha impostato la tua quest attiva: §c" + questId);
        }
    }

    @Override
    public String getPermission() {
        return "rubyisland.quest.admin";
    }

    @Override
    public int minArgs() {
        return 3;
    }

    @Override
    public Map<Integer, List<String>> getTabCompleter(CommandSender s, Command c, String l, String[] args) {
        return CommandMapBuilder.builder().set(2, new ArrayList<>(RubyIsland.getInstance().getQuestManager().getLoadedQuests().keySet())).getMap();
    }

    @Override
    public boolean allowedConsole() {
        return true;
    }
}