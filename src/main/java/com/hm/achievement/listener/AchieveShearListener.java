package com.hm.achievement.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerShearEntityEvent;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.NormalAchievements;

/**
 * Listener class to deal with Shear achievements (only sheep are taken into account).
 * 
 * @author Pyves
 *
 */
public class AchieveShearListener extends AbstractListener implements Listener {

	public AchieveShearListener(AdvancedAchievements plugin) {

		super(plugin);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerShearEntity(PlayerShearEntityEvent event) {

		if (event.getEntity() != null && !"sheep".equalsIgnoreCase(event.getEntity().getName())) {
			return;
		}

		Player player = event.getPlayer();
		NormalAchievements category = NormalAchievements.SHEARS;
		if (!shouldEventBeTakenIntoAccount(player, category)) {
			return;
		}

		updateStatisticAndAwardAchievementsIfAvailable(player, category, 1);
	}
}
