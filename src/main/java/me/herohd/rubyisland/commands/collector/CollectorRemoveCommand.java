package me.herohd.rubyisland.commands.collector;
import me.herohd.rubyisland.RubyIsland;
import me.herohd.rubyisland.collectors.CollectorManager;
import me.herohd.rubyisland.collectors.PlacedCollector;
import me.kr1s_d.commandframework.objects.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class CollectorRemoveCommand implements SubCommand {
    @Override
    public String getSubCommandId() { return "remove"; }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Questo comando può essere usato solo da un giocatore.");
            return;
        }

        Player admin = (Player) sender;
        Block targetBlock = admin.getTargetBlock(null, 10);

        if (targetBlock == null || targetBlock.getType() == Material.AIR) {
            admin.sendMessage(ChatColor.RED + "Non stai guardando nessun blocco.");
            return;
        }

        Location location = targetBlock.getLocation();
        CollectorManager collectorManager = RubyIsland.getInstance().getCollectorManager();
        PlacedCollector collector = collectorManager.activeCollectors.get(location);

        if (collector == null) {
            admin.sendMessage(ChatColor.RED + "Il blocco che stai guardando non è un collector attivo.");
            return;
        }

        // Il metodo pickUp si occupa di tutta la logica di rimozione e di dare l'item
        collector.pickUp(admin);
        admin.sendMessage(ChatColor.GREEN + "Collector rimosso e aggiunto al tuo inventario.");
    }

    @Override
    public String getPermission() { return "rubyisland.collector.admin.remove"; }

    @Override
    public int minArgs() { return 1; }

    @Override
    public Map<Integer, List<String>> getTabCompleter(CommandSender s, Command c, String l, String[] args) { return null; }

    @Override
    public boolean allowedConsole() { return false; }
}