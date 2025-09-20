package me.herohd.rubyisland.boosters;

import me.herohd.rubyisland.RubyIsland;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BoosterManager {

    private final RubyIsland plugin;
    // Struttura dati: UUID Giocatore -> Mappa di (Tipo Booster -> Booster Attivo)
    private final Map<UUID, Map<BoosterType, ActiveBooster>> activePlayerBoosters = new ConcurrentHashMap<>();

    public BoosterManager(RubyIsland plugin) {
        this.plugin = plugin;
    }

    /**
     * Attiva o rinnova un booster per un giocatore.
     * Questo è il metodo principale da chiamare.
     */
    public void activateBooster(Player player, BoosterType type, double value, long durationMinutes) {
        final UUID playerUuid = player.getUniqueId();

        // Prende la mappa dei booster del giocatore, o ne crea una nuova se non esiste
        Map<BoosterType, ActiveBooster> playerBoosters = activePlayerBoosters.computeIfAbsent(playerUuid, k -> new ConcurrentHashMap<>());

        ActiveBooster existingBooster = playerBoosters.get(type);

        if (existingBooster != null && existingBooster.isActive()) {
            // Booster esistente: annulla il vecchio task, aggiorna i valori e riprogramma
            existingBooster.cancelExpirationTask();
            existingBooster.addValue(value);
            existingBooster.resetDuration(durationMinutes);

            player.sendMessage("§b§lBOOSTER POTENZIATO! §f(" + type.getDisplayName() + ")");
            scheduleExpirationMessage(player, existingBooster);
        } else {
            // Nuovo booster: crealo e schedula il task di scadenza
            ActiveBooster newBooster = new ActiveBooster(type, value, durationMinutes);
            playerBoosters.put(type, newBooster);

            player.sendMessage("§b§lBOOSTER ATTIVATO! §f(" + type.getDisplayName() + ") per " + durationMinutes + " minuti!");
            scheduleExpirationMessage(player, newBooster);
        }
    }

    /**
     * Ottiene il valore di un tipo specifico di booster per un giocatore.
     * @return Il valore del booster (es. 0.5), o 0 se non attivo.
     */
    public double getBoosterValue(UUID playerUuid, BoosterType type) {
        Map<BoosterType, ActiveBooster> playerBoosters = activePlayerBoosters.get(playerUuid);
        if (playerBoosters == null) return 0.0;

        ActiveBooster booster = playerBoosters.get(type);
        if (booster != null && booster.isActive()) {
            return booster.getValue();
        }
        return 0.0;
    }

    private void scheduleExpirationMessage(Player player, ActiveBooster booster) {
        final UUID playerUuid = player.getUniqueId();
        final BoosterType type = booster.getType();
        long durationTicks = 20L * 60L * 10; // 10 minuti

        BukkitTask expirationTask = new BukkitRunnable() {
            @Override
            public void run() {
                Map<BoosterType, ActiveBooster> playerBoosters = activePlayerBoosters.get(playerUuid);
                if (playerBoosters == null) return;

                ActiveBooster currentBooster = playerBoosters.get(type);
                if (currentBooster != null && !currentBooster.isActive()) {
                    playerBoosters.remove(type); // Rimuovi solo il booster scaduto

                    Player onlinePlayer = Bukkit.getPlayer(playerUuid);
                    if (onlinePlayer != null) {
                        onlinePlayer.sendMessage("§c§lATTENZIONE! §eIl tuo " + type.getDisplayName() + " è appena scaduto!");
                    }
                }
            }
        }.runTaskLater(this.plugin, durationTicks);

        booster.setExpirationTask(expirationTask);
    }

    public boolean removeBooster(UUID playerUuid, BoosterType type) {
        Map<BoosterType, ActiveBooster> playerBoosters = activePlayerBoosters.get(playerUuid);
        if (playerBoosters == null) return false;

        ActiveBooster booster = playerBoosters.get(type);
        if (booster != null) {
            booster.cancelExpirationTask(); // Annulla il messaggio di scadenza
            playerBoosters.remove(type);
            return true;
        }
        return false;
    }

    /**
     * Restituisce una mappa di tutti i booster attivi per un giocatore.
     * @param playerUuid L'UUID del giocatore.
     * @return Una mappa contenente solo i booster attualmente attivi. Può essere vuota.
     */
    public Map<BoosterType, ActiveBooster> getActiveBoostersForPlayer(UUID playerUuid) {
        Map<BoosterType, ActiveBooster> playerBoosters = activePlayerBoosters.get(playerUuid);
        if (playerBoosters == null) {
            return Collections.emptyMap(); // Restituisce una mappa vuota se il giocatore non ha mai avuto booster
        }

        // Filtra via i booster scaduti per sicurezza
        playerBoosters.entrySet().removeIf(entry -> !entry.getValue().isActive());

        return playerBoosters;
    }
}
