package me.herohd.rubyisland.commands.points;


import me.herohd.rubyisland.RubyIsland;
import me.herohd.rubyisland.manager.PlayerPointsManager;
import me.kr1s_d.commandframework.objects.SubCommand;
import me.kr1s_d.commandframework.utils.CommandMapBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PointsAddCommand implements SubCommand {
    @Override public String getSubCommandId() { return "add"; }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // Uso: /... add <player> <type> <amount>
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Giocatore non trovato o non online.");
            return;
        }

        String type = args[2].toUpperCase();
        PlayerPointsManager pointsManager = RubyIsland.getInstance().getPlayerPointsManager();
        if (!pointsManager.getValidPointTypes().contains(type)) {
            sender.sendMessage(ChatColor.RED + "Tipo di punto non valido: " + type);
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "La quantità deve essere un numero.");
            return;
        }

        pointsManager.addPoints(target, type, amount);
        sender.sendMessage(ChatColor.GREEN + "Aggiunti " + amount + " punti di tipo '" + type + "' a " + target.getName() + ".");
        target.sendMessage("§c§lISOLOTTO§8: §fHai ricevuto §c+" + amount + "% §fper §c'" + type + "'!");
    }

    @Override public String getPermission() { return "rubyisland.points.admin"; }
    @Override public int minArgs() { return 4; }
    @Override public boolean allowedConsole() { return true; }

    @Override
    public Map<Integer, List<String>> getTabCompleter(CommandSender s, Command c, String l, String[] args) {
        if (args.length == 3) {
            List<String> pointTypes = new ArrayList<>(RubyIsland.getInstance().getPlayerPointsManager().getValidPointTypes());
            return CommandMapBuilder.builder().set(2, pointTypes).getMap();
        }
        return null;
    }
}

