package com.hm.achievement.placeholder;

import java.util.UUID;

import javax.inject.Inject;

import org.bukkit.entity.Player;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.CommandAchievements;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.config.AchievementMap;
import com.hm.achievement.db.CacheManager;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

/**
 * Class enabling usage of placeholder with PlaceholderAPI to get achievements stats in others plugins.
 * 
 * @author Phoetrix
 */
public class AchievementPlaceholderHook extends PlaceholderExpansion {

	private final AdvancedAchievements advancedAchievements;
	private final CacheManager cacheManager;
	private final AchievementMap achievementMap;

	@Inject
	public AchievementPlaceholderHook(AdvancedAchievements advancedAchievements, CacheManager cacheManager,
			AchievementMap achievementMap) {
		this.advancedAchievements = advancedAchievements;
		this.cacheManager = cacheManager;
		this.achievementMap = achievementMap;
	}

	@Override
	public boolean persist() {
		return true;
	}

	@Override
	public String onPlaceholderRequest(Player p, String identifier) {
		if ("total_achievements".equalsIgnoreCase(identifier)) {
			return Integer.toString(achievementMap.getAll().size());
		}

		if (p != null) {
			UUID uuid = p.getUniqueId();
			if ("achievements".equalsIgnoreCase(identifier)) {
				return Integer.toString(cacheManager.getPlayerAchievements(uuid).size());
			}

			if ("achievements_percentage".equalsIgnoreCase(identifier)) {
				return String.format("%.1f%%", 100 * (double) cacheManager.getPlayerAchievements(uuid).size()
						/ achievementMap.getAll().size());
			}

			if ("total_commands".equalsIgnoreCase(identifier)) {
				return Integer.toString(achievementMap.getForCategory(CommandAchievements.COMMANDS).size());
			}

			for (NormalAchievements category : NormalAchievements.values()) {
				if (category.toString().equalsIgnoreCase(identifier)) {
					long statistic = cacheManager.getAndIncrementStatisticAmount(category, uuid, 0);
					// If played time, convert from millis to hours and display one decimal.
					return category == NormalAchievements.PLAYEDTIME ? String.format("%.1f", statistic / 3600000.0)
							: Long.toString(statistic);
				} else if (("total_" + category).equalsIgnoreCase(identifier)) {
					return Integer.toString(achievementMap.getForCategory(category).size());
				}
			}

			for (MultipleAchievements category : MultipleAchievements.values()) {
				if (("total_" + category).equalsIgnoreCase(identifier)) {
					return Integer.toString(achievementMap.getForCategory(category).size());
				}
				for (String subcategory : achievementMap.getSubcategoriesForCategory(category)) {
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
