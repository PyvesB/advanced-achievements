package com.hm.achievement.placeholder;

import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.hm.achievement.AdvancedAchievements;
import org.bukkit.entity.Player;

import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.db.CacheManager;
import com.hm.mcshared.file.CommentedYamlConfiguration;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

/**
 * Class enabling usage of placeholder with PlaceholderAPI to get achievements stats in others plugins.
 * 
 * @author Phoetrix
 */
@Singleton
public class AchievementPlaceholderHook extends PlaceholderExpansion {

	private final AdvancedAchievements advancedAchievements;
	private final CommentedYamlConfiguration mainConfig;
	private final CacheManager cacheManager;
	private final Set<String> disabledCategories;
	private final Map<String, String> achievementsAndDisplayNames;

	@Inject
	public AchievementPlaceholderHook(AdvancedAchievements advancedAchievements,
				@Named("main") CommentedYamlConfiguration mainConfig,
                CacheManager cacheManager, Set<String> disabledCategories, Map<String, String> achievementsAndDisplayNames) {
		this.advancedAchievements = advancedAchievements;
		this.mainConfig = mainConfig;
		this.cacheManager = cacheManager;
		this.disabledCategories = disabledCategories;
		this.achievementsAndDisplayNames = achievementsAndDisplayNames;
	}

	@Override
	public String onPlaceholderRequest(Player p, String identifier) {
		if ("achievements".equalsIgnoreCase(identifier)) {
			return String.valueOf(cacheManager.getPlayerTotalAchievements(p.getUniqueId()));
		}

		if ("achievements_percentage".equalsIgnoreCase(identifier)) {
			return String.format("%.1f%%", 100 * (double) cacheManager.getPlayerTotalAchievements(p.getUniqueId())
					/ achievementsAndDisplayNames.size());
		}

		if ("total_achievements".equalsIgnoreCase(identifier)) {
			return String.valueOf(achievementsAndDisplayNames.size());
		}

		for (NormalAchievements category : NormalAchievements.values()) {
			String categoryName = category.toString();

			if (identifier.equalsIgnoreCase(categoryName)) {
				return String.valueOf(cacheManager.getAndIncrementStatisticAmount(category, p.getUniqueId(), 0));
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
					return String
							.valueOf(cacheManager.getAndIncrementStatisticAmount(category, subcategory, p.getUniqueId(), 0));
				}
			}
		}
		return null;
	}

	@Override
	public String getIdentifier() {
		return "aach";
	}

	@Override
	public String getAuthor() {
		return String.join(", ", advancedAchievements.getDescription().getAuthors());
	}

	@Override
	public String getVersion() {
		return advancedAchievements.getDescription().getVersion();
	}
}
