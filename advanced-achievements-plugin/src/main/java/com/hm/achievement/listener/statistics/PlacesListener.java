package com.hm.achievement.listener.statistics;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.utils.RewardParser;
import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Listener class to deal with Places achievements.
 * 
 * @author Pyves
 *
 */
@Singleton
public class PlacesListener extends AbstractListener {

	@Inject
	public PlacesListener(@Named("main") CommentedYamlConfiguration mainConfig, int serverVersion,
			Map<String, List<Long>> sortedThresholds, CacheManager cacheManager, RewardParser rewardParser) {
		super(MultipleAchievements.PLACES, mainConfig, serverVersion, sortedThresholds, cacheManager, rewardParser);
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		ItemStack placedItem = event.getItemInHand();

		Set<String> foundAchievements = new HashSet<>();

		String blockName = placedItem.getType().name().toLowerCase();
		if (player.hasPermission(category.toChildPermName(blockName))) {
			foundAchievements.addAll(findAchievementsByCategoryAndName(blockName + ':' + placedItem.getDurability()));
			foundAchievements.addAll(findAchievementsByCategoryAndName(blockName));
		}

		ItemMeta itemMeta = placedItem.getItemMeta();
		if (itemMeta != null && itemMeta.hasDisplayName()) {
			String displayName = itemMeta.getDisplayName();
			if (player.hasPermission(category.toChildPermName(StringUtils.deleteWhitespace(displayName)))) {
				foundAchievements.addAll(findAchievementsByCategoryAndName(displayName));
			}
		}

		updateStatisticAndAwardAchievementsIfAvailable(player, foundAchievements, 1);
	}
}
