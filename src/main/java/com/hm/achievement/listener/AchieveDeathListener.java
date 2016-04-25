package com.hm.achievement.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.db.DatabasePools;

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

		int deaths;
		if (!DatabasePools.getDeathHashMap().containsKey(player.getUniqueId().toString()))
			deaths = plugin.getDb().getNormalAchievementAmount(player, "deaths") + 1;
		else
			deaths = DatabasePools.getDeathHashMap().get(player.getUniqueId().toString()) + 1;

		DatabasePools.getDeathHashMap().put(player.getUniqueId().toString(), deaths);
		
		String configAchievement = "Deaths." + deaths;
		if (plugin.getReward().checkAchievement(configAchievement)) {

			plugin.getAchievementDisplay().displayAchievement(player, configAchievement);
			plugin.getDb().registerAchievement(player, plugin.getPluginConfig().getString(configAchievement + ".Name"),
					plugin.getPluginConfig().getString(configAchievement + ".Message"));
			plugin.getReward().checkConfig(player, configAchievement);
		}
	}
}
