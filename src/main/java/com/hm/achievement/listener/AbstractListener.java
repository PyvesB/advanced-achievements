package com.hm.achievement.listener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.utils.AchievementCommentedYamlConfiguration;
import com.hm.achievement.utils.PlayerAdvancedAchievementEvent.PlayerAdvancedAchievementEventBuilder;
import com.hm.achievement.utils.Reloadable;
import com.hm.mcshared.particle.ReflectionUtils.PackageType;

/**
 * Abstract class in charge of factoring out common functionality for the listener classes.
 * 
 * @author Pyves
 */
public abstract class AbstractListener implements Listener, Reloadable {

	protected final int version;
	protected final AdvancedAchievements plugin;

	private boolean configRestrictCreative;
	private boolean configRestrictSpectator;
	private boolean configRestrictAdventure;
	private Set<String> configExcludedWorlds;

	protected AbstractListener(AdvancedAchievements plugin) {
		this.plugin = plugin;

		// Simple parsing of game version. Might need to be updated in the future depending on how the Minecraft
		// versions change in the future.
		version = Integer.parseInt(PackageType.getServerVersion().split("_")[1]);
	}

	@Override
	public void extractConfigurationParameters() {
		configRestrictCreative = plugin.getPluginConfig().getBoolean("RestrictCreative", false);
		configRestrictSpectator = plugin.getPluginConfig().getBoolean("RestrictSpectator", true);
		configRestrictAdventure = plugin.getPluginConfig().getBoolean("RestrictAdventure", false);
		// Spectator mode introduced in Minecraft 1.8. Automatically relevant parameter for older versions.
		if (configRestrictSpectator && version < 8) {
			configRestrictSpectator = false;
		}
		configExcludedWorlds = new HashSet<>(plugin.getPluginConfig().getList("ExcludedWorlds"));
	}

	/**
	 * Determines whether the listened event should be taken into account.
	 * 
	 * @param player
	 * @param category
	 * @return
	 */
	protected boolean shouldEventBeTakenIntoAccount(Player player, NormalAchievements category) {
		boolean isNPC = player.hasMetadata("NPC");
		boolean permission = player.hasPermission(category.toPermName());
		boolean restrictedCreative = configRestrictCreative && player.getGameMode() == GameMode.CREATIVE;
		boolean restrictedSpectator = configRestrictSpectator && player.getGameMode() == GameMode.SPECTATOR;
		boolean restrictedAdventure = configRestrictAdventure && player.getGameMode() == GameMode.ADVENTURE;
		boolean excludedWorld = configExcludedWorlds.contains(player.getWorld().getName());

		return !isNPC && permission && !restrictedCreative && !restrictedSpectator && !restrictedAdventure
				&& !excludedWorld;
	}

	/**
	 * Determines whether the listened event should be taken into account. Ignore permission check.
	 * 
	 * @param player
	 * @param category
	 * @return
	 */
	protected boolean shouldEventBeTakenIntoAccountNoPermission(Player player) {
		boolean isNPC = player.hasMetadata("NPC");
		boolean restrictedCreative = configRestrictCreative && player.getGameMode() == GameMode.CREATIVE;
		boolean restrictedSpectator = configRestrictSpectator && player.getGameMode() == GameMode.SPECTATOR;
		boolean restrictedAdventure = configRestrictAdventure && player.getGameMode() == GameMode.ADVENTURE;
		boolean excludedWorld = configExcludedWorlds.contains(player.getWorld().getName());

		return !isNPC && !restrictedCreative && !restrictedSpectator && !restrictedAdventure && !excludedWorld;
	}

	/**
	 * Updates the statistic in the database for a NormalAchievement and awards an achievement if an available one is
	 * found.
	 * 
	 * @param player
	 * @param category
	 * @param incrementValue
	 */
	protected void updateStatisticAndAwardAchievementsIfAvailable(Player player, NormalAchievements category,
			int incrementValue) {
		long amount = plugin.getCacheManager().getAndIncrementStatisticAmount(category, player.getUniqueId(),
				incrementValue);

		if (incrementValue > 1) {
			// Every value must be checked to see whether it corresponds to an achievement's threshold.
			for (long threshold = amount - incrementValue + 1; threshold <= amount; ++threshold) {
				String configAchievement = category + "." + threshold;
				awardAchievementIfAvailable(player, configAchievement);
			}
		} else {
			String configAchievement = category + "." + amount;
			awardAchievementIfAvailable(player, configAchievement);
		}
	}

	/**
	 * Updates the statistic in the database for a MultipleAchievement and awards an achievement if an available one is
	 * found.
	 * 
	 * @param player
	 * @param category
	 * @param subcategory
	 * @param incrementValue
	 */
	protected void updateStatisticAndAwardAchievementsIfAvailable(Player player, MultipleAchievements category,
			String subcategory, int incrementValue) {
		long amount = plugin.getCacheManager().getAndIncrementStatisticAmount(category, subcategory,
				player.getUniqueId(), incrementValue);

		if (incrementValue > 1) {
			// Every value must be checked to see whether it corresponds to an achievement's threshold.
			for (long threshold = amount - incrementValue + 1; threshold <= amount; ++threshold) {
				String configAchievement = category + "." + subcategory + '.' + threshold;
				awardAchievementIfAvailable(player, configAchievement);
			}
		} else {
			String configAchievement = category + "." + subcategory + '.' + amount;
			awardAchievementIfAvailable(player, configAchievement);
		}
	}

	/**
	 * Awards an achievement if the corresponding threshold was found in the configuration file.
	 * 
	 * @param player
	 * @param configAchievement
	 */
	protected void awardAchievementIfAvailable(Player player, String configAchievement) {
		AchievementCommentedYamlConfiguration pluginConfig = plugin.getPluginConfig();
		if (pluginConfig.getString(configAchievement + ".Message", null) != null) {
			// Fire achievement event.
			PlayerAdvancedAchievementEventBuilder playerAdvancedAchievementEventBuilder = new PlayerAdvancedAchievementEventBuilder()
					.player(player).name(plugin.getPluginConfig().getString(configAchievement + ".Name"))
					.displayName(plugin.getPluginConfig().getString(configAchievement + ".DisplayName"))
					.message(plugin.getPluginConfig().getString(configAchievement + ".Message"))
					.commandRewards(plugin.getRewardParser().getCommandRewards(configAchievement, player))
					.itemReward(plugin.getRewardParser().getItemReward(configAchievement))
					.moneyReward(plugin.getRewardParser().getRewardAmount(configAchievement, "Money"))
					.experienceReward(plugin.getRewardParser().getRewardAmount(configAchievement, "Experience"))
					.maxHealthReward(plugin.getRewardParser().getRewardAmount(configAchievement, "IncreaseMaxHealth"));

			Bukkit.getServer().getPluginManager().callEvent(playerAdvancedAchievementEventBuilder.build());
		}
	}

	/**
	 * Determines whether an item is a water potion.
	 * 
	 * @param item
	 * @return
	 */
	protected boolean isWaterPotion(ItemStack item) {
		if (version >= 9) {
			PotionMeta meta = (PotionMeta) (item.getItemMeta());
			PotionType potionType = meta.getBasePotionData().getType();

			if (potionType == PotionType.WATER) {
				return true;
			}
		} else {
			// Method getBasePotionData does not exist for versions prior to Minecraft 1.9.
			if (item.getDurability() == 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Calculates the space available to accommodate a new item stack. This method takes empty slots and existing item
	 * stacks of the same type into account.
	 * 
	 * @param player
	 * @param newItemStack
	 * @return
	 */
	protected int getInventoryAvailableSpace(Player player, ItemStack newItemStack) {
		int availableSpace = 0;
		// Get all similar item stacks with a similar material in the player's inventory.
		HashMap<Integer, ? extends ItemStack> inventoryItemStackMap = player.getInventory().all(newItemStack.getType());
		// If matching item stack, add remaining space.
		for (ItemStack currentItemStack : inventoryItemStackMap.values()) {
			if (newItemStack.isSimilar(currentItemStack)) {
				availableSpace += (newItemStack.getMaxStackSize() - currentItemStack.getAmount());
			}
		}

		ItemStack[] storageContents;
		if (version >= 9) {
			storageContents = player.getInventory().getStorageContents();
		} else {
			storageContents = player.getInventory().getContents();
		}
		// Get all empty slots in the player's inventory.
		for (ItemStack currentItemStack : storageContents) {
			if (currentItemStack == null) {
				availableSpace += newItemStack.getMaxStackSize();
			}
		}

		return availableSpace;
	}
}
