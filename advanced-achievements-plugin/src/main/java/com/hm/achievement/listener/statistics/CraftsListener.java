package com.hm.achievement.listener.statistics;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;

import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.utils.RewardParser;
import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Listener class to deal with Crafts achievements.
 * 
 * @author Pyves
 *
 */
@Singleton
public class CraftsListener extends AbstractListener {

	@Inject
	public CraftsListener(@Named("main") CommentedYamlConfiguration mainConfig, int serverVersion,
			Map<String, List<Long>> sortedThresholds, CacheManager cacheManager, RewardParser rewardParser) {
		super(mainConfig, serverVersion, sortedThresholds, cacheManager, rewardParser);
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onCraftItem(CraftItemEvent event) {
		if (!(event.getWhoClicked() instanceof Player) || event.getAction() == InventoryAction.NOTHING
				|| event.getClick() == ClickType.NUMBER_KEY && event.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD) {
			return;
		}

		Player player = (Player) event.getWhoClicked();
		if (!shouldIncreaseBeTakenIntoAccountNoPermissions(player)) {
			return;
		}

		MultipleAchievements category = MultipleAchievements.CRAFTS;

		ItemStack item = event.getCurrentItem();
		String craftName = item.getType().name().toLowerCase();
		if (!player.hasPermission(category.toPermName() + '.' + craftName)) {
			return;
		}

		Set<String> foundAchievements = findAchievementsByCategoryAndName(category, craftName + ':' + item.getDurability());
		foundAchievements.addAll(findAchievementsByCategoryAndName(category, craftName));

		int eventAmount = event.getCurrentItem().getAmount();
		if (event.isShiftClick()) {
			int maxAmount = event.getInventory().getMaxStackSize();
			ItemStack[] matrix = event.getInventory().getMatrix();
			for (ItemStack itemStack : matrix) {
				if (itemStack != null && itemStack.getType() != Material.AIR) {
					int itemStackAmount = itemStack.getAmount();
					if (itemStackAmount < maxAmount && itemStackAmount > 0) {
						maxAmount = itemStackAmount;
					}
				}
			}
			eventAmount *= maxAmount;
			eventAmount = Math.min(eventAmount, getInventoryAvailableSpace(player, event.getCurrentItem()));
			if (eventAmount == 0) {
				return;
			}
		}

		int incrementValue = eventAmount; // Effectively final variable needed.
		foundAchievements.forEach(achievement -> updateStatisticAndAwardAchievementsIfAvailable(player, category,
				achievement, incrementValue));
	}
}
