package com.hm.achievement.listener.statistics;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.config.AchievementMap;
import com.hm.achievement.db.CacheManager;
import com.hm.mcshared.event.PlayerChangeAnimalOwnershipEvent;

/**
 * Listener class to deal with PetMasterGive achievements.
 * 
 * @author Pyves
 *
 */
@Singleton
public class PetMasterGiveListener extends AbstractListener {

	@Inject
	public PetMasterGiveListener(@Named("main") YamlConfiguration mainConfig, AchievementMap achievementMap,
			CacheManager cacheManager) {
		super(NormalAchievements.PETMASTERGIVE, mainConfig, achievementMap, cacheManager);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onChangeOwnership(PlayerChangeAnimalOwnershipEvent event) {
		// New owner null is /petm free invoked. Check that old owner is a connected player.
		if (event.getNewOwner() == null || !(event.getOldOwner() instanceof Player)) {
			return;
		}

		updateStatisticAndAwardAchievementsIfAvailable((Player) event.getOldOwner(), 1);
	}
}
