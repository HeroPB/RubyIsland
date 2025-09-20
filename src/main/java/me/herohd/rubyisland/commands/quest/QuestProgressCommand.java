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

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class QuestProgressCommand implements SubCommand {
    @Override
    public String getSubCommandId() {
        return "progress";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // Uso: /questadmin progress <add|remove> <player> <amount>
        String action = args[1];
        Player target = Bukkit.getPlayer(args[2]);
        int amount;

        try {
            amount = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cLa quantità deve essere un numero.");
            return;
        }

        if (target == null) {
            sender.sendMessage("§cQuesto comando può essere usato solo su giocatori online.");
            return;
        }

        QuestManager questManager = RubyIsland.getInstance().getQuestManager();
        PlayerQuestData data = questManager.getPlayerData(target);

        if (data == null || data.getActiveQuestId() == null) {
            sender.sendMessage("§cQuesto giocatore non ha una quest attiva.");
            return;
        }

        if (action.equalsIgnoreCase("add")) {
            questManager.progressQuest(target, amount);
            sender.sendMessage("§cAggiunti §f" + amount + " §cpunti progresso a §f" + target.getName());
        } else if (action.equalsIgnoreCase("remove")) {
            data.setProgress(Math.max(0, data.getProgress() - amount));
            RubyIsland.getInstance().getMySQLManager().savePlayerQuestStatus(data);
            sender.sendMessage("§fRimossi §e" + amount + " §fpunti progresso da §e" + target.getName());
        } else {
            sender.sendMessage("§fUso: §c/questadmin progress <add|remove> <player> <amount>");
        }
    }

    @Override
    public String getPermission() {
        return "rubyisland.quest.admin";
    }

    @Override
    public int minArgs() {
        return 4;
    }

    @Override
    public Map<Integer, List<String>> getTabCompleter(CommandSender s, Command c, String l, String[] args) {
        return CommandMapBuilder.builder().set(1, Arrays.asList("add", "remove")).set(3, Arrays.asList("<AMOUNT>")).getMap();
    }

    @Override
    public boolean allowedConsole() {
        return true;
    }
}