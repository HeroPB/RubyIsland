package me.herohd.rubyisland.commands;

import me.herohd.rubyisland.RubyIsland;
import me.herohd.rubyisland.objects.Island;
import me.herohd.rubyisland.utils.Messages;
import me.kr1s_d.commandframework.objects.SubCommand;
import me.kr1s_d.commandframework.utils.CommandMapBuilder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class IslandSetStatusCommand implements SubCommand {
    @Override
    public String getSubCommandId() {
        return "setstatus";
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        Player player = (Player) commandSender;
        String status = strings[1];
        Island is = RubyIsland.getInstance().getIslandManager().getIslandOrNull(player.getUniqueId().toString());
        if(is == null) {
            player.sendMessage(Messages.NOT_HAVE_ISLAND.getAsString());
            return;
        }
        switch (status.toUpperCase()) {
            case "OPEN":
                if(!is.isClosed()) {
                    player.sendMessage(Messages.ALREAD_IN_STATUS.getAsString().replace("%status%", "APERTA"));
                    return;
                }
                is.setClosed(false);
                player.sendMessage(Messages.OPEN.getAsString());
                break;
            case "CLOSE":
                if(is.isClosed()) {
                    player.sendMessage(Messages.ALREAD_IN_STATUS.getAsString().replace("%status%", "CHIUSA"));
                    return;
                }
                is.setClosed(true);
                player.sendMessage(Messages.CLOSE.getAsString());
                break;
            default:
                player.sendMessage(Messages.WRONG_ARGUMENT.getAsString());
        }
    }

    @Override
    public String getPermission() {
        return "rubyisland.user";
    }

    @Override
    public int minArgs() {
        return 1;
    }

    @Override
    public Map<Integer, List<String>> getTabCompleter(CommandSender commandSender, Command command, String s, String[] strings) {
        return CommandMapBuilder.builder().set(1, Arrays.asList("OPEN", "CLOSE")).getMap();
    }

    @Override
    public boolean allowedConsole() {
        return false;
    }
}
