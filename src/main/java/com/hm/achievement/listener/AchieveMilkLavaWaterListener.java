package com.hm.achievement.listener;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.NormalAchievements;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerBucketFillEvent;

import java.util.UUID;

/**
 * Listener class to deal with Milk, WaterBuckets and LavaBuckets achievements.
 *
 * @author Pyves
 */
public class AchieveMilkLavaWaterListener extends AbstractRateLimitedListener {

	public AchieveMilkLavaWaterListener(AdvancedAchievements plugin) {
		super(plugin);
	}

	@Override
	public void cleanPlayerData(UUID uuid) {
		String uuidString = uuid.toString();
		// The cooldown for this class must handle Milk, WaterBuckets and LavaBuckets achievements independently, hence
		// the prefix in the cooldown map.
		cooldownMap.remove(NormalAchievements.MILKS + uuidString);
		cooldownMap.remove(NormalAchievements.LAVABUCKETS + uuidString);
		cooldownMap.remove(NormalAchievements.WATERBUCKETS + uuidString);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerBucketFill(PlayerBucketFillEvent event) {
		Player player = event.getPlayer();

		Material resultBucket = event.getItemStack().getType();
		NormalAchievements category = getCategory(resultBucket);

		if (plugin.getDisabledCategorySet().contains(category.toString())) {
			return;
		}

		if (!shouldIncreaseBeTakenIntoAccount(player, category)
				|| isInCooldownPeriod(player, category.toString(), false, category)) {
			return;
		}

		updateStatisticAndAwardAchievementsIfAvailable(player, category, 1);
	}

	private NormalAchievements getCategory(Material resultBucket) {
		switch (resultBucket) {
			case MILK_BUCKET:
				return NormalAchievements.MILKS;
			case LAVA_BUCKET:
				return NormalAchievements.LAVABUCKETS;
			default:
				return NormalAchievements.WATERBUCKETS;
		}
	}
}
