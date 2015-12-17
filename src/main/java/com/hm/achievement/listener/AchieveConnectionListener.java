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
		if (event.getPlayer().isOp() && plugin.isUpdateNeeded()) {
			event.getPlayer().sendMessage(
					(new StringBuilder())
							.append(plugin.getChatHeader())
							.append("Advanced Achievements, new version available: v"
									+ plugin.getUpdateChecker().getVersion() + " Download at: ").toString());
			event.getPlayer()
					.sendMessage(
							(new StringBuilder()).append(ChatColor.WHITE).append(plugin.getUpdateChecker().getUrl())
									.toString());
		}

		// Initialise play time data for the player.
		if (plugin.getAchievePlayTimeRunnable() != null) {
			plugin.getConnectionListener().getJoinTime().put(event.getPlayer(), System.currentTimeMillis());
			plugin.getConnectionListener().getPlayTime()
					.put(event.getPlayer(), plugin.getDb().updateAndGetPlaytime(event.getPlayer(), 0L));
		}

		// Initialise distances data for the player.
		if (plugin.getAchieveDistanceRunnable() != null) {
			plugin.getAchieveDistanceRunnable().getAchievementDistancesBoat()
					.put(event.getPlayer(), plugin.getDb().updateAndGetDistance(event.getPlayer(), 0, "distanceboat"));
			plugin.getAchieveDistanceRunnable()
					.getAchievementDistancesMinecart()
					.put(event.getPlayer(),
							plugin.getDb().updateAndGetDistance(event.getPlayer(), 0, "distanceminecart"));
			plugin.getAchieveDistanceRunnable().getAchievementDistancesPig()
					.put(event.getPlayer(), plugin.getDb().updateAndGetDistance(event.getPlayer(), 0, "distancepig"));
			plugin.getAchieveDistanceRunnable().getAchievementDistancesHorse()
					.put(event.getPlayer(), plugin.getDb().updateAndGetDistance(event.getPlayer(), 0, "distancehorse"));
			plugin.getAchieveDistanceRunnable().getAchievementDistancesFoot()
					.put(event.getPlayer(), plugin.getDb().updateAndGetDistance(event.getPlayer(), 0, "distancefoot"));

			plugin.getAchieveDistanceRunnable().getPlayerLocations()
					.put(event.getPlayer(), event.getPlayer().getLocation());
		}

		// Schedule delayed task to check if player has a new Connections
		// achievement.
		Bukkit.getServer()
				.getScheduler()
				.scheduleSyncDelayedTask(Bukkit.getPluginManager().getPlugin("AdvancedAchievements"),
						new AchieveConnectionRunnable(event, plugin), 100);

	}

	public HashMap<Player, Long> getJoinTime() {

		return joinTime;
	}

	public HashMap<Player, Long> getPlayTime() {

		return playTime;
	}

}
