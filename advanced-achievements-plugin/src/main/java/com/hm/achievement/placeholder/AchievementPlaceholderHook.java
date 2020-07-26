package com.hm.achievement.placeholder;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.achievement.Achievement;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.db.AbstractDatabaseManager;
import com.hm.achievement.db.AchievementCache;
import com.hm.achievement.db.CacheManager;
import com.hm.mcshared.file.CommentedYamlConfiguration;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.*;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

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
	private final AbstractDatabaseManager abstractDatabaseManager;
	private final Map<String, String> namesToDisplayNames;
	private final Map<String, BiFunction<UUID, String[], String>> placeholderWithArgs = new HashMap<>();
	private final Map<String, Function<UUID, String>> placeholderWithoutArgs = new HashMap<>();
	private final Map<String, Supplier<String>> globalPlaceholders = new HashMap<>();
	private final AchievementCache achievementCache;

	@Inject
	public AchievementPlaceholderHook(AdvancedAchievements advancedAchievements,
			@Named("main") CommentedYamlConfiguration mainConfig,
			AbstractDatabaseManager databaseManager,
			CacheManager cacheManager,
			AchievementCache achievementCache,
			@Named("ntd") Map<String, String> namesToDisplayNames) {
		this.advancedAchievements = advancedAchievements;
		this.mainConfig = mainConfig;
		this.cacheManager = cacheManager;
		this.namesToDisplayNames = namesToDisplayNames;
		this.abstractDatabaseManager = databaseManager;
		this.achievementCache = achievementCache;
		setup();
	}

	private void setup() {
		globalPlaceholders.put("total_achievements", this::getTotalAchievements);
		placeholderWithArgs.put("unlocked", this::unlockedPlaceholder);
		placeholderWithArgs.put("goal", this::getGoal);
		placeholderWithArgs.put("message", this::getMessage);
		placeholderWithArgs.put("displayname", this::getDisplayName);
		placeholderWithArgs.put("requirement", this::getRequirement);
		placeholderWithArgs.put("goalscompleted", this::getCompleted);
		placeholderWithArgs.put("receiveddate", this::getCompletedDate);
		placeholderWithoutArgs.put("achievements", this::getAchievements);
		placeholderWithoutArgs.put("achievements_percentage", this::getAchievementsPercentage);
	}

	@Override
	public boolean persist() {
		return true;
	}

	@Override
	public String onPlaceholderRequest(Player p, String identifier) {
		if (globalPlaceholders.containsKey(identifier)) {
			return globalPlaceholders.get(identifier).get();
		}
		if (p == null) {
			return null;
		}

		UUID uuid = p.getUniqueId();
		String[] splitId = identifier.split("_");

		if (placeholderWithArgs.containsKey(splitId[0])) {
			return placeholderWithArgs.get(splitId[0]).apply(uuid, Arrays.copyOfRange(splitId, 1, splitId.length));
		} else if (placeholderWithoutArgs.containsKey(identifier)) {
			return placeholderWithoutArgs.get(identifier).apply(uuid);
		}

		String returnValue = getNormalAchievements(identifier, uuid);
		if (returnValue == null) {
			returnValue = getMultipleAchievements(identifier, uuid);
		}

		return returnValue;
	}

	public String getCompleted(UUID uuid, String[] args) {

		String regex = String.join("_", Arrays.copyOfRange(args, 2, args.length));
		String category = args[0];
		long[] count = { 0 };
		achievementCache.getByCategory(category).stream().filter(i -> i.getName().matches(regex)).forEach(achievement -> {

			count[0] += cacheManager.getReceivedAchievementsCache().getOrDefault(uuid, new HashSet<>()).stream()
					.filter(i -> {
						return i.equalsIgnoreCase(achievement.getName());
					}).count();
		});
		return "" + count[0];
	}

	public String getGoal(UUID uuid, String[] args) {
		String aName = String.join("_", args);
		System.out.println("aname: " + aName);
		Achievement a = achievementCache.getByName(aName);
		if (a == null)
			return null;
		return a.getGoal();
	}

	public String getMessage(UUID uuid, String[] args) {
		String aName = String.join("_", args);
		Achievement a = achievementCache.getByName(aName);
		if (a == null)
			return null;
		return a.getMessage();
	}

	public String getDisplayName(UUID uuid, String[] args) {
		String aName = String.join("_", args);
		Achievement a = achievementCache.getByName(aName);
		if (a == null)
			return null;
		return a.getDisplayName();
	}

	public String getRequirement(UUID uuid, String[] args) {
		String aName = String.join("_", args);
		Achievement a = achievementCache.getByName(aName);
		if (a == null)
			return null;
		return "" + a.getRequirement();
	}

	public String getCompletedDate(UUID uuid, String[] args) {
		String aName = String.join("_", args);
		if (cacheManager.hasPlayerAchievement(uuid, aName)) {
			return abstractDatabaseManager.getPlayerAchievementDate(uuid, aName);
		}
		return null;
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

	private String unlockedPlaceholder(UUID uuid, String[] args) {
		String name = String.join("_", args);
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
