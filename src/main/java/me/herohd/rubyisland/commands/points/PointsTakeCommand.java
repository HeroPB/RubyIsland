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

// --- SOTTOCOMANDO /pointsadmin take ---
public class PointsTakeCommand implements SubCommand {
    @Override
    public String getSubCommandId() {
        return "take";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // Uso: /... take <player> <type> <amount>
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) { /* ... messaggio errore ... */
            return;
        }

        String type = args[2].toUpperCase();
        PlayerPointsManager pointsManager = RubyIsland.getInstance().getPlayerPointsManager();
        if (!pointsManager.getValidPointTypes().contains(type)) { /* ... messaggio errore ... */
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[3]);
        } catch (NumberFormatException e) { /* ... messaggio errore ... */
            return;
        }

        pointsManager.takePoints(target, type, amount);
        sender.sendMessage(ChatColor.GREEN + "Rimossi " + amount + " punti di tipo '" + type + "' da " + target.getName() + ".");
        target.sendMessage(ChatColor.RED + "Ti sono stati rimossi " + amount + " punti di tipo '" + type + "'.");
    }

    @Override
    public String getPermission() {
        return "rubyisland.points.admin";
    }

    @Override
    public int minArgs() {
        return 4;
    }

    @Override
    public boolean allowedConsole() {
        return true;
    }

    @Override
    public Map<Integer, List<String>> getTabCompleter(CommandSender s, Command c, String l, String[] args) {
        if (args.length == 3) {
            List<String> pointTypes = new ArrayList<>(RubyIsland.getInstance().getPlayerPointsManager().getValidPointTypes());
            return CommandMapBuilder.builder().set(2, pointTypes).getMap();
        }
        return null;
    }
}
