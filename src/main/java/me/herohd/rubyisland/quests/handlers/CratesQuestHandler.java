package me.herohd.rubyisland.quests.handlers;

import me.herohd.rubyisland.quests.IQuestHandler;
import me.herohd.rubyisland.quests.PlayerQuestData;
import me.herohd.rubyisland.quests.Quest;
import me.herohd.rubyisland.quests.QuestManager;
import me.kr1s_d.rubycrates.api.events.CrateBulkOpenEvent;
import me.kr1s_d.rubycrates.api.events.CrateOpenEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.List;

public class CratesQuestHandler implements IQuestHandler {

    @Override
    public String getQuestType() {
        return "OPEN_CRATE";
    }

    @Override
    public void handleEvent(Event event, Player player, Quest quest, PlayerQuestData data, QuestManager manager) {
        if ((event instanceof CrateOpenEvent)) {
            CrateOpenEvent e = (CrateOpenEvent) event;
            List<String> requiredBlock = quest.getRequireTarget();
            if (requiredBlock.contains("ANY") || requiredBlock.contains(e.getCrate().getId())) {
                manager.progressQuest(player, 1);
            }
        return;
        }
        if ((event instanceof CrateBulkOpenEvent)) {
            CrateBulkOpenEvent e = (CrateBulkOpenEvent) event;
            List<String> requiredBlock = quest.getRequireTarget();
            if (requiredBlock.contains("ANY") || requiredBlock.contains(e.getCrate().getId())) {
                manager.progressQuest(player, e.getKeyAmount());
            }
        }
    }
}