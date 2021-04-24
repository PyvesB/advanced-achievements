package com.hm.achievement.listener.statistics;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.config.AchievementMap;
import com.hm.achievement.db.CacheManager;

/**
 * Listener class to deal with Places achievements.
 * 
 * @author Pyves
 *
 */
@Singleton
public class PlacesListener extends AbstractListener {

	@Inject
	public PlacesListener(@Named("main") YamlConfiguration mainConfig, int serverVersion, AchievementMap achievementMap,
			CacheManager cacheManager) {
		super(MultipleAchievements.PLACES, mainConfig, serverVersion, achievementMap, cacheManager);
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		ItemStack placedItem = event.getItemInHand();

		Set<String> subcategories = new HashSet<>();

		String blockName = placedItem.getType().name().toLowerCase();
		if (player.hasPermission(category.toChildPermName(blockName))) {
			addMatchingSubcategories(subcategories, blockName + ':' + placedItem.getDurability());
			addMatchingSubcategories(subcategories, blockName);
		}

		ItemMeta itemMeta = placedItem.getItemMeta();
		if (itemMeta != null && itemMeta.hasDisplayName()) {
			String displayName = itemMeta.getDisplayName();
			if (player.hasPermission(category.toChildPermName(StringUtils.deleteWhitespace(displayName)))) {
				addMatchingSubcategories(subcategories, displayName);
			}
		}

		updateStatisticAndAwardAchievementsIfAvailable(player, subcategories, 1);
	}
}
