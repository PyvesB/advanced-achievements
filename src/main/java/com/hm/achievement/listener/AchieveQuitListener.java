package com.hm.achievement.listener;

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
public class AchieveQuitListener implements Listener {

	private AdvancedAchievements plugin;

	public AchieveQuitListener(AdvancedAchievements plugin) {

		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerQuit(PlayerQuitEvent event) {

		final String playerUUID = event.getPlayer().getUniqueId().toString();

		// Clean cooldown HashMaps for book and list commands.
		plugin.getAchievementBookCommand().getPlayers().remove(playerUUID);
		plugin.getAchievementListCommand().getPlayers().remove(playerUUID);

		processAndCleanDistances(event, playerUUID);

		processAndCleanPlayedTime(playerUUID);

		// Remove player from HashSet cache for MaxLevel achievements.
		if (plugin.getXpListener() != null) {
			for (Integer achievementThreshold : plugin.getXpListener().getAchievementsCache().keySet())
				plugin.getXpListener().getAchievementsCache().remove(achievementThreshold, playerUUID);
		}

		// Remove player from HashSet for Connection achievements.
		if (plugin.getConnectionListener() != null)
			plugin.getConnectionListener().getPlayersAchieveConnectionRan().remove(playerUUID);
	}

	/**
	 * Writes the distances to the database and cleans the various in memory objects containing information about the
	 * disconnected player.
	 * 
	 * @param event
	 * @param playerUUID
	 */
	private void processAndCleanDistances(PlayerQuitEvent event, final String playerUUID) {

		// Remove player from Multimap caches for distance achievements.
		if (plugin.getAchieveDistanceRunnable() != null
				&& plugin.getAchieveDistanceRunnable().getPlayerLocations()
						.remove(playerUUID) != null) {
			for (Integer achievementThreshold : plugin.getAchieveDistanceRunnable().getFootAchievementsCache().keySet())
				plugin.getAchieveDistanceRunnable().getFootAchievementsCache().remove(achievementThreshold, playerUUID);
			for (Integer achievementThreshold : plugin.getAchieveDistanceRunnable().getHorseAchievementsCache()
					.keySet())
				plugin.getAchieveDistanceRunnable().getHorseAchievementsCache().remove(achievementThreshold,
						playerUUID);
			for (Integer achievementThreshold : plugin.getAchieveDistanceRunnable().getPigAchievementsCache().keySet())
				plugin.getAchieveDistanceRunnable().getPigAchievementsCache().remove(achievementThreshold, playerUUID);
			for (Integer achievementThreshold : plugin.getAchieveDistanceRunnable().getBoatAchievementsCache().keySet())
				plugin.getAchieveDistanceRunnable().getBoatAchievementsCache().remove(achievementThreshold, playerUUID);
			for (Integer achievementThreshold : plugin.getAchieveDistanceRunnable().getMinecartAchievementsCache()
					.keySet())
				plugin.getAchieveDistanceRunnable().getMinecartAchievementsCache().remove(achievementThreshold,
						playerUUID);
			for (Integer achievementThreshold : plugin.getAchieveDistanceRunnable().getGlidingAchievementsCache()
					.keySet())
				plugin.getAchieveDistanceRunnable().getGlidingAchievementsCache().remove(achievementThreshold,
						playerUUID);

			// Update database statistics for distances and clean HashMaps.
			if (plugin.isAsyncPooledRequestsSender()) {
				Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {

					@Override
					public void run() {

						// Items must be removed from HashMaps AFTER write to DB has finished. As this is an async task,
						// we could end up in a scenario where the player reconnects and data is not yet updated in the
						// database; in this case, the cached variables will still be valid.
						Integer distance = plugin.getPoolsManager().getDistanceFootHashMap().get(playerUUID);
						if (distance != null) {
							plugin.getDb().updateDistance(playerUUID, distance, "distancefoot");
							plugin.getPoolsManager().getDistanceFootHashMap().remove(playerUUID);
						}

						distance = plugin.getPoolsManager().getDistancePigHashMap().get(playerUUID);
						if (distance != null) {
							plugin.getDb().updateDistance(playerUUID, distance, "distancepig");
							plugin.getPoolsManager().getDistancePigHashMap().remove(playerUUID);
						}

						distance = plugin.getPoolsManager().getDistanceHorseHashMap().get(playerUUID);
						if (distance != null) {
							plugin.getDb().updateDistance(playerUUID, distance, "distancehorse");
							plugin.getPoolsManager().getDistanceHorseHashMap().remove(playerUUID);
						}

						distance = plugin.getPoolsManager().getDistanceBoatHashMap().get(playerUUID);
						if (distance != null) {
							plugin.getDb().updateDistance(playerUUID, distance, "distanceboat");
							plugin.getPoolsManager().getDistanceBoatHashMap().remove(playerUUID);
						}

						distance = plugin.getPoolsManager().getDistanceMinecartHashMap().get(playerUUID);
						if (distance != null) {
							plugin.getDb().updateDistance(playerUUID, distance, "distanceminecart");
							plugin.getPoolsManager().getDistanceMinecartHashMap().remove(playerUUID);
						}

						distance = plugin.getPoolsManager().getDistanceGlidingHashMap().get(playerUUID);
						if (distance != null) {
							plugin.getDb().updateDistance(playerUUID, distance, "distancegliding");
							plugin.getPoolsManager().getDistanceGlidingHashMap().remove(playerUUID);
						}
					}
				});
			} else {
				// Items can be removed from HashMaps directly, as this is done in the main thread of execution.
				Integer distance = plugin.getPoolsManager().getDistanceFootHashMap().remove(playerUUID);
				if (distance != null)
					plugin.getDb().updateDistance(playerUUID, distance, "distancefoot");

				distance = plugin.getPoolsManager().getDistancePigHashMap().remove(playerUUID);
				if (distance != null)
					plugin.getDb().updateDistance(playerUUID, distance, "distancepig");

				distance = plugin.getPoolsManager().getDistanceHorseHashMap().remove(playerUUID);
				if (distance != null)
					plugin.getDb().updateDistance(playerUUID, distance, "distancehorse");

				distance = plugin.getPoolsManager().getDistanceBoatHashMap().remove(playerUUID);
				if (distance != null)
					plugin.getDb().updateDistance(playerUUID, distance, "distanceboat");

				distance = plugin.getPoolsManager().getDistanceMinecartHashMap().remove(playerUUID);
				if (distance != null)
					plugin.getDb().updateDistance(playerUUID, distance, "distanceminecart");

				distance = plugin.getPoolsManager().getDistanceGlidingHashMap().remove(playerUUID);
				if (distance != null)
					plugin.getDb().updateDistance(playerUUID, distance, "distancegliding");
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
						Long playTime = plugin.getPoolsManager().getPlayedTimeHashMap().get(playerUUID);

						if (playTime != null) {
							plugin.getDb().updatePlaytime(playerUUID, playTime);
							plugin.getPoolsManager().getPlayedTimeHashMap().remove(playerUUID);
						}
					}

				});
			} else {
				// Items can be removed from HashMaps directly, as this is done in the main thread of execution.
				Long playTime = plugin.getPoolsManager().getPlayedTimeHashMap().remove(playerUUID);

				if (playTime != null)
					plugin.getDb().updatePlaytime(playerUUID, playTime);

			}
			// Remove player from Multimap cache for PlayedTime achievements.
			for (Integer achievementThreshold : plugin.getAchievePlayTimeRunnable().getAchievementsCache().keySet())
				plugin.getAchievePlayTimeRunnable().getAchievementsCache().remove(achievementThreshold, playerUUID);
		}
	}
}
