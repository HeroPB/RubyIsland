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

// --- SOTTOCOMANDO /pointsadmin check ---
public class PointsCheckCommand implements SubCommand {
    @Override
    public String getSubCommandId() {
        return "check";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // Uso: /... check <player> <type>
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) { /* ... messaggio errore ... */
            return;
        }

        String type = args[2].toUpperCase();
        PlayerPointsManager pointsManager = RubyIsland.getInstance().getPlayerPointsManager();
        if (!pointsManager.getValidPointTypes().contains(type)) { /* ... messaggio errore ... */
            return;
        }

        double points = pointsManager.getPoints(target, type);
        sender.sendMessage(ChatColor.YELLOW + target.getName() + " ha " + points + " punti di tipo '" + type + "'.");
    }

    @Override
    public String getPermission() {
        return "rubyisland.points.admin";
    }

    @Override
    public int minArgs() {
        return 3;
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
