package com.hm.achievement.listener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.AdvancedAchievementsUpdateChecker;
import com.hm.achievement.runnable.AchieveConnectionRunnable;

public class AchieveConnectionListener implements Listener {

	private AdvancedAchievements plugin;

	private Map<String, Long> joinTime;
	private Map<String, Long> playTime;

	public AchieveConnectionListener(AdvancedAchievements plugin) {

		this.plugin = plugin;
		if (plugin.isAsyncPooledRequestsSender()) {
			joinTime = new ConcurrentHashMap<String, Long>();
			playTime = new ConcurrentHashMap<String, Long>();
		} else {
			joinTime = new HashMap<String, Long>();
			playTime = new HashMap<String, Long>();
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent event) {

		// Check if OP to display new version message if needed.
		if (plugin.getUpdateChecker() != null && plugin.getUpdateChecker().isUpdateNeeded()
				&& event.getPlayer().hasPermission("achievement.update")) {
			event.getPlayer().sendMessage(plugin.getChatHeader() + "Update available: v"
					+ plugin.getUpdateChecker().getVersion() + ". Download at one of the following:");
			event.getPlayer().sendMessage(ChatColor.GRAY + AdvancedAchievementsUpdateChecker.BUKKIT_DONWLOAD_URL);
			event.getPlayer().sendMessage(ChatColor.GRAY + AdvancedAchievementsUpdateChecker.SPIGOT_DONWLOAD_URL);
		}

		// Initialise play time data for the player.
		// If runnable is null, no played time achievements have been set, no
		// need to store data.
		// If player doesn't have permissions, no need to store data.
		if (plugin.getAchievePlayTimeRunnable() != null
				&& event.getPlayer().hasPermission("achievement.count.playedtime")) {
			plugin.getConnectionListener().getJoinTime().put(event.getPlayer().getUniqueId().toString(),
					System.currentTimeMillis());
			plugin.getConnectionListener().getPlayTime().put(event.getPlayer().getUniqueId().toString(),
					plugin.getDb().updateAndGetPlaytime(event.getPlayer().getUniqueId().toString(), 0L));
		}

		// Schedule delayed task to check if player has a new Connections
		// achievement.
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(
				Bukkit.getPluginManager().getPlugin("AdvancedAchievements"),
				new AchieveConnectionRunnable(event.getPlayer(), plugin), 100);

	}

	public Map<String, Long> getJoinTime() {

		return joinTime;
	}

	public Map<String, Long> getPlayTime() {

		return playTime;
	}

}
