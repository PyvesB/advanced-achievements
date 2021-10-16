package com.hm.achievement.listener.statistics;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.raid.RaidFinishEvent;

import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.config.AchievementMap;
import com.hm.achievement.db.CacheManager;

/**
 * Processes raid win event.
 * 
 * @author Taavi Väänänen
 */
@Singleton
public class WinRaidListener extends AbstractListener {

	@Inject
	public WinRaidListener(@Named("main") YamlConfiguration mainConfig, AchievementMap achievementMap,
			CacheManager cacheManager) {
		super(NormalAchievements.RAIDSWON, mainConfig, achievementMap, cacheManager);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onRaidFinish(RaidFinishEvent event) {
		event.getWinners().forEach(player -> updateStatisticAndAwardAchievementsIfAvailable(player, 1));
	}
}
