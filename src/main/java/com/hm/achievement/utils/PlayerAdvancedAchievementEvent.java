package com.hm.achievement.utils;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

/**
 * Class representing an event fired when a player receives an achievement.
 * 
 * @author Pyves
 *
 */
public class PlayerAdvancedAchievementEvent extends Event implements Cancellable {

	private static final HandlerList HANDLERS = new HandlerList();

	private final Player player;
	private final String name;
	private final String displayName;
	private final String message;
	private final String commandMessage;
	private final String[] commandRewards;
	private final ItemStack itemReward;
	private final int moneyReward;
	private final int experienceReward;
	private final int maxHealthReward;
	private final int maxOxygenReward;

	private boolean cancelled;

	private PlayerAdvancedAchievementEvent(Player receiver, String name, String displayName, String message,
			String commandMessage, String[] commandRewards, ItemStack itemReward, int moneyReward, int experienceReward,
			int maxHealthReward, int maxOxygenReward) {
		player = receiver;
		this.name = name;
		this.displayName = displayName;
		this.message = message;
		this.commandMessage = commandMessage;
		this.commandRewards = commandRewards;
		this.itemReward = itemReward;
		this.moneyReward = moneyReward;
		this.experienceReward = experienceReward;
		this.maxHealthReward = maxHealthReward;
		this.maxOxygenReward = maxOxygenReward;
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

	public String getName() {
		return name;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getMessage() {
		return message;
	}

	public String getCommandMessage() {
		return commandMessage;
	}

	public String[] getCommandRewards() {
		return commandRewards;
	}

	public ItemStack getItemReward() {
		return itemReward;
	}

	public int getMoneyReward() {
		return moneyReward;
	}

	public int getExperienceReward() {
		return experienceReward;
	}

	public int getMaxHealthReward() {
		return maxHealthReward;
	}

	public int getMaxOxygenReward() {
		return maxOxygenReward;
	}

	public static class PlayerAdvancedAchievementEventBuilder {

		private Player player;
		private String name;
		private String displayName;
		private String message;
		private String commandMessage;
		private String[] commandRewards;
		private ItemStack itemReward;
		private int moneyReward;
		private int experienceReward;
		private int maxHealthReward;
		private int maxOxygenReward;

		public PlayerAdvancedAchievementEventBuilder player(Player player) {
			this.player = player;
			return this;
		}

		public PlayerAdvancedAchievementEventBuilder name(String name) {
			this.name = name;
			return this;
		}

		public PlayerAdvancedAchievementEventBuilder displayName(String displayName) {
			this.displayName = displayName;
			return this;
		}

		public PlayerAdvancedAchievementEventBuilder message(String message) {
			this.message = message;
			return this;
		}

		public PlayerAdvancedAchievementEventBuilder commandMessage(String commandMessage) {
			this.commandMessage = commandMessage;
			return this;
		}

		public PlayerAdvancedAchievementEventBuilder commandRewards(String[] commandRewards) {
			this.commandRewards = commandRewards;
			return this;
		}

		public PlayerAdvancedAchievementEventBuilder itemReward(ItemStack itemReward) {
			this.itemReward = itemReward;
			return this;
		}

		public PlayerAdvancedAchievementEventBuilder moneyReward(int moneyReward) {
			this.moneyReward = moneyReward;
			return this;
		}

		public PlayerAdvancedAchievementEventBuilder experienceReward(int experienceReward) {
			this.experienceReward = experienceReward;
			return this;
		}

		public PlayerAdvancedAchievementEventBuilder maxHealthReward(int maxHealthReward) {
			this.maxHealthReward = maxHealthReward;
			return this;
		}

		public PlayerAdvancedAchievementEventBuilder maxOxygenReward(int maxOxygenReward) {
			this.maxOxygenReward = maxOxygenReward;
			return this;
		}

		public PlayerAdvancedAchievementEvent build() {
			return new PlayerAdvancedAchievementEvent(player, name, displayName, message, commandMessage, commandRewards,
					itemReward, moneyReward, experienceReward, maxHealthReward, maxOxygenReward);
		}
	}
}
