package me.herohd.rubyisland.commands.boosters;

import me.herohd.rubyisland.RubyIsland;
import me.herohd.rubyisland.boosters.BoosterManager;
import me.herohd.rubyisland.boosters.BoosterType;
import me.kr1s_d.commandframework.objects.SubCommand;
import me.kr1s_d.commandframework.utils.CommandMapBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BoosterRemoveCommand implements SubCommand {
    @Override
    public String getSubCommandId() {
        return "remove";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (target == null || !target.hasPlayedBefore()) {
            sender.sendMessage(ChatColor.RED + "Giocatore non trovato.");
            return;
        }

        BoosterType type;
        try {
            type = BoosterType.valueOf(args[2].toUpperCase());
        } catch (IllegalArgumentException e) {
            sender.sendMessage(ChatColor.RED + "Tipo di booster non valido. Tipi validi: " +
                    Arrays.stream(BoosterType.values()).map(Enum::name).collect(Collectors.joining(", ")));
            return;
        }

        BoosterManager boosterManager = RubyIsland.getInstance().getBoosterManager();
        boolean removed = boosterManager.removeBooster(target.getUniqueId(), type);

        if (removed) {
            sender.sendMessage(ChatColor.GREEN + "Booster " + type.name() + " rimosso da " + target.getName() + ".");
        } else {
            sender.sendMessage(ChatColor.RED + "Il giocatore " + target.getName() + " non aveva un booster di tipo " + type.name() + " attivo.");
        }
    }

    @Override
    public String getPermission() {
        return "rubyisland.booster.admin.remove";
    }

    @Override
    public int minArgs() {
        return 3; // remove <player> <type>
    }

    @Override
    public Map<Integer, List<String>> getTabCompleter(CommandSender s, Command c, String l, String[] args) {
        return CommandMapBuilder.builder().set(2, Arrays.stream(BoosterType.values()).map(Enum::name).collect(Collectors.toList())).getMap();
    }

    @Override
    public boolean allowedConsole() {
        return true;
    }
}