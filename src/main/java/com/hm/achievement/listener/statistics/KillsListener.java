package com.hm.achievement.listener.statistics;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;

import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.command.ReloadCommand;
import com.hm.achievement.db.DatabaseCacheManager;
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
			Map<String, List<Long>> sortedThresholds, DatabaseCacheManager databaseCacheManager, RewardParser rewardParser,
			ReloadCommand reloadCommand) {
		super(mainConfig, serverVersion, sortedThresholds, databaseCacheManager, rewardParser, reloadCommand);
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

		if (mainConfig.isConfigurationSection(category + "." + mobName)
				&& player.hasPermission(category.toPermName() + '.' + mobName)) {
			updateStatisticAndAwardAchievementsIfAvailable(player, category, mobName, 1);
		}

		if (entity instanceof Player) {
			String specificPlayer = "specificplayer-" + ((Player) entity).getUniqueId().toString().toLowerCase();
			if (!mainConfig.isConfigurationSection(category + "." + specificPlayer)
					|| !player.hasPermission(category.toPermName() + '.' + specificPlayer)) {
				return;
			}

			updateStatisticAndAwardAchievementsIfAvailable(player, category, specificPlayer, 1);
		}
	}
}
