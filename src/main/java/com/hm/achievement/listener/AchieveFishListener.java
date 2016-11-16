package com.hm.achievement.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.NormalAchievements;

/**
 * Listener class to deal with Fish achievements.
 * 
 * @author Pyves
 *
 */
public class AchieveFishListener extends AbstractListener implements Listener {

	public AchieveFishListener(AdvancedAchievements plugin) {

		super(plugin);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerFish(PlayerFishEvent event) {

		if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH)
			return;

		Player player = event.getPlayer();
		NormalAchievements category = NormalAchievements.FISH;
		if (!shouldEventBeTakenIntoAccount(player, category))
			return;

		updateStatisticAndAwardAchievementsIfAvailable(player, category, 1);
	}
}
