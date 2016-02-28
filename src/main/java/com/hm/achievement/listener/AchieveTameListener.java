package com.hm.achievement.listener;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTameEvent;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.db.DatabasePools;

public class AchieveTameListener implements Listener {

	private AdvancedAchievements plugin;

	public AchieveTameListener(AdvancedAchievements plugin) {

		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerTame(EntityTameEvent event) {

		if (!(event.getOwner() instanceof Player))
			return;
		Player player = (Player) event.getOwner();
		if (!player.hasPermission("achievement.count.taming")
				|| plugin.isRestrictCreative() && player.getGameMode() == GameMode.CREATIVE
				|| plugin.isInExludedWorld(player))
			return;

		int tames;
		if (!DatabasePools.getTameHashMap().containsKey(player.getUniqueId().toString()))
			tames = plugin.getDb().getNormalAchievementAmount(player, "tames") + 1;
		else
			tames = DatabasePools.getTameHashMap().get(player.getUniqueId().toString()) + 1;

		DatabasePools.getTameHashMap().put(player.getUniqueId().toString(), tames);

		String configAchievement = "Taming." + tames;
		if (plugin.getReward().checkAchievement(configAchievement)) {

			plugin.getAchievementDisplay().displayAchievement(player, configAchievement);
			plugin.getDb().registerAchievement(player, plugin.getConfig().getString(configAchievement + ".Name"),
					plugin.getConfig().getString(configAchievement + ".Message"));

			plugin.getReward().checkConfig(player, configAchievement);
		}
	}
}
