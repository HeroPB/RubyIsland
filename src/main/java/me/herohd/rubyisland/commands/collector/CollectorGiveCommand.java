package me.herohd.rubyisland.commands.collector;

import me.herohd.rubyisland.RubyIsland;
import me.herohd.rubyisland.collectors.CollectorManager;
import me.herohd.rubyisland.collectors.CollectorType;
import me.herohd.rubyisland.utils.NBTEditor;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CollectorGiveCommand implements SubCommand {
    @Override
    public String getSubCommandId() { return "give"; }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // Uso: /... give <player> <collector_id> [blocks_inside]
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Giocatore non trovato o non online.");
            return;
        }

        String collectorId = args[2];
        CollectorManager collectorManager = RubyIsland.getInstance().getCollectorManager();
        CollectorType type = collectorManager.getCollectorTypes().get(collectorId);

        if (type == null) {
            sender.sendMessage(ChatColor.RED + "L'ID del collector '" + collectorId + "' non è valido.");
            return;
        }

        long blocksInside = 0;
        if (args.length > 3) {
            try {
                blocksInside = Long.parseLong(args[3]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "La quantità di blocchi deve essere un numero.");
                return;
            }
        }

        // 1. Crea l'item di base
        ItemStack collectorItem = type.getCollectorItem().clone();
        ItemMeta meta = collectorItem.getItemMeta();

        // 2. Costruisci nome e lore dinamicamente
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', type.getItemName()));
            long finalBlocksInside = blocksInside;
            List<String> lore = type.getItemLore().stream()
                    .map(line -> ChatColor.translateAlternateColorCodes('&', line)
                            .replace("%owner%", target.getName())
                            .replace("%blocks%", String.valueOf(finalBlocksInside)))
                    .collect(Collectors.toList());
            meta.setLore(lore);
            collectorItem.setItemMeta(meta);
        }

        // 3. Aggiungi tutti i dati NBT necessari
        collectorItem = NBTEditor.addNBT(collectorItem, "collector-id", collectorId);
        collectorItem = NBTEditor.addNBT(collectorItem, "collector-owner", target.getUniqueId().toString());
        collectorItem = NBTEditor.addNBT(collectorItem, "collector-amount", blocksInside);

        // 4. Dai l'item al giocatore
        if (target.getInventory().firstEmpty() == -1) {
            sender.sendMessage(ChatColor.RED + "L'inventario di " + target.getName() + " è pieno!");
        } else {
            target.getInventory().addItem(collectorItem);
            sender.sendMessage(ChatColor.GREEN + "Hai dato un collector di tipo '" + collectorId + "' a " + target.getName() + ".");
        }
    }

    @Override
    public String getPermission() { return "rubyisland.collector.admin.give"; }

    @Override
    public int minArgs() { return 3; }

    @Override
    public Map<Integer, List<String>> getTabCompleter(CommandSender s, Command c, String l, String[] args) {
        if (args.length == 3) {
            List<String> collectorIds = new ArrayList<>(RubyIsland.getInstance().getCollectorManager().getCollectorTypes().keySet());
            return CommandMapBuilder.builder().set(2, collectorIds).getMap();
        }
        return null;
    }

    @Override
    public boolean allowedConsole() { return true; }
}
