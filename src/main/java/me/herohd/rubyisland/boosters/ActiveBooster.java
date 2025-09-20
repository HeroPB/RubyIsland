package me.herohd.rubyisland.boosters;

import org.bukkit.scheduler.BukkitTask;
import java.util.concurrent.TimeUnit;

public class ActiveBooster {

    private final BoosterType type;
    private double value; // Es. 0.5 per un moltiplicatore, 1.0 per on/off
    private long expirationTimestamp;
    private BukkitTask expirationTask;

    public ActiveBooster(BoosterType type, double value, long durationMinutes) {
        this.type = type;
        this.value = value;
        this.expirationTimestamp = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(durationMinutes);
    }

    // Getters
    public BoosterType getType() { return type; }
    public double getValue() { return value; }
    public boolean isActive() { return System.currentTimeMillis() < expirationTimestamp; }

    // Metodi per la gestione
    public void addValue(double valueToAdd) { this.value += valueToAdd; }
    public void resetDuration(long durationMinutes) { this.expirationTimestamp = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(durationMinutes); }
    public void setExpirationTask(BukkitTask expirationTask) { this.expirationTask = expirationTask; }

    public void cancelExpirationTask() {
        if (this.expirationTask != null && !this.expirationTask.isCancelled()) {
            this.expirationTask.cancel();
        }
    }
    public long getExpirationTimestamp() {
        return expirationTimestamp;
    }
}