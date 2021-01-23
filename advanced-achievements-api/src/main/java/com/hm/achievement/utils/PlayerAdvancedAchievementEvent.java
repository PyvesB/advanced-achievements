package com.hm.achievement.utils;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.hm.achievement.domain.Achievement;

/**
 * Class representing an event fired when a player receives an achievement.
 *
 * @author Pyves
 */
public class PlayerAdvancedAchievementEvent extends Event implements Cancellable {

	private static final HandlerList HANDLERS = new HandlerList();

	private final Player player;
	private final Achievement achievement;

	private boolean cancelled;

	public PlayerAdvancedAchievementEvent(Player player, Achievement achievement) {
		this.player = player;
		this.achievement = achievement;
		cancelled = false;
	}

	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		cancelled = cancel;
	}

	public Player getPlayer() {
		return player;
	}

	public Achievement getAchievement() {
		return achievement;
	}

}
