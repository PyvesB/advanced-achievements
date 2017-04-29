package com.hm.achievement.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.NormalAchievements;

/**
 * Listener class to deal with EnderPearls achievements and update Distances.
 * 
 * @author Pyves
 *
 */
public class AchieveTeleportRespawnListener extends AbstractListener {

	public AchieveTeleportRespawnListener(AdvancedAchievements plugin) {
		super(plugin);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		// Update location of player if he respawns after dying.
		if (plugin.getDistanceRunnable() != null) {
			plugin.getDistanceRunnable().getPlayerLocations().put(event.getPlayer().getUniqueId().toString(),
					event.getRespawnLocation());
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
		if (plugin.getDistanceRunnable() != null) {
			plugin.getDistanceRunnable().getPlayerLocations().put(player.getUniqueId().toString(), event.getTo());
		}

		NormalAchievements category = NormalAchievements.ENDERPEARLS;

		if (event.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL
				|| plugin.getDisabledCategorySet().contains(category.toString())) {
			return;
		}

		if (!shouldEventBeTakenIntoAccount(player, category)) {
			return;
		}

		updateStatisticAndAwardAchievementsIfAvailable(player, category, 1);
	}
}
