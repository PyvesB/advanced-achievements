package com.hm.achievement.listener;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import com.hm.achievement.AdvancedAchievements;

/**
 * Listener class to deal with Distance and PlayedTime achievements.
 * 
 * @author Pyves
 *
 */
public class QuitListener extends AbstractListener implements Listener {

	public QuitListener(AdvancedAchievements plugin) {
		super(plugin);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerQuit(final PlayerQuitEvent event) {

		final UUID uuid = event.getPlayer().getUniqueId();
		final String uuidString = uuid.toString();

		// Delay cleaning up to avoid invalidating data immediately: players frequently disconnect and reconnect just
		// after. This also avoids players taking advantage of the reset of cooldowns.
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(
				Bukkit.getPluginManager().getPlugin(plugin.getDescription().getName()), new Runnable() {

					@Override
					public void run() {
						if (Bukkit.getPlayer(uuid) != null) {
							// Player reconnected.
							return;
						}

						// Prepare the removal of cached statistics from the different category caches.
						plugin.getCacheManager().signalPlayerDisconnectionToCachedStatistics(uuid);

						// Clean cooldown HashMap for book command.
						plugin.getAchievementBookCommand().getPlayersBookTime().remove(uuidString);

						// Clear achievements caches.
						plugin.getCacheManager().getReceivedAchievementsCache().removeAll(uuidString);
						plugin.getCacheManager().getNotReceivedAchievementsCache().removeAll(uuidString);
						plugin.getCacheManager().getTotalPlayerAchievementsCache().remove(uuidString);

						if (plugin.getDistanceRunnable() != null) {
							plugin.getDistanceRunnable().getPlayerLocations().remove(uuidString);
						}

						// Remove player from HashSet for Connection achievements.
						if (plugin.getConnectionListener() != null) {
							plugin.getConnectionListener().getPlayersAchieveConnectionRan().remove(uuidString);
						}

						// Remove player from cooldown structures.
						if (plugin.getBedListener() != null) {
							plugin.getBedListener().removePlayerFromCooldownMap(uuidString);
						}
						if (plugin.getTradeAnvilBrewSmeltListener() != null) {
							plugin.getTradeAnvilBrewSmeltListener().removePlayerFromCooldownMap(uuidString);
						}
						if (plugin.getMilkLavaWaterListener() != null) {
							plugin.getMilkLavaWaterListener().removePlayerFromCooldownMap(uuidString);
						}
						if (plugin.getHoeFertiliseFireworkMusicListener() != null) {
							plugin.getHoeFertiliseFireworkMusicListener().removePlayerFromCooldownMap(uuidString);
						}
					}
				}, 200);
	}
}
