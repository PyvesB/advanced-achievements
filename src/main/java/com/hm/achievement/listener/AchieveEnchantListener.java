package com.hm.achievement.listener;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;

import com.hm.achievement.AdvancedAchievements;

public class AchieveEnchantListener implements Listener {
	private AdvancedAchievements plugin;

	public AchieveEnchantListener(AdvancedAchievements plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEnchantItemEvent(EnchantItemEvent event) {

		Player player = event.getEnchanter();
		if (!player.hasPermission("achievement.get")
				|| plugin.isRestrictCreative()
				&& player.getGameMode() == GameMode.CREATIVE
				|| plugin.isInExludedWorld(player))
			return;

		Integer enchantments = plugin.getDb().registerEnchantment(player);
		String configAchievement = "Enchantments." + enchantments;
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
					format.format(now));

			plugin.getReward().checkConfig(player, configAchievement);
		}
	}
}
