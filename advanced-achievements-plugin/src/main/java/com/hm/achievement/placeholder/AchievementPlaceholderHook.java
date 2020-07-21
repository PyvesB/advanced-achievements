package com.hm.achievement.placeholder;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.achievement.Achievement;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.db.AbstractDatabaseManager;
import com.hm.achievement.db.CacheManager;
import com.hm.mcshared.file.CommentedYamlConfiguration;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;

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
	private final Map<String, BiFunction<UUID, String, String>> placeholderWithSingleArgs = new HashMap<>();
	private final Map<String, Function<UUID, String>> placeholderWithoutArgs = new HashMap<>();

	@Inject
	public AchievementPlaceholderHook(AdvancedAchievements advancedAchievements,
			@Named("main") CommentedYamlConfiguration mainConfig,
			AbstractDatabaseManager databaseManager,
			CacheManager cacheManager,
			@Named("ntd") Map<String, String> namesToDisplayNames) {
		this.advancedAchievements = advancedAchievements;
		this.mainConfig = mainConfig;
		this.cacheManager = cacheManager;
		this.namesToDisplayNames = namesToDisplayNames;
		this.abstractDatabaseManager = databaseManager;
		setup();
	}

	private void setup() {
		placeholderWithSingleArgs.put("unlocked_", this::unlockedPlaceholder);
		placeholderWithSingleArgs.put("goal_", this::getGoal);
		placeholderWithSingleArgs.put("message_", this::getMessage);
		placeholderWithSingleArgs.put("displayname_", this::getDisplayName);
		placeholderWithSingleArgs.put("requirement_", this::getRequirement);
		placeholderWithArgs.put("goalscompleted", this::getCompleted);
		placeholderWithSingleArgs.put("receivedDate_", this::getCompletedDate);

	}

	@Override
	public boolean persist() {
		return true;
	}

	@Override
	public String onPlaceholderRequest(Player p, String identifier) {
		String checkIdentifier = identifier.toLowerCase();
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

		String[] splitId = checkIdentifier.split("_");
		for (String argument : placeholderWithSingleArgs.keySet()) {
			if (checkIdentifier.startsWith(argument)) {
				return placeholderWithSingleArgs.get(argument).apply(uuid,
						checkIdentifier.replaceFirst("(?i)" + argument, ""));
			}
		}
		if (placeholderWithArgs.containsKey(splitId[0])) {
			return placeholderWithArgs.get(splitId[0]).apply(uuid, splitId);
		}
		if (placeholderWithoutArgs.containsKey(splitId[0])) {
			return placeholderWithoutArgs.get(splitId[0]).apply(uuid);
		}

		String returnValue = getNormalAchievements(identifier, uuid);
		if (returnValue == null) {
			returnValue = getMultipleAchievements(identifier, uuid);
		}

		return returnValue;
	}

	public String getCompleted(UUID uuid, String[] args) {
		if (args.length == 1)
			return null;
		String regex = args[0].replace("-", "_");
		String category = args[1].replace("-", "_");
		long[] count = { 0 };
		cacheManager.getByCategory(category).stream().filter(i -> i.getName().matches(regex)).forEach(achievement -> {
			count[0] = cacheManager.getReceivedAchievementsCache().get(uuid).stream()
					.filter(i -> i.equalsIgnoreCase(achievement.getName())).count();
		});
		return "" + count[0];
	}

	public String getGoal(UUID uuid, String aName) {
		System.out.println("getting goal for name: " + aName + " - " + cacheManager.getCache().size());
		Achievement a = cacheManager.getByName(aName);
		if (a == null)
			return null;
		return a.getGoal();
	}

	public String getMessage(UUID uuid, String aName) {
		Achievement a = cacheManager.getByName(aName);
		if (a == null)
			return null;
		return a.getMessage();
	}

	public String getDisplayName(UUID uuid, String aName) {
		Achievement a = cacheManager.getByName(aName);
		if (a == null)
			return null;
		return a.getDisplayName();
	}

	public String getRequirement(UUID uuid, String aName) {
		Achievement a = cacheManager.getByName(aName);
		if (a == null)
			return null;
		return "" + a.getRequirement();
	}

	public String getCompletedDate(UUID uuid, String aName) {
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
