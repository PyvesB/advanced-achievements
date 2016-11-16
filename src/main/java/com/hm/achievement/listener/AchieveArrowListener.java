package com.hm.achievement.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.NormalAchievements;

/**
 * Listener class to deal with Arrows achievements.
 * 
 * @author Pyves
 *
 */
public class AchieveArrowListener extends AbstractListener implements Listener {

	public AchieveArrowListener(AdvancedAchievements plugin) {

		super(plugin);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEntityShootBow(EntityShootBowEvent event) {

		if (!(event.getEntity() instanceof Player))
			return;

		Player player = (Player) event.getEntity();
		NormalAchievements category = NormalAchievements.ARROWS;
		if (!shouldEventBeTakenIntoAccount(player, category))
			return;

		updateStatisticAndAwardAchievementsIfAvailable(player, category, 1);
	}
}
