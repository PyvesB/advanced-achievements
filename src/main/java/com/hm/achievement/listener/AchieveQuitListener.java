package com.hm.achievement.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.runnable.AchieveDistanceRunnable;

public class AchieveQuitListener implements Listener {

	private AdvancedAchievements plugin;

	public AchieveQuitListener(AdvancedAchievements plugin) {

		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onQuitEvent(PlayerQuitEvent event) {
		
		if (plugin.getAchievementBookCommand().getPlayers().containsKey(event.getPlayer()))
			plugin.getAchievementBookCommand().getPlayers().remove(event.getPlayer());

		if (AchieveConnectionListener.getJoinTime().containsKey(event.getPlayer())) {
			plugin.getDb().updateAndGetPlaytime(
					event.getPlayer(),
					AchieveConnectionListener.getPlayTime().get(event.getPlayer()) + System.currentTimeMillis()
							- AchieveConnectionListener.getJoinTime().get(event.getPlayer()));

			AchieveConnectionListener.getPlayTime().remove(event.getPlayer());
			AchieveConnectionListener.getJoinTime().remove(event.getPlayer());
		}

		if (AchieveDistanceRunnable.getAchievementDistancesFoot().containsKey(event.getPlayer())) {
			plugin.getDb().updateAndGetDistance(event.getPlayer(),
					AchieveDistanceRunnable.getAchievementDistancesFoot().get(event.getPlayer()), "distancefoot");

			AchieveDistanceRunnable.getAchievementDistancesFoot().remove(event.getPlayer());

			plugin.getDb().updateAndGetDistance(event.getPlayer(),
					AchieveDistanceRunnable.getAchievementDistancesPig().get(event.getPlayer()), "distancepig");

			AchieveDistanceRunnable.getAchievementDistancesPig().remove(event.getPlayer());

			plugin.getDb().updateAndGetDistance(event.getPlayer(),
					AchieveDistanceRunnable.getAchievementDistancesHorse().get(event.getPlayer()), "distancehorse");

			AchieveDistanceRunnable.getAchievementDistancesHorse().remove(event.getPlayer());

			plugin.getDb().updateAndGetDistance(event.getPlayer(),
					AchieveDistanceRunnable.getAchievementDistancesBoat().get(event.getPlayer()), "distanceboat");

			AchieveDistanceRunnable.getAchievementDistancesBoat().remove(event.getPlayer());

			plugin.getDb().updateAndGetDistance(event.getPlayer(),
					AchieveDistanceRunnable.getAchievementDistancesMinecart().get(event.getPlayer()),
					"distanceminecart");

			AchieveDistanceRunnable.getAchievementDistancesMinecart().remove(event.getPlayer());
			
			AchieveDistanceRunnable.getAchievementLocations().remove(event.getPlayer());
		}

	}
}
