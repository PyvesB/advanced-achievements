package com.hm.achievement.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemBreakEvent;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.db.DatabasePools;

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

		int itemBreaks;
		if (!DatabasePools.getItemBreakHashMap().containsKey(player.getUniqueId().toString()))
			itemBreaks = plugin.getDb().getNormalAchievementAmount(player, "itembreaks") + 1;
		else
			itemBreaks = DatabasePools.getItemBreakHashMap().get(player.getUniqueId().toString()) + 1;

		DatabasePools.getItemBreakHashMap().put(player.getUniqueId().toString(), itemBreaks);
		
		String configAchievement = "ItemBreaks." + itemBreaks;
		if (plugin.getReward().checkAchievement(configAchievement)) {

			plugin.getAchievementDisplay().displayAchievement(player, configAchievement);
			plugin.getDb().registerAchievement(player, plugin.getPluginConfig().getString(configAchievement + ".Name"),
					plugin.getPluginConfig().getString(configAchievement + ".Message"));

			plugin.getReward().checkConfig(player, configAchievement);
		}
	}
}
