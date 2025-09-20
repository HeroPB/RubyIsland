package me.herohd.rubyisland.quests.handlers;

import me.herohd.rubyisland.event.UpgradePlantEvent;
import me.herohd.rubyisland.quests.IQuestHandler;
import me.herohd.rubyisland.quests.PlayerQuestData;
import me.herohd.rubyisland.quests.Quest;
import me.herohd.rubyisland.quests.QuestManager;
import me.kr1s_d.rubyenchantmanager.events.WEnchantGainObjectEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.List;

public class UpgradePlantQuestHandler implements IQuestHandler {
    @Override
    public String getQuestType() {
        return "UPGRADE_PLANT";
    }

    @Override
    public void handleEvent(Event event, Player player, Quest quest, PlayerQuestData data, QuestManager manager) {
        if (!(event instanceof UpgradePlantEvent)) {
            return;
        }

        UpgradePlantEvent e = (UpgradePlantEvent) event;
        List<String> requiredBlock = quest.getRequireTarget();
        if(requiredBlock.contains("ANY") || requiredBlock.contains(e.getPlant().getPlantType().getId())) {
            manager.progressQuest(player, 1);
        }
    }
}
