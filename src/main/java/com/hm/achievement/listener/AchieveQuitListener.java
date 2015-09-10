package com.hm.achievement.listener;

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

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onQuitEvent(PlayerQuitEvent event) {

		if (AchieveConnectionListener.getJoinTime().containsKey(event.getPlayer()))
			plugin.getDb().registerPlaytime(
					event.getPlayer(),
					AchieveConnectionListener.getPlayTime().get(event.getPlayer()) + System.currentTimeMillis()
							- AchieveConnectionListener.getJoinTime().get(event.getPlayer()));

		AchieveConnectionListener.getPlayTime().remove(event.getPlayer());
		AchieveConnectionListener.getJoinTime().remove(event.getPlayer());

		if (AchieveDistanceRunnable.getAchievementDistancesFoot().containsKey(event.getPlayer())) {
			plugin.getDb().registerDistanceFoot(event.getPlayer(),
					AchieveDistanceRunnable.getAchievementDistancesFoot().get(event.getPlayer()));

			AchieveDistanceRunnable.getAchievementDistancesFoot().remove(event.getPlayer());

			plugin.getDb().registerDistancePig(event.getPlayer(),
					AchieveDistanceRunnable.getAchievementDistancesPig().get(event.getPlayer()));

			AchieveDistanceRunnable.getAchievementDistancesPig().remove(event.getPlayer());

			plugin.getDb().registerDistanceHorse(event.getPlayer(),
					AchieveDistanceRunnable.getAchievementDistancesHorse().get(event.getPlayer()));

			AchieveDistanceRunnable.getAchievementDistancesHorse().remove(event.getPlayer());

			plugin.getDb().registerDistanceBoat(event.getPlayer(),
					AchieveDistanceRunnable.getAchievementDistancesBoat().get(event.getPlayer()));

			AchieveDistanceRunnable.getAchievementDistancesBoat().remove(event.getPlayer());

			plugin.getDb().registerDistanceMinecart(event.getPlayer(),
					AchieveDistanceRunnable.getAchievementDistancesMinecart().get(event.getPlayer()));

			AchieveDistanceRunnable.getAchievementDistancesMinecart().remove(event.getPlayer());
			AchieveDistanceRunnable.getAchievementLocations().remove(event.getPlayer());
		}

	}
}
