package me.herohd.rubyisland.plants;

import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.TimeUnit;

public class MineBooster {

    private double boostMultiplier;
    private long expirationTimestamp;
    private BukkitTask expirationTask; // <-- NUOVO CAMPO PER TRACCIARE IL TASK

    public MineBooster(double initialMultiplier, long durationMinutes) {
        this.boostMultiplier = initialMultiplier;
        this.expirationTimestamp = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(durationMinutes);
    }

    // --- NUOVI METODI PER GESTIRE IL TASK ---

    /**
     * Imposta il task di Bukkit che gestirà la scadenza.
     * @param expirationTask Il task schedulato.
     */
    public void setExpirationTask(BukkitTask expirationTask) {
        this.expirationTask = expirationTask;
    }

    /**
     * Annulla il task di scadenza precedente.
     * Fondamentale quando si rinnova un booster.
     */
    public void cancelExpirationTask() {
        if (this.expirationTask != null && !this.expirationTask.isCancelled()) {
            this.expirationTask.cancel();
        }
    }

    // --- METODI ESISTENTI (invariati) ---

    public double getBoostMultiplier() {
        return boostMultiplier;
    }

    public boolean isActive() {
        return System.currentTimeMillis() < expirationTimestamp;
    }

    public void addMultiplier(double multiplierToAdd) {
        this.boostMultiplier += multiplierToAdd;
    }

    public void resetDuration(long durationMinutes) {
        this.expirationTimestamp = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(durationMinutes);
    }
}