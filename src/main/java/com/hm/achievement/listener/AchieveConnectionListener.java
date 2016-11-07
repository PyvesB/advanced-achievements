package com.hm.achievement.listener;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.NormalAchievements;
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

	// Contains UUIDs of players for which a AchieveConnectionRunnable ran successfully without returning.
	private Set<String> playersAchieveConnectionRan;

	public AchieveConnectionListener(AdvancedAchievements plugin) {

		this.plugin = plugin;
		playersAchieveConnectionRan = new HashSet<>();
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent event) {

		Player player = event.getPlayer();

		// Check if OP to display new version message if needed.
		if (plugin.getUpdateChecker() != null && plugin.getUpdateChecker().isUpdateNeeded()
				&& player.hasPermission("achievement.update")) {
			player.sendMessage(plugin.getChatHeader() + "Update available: v" + plugin.getUpdateChecker().getVersion()
					+ ". Download at one of the following:");
			player.sendMessage(ChatColor.GRAY + UpdateChecker.BUKKIT_DONWLOAD_URL);
			player.sendMessage(ChatColor.GRAY + UpdateChecker.SPIGOT_DONWLOAD_URL);
		}

		scheduleTaskIfAllowed(player);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onWorldChanged(PlayerChangedWorldEvent event) {

		if (playersAchieveConnectionRan.contains(event.getPlayer().getUniqueId().toString()))
			return;

		scheduleTaskIfAllowed(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onGameModeChange(PlayerGameModeChangeEvent event) {

		if (playersAchieveConnectionRan.contains(event.getPlayer().getUniqueId().toString()))
			return;

		scheduleTaskIfAllowed(event.getPlayer());
	}

	/**
	 * Schedules a delayed task to deal with Connection achievements if player is not in restricted creative or blocked
	 * world.
	 * 
	 * @param player
	 */
	private void scheduleTaskIfAllowed(Player player) {

		// Do not schedule task as player is in restricted creative mode or is in a blocked world.
		if (plugin.isRestrictCreative() && player.getGameMode() == GameMode.CREATIVE || plugin.isInExludedWorld(player))
			return;

		// Schedule delayed task to check if player should receive a Connections achievement.
		if (!plugin.getDisabledCategorySet().contains(NormalAchievements.CONNECTIONS.toString()))
			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(
					Bukkit.getPluginManager().getPlugin("AdvancedAchievements"),
					new AchieveConnectionRunnable(player, plugin), 100);
	}

	public Set<String> getPlayersAchieveConnectionRan() {

		return playersAchieveConnectionRan;
	}
}
