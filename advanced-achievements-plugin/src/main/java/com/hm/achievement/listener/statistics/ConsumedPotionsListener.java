package com.hm.achievement.listener.statistics;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerItemConsumeEvent;

import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.utils.MaterialHelper;
import com.hm.achievement.utils.RewardParser;
import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Listener class to deal with ConsumedPotions achievements.
 * 
 * @author Pyves
 *
 */
@Singleton
public class ConsumedPotionsListener extends AbstractListener {

	private final MaterialHelper materialHelper;

	@Inject
	public ConsumedPotionsListener(@Named("main") CommentedYamlConfiguration mainConfig, int serverVersion,
			Map<String, List<Long>> sortedThresholds, CacheManager cacheManager, RewardParser rewardParser,
			MaterialHelper materialHelper) {
		super(NormalAchievements.CONSUMEDPOTIONS, mainConfig, serverVersion, sortedThresholds, cacheManager, rewardParser);
		this.materialHelper = materialHelper;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
		if (materialHelper.isAnyPotionButWater(event.getItem())) {
			updateStatisticAndAwardAchievementsIfAvailable(event.getPlayer(), 1);
		}
	}
}
