package me.herohd.rubyisland.commands;

import me.herohd.rubyisland.RubyIsland;
import me.herohd.rubyisland.objects.Island;
import me.herohd.rubyisland.utils.Messages;
import me.kr1s_d.commandframework.objects.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class IslandTrustCommand  implements SubCommand {
    @Override
    public String getSubCommandId() {
        return "trust";
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        Player player = (Player) commandSender;
        String ban = strings[1];
        Player op = Bukkit.getPlayer(ban);
        Island island = RubyIsland.getInstance().getIslandManager().getIslandTemp(player.getUniqueId().toString());
        if(op == null || op.getName().equalsIgnoreCase(player.getName())) {
            player.sendMessage(Messages.PLAYER_NOT_FOUND.getAsString());
            return;
        }
        if(island == null) {
            player.sendMessage(Messages.NOT_HAVE_ISLAND.getAsString());
            return;
        }
        String uuid = op.getUniqueId().toString();
        boolean banned = island.trustPlayer(uuid);
        if(banned) player.sendMessage(Messages.TRUST_PLAYER.getAsString().replace("%player%", op.getName()));
        else player.sendMessage(Messages.ALTREDY_EXIST_PLAYER.getAsString().replace("%type%", "trustato"));
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

