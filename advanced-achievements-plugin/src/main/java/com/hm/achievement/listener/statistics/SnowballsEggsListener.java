package com.hm.achievement.listener.statistics;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.bukkit.entity.Egg;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.ProjectileLaunchEvent;

import com.hm.achievement.category.Category;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.utils.RewardParser;
import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Listener class to deal with Snowballs and Eggs achievements.
 * 
 * @author Pyves
 *
 */
@Singleton
public class SnowballsEggsListener extends AbstractListener {

	private final Set<Category> disabledCategories;

	@Inject
	public SnowballsEggsListener(@Named("main") CommentedYamlConfiguration mainConfig, int serverVersion,
			Map<String, List<Long>> sortedThresholds, CacheManager cacheManager, RewardParser rewardParser,
			Set<Category> disabledCategories) {
		super(mainConfig, serverVersion, sortedThresholds, cacheManager, rewardParser);
		this.disabledCategories = disabledCategories;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onProjectileLaunch(ProjectileLaunchEvent event) {
		if (!(event.getEntity().getShooter() instanceof Player)) {
			return;
		}

		Player player = (Player) event.getEntity().getShooter();
		NormalAchievements category;
		if (event.getEntity() instanceof Snowball) {
			category = NormalAchievements.SNOWBALLS;
		} else if (event.getEntity() instanceof Egg) {
			category = NormalAchievements.EGGS;
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
