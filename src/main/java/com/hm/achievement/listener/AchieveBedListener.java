package com.hm.achievement.listener;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.NormalAchievements;

/**
 * Listener class to deal with Beds achievements.
 * 
 * @author Pyves
 *
 */
public class AchieveBedListener implements Listener {

	private AdvancedAchievements plugin;

	public AchieveBedListener(AdvancedAchievements plugin) {

		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerBedEnter(PlayerBedEnterEvent event) {

		Player player = event.getPlayer();

		if (!player.hasPermission("achievement.count.beds")
				|| plugin.isRestrictCreative() && player.getGameMode() == GameMode.CREATIVE
				|| plugin.isInExludedWorld(player))
			return;

		int beds = plugin.getPoolsManager().getPlayerBedAmount(player) + 1;

		plugin.getPoolsManager().getBedHashMap().put(player.getUniqueId().toString(), beds);

		String configAchievement = NormalAchievements.BEDS + "." + beds;
		if (plugin.getPluginConfig().getString(configAchievement + ".Message", null) != null) {

			plugin.getAchievementDisplay().displayAchievement(player, configAchievement);
			plugin.getDb().registerAchievement(player, plugin.getPluginConfig().getString(configAchievement + ".Name"),
					plugin.getPluginConfig().getString(configAchievement + ".Message"));
			plugin.getReward().checkConfig(player, configAchievement);
		}
	}

}
