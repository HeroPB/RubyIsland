package me.herohd.rubyisland.commands.boosters;

import me.herohd.rubyisland.RubyIsland;
import me.herohd.rubyisland.boosters.ActiveBooster;
import me.herohd.rubyisland.boosters.BoosterManager;
import me.herohd.rubyisland.boosters.BoosterType;
import me.kr1s_d.commandframework.objects.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
public class BoosterInfoCommand implements SubCommand {
    @Override
    public String getSubCommandId() {
        return "check";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (target == null || !target.hasPlayedBefore()) {
            sender.sendMessage(ChatColor.RED + "Giocatore non trovato.");
            return;
        }

        BoosterManager boosterManager = RubyIsland.getInstance().getBoosterManager();
        Map<BoosterType, ActiveBooster> activeBoosters = boosterManager.getActiveBoostersForPlayer(target.getUniqueId());

        if (activeBoosters.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "Il giocatore " + target.getName() + " non ha booster attivi.");
            return;
        }

        sender.sendMessage(ChatColor.GOLD + "---- Booster Attivi per " + target.getName() + " ----");
        for (Map.Entry<BoosterType, ActiveBooster> entry : activeBoosters.entrySet()) {
            ActiveBooster booster = entry.getValue();
            long remainingMillis = booster.getExpirationTimestamp() - System.currentTimeMillis();
            String remainingTime = formatTime(remainingMillis);

            sender.sendMessage(ChatColor.YELLOW + " - Tipo: " + ChatColor.WHITE + booster.getType().name() +
                    ChatColor.YELLOW + ", Valore: " + ChatColor.WHITE + booster.getValue() +
                    ChatColor.YELLOW + ", Tempo Rimanente: " + ChatColor.WHITE + remainingTime);
        }
    }

    private String formatTime(long millis) {
        if (millis < 0) return "Scaduto";
        long totalSeconds = TimeUnit.MILLISECONDS.toSeconds(millis);
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    @Override
    public String getPermission() {
        return "rubyisland.booster.admin.check";
    }

    @Override
    public int minArgs() {
        return 2; // check <player>
    }

    @Override
    public Map<Integer, List<String>> getTabCompleter(CommandSender s, Command c, String l, String[] args) {
        return null;
    }

    @Override
    public boolean allowedConsole() {
        return true;
    }
}