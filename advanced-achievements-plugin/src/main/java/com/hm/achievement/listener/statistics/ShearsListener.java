package com.hm.achievement.listener.statistics;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerShearEntityEvent;

import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.config.AchievementMap;
import com.hm.achievement.db.CacheManager;

/**
 * Listener class to deal with Shear achievements (only sheep are taken into account).
 * 
 * @author Pyves
 *
 */
@Singleton
public class ShearsListener extends AbstractListener {

	@Inject
	public ShearsListener(@Named("main") YamlConfiguration mainConfig, AchievementMap achievementMap,
			CacheManager cacheManager) {
		super(NormalAchievements.SHEARS, mainConfig, achievementMap, cacheManager);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerShearEntity(PlayerShearEntityEvent event) {
		if (!(event.getEntity() instanceof Sheep)) {
			return;
		}

		updateStatisticAndAwardAchievementsIfAvailable(event.getPlayer(), 1);
	}
}
