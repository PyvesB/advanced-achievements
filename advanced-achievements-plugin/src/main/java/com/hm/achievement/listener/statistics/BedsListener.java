package com.hm.achievement.listener.statistics;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerBedEnterEvent;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.utils.RewardParser;
import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Listener class to deal with Beds achievements.
 * 
 * @author Pyves
 *
 */
@Singleton
public class BedsListener extends AbstractRateLimitedListener {

	@Inject
	public BedsListener(@Named("main") CommentedYamlConfiguration mainConfig, int serverVersion,
			Map<String, List<Long>> sortedThresholds, CacheManager cacheManager, RewardParser rewardParser,
			AdvancedAchievements advancedAchievements, @Named("lang") CommentedYamlConfiguration langConfig, Logger logger) {
		super(mainConfig, serverVersion, sortedThresholds, cacheManager, rewardParser, advancedAchievements, langConfig,
				logger);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerBedEnter(PlayerBedEnterEvent event) {
		Player player = event.getPlayer();
		NormalAchievements category = NormalAchievements.BEDS;
		if (isInCooldownPeriod(player, false, category)) {
			return;
		}

		updateStatisticAndAwardAchievementsIfAvailable(player, category, 1);
	}
}
