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

public class AchieveConnectionListener implements Listener {

	private AdvancedAchievements plugin;

	private static HashMap<Player, Long> joinTime;
	private static HashMap<Player, Long> playTime;

	public AchieveConnectionListener(AdvancedAchievements plugin) {

		this.plugin = plugin;
		joinTime = new HashMap<Player, Long>();
		playTime = new HashMap<Player, Long>();
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent event) {

		if (event.getPlayer().isOp() && plugin.isUpdateNeeded()) {
			event.getPlayer().sendMessage(
					(new StringBuilder())
							.append(ChatColor.GRAY + "[" + ChatColor.DARK_PURPLE + plugin.getIcon() + ChatColor.GRAY
									+ "] ")
							.append("New version available: v" + plugin.getUpdateChecker().getVersion()
									+ " Download at: ").toString());
			event.getPlayer()
					.sendMessage(
							(new StringBuilder()).append(ChatColor.WHITE).append(plugin.getUpdateChecker().getUrl())
									.toString());
		}

		Bukkit.getServer()
				.getScheduler()
				.scheduleSyncDelayedTask(Bukkit.getPluginManager().getPlugin("AdvancedAchievements"),
						new AchieveConnectionRunnable(event, plugin), 100);

	}

	public static HashMap<Player, Long> getJoinTime() {

		return joinTime;
	}

	public static void setJoinTime(HashMap<Player, Long> joinTime) {

		AchieveConnectionListener.joinTime = joinTime;
	}

	public static HashMap<Player, Long> getPlayTime() {

		return playTime;
	}

	public static void setPlayTime(HashMap<Player, Long> playTime) {

		AchieveConnectionListener.playTime = playTime;
	}
}
