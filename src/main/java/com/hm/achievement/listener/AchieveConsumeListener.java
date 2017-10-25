package com.hm.achievement.listener;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerItemConsumeEvent;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.NormalAchievements;

/**
 * Listener class to deal with EatenItems and ConsumedPotions achievements.
 * 
 * @author Pyves
 *
 */
public class AchieveConsumeListener extends AbstractListener {

	public AchieveConsumeListener(AdvancedAchievements plugin) {
		super(plugin);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
		Player player = event.getPlayer();
		NormalAchievements category;

		Material itemMaterial = event.getItem().getType();
		if (itemMaterial == Material.POTION) {
			// Don't count drinking water toward ConsumePotions; check the potion type.
			if (isWaterPotion(event.getItem())) {
				return;
			}
			category = NormalAchievements.CONSUMEDPOTIONS;
		} else if (itemMaterial != Material.MILK_BUCKET) {
			category = NormalAchievements.EATENITEMS;
		} else {
			return;
		}

		if (plugin.getDisabledCategorySet().contains(category.toString())) {
			return;
		}

		if (!shouldIncreaseBeTakenIntoAccount(player, category)) {
			return;
		}

		updateStatisticAndAwardAchievementsIfAvailable(player, category, 1);
	}
}
