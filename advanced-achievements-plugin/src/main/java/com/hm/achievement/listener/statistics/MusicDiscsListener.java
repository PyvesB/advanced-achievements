package com.hm.achievement.listener.statistics;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.config.AchievementMap;
import com.hm.achievement.db.CacheManager;

/**
 * Listener class to deal with MusicDiscs achievements.
 *
 * @author Pyves
 *
 */
@Singleton
public class MusicDiscsListener extends AbstractRateLimitedListener {

	@Inject
	public MusicDiscsListener(@Named("main") YamlConfiguration mainConfig, AchievementMap achievementMap,
			CacheManager cacheManager, AdvancedAchievements advancedAchievements,
			@Named("lang") YamlConfiguration langConfig) {
		super(NormalAchievements.MUSICDISCS, mainConfig, achievementMap, cacheManager, advancedAchievements, langConfig);
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
