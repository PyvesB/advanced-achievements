package com.hm.achievement.listener;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.hm.achievement.event.PlayerAchievementEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import com.hm.achievement.AdvancedAchievements;

public class AchieveDeathListener implements Listener {

	private AdvancedAchievements plugin;

	public AchieveDeathListener(AdvancedAchievements plugin) {

		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerDeath(PlayerDeathEvent event) {

		if (!(event.getEntity() instanceof Player))
			return;
		Player player = (Player) event.getEntity();
		if (!player.hasPermission("achievement.get") || plugin.isInExludedWorld(player))
			return;

		Integer deaths = plugin.getDb().incrementAndGetNormalAchievement(player, "deaths");
		String configAchievement = "Deaths." + deaths;
		if (plugin.getReward().checkAchievement(configAchievement)) {

			plugin.getAchievementDisplay().displayAchievement(player, configAchievement);
			SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
			plugin.getDb().registerAchievement(player, plugin.getConfig().getString(configAchievement + ".Name"),
					plugin.getConfig().getString(configAchievement + ".Message"), format.format(new Date()));

			plugin.getServer().getPluginManager().callEvent(new PlayerAchievementEvent(player, configAchievement));

			plugin.getReward().checkConfig(player, configAchievement);
		}
	}
}
