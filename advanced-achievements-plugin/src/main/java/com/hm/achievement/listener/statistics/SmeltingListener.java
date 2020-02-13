package com.hm.achievement.listener.statistics;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;

import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.utils.InventoryHelper;
import com.hm.achievement.utils.RewardParser;
import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Listener class to deal with Smelting achievements.
 * 
 * @author Pyves
 *
 */
@Singleton
public class SmeltingListener extends AbstractListener {

	private final InventoryHelper inventoryHelper;

	@Inject
	public SmeltingListener(@Named("main") CommentedYamlConfiguration mainConfig, int serverVersion,
			Map<String, List<Long>> sortedThresholds, CacheManager cacheManager, RewardParser rewardParser,
			InventoryHelper inventoryHelper) {
		super(NormalAchievements.SMELTING, mainConfig, serverVersion, sortedThresholds, cacheManager, rewardParser);
		this.inventoryHelper = inventoryHelper;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent event) {
		if (event.getRawSlot() != 2 || event.getInventory().getType() != InventoryType.FURNACE
				|| event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR
				|| event.getAction() == InventoryAction.NOTHING
				|| event.getClick() == ClickType.NUMBER_KEY && event.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD) {
			return;
		}

		Player player = (Player) event.getWhoClicked();

		int eventAmount = event.getCurrentItem().getAmount();
		if (event.isShiftClick()) {
			eventAmount = Math.min(eventAmount, inventoryHelper.getAvailableSpace(player, event.getCurrentItem()));
			if (eventAmount == 0) {
				return;
			}
		}

		updateStatisticAndAwardAchievementsIfAvailable(player, eventAmount);
	}
}
