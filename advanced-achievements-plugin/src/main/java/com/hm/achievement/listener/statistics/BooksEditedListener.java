package com.hm.achievement.listener.statistics;

import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerEditBookEvent;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.config.AchievementMap;
import com.hm.achievement.db.CacheManager;

/**
 * Listener class to deal with BooksEdited achievements.
 * 
 * @author Pyves
 *
 */
@Singleton
public class BooksEditedListener extends AbstractRateLimitedListener {

	@Inject
	public BooksEditedListener(@Named("main") YamlConfiguration mainConfig, int serverVersion, AchievementMap achievementMap,
			CacheManager cacheManager, AdvancedAchievements advancedAchievements,
			@Named("lang") YamlConfiguration langConfig, Logger logger) {
		super(NormalAchievements.BOOKSEDITED, mainConfig, serverVersion, achievementMap, cacheManager, advancedAchievements,
				langConfig, logger);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerEditBookEvent(PlayerEditBookEvent event) {
		updateStatisticAndAwardAchievementsIfAvailable(event.getPlayer(), 1);
	}

}
