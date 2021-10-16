package com.hm.achievement.listener.statistics;

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

/**
 * Listener class to deal with Beds achievements.
 * 
 * @author Pyves
 *
 */
@Singleton
public class BedsListener extends AbstractRateLimitedListener {

	@Inject
	public BedsListener(@Named("main") YamlConfiguration mainConfig, AchievementMap achievementMap,
			CacheManager cacheManager, AdvancedAchievements advancedAchievements,
			@Named("lang") YamlConfiguration langConfig) {
		super(NormalAchievements.BEDS, mainConfig, achievementMap, cacheManager, advancedAchievements, langConfig);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerBedEnter(PlayerBedEnterEvent event) {
		updateStatisticAndAwardAchievementsIfAvailable(event.getPlayer(), 1);
	}
}
