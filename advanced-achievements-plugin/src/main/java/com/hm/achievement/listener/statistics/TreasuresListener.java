package com.hm.achievement.listener.statistics;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerFishEvent;

import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.config.AchievementMap;
import com.hm.achievement.db.CacheManager;

/**
 * Listener class to deal with Treasures achievements.
 * 
 * @author Pyves
 *
 */
@Singleton
public class TreasuresListener extends AbstractListener {

	private Set<String> fishableFish;

	@Inject
	public TreasuresListener(@Named("main") YamlConfiguration mainConfig, AchievementMap achievementMap,
			CacheManager cacheManager) {
		super(NormalAchievements.TREASURES, mainConfig, achievementMap, cacheManager);
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		fishableFish = new HashSet<>();
		for (String block : mainConfig.getStringList("FishableFish")) {
			fishableFish.add(block.toUpperCase());
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerFish(PlayerFishEvent event) {
		if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH
				|| fishableFish.contains(((Item) event.getCaught()).getItemStack().getType().name())) {
			return;
		}

		updateStatisticAndAwardAchievementsIfAvailable(event.getPlayer(), 1);
	}
}
