package com.hm.achievement.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemBreakEvent;

import com.hm.achievement.AdvancedAchievements;

public class AchieveItemBreakListener implements Listener {

	private AdvancedAchievements plugin;

	public AchieveItemBreakListener(AdvancedAchievements plugin) {

		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerItemBreakEvent(PlayerItemBreakEvent event) {

		Player player = event.getPlayer();
		if (!player.hasPermission("achievement.count.itembreaks") || plugin.isInExludedWorld(player))
			return;

		int itemBreaks = plugin.getDb().incrementAndGetNormalAchievement(player, "itembreaks");
		String configAchievement = "ItemBreaks." + itemBreaks;
		if (plugin.getReward().checkAchievement(configAchievement)) {

			plugin.getAchievementDisplay().displayAchievement(player, configAchievement);
			plugin.getDb().registerAchievement(player, plugin.getConfig().getString(configAchievement + ".Name"),
					plugin.getConfig().getString(configAchievement + ".Message"));

			plugin.getReward().checkConfig(player, configAchievement);
		}
	}
}
