package me.herohd.rubyisland.quests;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;

public interface IQuestHandler {

    /**
     * Restituisce l'identificatore testuale univoco per questo tipo di quest.
     * Es: "BLOCK_BREAK", "KILL_MOB", ecc.
     * @return La stringa che identifica il tipo di quest.
     */
    String getQuestType(); // MODIFICATO: Aggiunto questo metodo

    void handleEvent(Event event, Player player, Quest quest, PlayerQuestData data, QuestManager manager);
}