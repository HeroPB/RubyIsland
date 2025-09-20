package me.herohd.rubyisland.quests;


import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;
public class Quest {

    private final String fileId;
    private String questName;
    private String questType; // MODIFICATO: Da QuestType a String
    private String guiDisplayName;
    private List<String> guiLore;
    private ItemStack guiItem; // Modificato da Material a ItemStack
    private String messageComplete;
    private String messageStarted;
    private String messageProgress;
    private List<String> requireTarget;
    private int requireAmount;
    private List<String> rewards;

    public Quest(String fileId) {
        this.fileId = fileId;
    }

    public String getFileId() {
        return fileId;
    }

    public String getQuestName() {
        return questName;
    }

    public void setQuestName(String questName) {
        this.questName = questName;
    }

    public String getQuestType() {
        return questType;
    }

    public void setQuestType(String questType) {
        this.questType = questType;
    }

    public String getGuiDisplayName() {
        return guiDisplayName;
    }

    public void setGuiDisplayName(String guiDisplayName) {
        this.guiDisplayName = guiDisplayName;
    }

    public List<String> getGuiLore() {
        return guiLore;
    }

    public void setGuiLore(List<String> guiLore) {
        this.guiLore = guiLore;
    }

    public ItemStack getGuiItem() {
        return guiItem;
    }

    public void setGuiItem(ItemStack guiItem) {
        this.guiItem = guiItem;
    }

    public String getMessageComplete() {
        return messageComplete;
    }

    public void setMessageComplete(String messageComplete) {
        this.messageComplete = messageComplete;
    }

    public String getMessageStarted() {
        return messageStarted;
    }

    public void setMessageStarted(String messageStarted) {
        this.messageStarted = messageStarted;
    }

    public String getMessageProgress() {
        return messageProgress;
    }

    public void setMessageProgress(String messageProgress) {
        this.messageProgress = messageProgress;
    }

    public List<String> getRequireTarget() {
        return requireTarget;
    }

    public void setRequireTarget(List<String> requireTarget) {
        this.requireTarget = requireTarget;
    }

    public int getRequireAmount() {
        return requireAmount;
    }

    public void setRequireAmount(int requireAmount) {
        this.requireAmount = requireAmount;
    }

    public List<String> getRewards() {
        return rewards;
    }

    public void setRewards(List<String> rewards) {
        this.rewards = rewards;
    }
}