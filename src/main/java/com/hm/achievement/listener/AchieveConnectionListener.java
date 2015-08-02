package com.hm.achievement.listener;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.hm.achievement.AdvancedAchievements;

public class AchieveConnectionListener implements Listener {
	private AdvancedAchievements plugin;

	public AchieveConnectionListener(AdvancedAchievements plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent event) {

		if (event.getPlayer().isOp() && plugin.isUpdateNeeded()) {
			event.getPlayer().sendMessage(
					(new StringBuilder())
							.append(ChatColor.GRAY + "["
									+ ChatColor.DARK_PURPLE + plugin.getIcon()
									+ ChatColor.GRAY + "] ")
							.append("New version available: v"
									+ plugin.getUpdateChecker().getVersion()
									+ " Download at: ").toString());
			event.getPlayer().sendMessage(
					(new StringBuilder()).append(ChatColor.WHITE)
							.append(plugin.getUpdateChecker().getUrl())
							.toString());
		}

		Bukkit.getServer()
				.getScheduler()
				.scheduleSyncDelayedTask(
						Bukkit.getPluginManager().getPlugin(
								"AdvancedAchievements"),
						new AchieveConnectionRunnable(event, plugin), 100);

	}
}