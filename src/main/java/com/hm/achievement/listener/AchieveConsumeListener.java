package com.hm.achievement.listener;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.particle.ReflectionUtils.PackageType;

import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

/**
 * Listener class to deal with EatenItems and ConsumedPotions achievements.
 * 
 * @author Pyves
 *
 */
public class AchieveConsumeListener implements Listener {

	private AdvancedAchievements plugin;
	private int version;

	public AchieveConsumeListener(AdvancedAchievements plugin) {

		this.plugin = plugin;
		// Simple and fast check to retrieve Minecraft version. Might need to be updated depending on how the
		// Minecraft versions change in the future.
		version = Integer.parseInt(PackageType.getServerVersion().split("_")[1]);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerItemConsume(PlayerItemConsumeEvent event) {

		Player player = event.getPlayer();
		if (plugin.isRestrictCreative() && player.getGameMode() == GameMode.CREATIVE || plugin.isInExludedWorld(player))
			return;

		String configAchievement;

		if (event.getItem().getType() == Material.POTION && !plugin.getDisabledCategorySet().contains("ConsumedPotions")
				&& player.hasPermission("achievement.count.consumedpotions")) {

			// Don't count drinking water toward ConsumePotions; check the potion type.
			if (version >= 9) {
				PotionMeta meta = (PotionMeta) (event.getItem().getItemMeta());
				PotionType potionType = meta.getBasePotionData().getType();

				if (potionType == PotionType.WATER) {
					return;
				}
			} else {
				// Method getBasePotionData does not exist for versions prior to Minecraft 1.9.
				if (event.getItem().getDurability() == 0) {
					return;
				}
			}

			int consumedPotions = plugin.getPoolsManager().getPlayerConsumedPotionAmount(player) + 1;

			plugin.getPoolsManager().getConsumedPotionsHashMap().put(player.getUniqueId().toString(), consumedPotions);
			configAchievement = "ConsumedPotions." + consumedPotions;
		} else if (event.getItem().getType() != Material.MILK_BUCKET
				&& player.hasPermission("achievement.count.eatenitems")
				&& !plugin.getDisabledCategorySet().contains("EatenItems")) {
			int eatenItems = plugin.getPoolsManager().getPlayerEatenItemAmount(player) + 1;

			plugin.getPoolsManager().getEatenItemsHashMap().put(player.getUniqueId().toString(), eatenItems);
			configAchievement = "EatenItems." + eatenItems;
		} else
			return;

		if (plugin.getPluginConfig().getString(configAchievement + ".Message", null) != null) {

			plugin.getAchievementDisplay().displayAchievement(player, configAchievement);
			plugin.getDb().registerAchievement(player, plugin.getPluginConfig().getString(configAchievement + ".Name"),
					plugin.getPluginConfig().getString(configAchievement + ".Message"));
			plugin.getReward().checkConfig(player, configAchievement);
		}
	}
}
