package com.hm.achievement.listener.statistics;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.hm.achievement.category.Category;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.runnable.AchieveDistanceRunnable;
import com.hm.achievement.utils.RewardParser;
import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Listener class to deal with EnderPearls achievements and update Distances.
 * 
 * @author Pyves
 *
 */
@Singleton
public class EnderPearlsDistancesListener extends AbstractListener {

	private final Set<Category> disabledCategories;
	private final AchieveDistanceRunnable distanceRunnable;

	@Inject
	public EnderPearlsDistancesListener(@Named("main") CommentedYamlConfiguration mainConfig, int serverVersion,
			Map<String, List<Long>> sortedThresholds, CacheManager cacheManager, RewardParser rewardParser,
			Set<Category> disabledCategories, AchieveDistanceRunnable distanceRunnable) {
		super(mainConfig, serverVersion, sortedThresholds, cacheManager, rewardParser);
		this.disabledCategories = disabledCategories;
		this.distanceRunnable = distanceRunnable;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		// Update location of player if he respawns after dying.
		if (distanceRunnable != null) {
			distanceRunnable.updateLocation(event.getPlayer().getUniqueId(), event.getRespawnLocation());
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		Player player = event.getPlayer();

		// Event fired twice when teleporting with a nether portal: first time to go to nether with the cause
		// NETHER_PORTAL, then later on to change location in nether; we must only consider the second change because
		// the location of the player is not updated during the first event; if the distances are monitored by the
		// plugin between the two events, it would lead to incorrect results.
		if (event.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
			return;
		}

		// Update location of player if he teleports somewhere else.
		if (distanceRunnable != null) {
			distanceRunnable.updateLocation(player.getUniqueId(), event.getTo());
		}

		NormalAchievements category = NormalAchievements.ENDERPEARLS;

		if (event.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL || disabledCategories.contains(category)) {
			return;
		}

		if (!shouldIncreaseBeTakenIntoAccount(player, category)) {
			return;
		}

		updateStatisticAndAwardAchievementsIfAvailable(player, category, 1);
	}
}
