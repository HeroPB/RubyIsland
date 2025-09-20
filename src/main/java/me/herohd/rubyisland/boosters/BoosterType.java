package me.herohd.rubyisland.boosters;

public enum BoosterType {
    // Moltiplica i soldi guadagnati da un SellEvent
    MINE_MULTIPLIER("Booster delle Miniere", "§a§lBOOSTER§8: §fI tuoi guadagni dalle miniere sono potenziati!"),
    ENCHANT_MULTIPLIER("Booster degli Enchant", "§a§lBOOSTER§8: §fI tuoi guadagni dagli enchant sono potenziati!"),

    // Aumenta la probabilità di drop doppi dai mob
    DOUBLE_DROPS_CRATE("Booster di Crate", "§a§lBOOSTER§8: §fI tuoi drop dalle crate sono potenziati");

    private final String displayName;
    private final String activeMessage;

    BoosterType(String displayName, String activeMessage) {
        this.displayName = displayName;
        this.activeMessage = activeMessage;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getActiveMessage() {
        return activeMessage;
    }
}