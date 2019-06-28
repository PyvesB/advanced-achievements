package com.hm.achievement.listener.statistics;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerShearEntityEvent;

import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.utils.RewardParser;
import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Listener class to deal with Shear achievements (only sheep are taken into account).
 * 
 * @author Pyves
 *
 */
@Singleton
public class ShearsListener extends AbstractListener {

	@Inject
	public ShearsListener(@Named("main") CommentedYamlConfiguration mainConfig, int serverVersion,
			Map<String, List<Long>> sortedThresholds, CacheManager cacheManager, RewardParser rewardParser) {
		super(mainConfig, serverVersion, sortedThresholds, cacheManager, rewardParser);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerShearEntity(PlayerShearEntityEvent event) {
		if (!(event.getEntity() instanceof Sheep)) {
			return;
		}

		updateStatisticAndAwardAchievementsIfAvailable(event.getPlayer(), NormalAchievements.SHEARS, 1);
	}
}
