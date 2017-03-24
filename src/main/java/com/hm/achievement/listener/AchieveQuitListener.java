package com.hm.achievement.listener;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.NormalAchievements;

/**
 * Listener class to deal with Distance and PlayedTime achievements.
 * 
 * @author Pyves
 *
 */
public class AchieveQuitListener extends AbstractListener implements Listener {

	public AchieveQuitListener(AdvancedAchievements plugin) {
		super(plugin);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerQuit(PlayerQuitEvent event) {
		final String playerUUID = event.getPlayer().getUniqueId().toString();

		// Clean cooldown HashMap for book command.
		plugin.getAchievementBookCommand().getPlayersBookTime().remove(playerUUID);

		// Clear achievements caches.
		plugin.getPoolsManager().getReceivedAchievementsCache().removeAll(playerUUID);
		plugin.getPoolsManager().getNotReceivedAchievementsCache().removeAll(playerUUID);
		plugin.getPoolsManager().getTotalPlayerAchievementsCache().remove(playerUUID);

		processAndCleanDistances(playerUUID);

		processAndCleanPlayedTime(playerUUID);

		// Remove player from HashSet for Connection achievements.
		if (plugin.getConnectionListener() != null) {
			plugin.getConnectionListener().getPlayersAchieveConnectionRan().remove(playerUUID);
		}

		// Remove player from cooldown structures.
		if (plugin.getBedListener() != null) {
			plugin.getBedListener().removePlayerFromCooldownMap(playerUUID);
		}
		if (plugin.getInventoryClickListener() != null) {
			plugin.getInventoryClickListener().removePlayerFromCooldownMap(playerUUID);
		}
		if (plugin.getMilkLavaWaterListener() != null) {
			plugin.getMilkLavaWaterListener().removePlayerFromCooldownMap(playerUUID);
		}
		if (plugin.getHoeFertiliseFireworkMusicListener() != null) {
			plugin.getHoeFertiliseFireworkMusicListener().removePlayerFromCooldownMap(playerUUID);
		}
	}

	/**
	 * Writes the distances to the database and cleans the various in memory objects containing information about the
	 * disconnected player.
	 * 
	 * @param event
	 * @param playerUUID
	 */
	private void processAndCleanDistances(final String playerUUID) {
		// Remove player from Multimap caches for distance achievements.
		if (plugin.getAchieveDistanceRunnable() != null
				&& plugin.getAchieveDistanceRunnable().getPlayerLocations().remove(playerUUID) != null) {
			// Update database statistics for distances and clean HashMaps.
			if (plugin.isAsyncPooledRequestsSender()) {
				Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {

					@Override
					public void run() {
						updateAndRemoveDistance(playerUUID, NormalAchievements.DISTANCEFOOT);
						updateAndRemoveDistance(playerUUID, NormalAchievements.DISTANCEPIG);
						updateAndRemoveDistance(playerUUID, NormalAchievements.DISTANCEHORSE);
						updateAndRemoveDistance(playerUUID, NormalAchievements.DISTANCEBOAT);
						updateAndRemoveDistance(playerUUID, NormalAchievements.DISTANCEMINECART);
						updateAndRemoveDistance(playerUUID, NormalAchievements.DISTANCEGLIDING);
						updateAndRemoveDistance(playerUUID, NormalAchievements.DISTANCELLAMA);
					}

					private void updateAndRemoveDistance(final String playerUUID, NormalAchievements category) {
						// Items must be removed from HashMaps AFTER write to DB has finished. As this is an async task,
						// we could end up in a scenario where the player reconnects and data is not yet updated in the
						// database; in this case, the cached variables will still be valid.
						Map<String, Long> map = plugin.getPoolsManager().getHashMap(category);
						Long distance = map.get(playerUUID);
						if (distance != null) {
							plugin.getDb().updateStatistic(playerUUID, distance, category.toDBName());
							map.remove(playerUUID);
						}
					}
				});
			} else {
				// Items can be removed from HashMaps directly, as this is done in the main thread of execution.
				Long distance = plugin.getPoolsManager().getHashMap(NormalAchievements.DISTANCEFOOT).remove(playerUUID);
				if (distance != null) {
					plugin.getDb().updateStatistic(playerUUID, distance, NormalAchievements.DISTANCEFOOT.toDBName());
				}

				distance = plugin.getPoolsManager().getHashMap(NormalAchievements.DISTANCEPIG).remove(playerUUID);
				if (distance != null) {
					plugin.getDb().updateStatistic(playerUUID, distance, NormalAchievements.DISTANCEPIG.toDBName());
				}

				distance = plugin.getPoolsManager().getHashMap(NormalAchievements.DISTANCEHORSE).remove(playerUUID);
				if (distance != null) {
					plugin.getDb().updateStatistic(playerUUID, distance, NormalAchievements.DISTANCEHORSE.toDBName());
				}

				distance = plugin.getPoolsManager().getHashMap(NormalAchievements.DISTANCEBOAT).remove(playerUUID);
				if (distance != null) {
					plugin.getDb().updateStatistic(playerUUID, distance, NormalAchievements.DISTANCEBOAT.toDBName());
				}

				distance = plugin.getPoolsManager().getHashMap(NormalAchievements.DISTANCEMINECART).remove(playerUUID);
				if (distance != null) {
					plugin.getDb().updateStatistic(playerUUID, distance, NormalAchievements.DISTANCEMINECART.toDBName());
				}

				distance = plugin.getPoolsManager().getHashMap(NormalAchievements.DISTANCEGLIDING).remove(playerUUID);
				if (distance != null) {
					plugin.getDb().updateStatistic(playerUUID, distance, NormalAchievements.DISTANCEGLIDING.toDBName());
				}

				distance = plugin.getPoolsManager().getHashMap(NormalAchievements.DISTANCELLAMA).remove(playerUUID);
				if (distance != null) {
					plugin.getDb().updateStatistic(playerUUID, distance, NormalAchievements.DISTANCELLAMA.toDBName());
				}
			}
		}
	}

	/**
	 * Writes the played time to the database and cleans the various in memory objects containing information about the
	 * disconnected player.
	 * 
	 * @param playerUUID
	 */
	private void processAndCleanPlayedTime(final String playerUUID) {
		if (plugin.getAchievePlayTimeRunnable() != null) {
			// Update database statistics for played time and clean HashMaps.
			if (plugin.isAsyncPooledRequestsSender()) {
				Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {

					@Override
					public void run() {
						// Items must be removed from HashMap AFTER write to DB has finished. As this is an async task,
						// we could end up in a scenario where the player reconnects and data is not yet updated in the
						// database; in this case, the cached variables will still be valid.
						Long playTime = plugin.getPoolsManager().getHashMap(NormalAchievements.PLAYEDTIME)
								.get(playerUUID);

						if (playTime != null) {
							plugin.getDb().updateStatistic(playerUUID, playTime,
									NormalAchievements.PLAYEDTIME.toDBName());
							plugin.getPoolsManager().getHashMap(NormalAchievements.PLAYEDTIME).remove(playerUUID);
						}
					}
				});
			} else {
				// Items can be removed from HashMaps directly, as this is done in the main thread of execution.
				Long playTime = plugin.getPoolsManager().getHashMap(NormalAchievements.PLAYEDTIME).remove(playerUUID);

				if (playTime != null) {
					plugin.getDb().updateStatistic(playerUUID, playTime, NormalAchievements.PLAYEDTIME.toDBName());
				}
			}
		}
	}
}
