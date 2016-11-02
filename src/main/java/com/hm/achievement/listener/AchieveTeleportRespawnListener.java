package com.hm.achievement.listener;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.hm.achievement.AdvancedAchievements;

/**
 * Listener class to deal with EnderPearls achievements and update Distances.
 * 
 * @author Pyves
 *
 */
public class AchieveTeleportRespawnListener implements Listener {

	private AdvancedAchievements plugin;

	public AchieveTeleportRespawnListener(AdvancedAchievements plugin) {

		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerRespawn(PlayerRespawnEvent event) {

		// Update location of player if he respawns after dying.
		if (plugin.getAchieveDistanceRunnable() != null)
			plugin.getAchieveDistanceRunnable().getPlayerLocations().put(event.getPlayer().getUniqueId().toString(),
					event.getRespawnLocation());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerTeleport(PlayerTeleportEvent event) {

		// Event fired twice when teleporting with a nether portal: first time to go to nether with the cause
		// NETHER_PORTAL, then later on to change location in nether; we must only consider the second change because
		// the location of the player is not updated during the first event; if the distances are monitored by the
		// plugin between the two events, it would lead to incorrect results.
		if (event.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL)
			return;

		// Update location of player if he teleports somewhere else.
		if (plugin.getAchieveDistanceRunnable() != null)
			plugin.getAchieveDistanceRunnable().getPlayerLocations().put(event.getPlayer().getUniqueId().toString(),
					event.getTo());

		if (event.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL
				|| plugin.getDisabledCategorySet().contains("EnderPearls"))
			return;

		Player player = event.getPlayer();
		if (!player.hasPermission("achievement.count.enderpearls")
				|| plugin.isRestrictCreative() && player.getGameMode() == GameMode.CREATIVE
				|| plugin.isInExludedWorld(player))
			return;

		int enderpearls = plugin.getPoolsManager().getPlayerEnderPearlAmount(player) + 1;

		plugin.getPoolsManager().getEnderPearlHashMap().put(player.getUniqueId().toString(), enderpearls);

		String configAchievement = "EnderPearls." + enderpearls;
		if (plugin.getPluginConfig().getString(configAchievement + ".Message", null) != null) {

			plugin.getAchievementDisplay().displayAchievement(player, configAchievement);
			plugin.getDb().registerAchievement(player, plugin.getPluginConfig().getString(configAchievement + ".Name"),
					plugin.getPluginConfig().getString(configAchievement + ".Message"));
			plugin.getReward().checkConfig(player, configAchievement);
		}

	}

}
