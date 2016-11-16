package com.hm.achievement.listener;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.particle.ReflectionUtils.PackageType;

/**
 * Listener class to deal with EatenItems and ConsumedPotions achievements.
 * 
 * @author Pyves
 *
 */
public class AchieveConsumeListener extends AbstractListener implements Listener {

	final private int version;

	public AchieveConsumeListener(AdvancedAchievements plugin) {

		super(plugin);
		// Simple and fast check to retrieve Minecraft version. Might need to be updated depending on how the
		// Minecraft versions change in the future.
		version = Integer.parseInt(PackageType.getServerVersion().split("_")[1]);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerItemConsume(PlayerItemConsumeEvent event) {

		Player player = event.getPlayer();
		NormalAchievements category;

		if (event.getItem().getType() == Material.POTION) {
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

			category = NormalAchievements.CONSUMEDPOTIONS;
		} else if (event.getItem().getType() != Material.MILK_BUCKET) {
			category = NormalAchievements.EATENITEMS;
		} else {
			return;
		}

		if (plugin.getDisabledCategorySet().contains(category.toString()))
			return;

		if (!shouldEventBeTakenIntoAccount(player, category))
			return;

		updateStatisticAndAwardAchievementsIfAvailable(player, category, 1);
	}
}
