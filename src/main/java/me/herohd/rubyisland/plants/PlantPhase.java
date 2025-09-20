package me.herohd.rubyisland.plants;

public class PlantPhase {
    private final String textureMeta;
    private final String message;
    private final String type; // Es. "TIME"
    private final int require; // Es. 30 (minuti)

    public PlantPhase(String textureMeta, String message, String type, int require) {
        this.textureMeta = textureMeta;
        this.message = message;
        this.type = type;
        this.require = require;
    }

    public String getTextureMeta() {
        return textureMeta;
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }

    public int getRequire() {
        return require;
    }
}
