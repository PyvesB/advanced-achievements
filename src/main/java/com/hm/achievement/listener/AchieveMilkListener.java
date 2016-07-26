package com.hm.achievement.listener;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketFillEvent;

import com.hm.achievement.AdvancedAchievements;

/**
 * Listener class to deal with Milk achievements.
 * 
 * @author Pyves
 *
 */
public class AchieveMilkListener implements Listener {

	private AdvancedAchievements plugin;

	public AchieveMilkListener(AdvancedAchievements plugin) {

		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerBucketFill(PlayerBucketFillEvent event) {

		if (!(event.getItemStack().getType() == Material.MILK_BUCKET))
			return;
		Player player = event.getPlayer();
		if (!player.hasPermission("achievement.count.milk") || plugin.isInExludedWorld(player))
			return;

		int milks = plugin.getPoolsManager().getPlayerMilkAmount(player) + 1;

		plugin.getPoolsManager().getMilkHashMap().put(player.getUniqueId().toString(), milks);

		String configAchievement = "Milk." + milks;
		if (plugin.getPluginConfig().getString(configAchievement + ".Message", null) != null) {

			plugin.getAchievementDisplay().displayAchievement(player, configAchievement);
			plugin.getDb().registerAchievement(player, plugin.getPluginConfig().getString(configAchievement + ".Name"),
					plugin.getPluginConfig().getString(configAchievement + ".Message"));

			plugin.getReward().checkConfig(player, configAchievement);
		}
	}
}
