package com.hm.achievement.listener.statistics;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerLevelChangeEvent;

import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.config.AchievementMap;
import com.hm.achievement.db.CacheManager;

/**
 * Listener class to deal with MaxLevel achievements.
 * 
 * @author Pyves
 *
 */
@Singleton
public class LevelsListener extends AbstractListener {

	@Inject
	public LevelsListener(@Named("main") YamlConfiguration mainConfig, AchievementMap achievementMap,
			CacheManager cacheManager) {
		super(NormalAchievements.LEVELS, mainConfig, achievementMap, cacheManager);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerExpChange(PlayerLevelChangeEvent event) {
		Player player = event.getPlayer();

		int previousMaxLevel = (int) cacheManager.getAndIncrementStatisticAmount(NormalAchievements.LEVELS,
				player.getUniqueId(), 0);
		if (event.getNewLevel() <= previousMaxLevel) {
			return;
		}

		updateStatisticAndAwardAchievementsIfAvailable(player, event.getNewLevel() - previousMaxLevel);
	}
}
