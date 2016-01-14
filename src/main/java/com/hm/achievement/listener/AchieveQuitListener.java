package com.hm.achievement.listener;

import java.util.HashSet;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import com.hm.achievement.AdvancedAchievements;

public class AchieveQuitListener implements Listener {

	private AdvancedAchievements plugin;

	public AchieveQuitListener(AdvancedAchievements plugin) {

		this.plugin = plugin;
	}

	@SuppressWarnings("unchecked")
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerQuit(PlayerQuitEvent event) {

		// Clean HashMaps for commands.
		plugin.getAchievementBookCommand().getPlayers().remove(event.getPlayer());
		plugin.getAchievementListCommand().getPlayers().remove(event.getPlayer());

		// Clean HashSets cache.
		if (plugin.getAchieveDistanceRunnable() != null
				&& plugin.getAchieveDistanceRunnable().getPlayerLocations().remove(event.getPlayer()) != null) {
			for (HashSet<?> playerHashSet : plugin.getAchieveDistanceRunnable().getPlayerAchievementsFoot())
				((HashSet<Player>) playerHashSet).remove(event.getPlayer());
			for (HashSet<?> playerHashSet : plugin.getAchieveDistanceRunnable().getPlayerAchievementsHorse())
				((HashSet<Player>) playerHashSet).remove(event.getPlayer());
			for (HashSet<?> playerHashSet : plugin.getAchieveDistanceRunnable().getPlayerAchievementsPig())
				((HashSet<Player>) playerHashSet).remove(event.getPlayer());
			for (HashSet<?> playerHashSet : plugin.getAchieveDistanceRunnable().getPlayerAchievementsBoat())
				((HashSet<Player>) playerHashSet).remove(event.getPlayer());
			for (HashSet<?> playerHashSet : plugin.getAchieveDistanceRunnable().getPlayerAchievementsMinecart())
				((HashSet<Player>) playerHashSet).remove(event.getPlayer());

			// Update database statistics for distances and clean HashMaps.

			Integer distance = plugin.getAchieveDistanceRunnable().getAchievementDistancesFoot()
					.remove(event.getPlayer());
			if (distance != null)
				plugin.getDb().updateAndGetDistance(event.getPlayer(), distance, "distancefoot");

			distance = plugin.getAchieveDistanceRunnable().getAchievementDistancesPig().remove(event.getPlayer());
			if (distance != null)
				plugin.getDb().updateAndGetDistance(event.getPlayer(), distance, "distancepig");

			distance = plugin.getAchieveDistanceRunnable().getAchievementDistancesHorse().remove(event.getPlayer());
			if (distance != null)
				plugin.getDb().updateAndGetDistance(event.getPlayer(), distance, "distancehorse");

			distance = plugin.getAchieveDistanceRunnable().getAchievementDistancesBoat().remove(event.getPlayer());
			if (distance != null)
				plugin.getDb().updateAndGetDistance(event.getPlayer(), distance, "distanceboat");

			distance = plugin.getAchieveDistanceRunnable().getAchievementDistancesMinecart().remove(event.getPlayer());
			if (distance != null)
				plugin.getDb().updateAndGetDistance(event.getPlayer(), distance, "distanceminecart");
		}

		if (plugin.getAchievePlayTimeRunnable() != null) {

			// Update database statistics for played time and clean HashMaps.

			Long playTime = plugin.getConnectionListener().getPlayTime().remove(event.getPlayer());
			Long joinTime = plugin.getConnectionListener().getJoinTime().remove(event.getPlayer());

			if (playTime != null && joinTime != null)
				plugin.getDb().updateAndGetPlaytime(event.getPlayer(),
						playTime + System.currentTimeMillis() - joinTime);

			for (HashSet<?> playerHashSet : plugin.getAchievePlayTimeRunnable().getPlayerAchievements())
				((HashSet<Player>) playerHashSet).remove(event.getPlayer());
		}
	}
}
