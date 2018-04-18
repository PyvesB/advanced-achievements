package com.hm.achievement.listener.statistics;

import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.command.ReloadCommand;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.utils.RewardParser;
import com.hm.mcshared.file.CommentedYamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityBreedEvent;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Listener class to deal with Breeding achievements.
 */
@Singleton
public class BreedingListener extends AbstractListener {

	@Inject
	public BreedingListener(@Named("main") CommentedYamlConfiguration mainConfig, int serverVersion,
			Map<String, List<Long>> sortedThresholds, CacheManager cacheManager, RewardParser rewardParser,
			ReloadCommand reloadCommand) {
		super(mainConfig, serverVersion, sortedThresholds, cacheManager, rewardParser, reloadCommand);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEntityBreed(EntityBreedEvent event) {
		if (!(event.getBreeder() instanceof Player)) {
			return;
		}

		Player player = (Player) event.getBreeder();

		if (player == null) {
			return;
		}

		if (!shouldIncreaseBeTakenIntoAccountNoPermissions(player)) {
			return;
		}

		String mobName = event.getMother().getType().name().toLowerCase();

		MultipleAchievements category = MultipleAchievements.BREEDING;

		if (!player.hasPermission(category.toPermName() + '.' + mobName)) {
			return;
		}

		Set<String> foundAchievements = findAdvancementsByCategoryAndName(category, mobName);
		foundAchievements.forEach(achievement -> updateStatisticAndAwardAchievementsIfAvailable(player, category, achievement, 1));
	}
}
