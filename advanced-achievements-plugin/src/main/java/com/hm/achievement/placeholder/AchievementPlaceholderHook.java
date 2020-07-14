package com.hm.achievement.placeholder;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.db.CacheManager;
import com.hm.mcshared.file.CommentedYamlConfiguration;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Map;
import java.util.UUID;

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
		if (identifier.equalsIgnoreCase("total_achievements")) {
			return getTotalAchievements();
		}

		if (p == null) {
			return null;
		}

		UUID uuid = p.getUniqueId();

		if (identifier.equalsIgnoreCase("achievements")) {
			return getAchievements(uuid);
		}

		if (identifier.equalsIgnoreCase("achievements_percentage")) {
			return getAchievementsPercentage(uuid);
		}

		if (identifier.toLowerCase().startsWith("unlocked_")) {
			return unlockedPlaceholder(uuid, identifier.replaceFirst("(?i)unlocked_", ""));
		}

		String returnValue = getNormalAchievements(identifier, uuid);
		if (returnValue == null) {
			returnValue = getMultipleAchievements(identifier, uuid);
		}

		return returnValue;
	}

	private String getTotalAchievements() {
		return Integer.toString(namesToDisplayNames.size());
	}

	private String getAchievements(UUID uuid) {
		return Integer.toString(cacheManager.getPlayerTotalAchievements(uuid));
	}

	private String getAchievementsPercentage(UUID uuid) {
		return String.format("%.1f%%", 100 * (double) cacheManager.getPlayerTotalAchievements(uuid)
				/ namesToDisplayNames.size());
	}

	private String unlockedPlaceholder(UUID uuid, String name) {
		return Boolean.toString(cacheManager.hasPlayerAchievement(uuid, name));
	}

	private String getNormalAchievements(String identifier, UUID uuid) {
		for (NormalAchievements category : NormalAchievements.values()) {
			if (identifier.equalsIgnoreCase(category.toString())) {
				long statistic = cacheManager.getAndIncrementStatisticAmount(category, uuid, 0);
				// If played time, convert from millis to hours and display one decimal.
				return category == NormalAchievements.PLAYEDTIME ? String.format("%.1f", statistic / 3600000.0)
						: Long.toString(statistic);
			}
		}
		return null;
	}

	private String getMultipleAchievements(String identifier, UUID uuid) {
		for (MultipleAchievements category : MultipleAchievements.values()) {
			for (String subcategory : mainConfig.getShallowKeys(category.toString())) {
				String categoryPath = category + "_" + subcategory;
				if (identifier.equalsIgnoreCase(categoryPath)) {
					return Long.toString(cacheManager.getAndIncrementStatisticAmount(category, subcategory, uuid, 0));
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
