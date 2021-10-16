package com.hm.achievement.listener.statistics;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerItemBreakEvent;

import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.config.AchievementMap;
import com.hm.achievement.db.CacheManager;

/**
 * Listener class to deal with ItemBreaks achievements.
 * 
 * @author Pyves
 *
 */
@Singleton
public class ItemBreaksListener extends AbstractListener {

	@Inject
	public ItemBreaksListener(@Named("main") YamlConfiguration mainConfig, AchievementMap achievementMap,
			CacheManager cacheManager) {
		super(NormalAchievements.ITEMBREAKS, mainConfig, achievementMap, cacheManager);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerItemBreak(PlayerItemBreakEvent event) {
		updateStatisticAndAwardAchievementsIfAvailable(event.getPlayer(), 1);
	}
}
