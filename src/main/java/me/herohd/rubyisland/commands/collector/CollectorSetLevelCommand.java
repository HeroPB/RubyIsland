package me.herohd.rubyisland.commands.collector;


import me.herohd.rubyisland.RubyIsland;
import me.herohd.rubyisland.collectors.CollectorManager;
import me.kr1s_d.commandframework.objects.SubCommand;
import me.kr1s_d.commandframework.utils.CommandMapBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.Console;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CollectorSetLevelCommand implements SubCommand {
    @Override
    public String getSubCommandId() { return "setlevel"; }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // Uso: /... setlevel <player> <collector_id> <level>
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Giocatore non trovato o non online.");
            return;
        }

        String collectorId = args[2];
        CollectorManager collectorManager = RubyIsland.getInstance().getCollectorManager();
        if (!collectorManager.getCollectorTypes().containsKey(collectorId)) {
            sender.sendMessage(ChatColor.RED + "L'ID del collector '" + collectorId + "' non è valido.");
            return;
        }

        int level;
        try {
            level = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Il livello deve essere un numero intero.");
            return;
        }

        // 1. Aggiungi il permesso al giocatore
        String permission = "rubyisland.container." + collectorId + "." + level;
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + target.getName() + " permission set " + permission + " true");

        // 2. Aggiorna la cache del giocatore per applicare la modifica istantaneamente
        collectorManager.forceUpdatePlayerLevelCache(target.getUniqueId(), collectorId, level);

        sender.sendMessage(ChatColor.GREEN + "Hai impostato il livello " + level + " per il collector '" + collectorId + "' a " + target.getName() + ".");
        target.sendMessage("§c§lCOLLECTOR§8: §fIl tuo livello per il collector §c'" + collectorId + "' §fè stato aggiornato a §c" + level + "§f!");
    }

    @Override
    public String getPermission() { return "rubyisland.collector.admin.setlevel"; }

    @Override
    public int minArgs() { return 4; }

    @Override
    public Map<Integer, List<String>> getTabCompleter(CommandSender s, Command c, String l, String[] args) {
        if (args.length == 3) {
            List<String> collectorIds = new ArrayList<>(RubyIsland.getInstance().getCollectorManager().getCollectorTypes().keySet());
            return CommandMapBuilder.builder().set(2, collectorIds).getMap();
        }
        return null;
    }

    @Override
    public boolean allowedConsole() { return true; }
}
