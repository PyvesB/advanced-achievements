package com.hm.achievement.listener;

import org.bukkit.GameMode;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.NormalAchievements;

/**
 * Listener class to deal with Snowballs and Eggs achievements.
 * 
 * @author Pyves
 *
 */
public class AchieveSnowballEggListener implements Listener {

	private AdvancedAchievements plugin;

	public AchieveSnowballEggListener(AdvancedAchievements plugin) {

		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onProjectileLaunch(ProjectileLaunchEvent event) {

		if (!(event.getEntity() instanceof Snowball) && !(event.getEntity() instanceof Egg)
				|| !(event.getEntity().getShooter() instanceof Player))
			return;
		Player player = (Player) event.getEntity().getShooter();
		if (plugin.isRestrictCreative() && player.getGameMode() == GameMode.CREATIVE || plugin.isInExludedWorld(player))
			return;

		String configAchievement;
		if (player.hasPermission("achievement.count.snowballs")
				&& !plugin.getDisabledCategorySet().contains(NormalAchievements.SNOWBALLS.toString())
				&& event.getEntity() instanceof Snowball) {
			int snowballs = plugin.getPoolsManager().getPlayerSnowballAmount(player) + 1;

			plugin.getPoolsManager().getSnowballHashMap().put(player.getUniqueId().toString(), snowballs);

			configAchievement = NormalAchievements.SNOWBALLS + "." + snowballs;
		} else if (player.hasPermission("achievement.count.eggs")
				&& !plugin.getDisabledCategorySet().contains(NormalAchievements.EGGS.toString())) {
			int eggs = plugin.getPoolsManager().getPlayerEggAmount(player) + 1;

			plugin.getPoolsManager().getEggHashMap().put(player.getUniqueId().toString(), eggs);

			configAchievement = NormalAchievements.EGGS + "." + eggs;
		} else
			return;

		if (plugin.getPluginConfig().getString(configAchievement + ".Message", null) != null) {

			plugin.getAchievementDisplay().displayAchievement(player, configAchievement);
			plugin.getDb().registerAchievement(player, plugin.getPluginConfig().getString(configAchievement + ".Name"),
					plugin.getPluginConfig().getString(configAchievement + ".Message"));

			plugin.getReward().checkConfig(player, configAchievement);
		}
	}
}
