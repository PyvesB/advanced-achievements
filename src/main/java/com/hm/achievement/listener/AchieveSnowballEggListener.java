package com.hm.achievement.listener;

import org.bukkit.entity.Egg;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.ProjectileLaunchEvent;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.NormalAchievements;

/**
 * Listener class to deal with Snowballs and Eggs achievements.
 * 
 * @author Pyves
 *
 */
public class AchieveSnowballEggListener extends AbstractListener {

	public AchieveSnowballEggListener(AdvancedAchievements plugin) {
		super(plugin);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onProjectileLaunch(ProjectileLaunchEvent event) {
		if (!(event.getEntity().getShooter() instanceof Player)) {
			return;
		}

		Player player = (Player) event.getEntity().getShooter();
		NormalAchievements category;
		if (event.getEntity() instanceof Snowball) {
			category = NormalAchievements.SNOWBALLS;
		} else if (event.getEntity() instanceof Egg) {
			category = NormalAchievements.EGGS;
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
