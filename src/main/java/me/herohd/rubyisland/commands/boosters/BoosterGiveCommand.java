package me.herohd.rubyisland.commands.boosters;

import me.herohd.rubyisland.RubyIsland;
import me.herohd.rubyisland.boosters.BoosterManager;
import me.herohd.rubyisland.boosters.BoosterType;
import me.kr1s_d.commandframework.objects.SubCommand;
import me.kr1s_d.commandframework.utils.CommandMapBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BoosterGiveCommand implements SubCommand {
    @Override
    public String getSubCommandId() {
        return "give";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
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

        double value;
        try {
            value = Double.parseDouble(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Il valore deve essere un numero (es. 0.5).");
            return;
        }

        long duration;
        try {
            duration = Long.parseLong(args[4]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "La durata deve essere un numero intero (in minuti).");
            return;
        }

        BoosterManager boosterManager = RubyIsland.getInstance().getBoosterManager();
        boosterManager.activateBooster(target, type, value, duration);

        sender.sendMessage(ChatColor.GREEN + "Hai dato un booster " + type.name() + " a " + target.getName() + " per " + duration + " minuti.");
    }

    @Override
    public String getPermission() {
        return "rubyisland.booster.admin.give";
    }

    @Override
    public int minArgs() {
        return 4; // give <player> <type> <value> <duration>
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