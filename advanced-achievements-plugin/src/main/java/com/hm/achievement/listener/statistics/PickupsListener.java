package com.hm.achievement.listener.statistics;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityPickupItemEvent;

import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.config.AchievementMap;
import com.hm.achievement.db.CacheManager;

/**
 * Listener class to deal with ItemPickups achievements.
 * 
 * @author Pyves
 *
 */
@Singleton
public class PickupsListener extends AbstractListener {

	@Inject
	public PickupsListener(@Named("main") YamlConfiguration mainConfig, AchievementMap achievementMap,
			CacheManager cacheManager) {
		super(NormalAchievements.PICKUPS, mainConfig, achievementMap, cacheManager);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEntityPickupItem(EntityPickupItemEvent event) {
		if (event.getEntity() instanceof Player) {
			updateStatisticAndAwardAchievementsIfAvailable((Player) event.getEntity(), 1);
		}
	}
}
