package com.hm.achievement.listener;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

import com.hm.achievement.AdvancedAchievements;

public class AchieveCraftListener implements Listener {
	private AdvancedAchievements plugin;

	public AchieveCraftListener(AdvancedAchievements plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onInventoryCraft(CraftItemEvent event) {

		if (!(event.getWhoClicked() instanceof Player)
				|| event.getAction().name().equals("NOTHING"))
			return;

		Player player = (Player) event.getWhoClicked();
		if (!player.hasPermission("achievement.get")
				|| plugin.isRestrictCreative()
				&& player.getGameMode() == GameMode.CREATIVE
				|| plugin.isInExludedWorld(player))
			return;
		try {

			ItemStack item = event.getRecipe().getResult();
			String craftName = item.getType().name().toLowerCase();
			if (!plugin.getConfig().isConfigurationSection(
					"Crafts." + craftName))
				return;

			Integer times = plugin.getDb().registerCraft(player, item);
			String configAchievement = "Crafts." + craftName + "." + times;
			if (plugin.getReward().checkAchievement(configAchievement)) {
				String name = plugin.getConfig().getString(
						configAchievement + ".Name");
				String msg = plugin.getConfig().getString(
						configAchievement + ".Message");
				plugin.getAchievementDisplay().displayAchievement(player, name,
						msg);
				Date now = new Date();
				SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
				plugin.getDb().registerAchievement(
						player,
						plugin.getConfig().getString(
								configAchievement + ".Name"),
						plugin.getConfig().getString(
								configAchievement + ".Message"),
						format.format(now));

				plugin.getReward().checkConfig(player, configAchievement);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
