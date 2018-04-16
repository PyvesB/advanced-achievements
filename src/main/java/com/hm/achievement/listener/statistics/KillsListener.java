package com.hm.achievement.listener.statistics;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.google.common.collect.Sets;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;

import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.command.ReloadCommand;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.utils.RewardParser;
import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Listener class to deal with Kills achievements.
 * 
 * @author Pyves
 *
 */
@Singleton
public class KillsListener extends AbstractListener {

	@Inject
	public KillsListener(@Named("main") CommentedYamlConfiguration mainConfig, int serverVersion,
			Map<String, List<Long>> sortedThresholds, CacheManager cacheManager, RewardParser rewardParser,
			ReloadCommand reloadCommand) {
		super(mainConfig, serverVersion, sortedThresholds, cacheManager, rewardParser, reloadCommand);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEntityDeath(EntityDeathEvent event) {
		Player player = event.getEntity().getKiller();

		if (player == null) {
			return;
		}

		if (!shouldIncreaseBeTakenIntoAccountNoPermissions(player)) {
			return;
		}

		Entity entity = event.getEntity();

		if (!(entity instanceof LivingEntity)) {
			return;
		}

		String mobName;
		if (entity instanceof Player) {
			mobName = "player";
		} else {
			mobName = entity.getType().name().toLowerCase();
			if (entity instanceof Creeper && ((Creeper) entity).isPowered()) {
				mobName = "poweredcreeper";
			}
		}

		MultipleAchievements category = MultipleAchievements.KILLS;

		Set<String> foundAchievements = Sets.newHashSet();

		if (player.hasPermission(category.toPermName() + '.' + mobName)) {
			foundAchievements.addAll(findAdvancementsByCategoryAndName(category, mobName));
		}

		if (entity instanceof Player) {
			String specificPlayer = "specificplayer-" + entity.getUniqueId().toString().toLowerCase();
			if (player.hasPermission(category.toPermName() + '.' + specificPlayer)) {
				foundAchievements.addAll(findAdvancementsByCategoryAndName(category, specificPlayer));
			}
		}

		foundAchievements.forEach(achievement -> updateStatisticAndAwardAchievementsIfAvailable(player, category, achievement, 1));
	}
}
