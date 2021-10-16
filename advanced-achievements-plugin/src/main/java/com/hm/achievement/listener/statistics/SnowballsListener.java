package com.hm.achievement.listener.statistics;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.projectiles.ProjectileSource;

import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.config.AchievementMap;
import com.hm.achievement.db.CacheManager;

/**
 * Listener class to deal with Snowballsachievements.
 * 
 * @author Pyves
 *
 */
@Singleton
public class SnowballsListener extends AbstractListener {

	@Inject
	public SnowballsListener(@Named("main") YamlConfiguration mainConfig, AchievementMap achievementMap,
			CacheManager cacheManager) {
		super(NormalAchievements.SNOWBALLS, mainConfig, achievementMap, cacheManager);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onProjectileLaunch(ProjectileLaunchEvent event) {
		ProjectileSource shooter = event.getEntity().getShooter();
		if (!(shooter instanceof Player) || !(event.getEntity() instanceof Snowball)) {
			return;
		}

		updateStatisticAndAwardAchievementsIfAvailable((Player) shooter, 1);
	}
}
