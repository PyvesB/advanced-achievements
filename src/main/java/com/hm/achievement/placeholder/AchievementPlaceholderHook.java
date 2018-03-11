package com.hm.achievement.placeholder;

import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.bukkit.entity.Player;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.db.DatabaseCacheManager;
import com.hm.mcshared.file.CommentedYamlConfiguration;

import me.clip.placeholderapi.external.EZPlaceholderHook;

/**
 * Class enabling usage of placeholder with PlaceholderAPI to get achievements stats in others plugins.
 * 
 * @author Phoetrix
 *
 */
@Singleton
public class AchievementPlaceholderHook extends EZPlaceholderHook {

	private final CommentedYamlConfiguration mainConfig;
	private final DatabaseCacheManager databaseCacheManager;
	private final Set<String> disabledCategories;
	private final Map<String, String> achievementsAndDisplayNames;

	@Inject
	public AchievementPlaceholderHook(AdvancedAchievements advancedAchievements,
			@Named("main") CommentedYamlConfiguration mainConfig, DatabaseCacheManager databaseCacheManager,
			Set<String> disabledCategories, Map<String, String> achievementsAndDisplayNames) {
		super(advancedAchievements, "aach");
		this.mainConfig = mainConfig;
		this.databaseCacheManager = databaseCacheManager;
		this.disabledCategories = disabledCategories;
		this.achievementsAndDisplayNames = achievementsAndDisplayNames;
	}

	@Override
	public String onPlaceholderRequest(Player p, String identifier) {

		if ("achievements".equalsIgnoreCase(identifier)) {
			return String.valueOf(databaseCacheManager.getPlayerTotalAchievements(p.getUniqueId()));
		}

		if ("achievements_percentage".equalsIgnoreCase(identifier)) {
			return String.format("%.1f%%", 100 * (double) databaseCacheManager.getPlayerTotalAchievements(p.getUniqueId())
					/ achievementsAndDisplayNames.size());
		}

		if ("total_achievements".equalsIgnoreCase(identifier)) {
			return String.valueOf(achievementsAndDisplayNames.size());
		}

		for (NormalAchievements category : NormalAchievements.values()) {
			String categoryName = category.toString();

			if (identifier.equalsIgnoreCase(categoryName)) {
				return String.valueOf(databaseCacheManager.getAndIncrementStatisticAmount(category, p.getUniqueId(), 0));
			}
		}

		for (MultipleAchievements category : MultipleAchievements.values()) {
			String categoryName = category.toString();

			if (disabledCategories.contains(categoryName)) {
				continue;
			}

			for (String subcategory : mainConfig.getShallowKeys(categoryName)) {
				String categoryPath = categoryName + "_" + subcategory;

				if (identifier.equalsIgnoreCase(categoryPath)) {
					return String.valueOf(
							databaseCacheManager.getAndIncrementStatisticAmount(category, subcategory, p.getUniqueId(), 0));
				}
			}
		}
		return null;
	}
}
