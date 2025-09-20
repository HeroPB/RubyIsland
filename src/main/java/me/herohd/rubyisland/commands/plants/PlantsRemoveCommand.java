package me.herohd.rubyisland.commands.plants;

import me.herohd.rubyisland.RubyIsland;
import me.herohd.rubyisland.plants.TropicalPlantManager;
import me.herohd.rubyisland.plants.PlacedPlant;
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

public class PlantsRemoveCommand implements SubCommand {
    @Override
    public String getSubCommandId() {
        return "remove";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Questo comando può essere usato solo da un giocatore.");
            return;
        }

        Player admin = (Player) sender;
        Block targetBlock = admin.getTargetBlock(null, 10);

        if (targetBlock == null || targetBlock.getType() == Material.AIR) {
            admin.sendMessage(ChatColor.RED + "Non stai guardando nessun blocco o il blocco è troppo lontano.");
            return;
        }

        Location location = targetBlock.getLocation();
        TropicalPlantManager plantManager = RubyIsland.getInstance().getTropicalPlantManager();
        PlacedPlant plant = plantManager.getActivePlants().get(location);

        if (plant == null) {
            admin.sendMessage(ChatColor.RED + "Non c'è nessuna pianta tropicale in questa posizione.");
            return;
        }
        plant.pickUp(admin);
        admin.sendMessage(ChatColor.GOLD + "Hai rimosso forzatamente la pianta e l'hai ricevuta nel tuo inventario.");
    }

    @Override
    public String getPermission() {
        return "rubyisland.plant.admin.remove";
    }

    @Override
    public int minArgs() {
        return 1; // Solo il sottocomando "remove"
    }

    @Override
    public Map<Integer, List<String>> getTabCompleter(CommandSender s, Command c, String l, String[] args) {
        return null; // Nessun argomento da completare
    }

    @Override
    public boolean allowedConsole() {
        return false;
    }
}
