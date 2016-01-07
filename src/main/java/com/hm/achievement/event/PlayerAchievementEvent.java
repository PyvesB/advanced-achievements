package com.hm.achievement.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PlayerAchievementEvent extends PlayerEvent {

    private static final HandlerList handlers = new HandlerList();

    private String achievementname;

    public PlayerAchievementEvent(Player p, String Achievement){
        super(p);
        this.achievementname = Achievement;
    }

    public HandlerList getHandlers()
    {
        return handlers;
    }

    public static HandlerList getHandlerList()
    {
        return handlers;
    }

    public String getAchievementname() {
        return achievementname;
    }
}
