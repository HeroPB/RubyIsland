package me.herohd.rubyisland.quests.handlers;

import me.herohd.rubyisland.event.CollectorBreakEvent;
import me.herohd.rubyisland.quests.IQuestHandler;
import me.herohd.rubyisland.quests.PlayerQuestData;
import me.herohd.rubyisland.quests.Quest;
import me.herohd.rubyisland.quests.QuestManager;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.List;

public class CollectorBreakQuestHandler implements IQuestHandler {
    @Override
    public String getQuestType() {
        return "COLLECTOR_BREAK";
    }

    @Override
    public void handleEvent(Event event, Player player, Quest quest, PlayerQuestData data, QuestManager manager) {
        if (!(event instanceof CollectorBreakEvent)) {
            return;
        }

        CollectorBreakEvent e = (CollectorBreakEvent) event;
        List<String> requiredBlock = quest.getRequireTarget();
        if(requiredBlock.contains("ANY") || requiredBlock.contains(e.getCollectorId())) {
            manager.progressQuest(player, e.getAmountBreaked());
        }
    }
}
