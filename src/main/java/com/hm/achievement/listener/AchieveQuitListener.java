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
		plugin.getAchievementBookCommand().getPlayers().remove(event.getPlayer());
		plugin.getAchievementListCommand().getPlayers().remove(event.getPlayer());

		processAndCleanDistances(event, playerUUID);

		processAndCleanPlayedTime(playerUUID);

		// Remove player from HashSet cache for MaxLevel achievements.
		if (plugin.getXpListener() != null) {
			for (Integer achievementThreshold : plugin.getXpListener().getAchievementsCache().keySet())
				plugin.getXpListener().getAchievementsCache().remove(achievementThreshold, playerUUID);
		}
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
				&& plugin.getAchieveDistanceRunnable().getPlayerLocations().remove(event.getPlayer()) != null) {
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
						Integer distance = plugin.getAchieveDistanceRunnable().getAchievementDistancesFoot()
								.get(playerUUID);
						if (distance != null)
							plugin.getDb().updateAndGetDistance(playerUUID, distance, "distancefoot");
						plugin.getAchieveDistanceRunnable().getAchievementDistancesFoot().remove(playerUUID);

						distance = plugin.getAchieveDistanceRunnable().getAchievementDistancesPig().get(playerUUID);
						if (distance != null)
							plugin.getDb().updateAndGetDistance(playerUUID, distance, "distancepig");
						plugin.getAchieveDistanceRunnable().getAchievementDistancesPig().remove(playerUUID);

						distance = plugin.getAchieveDistanceRunnable().getAchievementDistancesHorse().get(playerUUID);
						if (distance != null)
							plugin.getDb().updateAndGetDistance(playerUUID, distance, "distancehorse");
						plugin.getAchieveDistanceRunnable().getAchievementDistancesHorse().remove(playerUUID);

						distance = plugin.getAchieveDistanceRunnable().getAchievementDistancesBoat().get(playerUUID);
						if (distance != null)
							plugin.getDb().updateAndGetDistance(playerUUID, distance, "distanceboat");
						plugin.getAchieveDistanceRunnable().getAchievementDistancesBoat().remove(playerUUID);

						distance = plugin.getAchieveDistanceRunnable().getAchievementDistancesMinecart()
								.get(playerUUID);
						if (distance != null)
							plugin.getDb().updateAndGetDistance(playerUUID, distance, "distanceminecart");
						plugin.getAchieveDistanceRunnable().getAchievementDistancesMinecart().remove(playerUUID);

						distance = plugin.getAchieveDistanceRunnable().getAchievementDistancesGliding().get(playerUUID);
						if (distance != null)
							plugin.getDb().updateAndGetDistance(playerUUID, distance, "distancegliding");
						plugin.getAchieveDistanceRunnable().getAchievementDistancesGliding().remove(playerUUID);
					}
				});
			} else {
				// Items can be removed from HashMaps directly, as this is done in the main thread of execution.
				Integer distance = plugin.getAchieveDistanceRunnable().getAchievementDistancesFoot().remove(playerUUID);
				if (distance != null)
					plugin.getDb().updateAndGetDistance(playerUUID, distance, "distancefoot");

				distance = plugin.getAchieveDistanceRunnable().getAchievementDistancesPig().remove(playerUUID);
				if (distance != null)
					plugin.getDb().updateAndGetDistance(playerUUID, distance, "distancepig");

				distance = plugin.getAchieveDistanceRunnable().getAchievementDistancesHorse().remove(playerUUID);
				if (distance != null)
					plugin.getDb().updateAndGetDistance(playerUUID, distance, "distancehorse");

				distance = plugin.getAchieveDistanceRunnable().getAchievementDistancesBoat().remove(playerUUID);
				if (distance != null)
					plugin.getDb().updateAndGetDistance(playerUUID, distance, "distanceboat");

				distance = plugin.getAchieveDistanceRunnable().getAchievementDistancesMinecart().remove(playerUUID);
				if (distance != null)
					plugin.getDb().updateAndGetDistance(playerUUID, distance, "distanceminecart");

				distance = plugin.getAchieveDistanceRunnable().getAchievementDistancesGliding().remove(playerUUID);
				if (distance != null)
					plugin.getDb().updateAndGetDistance(playerUUID, distance, "distancegliding");
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

						// Items must be removed from HashMaps AFTER write to DB has finished. As this is an async task,
						// we could end up in a scenario where the player reconnects and data is not yet updated in the
						// database; in this case, the cached variables will still be valid.
						Long playTime = plugin.getConnectionListener().getPlayTime().get(playerUUID);
						Long joinTime = plugin.getConnectionListener().getJoinTime().get(playerUUID);

						if (playTime != null && joinTime != null)
							plugin.getDb().updateAndGetPlaytime(playerUUID,
									playTime + System.currentTimeMillis() - joinTime);

						plugin.getConnectionListener().getPlayTime().remove(playerUUID);
						plugin.getConnectionListener().getJoinTime().remove(playerUUID);
					}

				});
			} else {
				// Items can be removed from HashMaps directly, as this is done in the main thread of execution.
				Long playTime = plugin.getConnectionListener().getPlayTime().remove(playerUUID);
				Long joinTime = plugin.getConnectionListener().getJoinTime().remove(playerUUID);

				if (playTime != null && joinTime != null)
					plugin.getDb().updateAndGetPlaytime(playerUUID, playTime + System.currentTimeMillis() - joinTime);

			}
			// Remove player from Multimap cache for PlayedTime achievements.
			for (Integer achievementThreshold : plugin.getAchievePlayTimeRunnable().getAchievementsCache().keySet())
				plugin.getAchievePlayTimeRunnable().getAchievementsCache().remove(achievementThreshold, playerUUID);
		}
	}
}
