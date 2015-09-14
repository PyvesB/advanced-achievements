package com.hm.achievement.listener;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemBreakEvent;

import com.hm.achievement.AdvancedAchievements;

public class AchieveItemBreakListener implements Listener {

	private AdvancedAchievements plugin;

	public AchieveItemBreakListener(AdvancedAchievements plugin) {

		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerItemBreakEvent(PlayerItemBreakEvent event) {

		Player player = (Player) event.getPlayer();
		if (!player.hasPermission("achievement.get") || plugin.isRestrictCreative()
				&& player.getGameMode() == GameMode.CREATIVE || plugin.isInExludedWorld(player))
			return;

		Integer itemBreaks = plugin.getDb().registerItemBreak(player);
		String configAchievement = "ItemBreaks." + itemBreaks;
		if (plugin.getReward().checkAchievement(configAchievement)) {

			plugin.getAchievementDisplay().displayAchievement(player, configAchievement);
			SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
			plugin.getDb().registerAchievement(player, plugin.getConfig().getString(configAchievement + ".Name"),
					plugin.getConfig().getString(configAchievement + ".Message"), format.format(new Date()));

			plugin.getReward().checkConfig(player, configAchievement);
		}
	}
}
