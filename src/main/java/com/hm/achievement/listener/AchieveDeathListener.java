package com.hm.achievement.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import com.hm.achievement.AdvancedAchievements;

public class AchieveDeathListener implements Listener {

	private AdvancedAchievements plugin;

	public AchieveDeathListener(AdvancedAchievements plugin) {

		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerDeath(PlayerDeathEvent event) {

		Player player = event.getEntity();

		if (player == null)
			return;

		if (!player.hasPermission("achievement.count.deaths") || plugin.isInExludedWorld(player))
			return;

		int deaths = plugin.getDb().incrementAndGetNormalAchievement(player, "deaths");
		String configAchievement = "Deaths." + deaths;
		if (plugin.getReward().checkAchievement(configAchievement)) {

			plugin.getAchievementDisplay().displayAchievement(player, configAchievement);
			plugin.getDb().registerAchievement(player, plugin.getConfig().getString(configAchievement + ".Name"),
					plugin.getConfig().getString(configAchievement + ".Message"));
			plugin.getReward().checkConfig(player, configAchievement);
		}
	}
}
