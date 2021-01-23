package com.hm.achievement.listener.statistics;

import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerBedEnterEvent;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.config.AchievementMap;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.utils.RewardParser;

/**
 * Listener class to deal with Beds achievements.
 * 
 * @author Pyves
 *
 */
@Singleton
public class BedsListener extends AbstractRateLimitedListener {

	@Inject
	public BedsListener(@Named("main") YamlConfiguration mainConfig, int serverVersion, AchievementMap achievementMap,
			CacheManager cacheManager, RewardParser rewardParser, AdvancedAchievements advancedAchievements,
			@Named("lang") YamlConfiguration langConfig, Logger logger) {
		super(NormalAchievements.BEDS, mainConfig, serverVersion, achievementMap, cacheManager, rewardParser,
				advancedAchievements, langConfig, logger);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerBedEnter(PlayerBedEnterEvent event) {
		updateStatisticAndAwardAchievementsIfAvailable(event.getPlayer(), 1);
	}
}
