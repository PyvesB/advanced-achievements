package com.hm.achievement.listener.statistics;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.bukkit.Material;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.utils.RewardParser;
import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Listener class to deal with MusicDiscs achievements.
 *
 * @author Pyves
 *
 */
@Singleton
public class MusicDiscsListener extends AbstractRateLimitedListener {

	@Inject
	public MusicDiscsListener(@Named("main") CommentedYamlConfiguration mainConfig,
			int serverVersion, Map<String, List<Long>> sortedThresholds, CacheManager cacheManager,
			RewardParser rewardParser, AdvancedAchievements advancedAchievements,
			@Named("lang") CommentedYamlConfiguration langConfig, Logger logger) {
		super(NormalAchievements.MUSICDISCS, mainConfig, serverVersion, sortedThresholds, cacheManager, rewardParser,
				advancedAchievements, langConfig, logger);
	}

	@EventHandler(priority = EventPriority.MONITOR) // Do NOT set ignoreCancelled to true, deprecated for this event.
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.useItemInHand() == Result.DENY || event.getAction() != Action.RIGHT_CLICK_BLOCK
				|| !event.getMaterial().isRecord() || event.getClickedBlock().getType() != Material.JUKEBOX) {
			return;
		}

		updateStatisticAndAwardAchievementsIfAvailable(event.getPlayer(), 1);
	}
}
