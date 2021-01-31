package com.hm.achievement.listener.statistics;

import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import com.hm.achievement.category.Category;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.config.AchievementMap;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.utils.StatisticIncreaseHandler;

/**
 * Abstract class in charge of factoring out common functionality for the listener classes.
 * 
 * @author Pyves
 */
public abstract class AbstractListener extends StatisticIncreaseHandler implements Listener {

	final Category category;
	Set<String> subcategories;

	AbstractListener(Category category, YamlConfiguration mainConfig, int serverVersion, AchievementMap achievementMap,
			CacheManager cacheManager) {
		super(mainConfig, serverVersion, achievementMap, cacheManager);
		this.category = category;
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();
		subcategories = achievementMap.getSubcategoriesForCategory(category);
	}

	public Category getCategory() {
		return category;
	}

	/**
	 * Updates the statistic in the database for a NormalAchievement and awards an achievement if an available one is
	 * found.
	 * 
	 * @param player
	 * @param incrementValue
	 */
	void updateStatisticAndAwardAchievementsIfAvailable(Player player, int incrementValue) {
		if (shouldIncreaseBeTakenIntoAccount(player, category)) {
			long amount = cacheManager.getAndIncrementStatisticAmount((NormalAchievements) category, player.getUniqueId(),
					incrementValue);
			checkThresholdsAndAchievements(player, category, amount);
		}
	}

	/**
	 * Updates the statistic in the database for a MultipleAchievement and awards an achievement if an available one is
	 * found.
	 * 
	 * @param player
	 * @param subcategories
	 * @param incrementValue
	 */
	void updateStatisticAndAwardAchievementsIfAvailable(Player player, Set<String> subcategories, int incrementValue) {
		if (shouldIncreaseBeTakenIntoAccount(player, category)) {
			subcategories.forEach(subcategory -> {
				long amount = cacheManager.getAndIncrementStatisticAmount((MultipleAchievements) category, subcategory,
						player.getUniqueId(), incrementValue);
				checkThresholdsAndAchievements(player, category, subcategory, amount);
			});
		}
	}

	/**
	 * Returns all achievements that match the provided identifier. This methods accounts for groups of sub-categories,
	 * e.g. 'zombie|pig_zombie|zombie_horse|zombie_villager'.
	 * 
	 * @param id the identifier to match
	 * 
	 * @return all matched achievements
	 * @author tassu
	 */
	Set<String> findAchievementsByCategoryAndName(String id) {
		return subcategories.stream()
				.filter(keys -> keys.equals(id) || keys.startsWith(id + '|') || keys.contains('|' + id + '|')
						|| keys.endsWith('|' + id))
				.collect(Collectors.toSet());
	}

}
