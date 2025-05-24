package me.herohd.rubyisland.utils;

import me.herohd.rubyisland.RubyIsland;

public enum Messages {
    PREFIX("general.prefix"),
    TELEPORT_OWN("messages.teleport-own-island"),
    TELEPORT_OTHER("messages.teleport-other-island"),
    ISLAND_NOT_FOUND("messages.island-not-found"),
    PLAYER_NOT_FOUND("messages.player-not-found"),
    NOT_HAVE_ISLAND("messages.you-not-have-island"),
    NOT_IN_YOUR_ISLAND("messages.not-in-your-island"),
    NEW_SPAWNPOINT("messages.new-spawnpoint"),
    BAN_PLAYER("messages.ban-player"),
    UNBAN_PLAYER("messages.unban-player"),
    ADD_PLAYER("messages.add-player"),
    TRUST_PLAYER("messages.trust-player"),
    ALTREDY_EXIST_PLAYER("messages.already-exist-player"),
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
