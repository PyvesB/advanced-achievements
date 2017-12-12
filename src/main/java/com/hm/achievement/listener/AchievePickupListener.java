package com.hm.achievement.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerPickupItemEvent;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.NormalAchievements;

/**
 * Listener class to deal with ItemPickups achievements. Keep PlayerPickupItemEvent for now, as it was only introduced
 * in late Minecraft 1.12 versions.
 * 
 * @author Pyves
 *
 */
@SuppressWarnings("deprecation")
public class AchievePickupListener extends AbstractListener {

	public AchievePickupListener(AdvancedAchievements plugin) {
		super(plugin);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		Player player = event.getPlayer();
		NormalAchievements category = NormalAchievements.PICKUPS;
		if (!shouldIncreaseBeTakenIntoAccount(player, category)) {
			return;
		}

		updateStatisticAndAwardAchievementsIfAvailable(player, category, 1);
	}
}
