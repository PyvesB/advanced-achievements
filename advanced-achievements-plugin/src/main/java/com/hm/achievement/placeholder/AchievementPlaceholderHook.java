package com.hm.achievement.placeholder;

import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.bukkit.entity.Player;

import com.hm.achievement.AdvancedAchievements;
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
	private final Map<String, String> namesToDisplayNames;

	@Inject
	public AchievementPlaceholderHook(AdvancedAchievements advancedAchievements,
			@Named("main") CommentedYamlConfiguration mainConfig, CacheManager cacheManager,
			@Named("ntd") Map<String, String> namesToDisplayNames) {
		this.advancedAchievements = advancedAchievements;
		this.mainConfig = mainConfig;
		this.cacheManager = cacheManager;
		this.namesToDisplayNames = namesToDisplayNames;
	}

	@Override
	public boolean persist() {
		return true;
	}

	@Override
	public String onPlaceholderRequest(Player p, String identifier) {
		if ("total_achievements".equalsIgnoreCase(identifier)) {
			return Integer.toString(namesToDisplayNames.size());
		}

		if (p != null) {
			UUID uuid = p.getUniqueId();
			if ("achievements".equalsIgnoreCase(identifier)) {
				return Integer.toString(cacheManager.getPlayerTotalAchievements(uuid));
			}

			if ("achievements_percentage".equalsIgnoreCase(identifier)) {
				return String.format("%.1f%%", 100 * (double) cacheManager.getPlayerTotalAchievements(uuid)
						/ namesToDisplayNames.size());
			}

			for (NormalAchievements category : NormalAchievements.values()) {
				if (category.toString().equalsIgnoreCase(identifier)) {
					long statistic = cacheManager.getAndIncrementStatisticAmount(category, uuid, 0);
					// If played time, convert from millis to hours and display one decimal.
					return category == NormalAchievements.PLAYEDTIME ? String.format("%.1f", statistic / 3600000.0)
							: Long.toString(statistic);
				}
			}

			for (MultipleAchievements category : MultipleAchievements.values()) {
				for (String subcategory : mainConfig.getShallowKeys(category.toString())) {
					String categoryPath = category + "_" + subcategory;
					if (categoryPath.equalsIgnoreCase(identifier)) {
						return Long.toString(cacheManager.getAndIncrementStatisticAmount(category, subcategory, uuid, 0));
					}
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
