package com.hm.achievement.listener;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketFillEvent;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.NormalAchievements;

/**
 * Listener class to deal with Milk achievements.
 * 
 * @author Pyves
 *
 */
public class AchieveMilkListener extends AbstractListener implements Listener {

	public AchieveMilkListener(AdvancedAchievements plugin) {

		super(plugin);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerBucketFill(PlayerBucketFillEvent event) {

		if (event.getItemStack().getType() != Material.MILK_BUCKET)
			return;

		Player player = event.getPlayer();
		NormalAchievements category = NormalAchievements.MILKS;
		if (!shouldEventBeTakenIntoAccountNoCreative(player, category))
			return;

		updateStatisticAndAwardAchievementsIfAvailable(player, category, 1);
	}
}
