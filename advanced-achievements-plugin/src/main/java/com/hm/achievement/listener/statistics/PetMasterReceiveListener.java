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
 * Listener class to deal with PetMasterReceive achievements.
 * 
 * @author Pyves
 *
 */
@Singleton
public class PetMasterReceiveListener extends AbstractListener {

	@Inject
	public PetMasterReceiveListener(@Named("main") YamlConfiguration mainConfig, AchievementMap achievementMap,
			CacheManager cacheManager) {
		super(NormalAchievements.PETMASTERRECEIVE, mainConfig, achievementMap, cacheManager);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onChangeOwnership(PlayerChangeAnimalOwnershipEvent event) {
		Player receiverPlayer = (Player) event.getNewOwner();
		if (receiverPlayer == null) {
			// /petm free command ignored.
			return;
		}

		updateStatisticAndAwardAchievementsIfAvailable(receiverPlayer, 1);
	}
}
