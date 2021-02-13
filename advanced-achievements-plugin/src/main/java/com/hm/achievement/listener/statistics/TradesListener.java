package com.hm.achievement.listener.statistics;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;

import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.config.AchievementMap;
import com.hm.achievement.db.CacheManager;

/**
 * Listener class to deal with Trades achievements.
 * 
 * @author Pyves
 *
 */
@Singleton
public class TradesListener extends AbstractListener {

	@Inject
	public TradesListener(@Named("main") YamlConfiguration mainConfig, int serverVersion, AchievementMap achievementMap,
			CacheManager cacheManager) {
		super(NormalAchievements.TRADES, mainConfig, serverVersion, achievementMap, cacheManager);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent event) {
		if ((event.getClick() != ClickType.NUMBER_KEY || event.getAction() != InventoryAction.HOTBAR_MOVE_AND_READD)
				&& event.getRawSlot() == 2 && event.getInventory().getType() == InventoryType.MERCHANT
				&& event.getAction() != InventoryAction.NOTHING) {
			updateStatisticAndAwardAchievementsIfAvailable((Player) event.getWhoClicked(), 1);
		}
	}
}
