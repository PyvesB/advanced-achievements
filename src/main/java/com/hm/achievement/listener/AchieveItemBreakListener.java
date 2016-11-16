package com.hm.achievement.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemBreakEvent;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.NormalAchievements;

/**
 * Listener class to deal with ItemBreaks achievements.
 * 
 * @author Pyves
 *
 */
public class AchieveItemBreakListener extends AbstractListener implements Listener {

	public AchieveItemBreakListener(AdvancedAchievements plugin) {

		super(plugin);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerItemBreak(PlayerItemBreakEvent event) {

		Player player = event.getPlayer();
		NormalAchievements category = NormalAchievements.ITEMBREAKS;
		if (!shouldEventBeTakenIntoAccountNoCreative(player, category))
			return;

		updateStatisticAndAwardAchievementsIfAvailable(player, category, 1);
	}
}
