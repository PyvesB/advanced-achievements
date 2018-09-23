package com.hm.achievement.listener.statistics;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerFishEvent;

import com.hm.achievement.category.Category;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.utils.RewardParser;
import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Listener class to deal with Fish and Treasure achievements.
 * 
 * @author Pyves
 *
 */
@Singleton
public class CaughtFishTreasuresListener extends AbstractListener {

	private final Set<Category> disabledCategories;

	@Inject
	public CaughtFishTreasuresListener(@Named("main") CommentedYamlConfiguration mainConfig, int serverVersion,
			Map<String, List<Long>> sortedThresholds, CacheManager cacheManager, RewardParser rewardParser,
			Set<Category> disabledCategories) {
		super(mainConfig, serverVersion, sortedThresholds, cacheManager, rewardParser);
		this.disabledCategories = disabledCategories;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerFish(PlayerFishEvent event) {
		if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) {
			return;
		}

		Player player = event.getPlayer();
		NormalAchievements category;
		Material caughtMaterial = ((Item) event.getCaught()).getItemStack().getType();
		if (caughtMaterial.name().endsWith("FISH")
				|| serverVersion >= 13 && (caughtMaterial == Material.COD || caughtMaterial == Material.SALMON)) {
			category = NormalAchievements.FISH;
		} else {
			category = NormalAchievements.TREASURES;
		}

		if (disabledCategories.contains(category)) {
			return;
		}

		if (!shouldIncreaseBeTakenIntoAccount(player, category)) {
			return;
		}

		updateStatisticAndAwardAchievementsIfAvailable(player, category, 1);
	}
}
