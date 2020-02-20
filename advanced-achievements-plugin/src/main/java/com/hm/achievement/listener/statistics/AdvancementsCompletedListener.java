package com.hm.achievement.listener.statistics;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.utils.RewardParser;
import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Listener class to deal with Advancement achievements.
 * 
 * @author Ghoti_Mayo
 *
 */
@Singleton
public class AdvancementsCompletedListener extends AbstractListener {

	@Inject
	public AdvancementsCompletedListener(@Named("main") CommentedYamlConfiguration mainConfig, int serverVersion,
			Map<String, List<Long>> sortedThresholds, CacheManager cacheManager, RewardParser rewardParser) {
		super(NormalAchievements.ADVANCEMENTSCOMPLETED, mainConfig, serverVersion, sortedThresholds, cacheManager,
				rewardParser);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerDoneAdvancement(PlayerAdvancementDoneEvent event) {
		updateStatisticAndAwardAchievementsIfAvailable(event.getPlayer(), 1);
	}
}
