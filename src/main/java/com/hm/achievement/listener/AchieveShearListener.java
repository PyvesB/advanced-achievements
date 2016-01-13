package com.hm.achievement.listener;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerShearEntityEvent;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.db.DatabasePools;

public class AchieveShearListener implements Listener {

	private AdvancedAchievements plugin;

	public AchieveShearListener(AdvancedAchievements plugin) {

		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerShearEntityEvent(PlayerShearEntityEvent event) {

		Player player = event.getPlayer();
		if (!player.hasPermission("achievement.count.shears")
				|| plugin.isRestrictCreative() && player.getGameMode() == GameMode.CREATIVE
				|| plugin.isInExludedWorld(player))
			return;

		int shears;
		if (!DatabasePools.getShearHashMap().containsKey(player.getUniqueId().toString()))
			shears = plugin.getDb().getNormalAchievementAmount(player, "shears") + 1;
		else
			shears = DatabasePools.getShearHashMap().get(player.getUniqueId().toString()) + 1;

		DatabasePools.getShearHashMap().put(player.getUniqueId().toString(), shears);

		String configAchievement = "Shear." + shears;
		if (plugin.getReward().checkAchievement(configAchievement)) {

			plugin.getAchievementDisplay().displayAchievement(player, configAchievement);
			plugin.getDb().registerAchievement(player, plugin.getConfig().getString(configAchievement + ".Name"),
					plugin.getConfig().getString(configAchievement + ".Message"));

			plugin.getReward().checkConfig(player, configAchievement);
		}
	}
}
