package com.hm.achievement.utils;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

/**
 * Class representing an event fired when a player receives an achievement.
 *
 * @author Pyves
 */
public class PlayerAdvancedAchievementEvent extends Event implements Cancellable {

	private static final HandlerList HANDLERS = new HandlerList();

	private final Player player;
	private final String name;
	private final String displayName;
	private final String message;
	private final String type;
	private final List<String> commandMessages;
	private final String[] commandRewards;
	private final ItemStack[] itemRewards;
	private final int moneyReward;
	private final int experienceReward;
	private final int maxHealthReward;
	private final int maxOxygenReward;

	private boolean cancelled;

	private PlayerAdvancedAchievementEvent(Player receiver, String name, String displayName, String message,
			String type, List<String> commandMessages, String[] commandRewards, ItemStack[] itemRewards, int moneyReward,
			int experienceReward, int maxHealthReward, int maxOxygenReward) {
		player = receiver;
		this.name = name;
		this.displayName = displayName;
		this.message = message;
		this.type = type;
		this.commandMessages = commandMessages;
		this.commandRewards = commandRewards;
		this.itemRewards = itemRewards;
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

	public boolean isCancelled() {
		return cancelled;
	}

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

	public String getType() {
		return type;
	}

	/**
	 * @deprecated use {@link #getCommandMessages()} instead
	 */
	@Deprecated
	public String getCommandMessage() {
		return StringUtils.join(commandMessages, ' ');
	}

	public List<String> getCommandMessages() {
		return commandMessages;
	}

	public String[] getCommandRewards() {
		return commandRewards;
	}

	/**
	 * @deprecated use {@link #getItemRewards()} instead
	 */
	@Deprecated
	public ItemStack getItemReward() {
		return itemRewards == null || itemRewards.length == 0 ? null : itemRewards[0];
	}

	public ItemStack[] getItemRewards() {
		return itemRewards;
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
		private String type;
		private List<String> commandMessage;
		private String[] commandRewards;
		private ItemStack[] itemRewards;
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

		public PlayerAdvancedAchievementEventBuilder type(String type) {
			this.type = type;
			return this;
		}

		/**
		 * @deprecated use {@link #commandMessage(List)} instead
		 */
		@Deprecated
		public PlayerAdvancedAchievementEventBuilder commandMessage(String commandMessage) {
			this.commandMessage = Collections.singletonList(commandMessage);
			return this;
		}

		public PlayerAdvancedAchievementEventBuilder commandMessage(List<String> commandMessage) {
			this.commandMessage = commandMessage;
			return this;
		}

		public PlayerAdvancedAchievementEventBuilder commandRewards(String[] commandRewards) {
			this.commandRewards = commandRewards;
			return this;
		}

		/**
		 * @deprecated use {@link #itemRewards(ItemStack[])} instead
		 */
		@Deprecated
		public PlayerAdvancedAchievementEventBuilder itemReward(ItemStack itemReward) {
			this.itemRewards = new ItemStack[] { itemReward };
			return this;
		}

		public PlayerAdvancedAchievementEventBuilder itemRewards(ItemStack[] itemRewards) {
			this.itemRewards = itemRewards;
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
			return new PlayerAdvancedAchievementEvent(player, name, displayName, message, type, commandMessage,
					commandRewards, itemRewards, moneyReward, experienceReward, maxHealthReward, maxOxygenReward);
		}
	}
}
