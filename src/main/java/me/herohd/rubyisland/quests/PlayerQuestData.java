package me.herohd.rubyisland.quests;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerQuestData {

    private final UUID playerId;
    private String activeQuestId;
    private double progress;
    private final Set<String> completedQuests; // NUOVO: Storico delle quest

    public PlayerQuestData(UUID playerId, String activeQuestId) {
        this.playerId = playerId;
        this.activeQuestId = activeQuestId;
        this.progress = 0;
        this.completedQuests = new HashSet<>();
    }

    public void incrementProgress(double amount) {
        this.progress += amount;
    }

    public void addCompletedQuest(String questId) {
        this.completedQuests.add(questId);
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public String getActiveQuestId() {
        return activeQuestId;
    }

    public void setActiveQuestId(String activeQuestId) {
        this.activeQuestId = activeQuestId;
    }

    public double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }

    public Set<String> getCompletedQuests() {
        return completedQuests;
    }
    public boolean hasCompletedQuest(String questId) {
        if (questId == null || questId.isEmpty()) {
            return false;
        }
        return this.completedQuests.contains(questId);
    }
}