package me.herohd.rubyisland.utils;

import me.herohd.rubyisland.RubyIsland;

public enum Messages {
    PREFIX("general.prefix"),
    TELEPORT_OWN("messages.teleport-own-island"),
    TELEPORT_OTHER("messages.teleport-other-island"),
    ISLAND_NOT_FOUND("messages.island-not-found"),
    WRONG_ARGUMENT("messages.wrong-argument");

    private final Config config = RubyIsland.getInstance().getConfigYML();
    private final String path;
    Messages(String path) {
        this.path = path;
    }

    public String getAsString(){
        return PREFIX.getOnlyString() + config.getColoredString(path);
    }
    public String getOnlyString(){
        return config.getColoredString(path);
    }
}
