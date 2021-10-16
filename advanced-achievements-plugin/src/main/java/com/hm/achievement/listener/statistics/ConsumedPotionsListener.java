package com.hm.achievement.listener.statistics;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerItemConsumeEvent;

import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.config.AchievementMap;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.utils.MaterialHelper;

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
	public ConsumedPotionsListener(@Named("main") YamlConfiguration mainConfig, AchievementMap achievementMap,
			CacheManager cacheManager, MaterialHelper materialHelper) {
		super(NormalAchievements.CONSUMEDPOTIONS, mainConfig, achievementMap, cacheManager);
		this.materialHelper = materialHelper;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
		if (materialHelper.isAnyPotionButWater(event.getItem())) {
			updateStatisticAndAwardAchievementsIfAvailable(event.getPlayer(), 1);
		}
	}
}
