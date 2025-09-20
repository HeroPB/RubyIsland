package me.herohd.rubyisland.quests.handlers;

import me.herohd.rubyisland.quests.IQuestHandler;
import me.herohd.rubyisland.quests.PlayerQuestData;
import me.herohd.rubyisland.quests.Quest;
import me.herohd.rubyisland.quests.QuestManager;
import me.kr1s_d.rubyregionmanager.api.RegionAPI;
import me.kr1s_d.rubyregionmanager.api.objects.IRegion;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.List;

public class MineQuestHandler implements IQuestHandler {

    @Override
    public String getQuestType() {
        return "MINE_BREAK";
    }

    @Override
    public void handleEvent(Event event, Player player, Quest quest, PlayerQuestData data, QuestManager manager) {
        if (!(event instanceof BlockBreakEvent)) {
            return;
        }

        BlockBreakEvent e = (BlockBreakEvent) event;
        List<String> requiredBlock = quest.getRequireTarget();

        if(e.isCancelled()) return;
        IRegion loc = RegionAPI.getRegionByLocation(e.getBlock().getLocation());
        if(loc == null) return;
        String mineName = loc.getMetadataValue("mine");
        if(mineName == null) return;

        if (requiredBlock.contains("ANY") || requiredBlock.contains(mineName)) {
            manager.progressQuest(player, 1);
        }
    }
}