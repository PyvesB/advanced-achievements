package com.hm.achievement.listener;

import java.text.SimpleDateFormat;
import java.util.Date;

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

		Player player = (Player) event.getPlayer();
		if (!player.hasPermission("achievement.get") || plugin.isRestrictCreative()
				&& player.getGameMode() == GameMode.CREATIVE || plugin.isInExludedWorld(player))
			return;

		Integer shears = 0;
		if (!DatabasePools.getShearHashMap().containsKey(player.getUniqueId().toString()))
			shears = plugin.getDb().getShear(player) + 1;
		else
			shears = DatabasePools.getShearHashMap().get(player.getUniqueId().toString()) + 1;

		DatabasePools.getShearHashMap().put(player.getUniqueId().toString(), shears);

		String configAchievement = "Shear." + shears;
		if (plugin.getReward().checkAchievement(configAchievement)) {

			plugin.getAchievementDisplay().displayAchievement(player, configAchievement);
			SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
			plugin.getDb().registerAchievement(player, plugin.getConfig().getString(configAchievement + ".Name"),
					plugin.getConfig().getString(configAchievement + ".Message"), format.format(new Date()));

			plugin.getReward().checkConfig(player, configAchievement);
		}
	}
}
