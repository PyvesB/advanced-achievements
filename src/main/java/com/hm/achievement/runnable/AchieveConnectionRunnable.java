package com.hm.achievement.runnable;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.entity.Player;
import com.hm.achievement.AdvancedAchievements;

public class AchieveConnectionRunnable implements Runnable {

	private Player player;
	private AdvancedAchievements plugin;

	public AchieveConnectionRunnable(Player player, AdvancedAchievements plugin) {

		this.player = player;
		this.plugin = plugin;
	}

	@Override
	public void run() {

		// Check if player is still online.
		if (!player.isOnline())
			return;
		if (!player.hasPermission("achievement.count.connections"))
			return;
		Date now = new Date();
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");

		if (!format.format(now).equals(plugin.getDb().getPlayerConnectionDate(player))) {

			int connections = plugin.getDb().updateAndGetConnection(player, format.format(now));
			String configAchievement = "Connections." + connections;
			if (plugin.getPluginConfig().getString(configAchievement + ".Message", null) != null) {

				plugin.getAchievementDisplay().displayAchievement(player, configAchievement);
				plugin.getDb().registerAchievement(player, plugin.getPluginConfig().getString(configAchievement + ".Name"),
						plugin.getPluginConfig().getString(configAchievement + ".Message"));
				plugin.getReward().checkConfig(player, configAchievement);
			}
		}

	}

}
