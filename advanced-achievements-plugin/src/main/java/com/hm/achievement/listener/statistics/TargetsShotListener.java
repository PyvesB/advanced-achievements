package com.hm.achievement.listener.statistics;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.ProjectileHitEvent;

import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.utils.RewardParser;
import com.hm.mcshared.file.CommentedYamlConfiguration;

@Singleton
public class TargetsShotListener extends AbstractListener {

	@Inject
	public TargetsShotListener(@Named("main") CommentedYamlConfiguration mainConfig, int serverVersion,
			Map<String, List<Long>> sortedThresholds, CacheManager cacheManager, RewardParser rewardParser) {
		super(mainConfig, serverVersion, sortedThresholds, cacheManager, rewardParser);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onProjectileHit(ProjectileHitEvent event) {
		if (!(event.getEntity().getShooter() instanceof Player)) {
			return;
		}

		Player player = (Player) event.getEntity().getShooter();
		MultipleAchievements category = MultipleAchievements.TARGETSSHOT;
		if (!shouldIncreaseBeTakenIntoAccount(player, category)) {
			return;
		}

		String targetName;
		if (event.getHitEntity() != null) {
			targetName = event.getHitEntity().getType().name().toLowerCase();
		} else if (event.getHitBlock() != null) {
			targetName = event.getHitBlock().getType().name().toLowerCase();
		} else {
			return;
		}

		if (!player.hasPermission(category.toPermName() + '.' + targetName)) {
			return;
		}

		Set<String> foundAchievements = findAchievementsByCategoryAndName(category, targetName);
		foundAchievements.forEach(achievement -> updateStatisticAndAwardAchievementsIfAvailable(player, category,
				achievement, 1));
	}
}
