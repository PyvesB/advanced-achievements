package com.hm.achievement.listener;

import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.utils.StatisticIncreaseHandler;

/**
 * Abstract class in charge of factoring out common functionality for the listener classes.
 * 
 * @author Pyves
 */
public abstract class AbstractListener extends StatisticIncreaseHandler implements Listener {

	protected AbstractListener(AdvancedAchievements plugin) {
		super(plugin);
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
	protected void updateStatisticAndAwardAchievementsIfAvailable(Player player, MultipleAchievements category,
			String subcategory, int incrementValue) {
		long amount = plugin.getCacheManager().getAndIncrementStatisticAmount(category, subcategory,
				player.getUniqueId(), incrementValue);
		checkThresholdsAndAchievements(player, category + "." + subcategory, amount);
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
