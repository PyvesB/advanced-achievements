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

		// Update location of player if he teleports somewhere else.
		if (plugin.getAchieveDistanceRunnable() != null)
			plugin.getAchieveDistanceRunnable().getPlayerLocations().put(event.getPlayer(), event.getTo());

		if (event.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL)
			return;

		Player player = (Player) event.getPlayer();
		if (!player.hasPermission("achievement.count.enderpeals")
				|| plugin.isRestrictCreative() && player.getGameMode() == GameMode.CREATIVE
				|| plugin.isInExludedWorld(player))
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
