package com.hm.achievement.listener.statistics;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.utils.RewardParser;
import com.hm.mcshared.event.PlayerChangeAnimalOwnershipEvent;
import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Listener class to deal with PetMasterReceive achievements.
 * 
 * @author Pyves
 *
 */
@Singleton
public class PetMasterReceiveListener extends AbstractListener {

	@Inject
	public PetMasterReceiveListener(@Named("main") CommentedYamlConfiguration mainConfig, int serverVersion,
			Map<String, List<Long>> sortedThresholds, CacheManager cacheManager, RewardParser rewardParser) {
		super(NormalAchievements.PETMASTERRECEIVE, mainConfig, serverVersion, sortedThresholds, cacheManager, rewardParser);
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
