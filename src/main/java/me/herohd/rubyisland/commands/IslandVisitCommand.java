package me.herohd.rubyisland.commands;

import me.herohd.rubyisland.RubyIsland;
import me.herohd.rubyisland.objects.Island;
import me.herohd.rubyisland.utils.Messages;
import me.kr1s_d.commandframework.objects.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class IslandVisitCommand implements SubCommand
{
    @Override
    public String getSubCommandId() {
        return "visit";
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        Player player = (Player) commandSender;
        if(strings.length == 1) {
            player.sendMessage(Messages.WRONG_ARGUMENT.getAsString());
            return;
        }
        String tpPlayer = strings[1];
        String UUID = Bukkit.getOfflinePlayer(tpPlayer).getUniqueId().toString();
        final Island islandTemp = RubyIsland.getInstance().getIslandManager().getIslandTemp(UUID);
        if (islandTemp == null) {
            player.sendMessage(Messages.ISLAND_NOT_FOUND.getAsString());
            return;
        }

        if(!player.isOp() && (islandTemp.isBanned(player.getUniqueId().toString()) || !islandTemp.canJoin(player.getUniqueId().toString()))) {
            player.sendMessage(Messages.BANNED.getAsString());
            return;
        }
        islandTemp.teleport(player);
        player.sendMessage(Messages.TELEPORT_OTHER.getAsString().replace("%player%", tpPlayer));
    }

    @Override
    public String getPermission() {
        return  "rubyisland.user";
    }

    @Override
    public int minArgs() {
        return 0;
    }

    @Override
    public Map<Integer, List<String>> getTabCompleter(CommandSender commandSender, Command command, String s, String[] strings) {
        return Collections.emptyMap();
    }

    @Override
    public boolean allowedConsole() {
        return false;
    }
}
