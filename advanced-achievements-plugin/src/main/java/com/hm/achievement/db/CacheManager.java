package com.hm.achievement.db;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;

import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.lifecycle.Cleanable;
import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Class used to provide a cache wrapper for various database statistics, in order to reduce load of database and enable
 * faster in-memory operations.
 * 
 * @author Pyves
 *
 */
@Singleton
public class CacheManager implements Cleanable {

	private final CommentedYamlConfiguration mainConfig;
	private final AbstractDatabaseManager databaseManager;
	// Statistics of the different players for normal achievements; keys in the inner maps correspond to UUIDs.
	private final Map<NormalAchievements, Map<String, CachedStatistic>> normalAchievementsToPlayerStatistics;
	// Statistics of the different players for multiple achievements; keys in the inner maps correspond to concatenated
	// UUIDs and block/entity/command identifiers.
	private final Map<MultipleAchievements, Map<String, CachedStatistic>> multipleAchievementsToPlayerStatistics;
	// Multimaps corresponding to the different achievements received by the players.
	private final Map<UUID, Set<String>> receivedAchievementsCache;
	private final Map<UUID, Set<String>> notReceivedAchievementsCache;
	// Map corresponding to the total amount of achievements received by each player.
	private final Map<UUID, Integer> totalPlayerAchievementsCache;

	@Inject
	public CacheManager(@Named("main") CommentedYamlConfiguration mainConfig, AbstractDatabaseManager databaseManager) {
		this.mainConfig = mainConfig;
		this.databaseManager = databaseManager;
		normalAchievementsToPlayerStatistics = new EnumMap<>(NormalAchievements.class);
		multipleAchievementsToPlayerStatistics = new EnumMap<>(MultipleAchievements.class);
		receivedAchievementsCache = new HashMap<>();
		notReceivedAchievementsCache = new HashMap<>();

		// ConcurrentHashMaps are necessary to guarantee thread safety.
		for (NormalAchievements normalAchievement : NormalAchievements.values()) {
			normalAchievementsToPlayerStatistics.put(normalAchievement, new ConcurrentHashMap<String, CachedStatistic>());
		}
		for (MultipleAchievements multipleAchievement : MultipleAchievements.values()) {
			multipleAchievementsToPlayerStatistics.put(multipleAchievement,
					new ConcurrentHashMap<String, CachedStatistic>());
		}
		totalPlayerAchievementsCache = new ConcurrentHashMap<>();
	}

	@Override
	public void cleanPlayerData(UUID uuid) {
		// Clear achievements caches.
		receivedAchievementsCache.remove(uuid);
		notReceivedAchievementsCache.remove(uuid);
		totalPlayerAchievementsCache.remove(uuid);

		// Indicate to the relevant cached statistics that the player has disconnected.
		for (MultipleAchievements category : MultipleAchievements.values()) {
			Map<String, CachedStatistic> categoryMap = getHashMap(category);
			for (String subcategory : mainConfig.getShallowKeys(category.toString())) {
				CachedStatistic statistic = categoryMap.get(getMultipleCategoryCacheKey(category, uuid, subcategory));
				if (statistic != null) {
					statistic.signalPlayerDisconnection();
				}
			}
		}
		for (NormalAchievements category : NormalAchievements.values()) {
			CachedStatistic statistic = getHashMap(category).get(uuid.toString());
			if (statistic != null) {
				statistic.signalPlayerDisconnection();
			}
		}
	}

	/**
	 * Retrieves a HashMap for a NormalAchievement based on the category.
	 * 
	 * @param category
	 * @return the map of cached statistics for a Normal category
	 */
	public Map<String, CachedStatistic> getHashMap(NormalAchievements category) {
		return normalAchievementsToPlayerStatistics.get(category);
	}

	/**
	 * Retrieves a HashMap for a MultipleAchievement based on the category.
	 * 
	 * @param category
	 * @return the map of cached statistics for a Multiple category
	 */
	public Map<String, CachedStatistic> getHashMap(MultipleAchievements category) {
		return multipleAchievementsToPlayerStatistics.get(category);
	}

	/**
	 * Increases the statistic for a NormalAchievement by the given value and returns the updated statistic value. Calls
	 * the database if not found in the cache.
	 * 
	 * @param category
	 * @param player
	 * @param value
	 * @return the updated statistic value
	 */
	public long getAndIncrementStatisticAmount(NormalAchievements category, UUID player, int value) {
		CachedStatistic statistic = getHashMap(category).get(player.toString());
		if (statistic == null) {
			statistic = new CachedStatistic(databaseManager.getNormalAchievementAmount(player, category), true);
			getHashMap(category).put(player.toString(), statistic);
		}
		if (value > 0) {
			long newValue = statistic.getValue() + value;
			statistic.setValue(newValue);
			return newValue;
		}
		return statistic.getValue();
	}

	/**
	 * Increases the statistic for a MultipleAchievement by the given value and returns the updated statistic value.
	 * Calls the database if not found in the cache.
	 * 
	 * @param category
	 * @param subcategory
	 * @param player
	 * @param value
	 * @return the updated statistic value
	 */
	public long getAndIncrementStatisticAmount(MultipleAchievements category, String subcategory, UUID player, int value) {
		CachedStatistic statistic = getHashMap(category).get(getMultipleCategoryCacheKey(category, player, subcategory));
		if (statistic == null) {
			String subcategoryDBName = subcategory;
			if (category == MultipleAchievements.PLAYERCOMMANDS) {
				subcategoryDBName = StringUtils.deleteWhitespace(subcategory);
			}
			statistic = new CachedStatistic(
					databaseManager.getMultipleAchievementAmount(player, category, subcategoryDBName), true);
			getHashMap(category).put(getMultipleCategoryCacheKey(category, player, subcategory), statistic);
		}
		if (value > 0) {
			long newValue = statistic.getValue() + value;
			statistic.setValue(newValue);
			return newValue;
		}
		return statistic.getValue();
	}

	/**
	 * Returns whether player has received a specific achievement.
	 * 
	 * @param player
	 * @param name
	 * @return true if achievement received by player, false otherwise
	 */
	public boolean hasPlayerAchievement(UUID player, String name) {
		Set<String> playerReceived = receivedAchievementsCache.computeIfAbsent(player, s -> new HashSet<>());
		if (playerReceived.contains(name)) {
			return true;
		}
		Set<String> playerNotReceived = notReceivedAchievementsCache.computeIfAbsent(player, s -> new HashSet<>());
		if (playerNotReceived.contains(name)) {
			return false;
		}

		boolean received = databaseManager.hasPlayerAchievement(player, name);
		if (received) {
			playerReceived.add(name);
		} else {
			playerNotReceived.add(name);
		}
		return received;
	}

	/**
	 * Returns the total number of achievements received by a player. Can be called asynchronously by BungeeTabListPlus,
	 * method must therefore be synchronized to avoid race conditions if a player calls /aach stats at the same time.
	 * 
	 * @param player
	 * @return the number of achievements received by the player
	 */
	public synchronized int getPlayerTotalAchievements(UUID player) {
		Integer totalAchievements = totalPlayerAchievementsCache.get(player);
		if (totalAchievements == null) {
			totalAchievements = databaseManager.getPlayerAchievementsAmount(player);
			totalPlayerAchievementsCache.put(player, totalAchievements);
		}
		return totalAchievements;
	}

	/**
	 * Returns a key for the multipleAchievementsToPlayerStatistics structure. Concatenation of player UUID and
	 * subcategory name, with removed whitespaces for PlayerCommands.
	 * 
	 * @param category
	 * @param player
	 * @param subcategory
	 * @return the statistics key for a Multiple category
	 */
	public String getMultipleCategoryCacheKey(MultipleAchievements category, UUID player, String subcategory) {
		if (category == MultipleAchievements.PLAYERCOMMANDS) {
			return player.toString() + StringUtils.deleteWhitespace(subcategory);
		}
		return player.toString() + subcategory;
	}

	/**
	 * Adds an achievement to the achievement received cache and removes it from the not received cache. A call to
	 * {@link #hasPlayerAchievement(UUID, String)} is expected to have been made made beforehand for the same player.
	 * 
	 * @param player
	 * @param achievementName
	 */
	public void registerNewlyReceivedAchievement(UUID player, String achievementName) {
		receivedAchievementsCache.get(player).add(achievementName);
		notReceivedAchievementsCache.get(player).remove(achievementName);
		totalPlayerAchievementsCache.put(player, getPlayerTotalAchievements(player) + 1);
	}

	/**
	 * Removes an achievement from the achievement received cache and adds it to the not received cache. A call to
	 * {@link #hasPlayerAchievement(UUID, String)} is expected to have been made made beforehand for the same player.
	 * 
	 * @param player
	 * @param achievementName
	 */
	public void removePreviouslyReceivedAchievement(UUID player, String achievementName) {
		receivedAchievementsCache.get(player).remove(achievementName);
		notReceivedAchievementsCache.get(player).add(achievementName);
		totalPlayerAchievementsCache.put(player, getPlayerTotalAchievements(player) - 1);
	}

}
