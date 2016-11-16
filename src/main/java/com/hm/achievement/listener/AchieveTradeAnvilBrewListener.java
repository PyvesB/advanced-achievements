package com.hm.achievement.listener;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.NormalAchievements;

/**
 * Listener class to deal with Trades, AnvilsUsed and Brewing achievements.
 * 
 * @author Pyves
 *
 */
public class AchieveTradeAnvilBrewListener extends AbstractListener implements Listener {

	public AchieveTradeAnvilBrewListener(AdvancedAchievements plugin) {

		super(plugin);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent event) {

		if (event.getRawSlot() != 0 && event.getRawSlot() != 1 && event.getRawSlot() != 2
				|| event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR)
			return;

		Player player = (Player) event.getWhoClicked();
		if (!shouldEventBeTakenIntoAccountNoPermission(player)
				|| event.isShiftClick() && player.getInventory().firstEmpty() < 0)
			return;

		NormalAchievements category;
		if (event.getRawSlot() == 2 && event.getInventory().getType() == InventoryType.MERCHANT) {
			category = NormalAchievements.TRADES;
		} else if (event.getRawSlot() == 2 && event.getInventory().getType() == InventoryType.ANVIL) {
			category = NormalAchievements.ANVILS;
		} else if (event.getInventory().getType() == InventoryType.BREWING) {
			category = NormalAchievements.BREWING;
		} else {
			return;
		}

		if (plugin.getDisabledCategorySet().contains(category.toString()))
			return;

		if (!shouldEventBeTakenIntoAccount(player, category))
			return;

		updateStatisticAndAwardAchievementsIfAvailable(player, category, 1);
	}
}
