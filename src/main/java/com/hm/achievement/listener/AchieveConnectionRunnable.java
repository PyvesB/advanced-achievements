package com.hm.achievement.listener;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;

import com.hm.achievement.AdvancedAchievements;

public class AchieveConnectionRunnable implements Runnable {

	private PlayerJoinEvent event;
	private AdvancedAchievements plugin;

	public AchieveConnectionRunnable(PlayerJoinEvent event,
			AdvancedAchievements plugin) {
		this.event = event;
		this.plugin = plugin;
	}

	public void run() {

		Player player = (Player) event.getPlayer();
		if (!player.hasPermission("achievement.get"))
			return;
		Date now = new Date();
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");

		if (!format.format(now)
				.equals(plugin.getDb().getConnectionDate(player))) {
			
			Integer connections = plugin.getDb().registerConnection(player, format.format(now));
			String configAchievement = "Connections." + connections;
			if (plugin.getReward().checkAchievement(
					configAchievement)) {
				String name = plugin.getConfig().getString(
						configAchievement + ".Name");
				String msg = plugin.getConfig().getString(
						configAchievement + ".Message");
				plugin.getAchievementDisplay().displayAchievement(player, name,
						msg);
				plugin.getDb().registerAchievement(
						player,
						plugin.getConfig().getString(
								configAchievement + ".Name"),
						plugin.getConfig().getString(
								configAchievement + ".Message"),
						"&0" + format.format(now));
				plugin.getReward().checkConfig(player, configAchievement);
			}
		}

	}

}
