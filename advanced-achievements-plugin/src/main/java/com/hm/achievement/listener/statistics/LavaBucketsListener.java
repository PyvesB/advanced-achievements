package com.hm.achievement.listener.statistics;

import java.util.logging.Logger;

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
import com.hm.achievement.utils.RewardParser;

/**
 * Listener class to deal with LavaBuckets achievements.
 *
 * @author Pyves
 */
@Singleton
public class LavaBucketsListener extends AbstractRateLimitedListener {

	@Inject
	public LavaBucketsListener(@Named("main") YamlConfiguration mainConfig, int serverVersion, AchievementMap achievementMap,
			CacheManager cacheManager, RewardParser rewardParser, AdvancedAchievements advancedAchievements,
			@Named("lang") YamlConfiguration langConfig, Logger logger) {
		super(NormalAchievements.LAVABUCKETS, mainConfig, serverVersion, achievementMap, cacheManager, rewardParser,
				advancedAchievements, langConfig, logger);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerBucketFill(PlayerBucketFillEvent event) {
		if (event.getItemStack().getType() == Material.LAVA_BUCKET) {
			updateStatisticAndAwardAchievementsIfAvailable(event.getPlayer(), 1);
		}
	}
}
