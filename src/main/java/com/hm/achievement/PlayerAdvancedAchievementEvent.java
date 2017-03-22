package com.hm.achievement;

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

	private static final HandlerList handlers = new HandlerList();

	private final Player player;
	private final String name;
	private final String displayName;
	private final String message;
	private final String[] commandRewards;
	private final ItemStack itemReward;
	private final int moneyReward;

	private boolean cancelled;

	@Override
	public HandlerList getHandlers() {

		return handlers;
	}

	public static HandlerList getHandlerList() {

		return handlers;
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

	public String[] getCommandRewards() {
		return commandRewards;
	}

	public ItemStack getItemReward() {
		return itemReward;
	}

	public int getMoneyReward() {
		return moneyReward;
	}

	private PlayerAdvancedAchievementEvent(Player receiver, String name, String displayName, String message,
			String[] commandRewards, ItemStack itemReward, int moneyReward) {
		this.player = receiver;
		this.name = name;
		this.displayName = displayName;
		this.message = message;
		this.commandRewards = commandRewards;
		this.itemReward = itemReward;
		this.moneyReward = moneyReward;
		this.cancelled = false;
	}

	public static class PlayerAdvancedAchievementEventBuilder {

		private Player player;
		private String name;
		private String displayName;
		private String message;
		private String[] commandRewards;
		private ItemStack itemReward;
		private int moneyReward;

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

		public PlayerAdvancedAchievementEvent build() {
			return new PlayerAdvancedAchievementEvent(player, name, displayName, message, commandRewards, itemReward,
					moneyReward);
		}
	}
}
