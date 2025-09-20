package me.herohd.rubyisland.quests.handlers;

import me.herohd.rubyisland.quests.IQuestHandler;
import me.herohd.rubyisland.quests.PlayerQuestData;
import me.herohd.rubyisland.quests.Quest;
import me.herohd.rubyisland.quests.QuestManager;
import me.herohd.rubyisland.utils.IslandUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.List;

public class BlockBreakQuestHandler implements IQuestHandler {
    @Override
    public String getQuestType() {
        return "BLOCK_BREAK";
    }

    @Override
    public void handleEvent(Event event, Player player, Quest quest, PlayerQuestData data, QuestManager manager) {
        if (!(event instanceof BlockBreakEvent)) {
            return;
        }

        BlockBreakEvent e = (BlockBreakEvent) event;
        if(!IslandUtils.isInHisIsland(player, e.getBlock().getLocation())) return;
        List<String> requiredBlock = quest.getRequireTarget();
        for (String s : requiredBlock) {
            if(s.equalsIgnoreCase("ANY")) {
                manager.progressQuest(player, 1);
                return;
            }
            final String[] split = s.split(";");
            final boolean a = e.getBlock().getType().toString().equalsIgnoreCase(split[0]);
            final boolean b = e.getBlock().getData() == Integer.parseInt(split[1]);
            if(a && b) manager.progressQuest(player, 1);
        }
    }
}
