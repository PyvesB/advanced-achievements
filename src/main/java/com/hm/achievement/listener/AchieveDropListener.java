package com.hm.achievement.listener;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.NormalAchievements;

/**
 * Listener class to deal with ItemDrops achievements.
 * 
 * @author Pyves
 *
 */
public class AchieveDropListener implements Listener {

	private AdvancedAchievements plugin;

	public AchieveDropListener(AdvancedAchievements plugin) {

		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerDropItem(PlayerDropItemEvent event) {

		Player player = event.getPlayer();
		if (!player.hasPermission("achievement.count.itemdrops")
				|| plugin.isRestrictCreative() && player.getGameMode() == GameMode.CREATIVE
				|| plugin.isInExludedWorld(player))
			return;

		int drops = plugin.getPoolsManager().getPlayerDropAmount(player) + 1;

		plugin.getPoolsManager().getDropHashMap().put(player.getUniqueId().toString(), drops);

		String configAchievement = NormalAchievements.DROPS + "." + drops;
		if (plugin.getPluginConfig().getString(configAchievement + ".Message", null) != null) {

			plugin.getAchievementDisplay().displayAchievement(player, configAchievement);
			plugin.getDb().registerAchievement(player, plugin.getPluginConfig().getString(configAchievement + ".Name"),
					plugin.getPluginConfig().getString(configAchievement + ".Message"));

			plugin.getReward().checkConfig(player, configAchievement);
		}
	}
}
