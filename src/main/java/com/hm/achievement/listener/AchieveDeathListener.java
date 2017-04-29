package com.hm.achievement.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.NormalAchievements;

/**
 * Listener class to deal with Deaths achievements.
 * 
 * @author Pyves
 *
 */
public class AchieveDeathListener extends AbstractListener {

	public AchieveDeathListener(AdvancedAchievements plugin) {
		super(plugin);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		if (player == null) {
			return;
		}

		NormalAchievements category = NormalAchievements.DEATHS;
		if (!shouldEventBeTakenIntoAccount(player, category)) {
			return;
		}

		updateStatisticAndAwardAchievementsIfAvailable(player, category, 1);
	}
}
