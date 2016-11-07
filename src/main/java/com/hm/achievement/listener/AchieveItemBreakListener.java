package com.hm.achievement.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemBreakEvent;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.NormalAchievements;

/**
 * Listener class to deal with ItemBreaks achievements.
 * 
 * @author Pyves
 *
 */
public class AchieveItemBreakListener implements Listener {

	private AdvancedAchievements plugin;

	public AchieveItemBreakListener(AdvancedAchievements plugin) {

		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerItemBreak(PlayerItemBreakEvent event) {

		Player player = event.getPlayer();
		if (!player.hasPermission("achievement.count.itembreaks") || plugin.isInExludedWorld(player))
			return;

		int itemBreaks = plugin.getPoolsManager().getPlayerItemBreakAmount(player) + 1;

		plugin.getPoolsManager().getItemBreakHashMap().put(player.getUniqueId().toString(), itemBreaks);

		String configAchievement = NormalAchievements.ITEMBREAKS + "." + itemBreaks;
		if (plugin.getPluginConfig().getString(configAchievement + ".Message", null) != null) {

			plugin.getAchievementDisplay().displayAchievement(player, configAchievement);
			plugin.getDb().registerAchievement(player, plugin.getPluginConfig().getString(configAchievement + ".Name"),
					plugin.getPluginConfig().getString(configAchievement + ".Message"));

			plugin.getReward().checkConfig(player, configAchievement);
		}
	}
}
