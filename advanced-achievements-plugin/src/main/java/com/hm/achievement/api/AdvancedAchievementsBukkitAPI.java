package com.hm.achievement.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.Bukkit;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.db.AbstractDatabaseManager;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.db.data.AwardedDBAchievement;

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
	private final Map<String, String> namesToDisplayNames;
	private final Logger logger;

	@Inject
	AdvancedAchievementsBukkitAPI(AdvancedAchievements advancedAchievements, Logger logger, CacheManager cacheManager,
			AbstractDatabaseManager databaseManager, @Named("ntd") Map<String, String> namesToDisplayNames) {
		this.advancedAchievements = advancedAchievements;
		this.logger = logger;
		this.cacheManager = cacheManager;
		this.databaseManager = databaseManager;
		this.namesToDisplayNames = namesToDisplayNames;
	}

	/**
	 * Returns a ready to use API instance. The caller must previously check whether the plugin is enabled and has a
	 * minimum version of 5.0.
	 *
	 * @return API instance
	 * @deprecated use the AdvancedAchievementsAPIFetcher class in the public API
	 */
	@Deprecated
	public static AdvancedAchievementsAPI linkAdvancedAchievements() {
		return ((AdvancedAchievements) Bukkit.getPluginManager().getPlugin("AdvancedAchievements"))
				.getAdvancedAchievementsAPI();
	}

	@Override
	public int getAdvancedAchievementsVersionCode() {
		String version = advancedAchievements.getDescription().getVersion();
		int versionCode = 100
				* Integer.parseInt(Character.toString(advancedAchievements.getDescription().getVersion().charAt(0)))
				+ 10 * Integer.parseInt(Character.toString(advancedAchievements.getDescription().getVersion().charAt(2)));
		if (version.length() > 4) {
			versionCode += Integer
					.parseInt(Character.toString(advancedAchievements.getDescription().getVersion().charAt(4)));
		}
		return versionCode;
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
		// Underlying structures do not support concurrent operations and are only used by the main server thread. Not
		// thread-safe to modify or read them asynchronously. Do not use cached data if player is offline.
		if (Bukkit.isPrimaryThread() && isPlayerOnline(player)) {
			return cacheManager.hasPlayerAchievement(player, achievementName);
		} else {
			return databaseManager.hasPlayerAchievement(player, achievementName);
		}
	}

	@Override
	public List<Achievement> getPlayerAchievementsList(UUID player) {
		validateNotNull(player, "Player");
		return databaseManager.getPlayerAchievementsList(player).stream().map(AwardedDBAchievement::toAPIAchievement)
				.collect(Collectors.toList());
	}

	@Override
	public int getPlayerTotalAchievements(UUID player) {
		validateNotNull(player, "Player");
		// Only use cached data if player is online.
		if (isPlayerOnline(player)) {
			return cacheManager.getPlayerTotalAchievements(player);
		} else {
			return databaseManager.getPlayerAchievementsAmount(player);
		}
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
		// Underlying structures do not support concurrent write operations and are only modified by the main server
		// thread. Do not use cache if player is offline.
		if (Bukkit.isPrimaryThread() && isPlayerOnline(player)) {
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
		// Underlying structures do not support concurrent write operations and are only modified by the main server
		// thread. Do not use cache if player is offline.
		if (Bukkit.isPrimaryThread() && isPlayerOnline(player)) {
			return cacheManager.getAndIncrementStatisticAmount(category, subcategory, player, 0);
		} else {
			return databaseManager.getMultipleAchievementAmount(player, category, subcategory);
		}
	}

	@Override
	public String getDisplayNameForName(String achievementName) {
		validateNotEmpty(achievementName, "Achievement Name");
		return namesToDisplayNames.get(achievementName);
	}

	@Override
	public Map<UUID, Integer> getPlayersTotalAchievements() {
		return databaseManager.getPlayersAchievementsAmount();
	}

	/**
	 * Checks whether the player is online by making a call on the server's main thread of execution.
	 *
	 * @param player
	 * @return true if player is online, false otherwise
	 */
	private boolean isPlayerOnline(UUID player) {
		if (Bukkit.isPrimaryThread()) {
			return Bukkit.getPlayer(player) != null;
		}
		// Called asynchronously. To ensure thread safety we must issue a call on the server's main thread of execution.
		Future<Boolean> onlineCheckFuture = Bukkit.getScheduler().callSyncMethod(advancedAchievements,
				() -> Bukkit.getPlayer(player) != null);

		boolean playerOnline = true;
		try {
			playerOnline = onlineCheckFuture.get();
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE, "Thread interrupted while checking whether player online:", e);
			Thread.currentThread().interrupt();
		} catch (ExecutionException e) {
			logger.log(Level.SEVERE, "Unexpected execution exception while checking whether player online:", e);
		} catch (CancellationException ignored) {
			// Task can be cancelled when plugin disabled.
		}
		return playerOnline;
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
