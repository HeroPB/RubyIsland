package me.herohd.rubyisland.commands.plants;


import me.herohd.rubyisland.RubyIsland;
import me.herohd.rubyisland.plants.TropicalPlantManager;
import me.herohd.rubyisland.plants.PlantType;
import me.kr1s_d.commandframework.objects.SubCommand;
import me.kr1s_d.commandframework.utils.CommandMapBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PlantGiveCommand implements SubCommand {
    @Override
    public String getSubCommandId() {
        return "give";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // Uso: /... give <player> <plant_id> [amount]
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Giocatore non trovato o non online.");
            return;
        }

        String plantId = args[2];
        TropicalPlantManager plantManager = RubyIsland.getInstance().getTropicalPlantManager();
        PlantType plantType = plantManager.getPlantTypes().get(plantId);

        if (plantType == null) {
            sender.sendMessage(ChatColor.RED + "L'ID della pianta '" + plantId + "' non è valido.");
            sender.sendMessage(ChatColor.YELLOW + "Usa il tab-completer per vedere gli ID disponibili.");
            return;
        }

        int amount = 1;
        if (args.length > 3) {
            try {
                amount = Integer.parseInt(args[3]);
                if (amount <= 0) {
                    sender.sendMessage(ChatColor.RED + "La quantità deve essere un numero positivo.");
                    return;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "La quantità deve essere un numero valido.");
                return;
            }
        }

        // Clona l'item e imposta la quantità
        ItemStack plantItem = plantType.getPlantItem().clone();
        ItemMeta meta = plantItem.getItemMeta();
        List<String> firstLore = meta.getLore();
        List<String> result = new ArrayList<>();

        for (String s : firstLore) {
            result.add(s.replaceAll("%level%", "1"));
        }

        meta.setLore(result);
        plantItem.setItemMeta(meta);
        plantItem.setAmount(amount);

        // Dai l'item al giocatore
        if (target.getInventory().firstEmpty() == -1) {
            sender.sendMessage(ChatColor.RED + "L'inventario di " + target.getName() + " è pieno!");
            // Opzionale: Fai cadere l'item a terra
            // target.getWorld().dropItem(target.getLocation(), plantItem);
        } else {
            target.getInventory().addItem(plantItem);
            sender.sendMessage(ChatColor.GREEN + "Hai dato " + amount + "x " + plantItem.getItemMeta().getDisplayName() + ChatColor.GREEN + " a " + target.getName() + ".");
            target.sendMessage(ChatColor.GREEN + "Hai ricevuto " + amount + "x " + plantItem.getItemMeta().getDisplayName() + ChatColor.GREEN + "!");
        }
    }

    @Override
    public String getPermission() {
        return "rubyisland.plant.admin";
    }

    @Override
    public int minArgs() {
        return 3; // give <player> <plant_id>
    }

    @Override
    public Map<Integer, List<String>> getTabCompleter(CommandSender s, Command c, String l, String[] args) {
        // Suggerisce gli ID delle piante come terzo argomento (indice 2)
        if (args.length == 3) {
            List<String> plantIds = new ArrayList<>(RubyIsland.getInstance().getTropicalPlantManager().getPlantTypes().keySet());
            return CommandMapBuilder.builder().set(2, plantIds).getMap();
        }
        // Suggerisce la quantità come quarto argomento (indice 3)
        if (args.length == 4) {
            return CommandMapBuilder.builder().set(3, Arrays.asList("<amount>")).getMap();
        }
        return null;
    }

    @Override
    public boolean allowedConsole() {
        return true;
    }
}
