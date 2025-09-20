package me.herohd.rubyisland.quests.handlers;

import me.herohd.rubyisland.event.CollectorBreakEvent;
import me.herohd.rubyisland.event.CollectorFillEvent;
import me.herohd.rubyisland.quests.IQuestHandler;
import me.herohd.rubyisland.quests.PlayerQuestData;
import me.herohd.rubyisland.quests.Quest;
import me.herohd.rubyisland.quests.QuestManager;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.List;

public class CollectorFillQuestHandler implements IQuestHandler {
    @Override
    public String getQuestType() {
        return "COLLECTOR_FILL";
    }

    @Override
    public void handleEvent(Event event, Player player, Quest quest, PlayerQuestData data, QuestManager manager) {
        if (!(event instanceof CollectorFillEvent)) {
            return;
        }

        CollectorFillEvent e = (CollectorFillEvent) event;
        List<String> requiredBlock = quest.getRequireTarget();
        if(requiredBlock.contains("ANY") || requiredBlock.contains(e.getCollectorId())) {
            manager.progressQuest(player, e.getAmountFilled());
        }
    }
}
