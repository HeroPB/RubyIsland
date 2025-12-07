package me.herohd.rubyisland.commands;

import me.herohd.rubyisland.RubyIsland;
import me.herohd.rubyisland.manager.IslandChunkManager;
import me.herohd.rubyisland.objects.Island;
import me.herohd.rubyisland.utils.IslandUtils;
import me.herohd.rubyisland.utils.Messages;
import me.kr1s_d.commandframework.objects.BaseCommand;
import me.kr1s_d.commandframework.objects.SubCommand;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

@BaseCommand
public class IslandMainCommand implements SubCommand {
    @Override
    public String getSubCommandId() {
        return null;
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        Player player = (Player) commandSender;
        Island island = RubyIsland.getInstance().getIslandManager().getIsland(player.getUniqueId().toString());
        island.teleport(player);
        final Location location = IslandChunkManager.getCenterFromId(island.getId()).clone().add(1, 0, 0);
        location.setWorld(island.getSpawn().getWorld());
        RubyIsland.getInstance().getNpcManager().spawnNpc(player, location);

        player.sendMessage(Messages.TELEPORT_OWN.getAsString());
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
        return null;
    }

    @Override
    public boolean allowedConsole() {
        return false;
    }
}
