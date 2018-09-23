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
import org.bukkit.event.player.PlayerItemConsumeEvent;

import com.hm.achievement.category.Category;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.utils.RewardParser;
import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Listener class to deal with EatenItems and ConsumedPotions achievements.
 * 
 * @author Pyves
 *
 */
@Singleton
public class ConsumedPotionsEatenItemsListener extends AbstractListener {

	private final Set<Category> disabledCategories;

	@Inject
	public ConsumedPotionsEatenItemsListener(@Named("main") CommentedYamlConfiguration mainConfig, int serverVersion,
			Map<String, List<Long>> sortedThresholds, CacheManager cacheManager, RewardParser rewardParser,
			Set<Category> disabledCategories) {
		super(mainConfig, serverVersion, sortedThresholds, cacheManager, rewardParser);
		this.disabledCategories = disabledCategories;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
		Player player = event.getPlayer();
		NormalAchievements category;

		Material itemMaterial = event.getItem().getType();
		if (itemMaterial == Material.POTION) {
			// Don't count drinking water toward ConsumePotions; check the potion type.
			if (isWaterPotion(event.getItem())) {
				return;
			}
			category = NormalAchievements.CONSUMEDPOTIONS;
		} else if (itemMaterial != Material.MILK_BUCKET) {
			category = NormalAchievements.EATENITEMS;
		} else {
			return;
		}

		if (disabledCategories.contains(category)) {
			return;
		}

		if (!shouldIncreaseBeTakenIntoAccount(player, category)) {
			return;
		}

		updateStatisticAndAwardAchievementsIfAvailable(player, category, 1);
	}
}
