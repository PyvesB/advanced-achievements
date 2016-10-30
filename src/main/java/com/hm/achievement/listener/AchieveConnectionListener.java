package com.hm.achievement.listener;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.runnable.AchieveConnectionRunnable;
import com.hm.achievement.utils.UpdateChecker;

/**
 * Listener class to deal with Connections achievements, as well as update checker and initialisation of played time
 * statistics.
 * 
 * @author Pyves
 *
 */
public class AchieveConnectionListener implements Listener {

	private AdvancedAchievements plugin;

	public AchieveConnectionListener(AdvancedAchievements plugin) {

		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent event) {

		// Check if OP to display new version message if needed.
		if (plugin.getUpdateChecker() != null && plugin.getUpdateChecker().isUpdateNeeded()
				&& event.getPlayer().hasPermission("achievement.update")) {
			event.getPlayer().sendMessage(plugin.getChatHeader() + "Update available: v"
					+ plugin.getUpdateChecker().getVersion() + ". Download at one of the following:");
			event.getPlayer().sendMessage(ChatColor.GRAY + UpdateChecker.BUKKIT_DONWLOAD_URL);
			event.getPlayer().sendMessage(ChatColor.GRAY + UpdateChecker.SPIGOT_DONWLOAD_URL);
		}

		// Schedule delayed task to check if player has a new Connections achievement.
		if (!plugin.getDisabledCategorySet().contains("Connections"))
			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(
					Bukkit.getPluginManager().getPlugin("AdvancedAchievements"),
					new AchieveConnectionRunnable(event.getPlayer(), plugin), 100);

	}
}
