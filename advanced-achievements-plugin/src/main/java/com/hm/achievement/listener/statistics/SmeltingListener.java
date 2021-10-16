package com.hm.achievement.listener.statistics;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.config.AchievementMap;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.utils.InventoryHelper;

/**
 * Listener class to deal with Smelting achievements.
 * 
 * @author Pyves
 *
 */
@Singleton
public class SmeltingListener extends AbstractListener {

	@Inject
	public SmeltingListener(@Named("main") YamlConfiguration mainConfig, AchievementMap achievementMap,
			CacheManager cacheManager) {
		super(NormalAchievements.SMELTING, mainConfig, achievementMap, cacheManager);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent event) {
		ItemStack item = event.getCurrentItem();
		if (event.getRawSlot() != 2 || event.getInventory().getType() != InventoryType.FURNACE || item == null
				|| item.getType() == Material.AIR || event.getAction() == InventoryAction.NOTHING
				|| event.getClick() == ClickType.NUMBER_KEY && event.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD) {
			return;
		}

		Player player = (Player) event.getWhoClicked();

		int eventAmount = item.getAmount();
		if (event.isShiftClick()) {
			eventAmount = Math.min(eventAmount, InventoryHelper.getAvailableSpace(player, item));
			if (eventAmount == 0) {
				return;
			}
		}

		updateStatisticAndAwardAchievementsIfAvailable(player, eventAmount);
	}
}
