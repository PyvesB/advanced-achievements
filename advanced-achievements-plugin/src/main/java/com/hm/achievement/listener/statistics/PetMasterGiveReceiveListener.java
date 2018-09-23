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

import com.hm.achievement.category.Category;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.utils.RewardParser;
import com.hm.mcshared.event.PlayerChangeAnimalOwnershipEvent;
import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Listener class to deal with PetMasterGive and PetMasterReceive achievements.
 * 
 * @author Pyves
 *
 */
@Singleton
public class PetMasterGiveReceiveListener extends AbstractListener {

	private final Set<Category> disabledCategories;

	@Inject
	public PetMasterGiveReceiveListener(@Named("main") CommentedYamlConfiguration mainConfig, int serverVersion,
			Map<String, List<Long>> sortedThresholds, CacheManager cacheManager, RewardParser rewardParser,
			Set<Category> disabledCategories) {
		super(mainConfig, serverVersion, sortedThresholds, cacheManager, rewardParser);
		this.disabledCategories = disabledCategories;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onChangeOwnership(PlayerChangeAnimalOwnershipEvent event) {
		Player receiverPlayer = (Player) event.getNewOwner();
		if (receiverPlayer == null) {
			// /petm free command ignored.
			return;
		}
		NormalAchievements categoryReceive = NormalAchievements.PETMASTERRECEIVE;

		if (!shouldIncreaseBeTakenIntoAccount(receiverPlayer, categoryReceive)) {
			return;
		}

		if (!disabledCategories.contains(categoryReceive)) {
			updateStatisticAndAwardAchievementsIfAvailable(receiverPlayer, categoryReceive, 1);
		}

		Player giverPlayer = (Player) event.getOldOwner();
		NormalAchievements categoryGive = NormalAchievements.PETMASTERGIVE;
		if (!shouldIncreaseBeTakenIntoAccount(giverPlayer, categoryGive)) {
			return;
		}

		if (!disabledCategories.contains(categoryGive)) {
			updateStatisticAndAwardAchievementsIfAvailable(giverPlayer, categoryGive, 1);
		}
	}
}
