package com.hm.achievement.listener;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.hm.achievement.AdvancedAchievements;

public class AchieveTeleportListener implements Listener {

	private AdvancedAchievements plugin;

	public AchieveTeleportListener(AdvancedAchievements plugin) {

		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void playerTeleport(PlayerTeleportEvent event) {
		
		// Event fired twice when teleporting with a nether portal: first time
		// to go to nether with the cause NETHER_PORTAL, then later on to change
		// location in nether; we must only consider the second change because
		// the location of the player is not updated during the first event;
		// if the distances are monitored by the plugin between the two events,
		// it would lead to incorrect results.
		if (event.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL)
			return;

		// Update location of player if he teleports somewhere else.
		if (plugin.getAchieveDistanceRunnable() != null)
			plugin.getAchieveDistanceRunnable().getPlayerLocations().put(event.getPlayer(), event.getTo());

		if (event.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL)
			return;

		Player player = event.getPlayer();
		if (!player.hasPermission("achievement.count.enderpeals")
				|| plugin.isRestrictCreative() && player.getGameMode() == GameMode.CREATIVE
				|| plugin.isInExludedWorld(player)
				|| plugin.getConfig().getConfigurationSection("EnderPearls").getKeys(false).size() == 0)
			return;

		int enderpearls = plugin.getPoolsManager().getPlayerEnderPearlAmount(player) + 1;

		plugin.getPoolsManager().getEnderPearlHashMap().put(player.getUniqueId().toString(), enderpearls);

		String configAchievement = "EnderPearls." + enderpearls;
		if (plugin.getReward().checkAchievement(configAchievement)) {

			plugin.getAchievementDisplay().displayAchievement(player, configAchievement);
			plugin.getDb().registerAchievement(player, plugin.getPluginConfig().getString(configAchievement + ".Name"),
					plugin.getPluginConfig().getString(configAchievement + ".Message"));
			plugin.getReward().checkConfig(player, configAchievement);
		}

	}

}
