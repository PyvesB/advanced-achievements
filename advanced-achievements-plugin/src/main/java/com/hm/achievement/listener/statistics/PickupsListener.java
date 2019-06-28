package com.hm.achievement.listener.statistics;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerPickupItemEvent;

import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.utils.RewardParser;
import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Listener class to deal with ItemPickups achievements. Keep PlayerPickupItemEvent for now, as it was only introduced
 * in late Minecraft 1.12 versions.
 * 
 * @author Pyves
 *
 */
@SuppressWarnings("deprecation")
@Singleton
public class PickupsListener extends AbstractListener {

	@Inject
	public PickupsListener(@Named("main") CommentedYamlConfiguration mainConfig, int serverVersion,
			Map<String, List<Long>> sortedThresholds, CacheManager cacheManager, RewardParser rewardParser) {
		super(mainConfig, serverVersion, sortedThresholds, cacheManager, rewardParser);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		updateStatisticAndAwardAchievementsIfAvailable(event.getPlayer(), NormalAchievements.PICKUPS, 1);
	}
}
