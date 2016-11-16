package com.hm.achievement.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.NormalAchievements;

/**
 * Listener class to deal with Enchantments achievements.
 * 
 * @author Pyves
 *
 */
public class AchieveEnchantListener extends AbstractListener implements Listener {

	public AchieveEnchantListener(AdvancedAchievements plugin) {

		super(plugin);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEnchantItem(EnchantItemEvent event) {

		Player player = event.getEnchanter();
		NormalAchievements category = NormalAchievements.ENCHANTMENTS;
		if (!shouldEventBeTakenIntoAccount(player, category))
			return;

		updateStatisticAndAwardAchievementsIfAvailable(player, category, 1);
	}
}
