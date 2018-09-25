package com.hm.achievement.listener.statistics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.utils.RewardParser;
import com.hm.achievement.utils.StatisticIncreaseHandler;
import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Abstract class in charge of factoring out common functionality for the listener classes.
 * 
 * @author Pyves
 */
public abstract class AbstractListener extends StatisticIncreaseHandler implements Listener {

	AbstractListener(CommentedYamlConfiguration mainConfig, int serverVersion, Map<String, List<Long>> sortedThresholds,
			CacheManager cacheManager, RewardParser rewardParser) {
		super(mainConfig, serverVersion, sortedThresholds, cacheManager, rewardParser);
	}

	/**
	 * Updates the statistic in the database for a NormalAchievement and awards an achievement if an available one is
	 * found.
	 * 
	 * @param player
	 * @param category
	 * @param incrementValue
	 */
	void updateStatisticAndAwardAchievementsIfAvailable(Player player, NormalAchievements category, int incrementValue) {
		long amount = cacheManager.getAndIncrementStatisticAmount(category, player.getUniqueId(), incrementValue);
		checkThresholdsAndAchievements(player, category.toString(), amount);
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
	void updateStatisticAndAwardAchievementsIfAvailable(Player player, MultipleAchievements category, String subcategory,
			int incrementValue) {
		long amount = cacheManager.getAndIncrementStatisticAmount(category, subcategory, player.getUniqueId(),
				incrementValue);
		checkThresholdsAndAchievements(player, category + "." + subcategory, amount);
	}

	/**
	 * Determines whether an item is a water potion.
	 * 
	 * @param item
	 * @return true if the item is a water potion, false otherwise
	 */
	@SuppressWarnings("deprecation")
	boolean isWaterPotion(ItemStack item) {
		if (serverVersion >= 9) {
			// Method getBasePotionData does not exist for versions prior to Minecraft 1.9.
			return ((PotionMeta) (item.getItemMeta())).getBasePotionData().getType() == PotionType.WATER;
		}
		return item.getDurability() == 0;
	}

	/**
	 * Calculates the space available to accommodate a new item stack. This method takes empty slots and existing item
	 * stacks of the same type into account.
	 * 
	 * @param player
	 * @param newItemStack
	 * @return the available space for the item
	 */
	int getInventoryAvailableSpace(Player player, ItemStack newItemStack) {
		int availableSpace = 0;
		// Get all similar item stacks with a similar material in the player's inventory.
		HashMap<Integer, ? extends ItemStack> inventoryItemStackMap = player.getInventory().all(newItemStack.getType());
		// If matching item stack, add remaining space.
		for (ItemStack currentItemStack : inventoryItemStackMap.values()) {
			if (newItemStack.isSimilar(currentItemStack)) {
				availableSpace += (newItemStack.getMaxStackSize() - currentItemStack.getAmount());
			}
		}

		ItemStack[] storageContents = serverVersion >= 9 ? player.getInventory().getStorageContents()
				: player.getInventory().getContents();
		// Get all empty slots in the player's inventory.
		for (ItemStack currentItemStack : storageContents) {
			if (currentItemStack == null) {
				availableSpace += newItemStack.getMaxStackSize();
			}
		}

		return availableSpace;
	}

	/**
	 * Returns all achievements that match the provided identifier.
	 * 
	 * @param category the category to search from
	 * @param identifier the identifier to match
	 * @return all matched achievements
	 * @author tassu
	 */
	Set<String> findAchievementsByCategoryAndName(MultipleAchievements category, String identifier) {
		return mainConfig.getShallowKeys(category.toString()).stream()
				.filter(keys -> ArrayUtils.contains(StringUtils.split(keys, '|'), identifier))
				.collect(Collectors.toSet());
	}

}
