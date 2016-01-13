package com.hm.achievement.listener;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.AdvancedAchievementsUpdateChecker;
import com.hm.achievement.runnable.AchieveConnectionRunnable;

public class AchieveConnectionListener implements Listener {

	private AdvancedAchievements plugin;

	private HashMap<Player, Long> joinTime;
	private HashMap<Player, Long> playTime;

	public AchieveConnectionListener(AdvancedAchievements plugin) {

		this.plugin = plugin;
		joinTime = new HashMap<Player, Long>();
		playTime = new HashMap<Player, Long>();
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent event) {

		// Check if OP to display new version message if needed.
		if (event.getPlayer().isOp() && plugin.getUpdateChecker().isUpdateNeeded()) {
			event.getPlayer().sendMessage(plugin.getChatHeader() + "Update available for Advanced Achievements: v"
					+ plugin.getUpdateChecker().getVersion());
			event.getPlayer().sendMessage(plugin.getChatHeader() + "Download at one of the following locations:");
			event.getPlayer().sendMessage(ChatColor.GRAY + AdvancedAchievementsUpdateChecker.BUKKIT_DONWLOAD_URL);
			event.getPlayer().sendMessage(ChatColor.GRAY + AdvancedAchievementsUpdateChecker.SPIGOT_DONWLOAD_URL);
		}

		// Initialise play time data for the player.
		// If runnable is null, no played time achievements have been set, no
		// need to store data.
		// If player doesn't have permissions, no need to store data.
		if (plugin.getAchievePlayTimeRunnable() != null
				&& event.getPlayer().hasPermission("achievement.count.playedtime")) {
			plugin.getConnectionListener().getJoinTime().put(event.getPlayer(), System.currentTimeMillis());
			plugin.getConnectionListener().getPlayTime().put(event.getPlayer(),
					plugin.getDb().updateAndGetPlaytime(event.getPlayer(), 0L));
		}

		// Initialise distances data for the player.
		// If runnable is null, no distance achievements have been set, no
		// need to store data.
		// If player doesn't have permissions, no need to store data.
		if (plugin.getAchieveDistanceRunnable() != null) {
			if (event.getPlayer().hasPermission("achievement.count.distanceboat"))
				plugin.getAchieveDistanceRunnable().getAchievementDistancesBoat().put(event.getPlayer(),
						plugin.getDb().updateAndGetDistance(event.getPlayer(), 0, "distanceboat"));
			if (event.getPlayer().hasPermission("achievement.count.distanceminecart"))
				plugin.getAchieveDistanceRunnable().getAchievementDistancesMinecart().put(event.getPlayer(),
						plugin.getDb().updateAndGetDistance(event.getPlayer(), 0, "distanceminecart"));
			if (event.getPlayer().hasPermission("achievement.count.distancepig"))
				plugin.getAchieveDistanceRunnable().getAchievementDistancesPig().put(event.getPlayer(),
						plugin.getDb().updateAndGetDistance(event.getPlayer(), 0, "distancepig"));
			if (event.getPlayer().hasPermission("achievement.count.distancehorse"))
				plugin.getAchieveDistanceRunnable().getAchievementDistancesHorse().put(event.getPlayer(),
						plugin.getDb().updateAndGetDistance(event.getPlayer(), 0, "distancehorse"));
			if (event.getPlayer().hasPermission("achievement.count.distancefoot"))
				plugin.getAchieveDistanceRunnable().getAchievementDistancesFoot().put(event.getPlayer(),
						plugin.getDb().updateAndGetDistance(event.getPlayer(), 0, "distancefoot"));

			plugin.getAchieveDistanceRunnable().getPlayerLocations().put(event.getPlayer(),
					event.getPlayer().getLocation());
		}

		// Schedule delayed task to check if player has a new Connections
		// achievement.
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(
				Bukkit.getPluginManager().getPlugin("AdvancedAchievements"),
				new AchieveConnectionRunnable(event, plugin), 100);

	}

	public HashMap<Player, Long> getJoinTime() {

		return joinTime;
	}

	public HashMap<Player, Long> getPlayTime() {

		return playTime;
	}

}
