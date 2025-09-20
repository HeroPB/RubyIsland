package me.herohd.rubyisland.plants;

import java.util.List;

public class PlantUpgrade {
    private final double sellReward;
    private final String sellType; // Es. "TOKEN"
    private final List<String> requirements; // Sostituisce i vecchi campi

    public PlantUpgrade(double sellReward, String sellType, List<String> requirements) {
        this.sellReward = sellReward;
        this.sellType = sellType;
        this.requirements = requirements;
    }


    public double getSellReward() {
        return sellReward;
    }

    public String getSellType() {
        return sellType;
    }

    public List<String> getRequirements() {
        return requirements;
    }
}
