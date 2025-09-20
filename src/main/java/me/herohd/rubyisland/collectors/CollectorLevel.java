package me.herohd.rubyisland.collectors;

public class CollectorLevel {
    private final long maxCapacity;
    private final int breakAmount;

    public CollectorLevel(long maxCapacity, int breakAmount) {
        this.maxCapacity = maxCapacity;
        this.breakAmount = breakAmount;
    }

    public long getMaxCapacity() {
        return maxCapacity;
    }

    public int getBreakAmount() {
        return breakAmount;
    }
}
