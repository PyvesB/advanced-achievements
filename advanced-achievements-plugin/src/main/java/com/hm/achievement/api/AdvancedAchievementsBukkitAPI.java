package com.hm.achievement.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.config.AchievementMap;
import com.hm.achievement.db.AbstractDatabaseManager;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.domain.AwardedAchievement;
import com.hm.achievement.utils.StatisticIncreaseHandler;

/**
 * Underlying implementation of the AdvancedAchievementsAPI interface.
 *
 * @author Pyves
 */
@Singleton
public class AdvancedAchievementsBukkitAPI implements AdvancedAchievementsAPI {

	private final AdvancedAchievements advancedAchievements;
	private final CacheManager cacheManager;
	private final AbstractDatabaseManager databaseManager;
	private final StatisticIncreaseHandler statisticIncreaseHandler;
	private final AchievementMap achievementMap;

	@Inject
	AdvancedAchievementsBukkitAPI(AdvancedAchievements advancedAchievements, CacheManager cacheManager,
			AbstractDatabaseManager databaseManager, StatisticIncreaseHandler statisticIncreaseHandler,
			AchievementMap achievementMap) {
		this.advancedAchievements = advancedAchievements;
		this.cacheManager = cacheManager;
		this.databaseManager = databaseManager;
		this.statisticIncreaseHandler = statisticIncreaseHandler;
		this.achievementMap = achievementMap;
	}

	@Override
	public Version getAdvancedAchievementsVersion() {
		String[] versionParts = StringUtils.split(advancedAchievements.getDescription().getVersion(), '.');
		int major = versionParts.length > 0 ? NumberUtils.toInt(versionParts[0]) : 0;
		int minor = versionParts.length > 1 ? NumberUtils.toInt(versionParts[1]) : 0;
		int patch = versionParts.length > 2 ? NumberUtils.toInt(versionParts[2]) : 0;
		return new AdvancedAchievementsAPI.Version(major, minor, patch);
	}

	@Override
	public boolean hasPlayerReceivedAchievement(UUID player, String achievementName) {
		validateNotNull(player, "Player");
		validateNotEmpty(achievementName, "Achievement Name");
		return cacheManager.hasPlayerAchievement(player, achievementName);
	}

	@Override
	public List<com.hm.achievement.domain.Achievement> getAllAchievements() {
		return new ArrayList<>(achievementMap.getAll());
	}

	@Override
	public List<AwardedAchievement> getPlayerAchievements(UUID player) {
		validateNotNull(player, "Player");
		return databaseManager.getPlayerAchievementsList(player).stream()
				.filter(a -> achievementMap.getForName(a.getName()) != null)
				.map(a -> new AwardedAchievement(achievementMap.getForName(a.getName()), player, a.getDateAwarded()))
				.collect(Collectors.toList());
	}

	@SuppressWarnings("deprecation")
	@Override
	public List<Achievement> getPlayerAchievementsList(UUID player) {
		validateNotNull(player, "Player");
		return databaseManager.getPlayerAchievementsList(player).stream()
				.filter(a -> achievementMap.getForName(a.getName()) != null)
				.map(a -> new Achievement(a.getName(), achievementMap.getForName(a.getName()).getMessage(),
						a.getFormattedDate()))
				.collect(Collectors.toList());
	}

	@Override
	public int getPlayerTotalAchievements(UUID player) {
		validateNotNull(player, "Player");
		return cacheManager.getPlayerAchievements(player).size();
	}

	@Override
	public Rank getPlayerRank(UUID player, long rankingPeriodStart) {
		validateNotNull(player, "Player");
		Map<String, Integer> rankings = databaseManager.getTopList(rankingPeriodStart);
		List<Integer> achievementCounts = new ArrayList<>(rankings.values());
		Integer achievementsCount = rankings.get(player.toString());
		if (achievementsCount != null) {
			// Rank is the first index in the list that has received as many achievements as the player.
			int playerRank = achievementCounts.indexOf(achievementsCount) + 1;
			return new Rank(playerRank, rankings.size());
		} else {
			return new Rank(Integer.MAX_VALUE, rankings.size());
		}
	}

	@Override
	public List<UUID> getTopPlayers(int numOfPlayers, long rankingPeriodStart) {
		return databaseManager.getTopList(rankingPeriodStart).keySet().stream().limit(numOfPlayers).map(UUID::fromString)
				.collect(Collectors.toList());
	}

	@Override
	public long getStatisticForNormalCategory(UUID player, NormalAchievements category) {
		validateNotNull(player, "Player");
		validateNotNull(category, "Category");
		// Underlying cached statistic structures are only populated by the main server thread.
		if (Bukkit.isPrimaryThread()) {
			return cacheManager.getAndIncrementStatisticAmount(category, player, 0);
		} else {
			return databaseManager.getNormalAchievementAmount(player, category);
		}
	}

	@Override
	public long getStatisticForMultipleCategory(UUID player, MultipleAchievements category, String subcategory) {
		validateNotNull(player, "Player");
		validateNotNull(category, "Category");
		validateNotEmpty(subcategory, "Sub-category");
		// Underlying cached statistic structures are only populated by the main server thread.
		if (Bukkit.isPrimaryThread()) {
			return cacheManager.getAndIncrementStatisticAmount(category, subcategory, player, 0);
		} else {
			return databaseManager.getMultipleAchievementAmount(player, category, subcategory);
		}
	}

	@Override
	public String getDisplayNameForName(String achievementName) {
		validateNotEmpty(achievementName, "Achievement Name");
		com.hm.achievement.domain.Achievement achievement = achievementMap.getForName(achievementName);
		return achievement == null ? null : achievement.getDisplayName();
	}

	@Override
	public Map<UUID, Integer> getPlayersTotalAchievements() {
		return databaseManager.getPlayersAchievementsAmount();
	}

	@Override
	public void incrementCategoryForPlayer(NormalAchievements category, Player player, int valueToAdd) {
		validateNotNull(category, "category");
		validateNotNull(player, "player");

		long amount = cacheManager.getAndIncrementStatisticAmount(category, player.getUniqueId(), valueToAdd);
		statisticIncreaseHandler.checkThresholdsAndAchievements(player, category, amount);
	}

	@Override
	public void incrementCategoryForPlayer(MultipleAchievements category, String subcategory, Player player,
			int valueToAdd) {
		validateNotNull(category, "category");
		validateNotEmpty(subcategory, "subcategory");
		validateNotNull(player, "player");

		long amount = cacheManager.getAndIncrementStatisticAmount(category, subcategory, player.getUniqueId(), valueToAdd);
		statisticIncreaseHandler.checkThresholdsAndAchievements(player, category, subcategory, amount);
	}

	/**
	 * Throws an IllegalArgumentException if the argument is null.
	 *
	 * @param argument
	 * @param argumentName
	 */
	private void validateNotNull(Object argument, String argumentName) {
		if (argument == null) {
			throw new IllegalArgumentException(argumentName + " cannot be null.");
		}
	}

	/**
	 * Throws an IllegalArgumentException if the string is empty (i.e. null or "").
	 *
	 * @param argument
	 * @param argumentName
	 */
	private void validateNotEmpty(String argument, String argumentName) {
		if (StringUtils.isEmpty(argument)) {
			throw new IllegalArgumentException(argumentName + " cannot be empty.");
		}
	}

}
