package com.hm.achievement.listener.statistics;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityBreedEvent;

import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.command.ReloadCommand;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.utils.RewardParser;
import com.hm.mcshared.file.CommentedYamlConfiguration;

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

		Entity entity = event.getMother();

		if (!(entity instanceof LivingEntity)) {
			return;
		}

		String mobName = entity.getType().name().toLowerCase();

		MultipleAchievements category = MultipleAchievements.BREEDING;

		if (mainConfig.isConfigurationSection(category + "." + mobName)
				&& player.hasPermission(category.toPermName() + '.' + mobName)) {
			updateStatisticAndAwardAchievementsIfAvailable(player, category, mobName, 1);
		}
	}
}
