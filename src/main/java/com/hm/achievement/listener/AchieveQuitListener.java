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
	public void onQuitEvent(PlayerQuitEvent event) {

		plugin.getAchievementBookCommand().getPlayers().remove(event.getPlayer());
		plugin.getAchievementListCommand().getPlayers().remove(event.getPlayer());

		if (plugin.getAchieveDistanceRunnable() != null
				&& plugin.getAchieveDistanceRunnable().getAchievementDistancesFoot().containsKey(event.getPlayer())) {
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

			if (plugin.getAchieveDistanceRunnable().getAchievementDistancesFoot().containsKey(event.getPlayer())) {
				plugin.getDb().updateAndGetDistance(event.getPlayer(),
						plugin.getAchieveDistanceRunnable().getAchievementDistancesFoot().get(event.getPlayer()),
						"distancefoot");

				plugin.getAchieveDistanceRunnable().getAchievementDistancesFoot().remove(event.getPlayer());

				plugin.getDb().updateAndGetDistance(event.getPlayer(),
						plugin.getAchieveDistanceRunnable().getAchievementDistancesPig().get(event.getPlayer()),
						"distancepig");

				plugin.getAchieveDistanceRunnable().getAchievementDistancesPig().remove(event.getPlayer());

				plugin.getDb().updateAndGetDistance(event.getPlayer(),
						plugin.getAchieveDistanceRunnable().getAchievementDistancesHorse().get(event.getPlayer()),
						"distancehorse");

				plugin.getAchieveDistanceRunnable().getAchievementDistancesHorse().remove(event.getPlayer());

				plugin.getDb().updateAndGetDistance(event.getPlayer(),
						plugin.getAchieveDistanceRunnable().getAchievementDistancesBoat().get(event.getPlayer()),
						"distanceboat");

				plugin.getAchieveDistanceRunnable().getAchievementDistancesBoat().remove(event.getPlayer());

				plugin.getDb().updateAndGetDistance(event.getPlayer(),
						plugin.getAchieveDistanceRunnable().getAchievementDistancesMinecart().get(event.getPlayer()),
						"distanceminecart");

				plugin.getAchieveDistanceRunnable().getAchievementDistancesMinecart().remove(event.getPlayer());

				plugin.getAchieveDistanceRunnable().getPlayerLocations().remove(event.getPlayer());
			}
		}
		if (plugin.getAchievePlayTimeRunnable() != null
				&& plugin.getConnectionListener().getJoinTime().containsKey(event.getPlayer())) {
			plugin.getDb().updateAndGetPlaytime(
					event.getPlayer(),
					plugin.getConnectionListener().getPlayTime().get(event.getPlayer()) + System.currentTimeMillis()
							- plugin.getConnectionListener().getJoinTime().get(event.getPlayer()));

			plugin.getConnectionListener().getPlayTime().remove(event.getPlayer());
			plugin.getConnectionListener().getJoinTime().remove(event.getPlayer());
			for (HashSet<?> playerHashSet : plugin.getAchievePlayTimeRunnable().getPlayerAchievements())
				((HashSet<Player>) playerHashSet).remove(event.getPlayer());
		}
	}
}
