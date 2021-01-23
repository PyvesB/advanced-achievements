package com.hm.achievement.listener.statistics;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityBreedEvent;

import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.config.AchievementMap;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.utils.RewardParser;

/**
 * Listener class to deal with Breeding achievements.
 */
@Singleton
public class BreedingListener extends AbstractListener {

	@Inject
	public BreedingListener(@Named("main") YamlConfiguration mainConfig, int serverVersion, AchievementMap achievementMap,
			CacheManager cacheManager, RewardParser rewardParser) {
		super(MultipleAchievements.BREEDING, mainConfig, serverVersion, achievementMap, cacheManager, rewardParser);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEntityBreed(EntityBreedEvent event) {
		if (!(event.getBreeder() instanceof Player)) {
			return;
		}

		String mobName = event.getMother().getType().name().toLowerCase();
		if (!event.getBreeder().hasPermission(category.toChildPermName(mobName))) {
			return;
		}

		Set<String> foundAchievements = findAchievementsByCategoryAndName(mobName);
		updateStatisticAndAwardAchievementsIfAvailable((Player) event.getBreeder(), foundAchievements, 1);
	}
}
