package com.hm.achievement.listener;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;

import com.hm.achievement.AdvancedAchievements;

public class AchieveXPListener implements Listener {

	private AdvancedAchievements plugin;

	public AchieveXPListener(AdvancedAchievements plugin) {

		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerExpChangeEvent(PlayerExpChangeEvent event) {

		Player player = event.getPlayer();

		if (!player.hasPermission("achievement.count.maxlevel")
				|| plugin.isRestrictCreative() && player.getGameMode() == GameMode.CREATIVE
				|| plugin.isInExludedWorld(player))
			return;

		if ((1 - player.getExp()) * player.getExpToLevel() >= event.getAmount())
			return;

		int levels = plugin.getDb().incrementAndGetMaxLevel(player);
		String configAchievement = "MaxLevel." + levels;
		if (plugin.getReward().checkAchievement(configAchievement)) {

			if (plugin.getDb().hasPlayerAchievement(player, plugin.getConfig().getString(configAchievement + ".Name")))
				return;
			plugin.getAchievementDisplay().displayAchievement(player, configAchievement);
			plugin.getDb().registerAchievement(player, plugin.getConfig().getString(configAchievement + ".Name"),
					plugin.getConfig().getString(configAchievement + ".Message"));
			plugin.getReward().checkConfig(player, configAchievement);
		}
	}

}
