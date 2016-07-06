package com.hm.achievement.listener;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;

import com.hm.achievement.AdvancedAchievements;

public class AchieveConsumeListener implements Listener {

	private AdvancedAchievements plugin;

	public AchieveConsumeListener(AdvancedAchievements plugin) {

		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerItemConsume(PlayerItemConsumeEvent event) {

		Player player = event.getPlayer();
		if (plugin.isRestrictCreative() && player.getGameMode() == GameMode.CREATIVE || plugin.isInExludedWorld(player))
			return;

		String configAchievement;

		if (event.getItem().getType() == Material.POTION && !plugin.getDisabledCategorySet().contains("ConsumedPotions")
				&& player.hasPermission("achievement.count.consumedpotions")) {
			int consumedPotions = plugin.getPoolsManager().getPlayerConsumedPotionAmount(player) + 1;

			plugin.getPoolsManager().getConsumedPotionsHashMap().put(player.getUniqueId().toString(), consumedPotions);
			configAchievement = "ConsumedPotions." + consumedPotions;
		} else if (player.hasPermission("achievement.count.eatenitems")
				&& !plugin.getDisabledCategorySet().contains("EatenItems")) {
			int eatenItems = plugin.getPoolsManager().getPlayerEatenItemAmount(player) + 1;

			plugin.getPoolsManager().getEatenItemsHashMap().put(player.getUniqueId().toString(), eatenItems);
			configAchievement = "EatenItems." + eatenItems;
		} else
			return;

		if (plugin.getReward().checkAchievement(configAchievement)) {

			plugin.getAchievementDisplay().displayAchievement(player, configAchievement);
			plugin.getDb().registerAchievement(player, plugin.getPluginConfig().getString(configAchievement + ".Name"),
					plugin.getPluginConfig().getString(configAchievement + ".Message"));
			plugin.getReward().checkConfig(player, configAchievement);
		}
	}
}
