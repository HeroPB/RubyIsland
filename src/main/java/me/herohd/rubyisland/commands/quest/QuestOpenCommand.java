package me.herohd.rubyisland.commands.quest;

import me.herohd.rubyisland.gui.QuestGui;
import me.kr1s_d.commandframework.objects.SubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class QuestOpenCommand implements SubCommand {
    @Override
    public String getSubCommandId() {
        return "open";
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        Player player = (Player) commandSender;
        new QuestGui(player);
    }

    @Override
    public String getPermission() {
        return "rubyisland.quest.default";
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
