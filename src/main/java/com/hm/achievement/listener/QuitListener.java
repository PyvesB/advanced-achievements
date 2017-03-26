package com.hm.achievement.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import com.hm.achievement.AdvancedAchievements;

/**
 * Listener class to deal with Distance and PlayedTime achievements.
 * 
 * @author Pyves
 *
 */
public class QuitListener extends AbstractListener implements Listener {

	public QuitListener(AdvancedAchievements plugin) {
		super(plugin);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerQuit(PlayerQuitEvent event) {
		final String playerUUID = event.getPlayer().getUniqueId().toString();

		// Clean cooldown HashMap for book command.
		plugin.getAchievementBookCommand().getPlayersBookTime().remove(playerUUID);

		// Clear achievements caches.
		plugin.getPoolsManager().getReceivedAchievementsCache().removeAll(playerUUID);
		plugin.getPoolsManager().getNotReceivedAchievementsCache().removeAll(playerUUID);
		plugin.getPoolsManager().getTotalPlayerAchievementsCache().remove(playerUUID);

		if (plugin.getDistanceRunnable() != null) {
			plugin.getDistanceRunnable().getPlayerLocations().remove(playerUUID);
		}

		// Remove player from HashSet for Connection achievements.
		if (plugin.getConnectionListener() != null) {
			plugin.getConnectionListener().getPlayersAchieveConnectionRan().remove(playerUUID);
		}

		// Remove player from cooldown structures.
		if (plugin.getBedListener() != null) {
			plugin.getBedListener().removePlayerFromCooldownMap(playerUUID);
		}
		if (plugin.getTradeAnvilBrewSmeltListener() != null) {
			plugin.getTradeAnvilBrewSmeltListener().removePlayerFromCooldownMap(playerUUID);
		}
		if (plugin.getMilkLavaWaterListener() != null) {
			plugin.getMilkLavaWaterListener().removePlayerFromCooldownMap(playerUUID);
		}
		if (plugin.getHoeFertiliseFireworkMusicListener() != null) {
			plugin.getHoeFertiliseFireworkMusicListener().removePlayerFromCooldownMap(playerUUID);
		}
	}
}
