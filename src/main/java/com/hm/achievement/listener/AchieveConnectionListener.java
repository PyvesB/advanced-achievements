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
			plugin.getConnectionListener().getJoinTime().put(event.getPlayer(), System.currentTimeMillis());
			plugin.getConnectionListener().getPlayTime().put(event.getPlayer(),
					plugin.getDb().updateAndGetPlaytime(event.getPlayer(), 0L));
		}

		// Schedule delayed task to check if player has a new Connections
		// achievement.
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(
				Bukkit.getPluginManager().getPlugin("AdvancedAchievements"),
				new AchieveConnectionRunnable(event.getPlayer(), plugin), 100);

	}

	public HashMap<Player, Long> getJoinTime() {

		return joinTime;
	}

	public HashMap<Player, Long> getPlayTime() {

		return playTime;
	}

}
