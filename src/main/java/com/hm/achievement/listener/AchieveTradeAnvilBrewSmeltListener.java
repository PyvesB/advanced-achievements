package com.hm.achievement.listener;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.NormalAchievements;

/**
 * Listener class to deal with Trades, AnvilsUsed, Smelting and Brewing achievements.
 * 
 * @author Pyves
 *
 */
public class AchieveTradeAnvilBrewSmeltListener extends AbstractListener implements Listener {

	public AchieveTradeAnvilBrewSmeltListener(AdvancedAchievements plugin) {

		super(plugin);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent event) {

		if (event.getRawSlot() < 0 || event.getRawSlot() > 2 || event.getCurrentItem() == null
				|| event.getCurrentItem().getType() == Material.AIR || event.getAction() == InventoryAction.NOTHING) {
			return;
		}

		Player player = (Player) event.getWhoClicked();
		NormalAchievements category;
		InventoryType inventoryType = event.getInventory().getType();
		if (event.getRawSlot() == 2 && inventoryType == InventoryType.MERCHANT) {
			category = NormalAchievements.TRADES;
		} else if (event.getRawSlot() == 2 && inventoryType == InventoryType.ANVIL) {
			category = NormalAchievements.ANVILS;
		} else if (inventoryType == InventoryType.BREWING) {
			category = NormalAchievements.BREWING;
		} else if (event.getRawSlot() == 2 && inventoryType == InventoryType.FURNACE) {
			category = NormalAchievements.SMELTING;
		} else {
			return;
		}

		if (plugin.getDisabledCategorySet().contains(category.toString())) {
			return;
		}

		if (!shouldEventBeTakenIntoAccount(player, category)
				|| category == NormalAchievements.BREWING && isInCooldownPeriod(player)) {
			return;
		}

		int eventAmount = event.getCurrentItem().getAmount();
		// Case where the player uses shift + click but there are no empty slots in his inventory. Part or all of the
		// items may still be successfully retrieved if the player's inventory has some non full stacks corresponding to
		// the exact same ItemStack.
		if (event.isShiftClick() && player.getInventory().firstEmpty() < 0) {
			int availableSpaces = 0;
			Material eventMaterial = event.getCurrentItem().getType();
			// Get all similar ItemStacks in the player's inventory.
			HashMap<Integer, ? extends ItemStack> inventoryEventItemStackMap = player.getInventory().all(eventMaterial);
			// For each of these stacks, add their remaining space.
			for (ItemStack itemStack : inventoryEventItemStackMap.values()) {
				availableSpaces += (eventMaterial.getMaxStackSize() - itemStack.getAmount());
			}
			// Not enough space available to fit everything: the statistic will only increase by the number of items
			// actually retrieved in the inventory during this action.
			if (availableSpaces < eventAmount) {
				eventAmount = availableSpaces;
			}
			if (eventAmount == 0) {
				return;
			}
		}

		updateStatisticAndAwardAchievementsIfAvailable(player, category, eventAmount);
	}
}
