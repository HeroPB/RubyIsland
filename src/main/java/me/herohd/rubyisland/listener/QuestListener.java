package me.herohd.rubyisland.listener;

import com.vk2gpz.tokenenchant.api.TokenEnchantAPI;
import jdk.nashorn.internal.parser.Token;
import me.clip.autosell.events.SellEvent;
import me.herohd.rubyisland.RubyIsland;
import me.herohd.rubyisland.utils.Formatter;
import me.herohd.rubyisland.utils.Utils;
import me.kr1s_d.rubycrates.api.events.CrateOpenEvent;
import me.kr1s_d.rubycrates.api.objects.crate.ICrateReward;
import me.kr1s_d.rubyenchantmanager.events.WEnchantGainObjectEvent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class QuestListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        RubyIsland.getInstance().getQuestManager().loadPlayerData(player);
    }

    @EventHandler
    public void onSellEvent(SellEvent event) {
        final OfflinePlayer offline = event.getPlayer();
        if(!offline.isOnline()) return;
        final Player player = Bukkit.getPlayer(offline.getName());
        if(player == null) return;

        final double gained = event.getGained();
        double newGained = gained * RubyIsland.getInstance().getPlayerPointsManager().getPoints(player, "GUADAGNO_BLOCCHI") / 100;
        RubyIsland.getEconomy().depositPlayer(player, newGained);
    }

    @EventHandler
    public void onCrateOpen(CrateOpenEvent event) {
        final Player player = event.getOpener();
        final double points = RubyIsland.getInstance().getPlayerPointsManager().getPoints(player, "DOPPIE_RICOMPENSE");
        final boolean b = Utils.checkChance(points);
        if(!b) return;

        final ICrateReward next = event.getCrate().getCrateRewardsList().next();
        next.executeCommand(player);
    }

    @EventHandler
    public void onEnchantGaine(WEnchantGainObjectEvent event) {
        final Player player = event.getPlayer();
        final double points = RubyIsland.getInstance().getPlayerPointsManager().getPoints(player, "GUADAGNO_ENCHANT");
        final double gained = event.getGained() *  points/100;
        if(gained <= 0) return;

        switch (event.getType().toUpperCase()) {
            case "CHARITY":
            case "MIDASAURA_MONEY":
                RubyIsland.getEconomy().depositPlayer(player, gained);
                player.sendMessage("§c§lEVENTO§f: Hai trovato §c" + Formatter.format(gained) + " §fsoldi");
                break;
            default:
                TokenEnchantAPI.getInstance().addTokens(player, gained);
                player.sendMessage("§e§lEVENTO§f: Hai trovato §e" + Formatter.format(gained) + " §ftoken");
        }
    }
}
