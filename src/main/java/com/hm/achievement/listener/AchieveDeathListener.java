package com.hm.achievement.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.NormalAchievements;

/**
 * Listener class to deal with Deaths achievements.
 * 
 * @author Pyves
 *
 */
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

		int deaths = plugin.getPoolsManager().getPlayerDeathAmount(player) + 1;

		plugin.getPoolsManager().getDeathHashMap().put(player.getUniqueId().toString(), deaths);

		String configAchievement = NormalAchievements.DEATHS + "." + deaths;
		if (plugin.getPluginConfig().getString(configAchievement + ".Message", null) != null) {

			plugin.getAchievementDisplay().displayAchievement(player, configAchievement);
			plugin.getDb().registerAchievement(player, plugin.getPluginConfig().getString(configAchievement + ".Name"),
					plugin.getPluginConfig().getString(configAchievement + ".Message"));
			plugin.getReward().checkConfig(player, configAchievement);
		}
	}
}
