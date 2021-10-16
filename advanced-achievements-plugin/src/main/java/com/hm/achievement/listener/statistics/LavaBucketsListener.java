package com.hm.achievement.listener.statistics;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerBucketFillEvent;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.config.AchievementMap;
import com.hm.achievement.db.CacheManager;

/**
 * Listener class to deal with LavaBuckets achievements.
 *
 * @author Pyves
 */
@Singleton
public class LavaBucketsListener extends AbstractRateLimitedListener {

	@Inject
	public LavaBucketsListener(@Named("main") YamlConfiguration mainConfig, AchievementMap achievementMap,
			CacheManager cacheManager, AdvancedAchievements advancedAchievements,
			@Named("lang") YamlConfiguration langConfig) {
		super(NormalAchievements.LAVABUCKETS, mainConfig, achievementMap, cacheManager, advancedAchievements, langConfig);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerBucketFill(PlayerBucketFillEvent event) {
		if (event.getItemStack().getType() == Material.LAVA_BUCKET) {
			updateStatisticAndAwardAchievementsIfAvailable(event.getPlayer(), 1);
		}
	}
}
