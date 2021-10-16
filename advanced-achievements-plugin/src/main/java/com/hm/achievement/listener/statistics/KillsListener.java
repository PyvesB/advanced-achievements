package com.hm.achievement.listener.statistics;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;

import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.config.AchievementMap;
import com.hm.achievement.db.CacheManager;

/**
 * Listener class to deal with Kills achievements.
 * 
 * @author Pyves
 *
 */
@Singleton
public class KillsListener extends AbstractListener {

	@Inject
	public KillsListener(@Named("main") YamlConfiguration mainConfig, AchievementMap achievementMap,
			CacheManager cacheManager) {
		super(MultipleAchievements.KILLS, mainConfig, achievementMap, cacheManager);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEntityDeath(EntityDeathEvent event) {
		Player player = event.getEntity().getKiller();
		if (player == null) {
			return;
		}

		Entity entity = event.getEntity();
		String mobType = (entity instanceof Creeper && ((Creeper) entity).isPowered()) ? "poweredcreeper"
				: entity.getType().name().toLowerCase();

		Set<String> subcategories = new HashSet<>();

		if (player.hasPermission(category.toChildPermName(mobType))) {
			addMatchingSubcategories(subcategories, mobType);
		}

		if (entity.getCustomName() != null
				&& player.hasPermission(category.toChildPermName(StringUtils.deleteWhitespace(entity.getCustomName())))) {
			addMatchingSubcategories(subcategories, entity.getCustomName());
		}

		if (entity instanceof Player) {
			String specificPlayer = "specificplayer-" + entity.getUniqueId();
			if (player.hasPermission(category.toChildPermName(specificPlayer))) {
				addMatchingSubcategories(subcategories, specificPlayer);
			}
		}

		updateStatisticAndAwardAchievementsIfAvailable(player, subcategories, 1);
	}
}
