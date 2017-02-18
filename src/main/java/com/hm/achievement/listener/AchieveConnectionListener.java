package com.hm.achievement.listener;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
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

/**
 * Listener class to deal with Connections achievements, as well as update checker.
 * 
 * @author Pyves
 *
 */
public class AchieveConnectionListener extends AbstractListener implements Listener {

	// Contains UUIDs of players for which a AchieveConnectionRunnable ran successfully without returning.
	private final Set<String> playersAchieveConnectionRan;

	public AchieveConnectionListener(AdvancedAchievements plugin) {

		super(plugin);
		playersAchieveConnectionRan = new HashSet<>();
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent event) {

		scheduleTaskIfAllowed(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onWorldChanged(PlayerChangedWorldEvent event) {

		if (playersAchieveConnectionRan.contains(event.getPlayer().getUniqueId().toString())) {
			return;
		}

		scheduleTaskIfAllowed(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onGameModeChange(PlayerGameModeChangeEvent event) {

		if (playersAchieveConnectionRan.contains(event.getPlayer().getUniqueId().toString())) {
			return;
		}

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
		if (player.hasMetadata("NPC") || plugin.isRestrictCreative() && player.getGameMode() == GameMode.CREATIVE
				|| plugin.isRestrictSpectator() && player.getGameMode() == GameMode.SPECTATOR
				|| plugin.isInExludedWorld(player)) {
			return;
		}

		// Schedule delayed task to check if player should receive a Connections achievement.
		if (!plugin.getDisabledCategorySet().contains(NormalAchievements.CONNECTIONS.toString())) {
			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(
					Bukkit.getPluginManager().getPlugin(plugin.getDescription().getName()),
					new AchieveConnectionRunnable(player, plugin), 100);
		}
	}

	public Set<String> getPlayersAchieveConnectionRan() {

		return playersAchieveConnectionRan;
	}
}
