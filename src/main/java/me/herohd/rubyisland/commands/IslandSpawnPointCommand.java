package me.herohd.rubyisland.commands;

import me.herohd.rubyisland.RubyIsland;
import me.herohd.rubyisland.objects.Island;
import me.herohd.rubyisland.utils.IslandUtils;
import me.herohd.rubyisland.utils.Messages;
import me.kr1s_d.commandframework.objects.SubCommand;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class IslandSpawnPointCommand implements SubCommand {
    @Override
    public String getSubCommandId() {
        return "sethome";
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        Player player = (Player) commandSender;
        Location loc = player.getLocation();
        Island island = RubyIsland.getInstance().getIslandManager().getIslandTemp(player.getUniqueId().toString());
        if(island == null) {
            player.sendMessage(Messages.NOT_IN_YOUR_ISLAND.getAsString());
            return;
        }
        if(!IslandUtils.isInHisIsland(player, loc)) {
            player.sendMessage(Messages.NOT_IN_YOUR_ISLAND.getAsString());
            return;
        }
        island.setSpawn(loc);
        island.save();
        player.sendMessage(Messages.NEW_SPAWNPOINT.getAsString());
    }

    @Override
    public String getPermission() {
        return "rubyisland.user";
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
