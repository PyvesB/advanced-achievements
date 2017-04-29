package com.hm.achievement.listener;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

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
		} else if (itemMaterial != Material.MILK_BUCKET) {
			category = NormalAchievements.EATENITEMS;
		} else {
			return;
		}

		if (plugin.getDisabledCategorySet().contains(category.toString())) {
			return;
		}

		if (!shouldEventBeTakenIntoAccount(player, category)) {
			return;
		}

		updateStatisticAndAwardAchievementsIfAvailable(player, category, 1);
	}
}
