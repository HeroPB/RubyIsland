package me.herohd.rubyisland.quests.handlers;

import me.herohd.rubyisland.quests.IQuestHandler;
import me.herohd.rubyisland.quests.PlayerQuestData;
import me.herohd.rubyisland.quests.Quest;
import me.herohd.rubyisland.quests.QuestManager;
import me.herohd.rubyisland.utils.IslandUtils;
import me.kr1s_d.rubyregionmanager.api.RegionAPI;
import me.kr1s_d.rubyregionmanager.api.objects.IRegion;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.List;

public class BlockBreakTwoQuestHandler implements IQuestHandler {
    @Override
    public String getQuestType() {
        return "BLOCK_BREAK_MINE";
    }

    @Override
    public void handleEvent(Event event, Player player, Quest quest, PlayerQuestData data, QuestManager manager) {
        if (!(event instanceof BlockBreakEvent)) {
            return;
        }



        BlockBreakEvent e = (BlockBreakEvent) event;


        IRegion loc = RegionAPI.getRegionByLocation(e.getBlock().getLocation());
        if(loc == null) return;
        String mineName = loc.getMetadataValue("mine");
        if(mineName == null) return;


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
