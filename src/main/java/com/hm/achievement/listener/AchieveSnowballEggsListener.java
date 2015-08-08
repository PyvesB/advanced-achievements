package com.hm.achievement.listener;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.GameMode;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.db.DatabasePools;

public class AchieveSnowballEggsListener implements Listener {
	private AdvancedAchievements plugin;

	public AchieveSnowballEggsListener(AdvancedAchievements plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onProjectileLaunchEvent(ProjectileLaunchEvent event) {

		if (!(event.getEntity() instanceof Snowball)
				&& !(event.getEntity() instanceof Egg)
				|| !(event.getEntity().getShooter() instanceof Player))
			return;
		Player player = (Player) event.getEntity().getShooter();
		if (!player.hasPermission("achievement.get")
				|| plugin.isRestrictCreative()
				&& player.getGameMode() == GameMode.CREATIVE
				|| plugin.isInExludedWorld(player))
			return;

		String configAchievement = "";
		if (event.getEntity() instanceof Snowball) {
			Integer snowballs = 0;
			if (!DatabasePools.getSnowballHashMap().containsKey(
					player.getUniqueId().toString()))
				snowballs = plugin.getDb().getSnowballs(player) + 1;
			else
				snowballs = DatabasePools.getSnowballHashMap().get(
						player.getUniqueId().toString()) + 1;

			DatabasePools.getSnowballHashMap().put(
					player.getUniqueId().toString(), snowballs);

			configAchievement = "Snowballs." + snowballs;
		} else {
			Integer eggs = 0;
			if (!DatabasePools.getEggHashMap().containsKey(
					player.getUniqueId().toString()))
				eggs = plugin.getDb().getEggs(player) + 1;
			else
				eggs = DatabasePools.getEggHashMap().get(
						player.getUniqueId().toString()) + 1;

			DatabasePools.getEggHashMap().put(player.getUniqueId().toString(),
					eggs);

			configAchievement = "Eggs." + eggs;
		}
		if (plugin.getReward().checkAchievement(configAchievement)) {
			String name = plugin.getConfig().getString(
					configAchievement + ".Name");
			String msg = plugin.getConfig().getString(
					configAchievement + ".Message");
			plugin.getAchievementDisplay()
					.displayAchievement(player, name, msg);
			Date now = new Date();
			SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
			plugin.getDb().registerAchievement(
					player,
					plugin.getConfig().getString(configAchievement + ".Name"),
					plugin.getConfig()
							.getString(configAchievement + ".Message"),
					"&0" + format.format(now));

			plugin.getReward().checkConfig(player, configAchievement);
		}
	}
}