package com.hm.achievement.db;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.HashMultimap;
import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;

/**
 * Class used to provide a cache wrapper for the database statistics, in order to reduce load of database and enable
 * more modularity for the user.
 * 
 * @author Pyves
 *
 */
public class DatabasePoolsManager {

	private final AdvancedAchievements plugin;
	// Statistics of the different players for normal achievements; keys in the inner maps correspond to UUIDs.
	private final Map<NormalAchievements, Map<String, Long>> normalAchievementsToPlayerStatistics;
	// Statistics of the different players for multiple achievements; keys in the inner maps correspond to concatenated
	// UUIDs and block/entity/command identifiers.
	private final Map<MultipleAchievements, Map<String, Long>> multipleAchievementsToPlayerStatistics;

	// Multimaps corresponding to the different achievements received by the players.
	private HashMultimap<String, String> receivedAchievementsCache;
	private HashMultimap<String, String> notReceivedAchievementsCache;

	// Map corresponding to the total amount of achievements received by each player.
	private Map<String, Integer> totalPlayerAchievementsCache;

	public DatabasePoolsManager(AdvancedAchievements plugin) {
		this.plugin = plugin;
		normalAchievementsToPlayerStatistics = new EnumMap<>(NormalAchievements.class);
		multipleAchievementsToPlayerStatistics = new EnumMap<>(MultipleAchievements.class);
	}

	public void databasePoolsInit() {
		receivedAchievementsCache = HashMultimap.create();
		notReceivedAchievementsCache = HashMultimap.create();

		// ConcurrentHashMaps are necessary to guarantee thread safety.
		for (NormalAchievements normalAchievement : NormalAchievements.values()) {
			normalAchievementsToPlayerStatistics.put(normalAchievement, new ConcurrentHashMap<String, Long>());
		}
		for (MultipleAchievements multipleAchievement : MultipleAchievements.values()) {
			multipleAchievementsToPlayerStatistics.put(multipleAchievement, new ConcurrentHashMap<String, Long>());
		}
		totalPlayerAchievementsCache = new ConcurrentHashMap<String, Integer>();
	}

	/**
	 * Retrieves a HashMap for a NormalAchievement based on the category.
	 * 
	 * @param category
	 * @return
	 */
	public Map<String, Long> getHashMap(NormalAchievements category) {
		if (category == NormalAchievements.CONNECTIONS) {
			throw new IllegalArgumentException("Connections does not have a corresponding HashMap.");
		} else {
			return normalAchievementsToPlayerStatistics.get(category);
		}
	}

	/**
	 * Retrieves a HashMap for a MultipleAchievement based on the category.
	 * 
	 * @param category
	 * @return
	 */
	public Map<String, Long> getHashMap(MultipleAchievements category) {
		return multipleAchievementsToPlayerStatistics.get(category);
	}

	/**
	 * Increases the statistic for a NormalAchievement by the given value and returns the updated statistic. Calls the
	 * database if not found in the pools.
	 * 
	 * @param category
	 * @param player
	 * @param value
	 * @return
	 */
	public long getAndIncrementStatisticAmount(NormalAchievements category, UUID player, int value) {
		Map<String, Long> categoryHashMap = getHashMap(category);
		String uuid = player.toString();
		Long oldAmount = categoryHashMap.get(uuid);
		if (oldAmount == null) {
			oldAmount = plugin.getDatabaseManager().getNormalAchievementAmount(player, category);
		}
		Long newValue = oldAmount + value;

		categoryHashMap.put(uuid, newValue);
		return newValue;
	}

	/**
	 * Increases the statistic for a MultipleAchievement by the given value and returns the updated statistic. Calls the
	 * database if not found in the pools.
	 * 
	 * @param category
	 * @param subcategory
	 * @param player
	 * @param value
	 * @return
	 */
	public long getAndIncrementStatisticAmount(MultipleAchievements category, String subcategory, UUID player,
			int value) {
		Map<String, Long> categoryHashMap = getHashMap(category);
		String uuid = player.toString();
		String subcategoryDBName;
		if (category == MultipleAchievements.PLAYERCOMMANDS) {
			subcategoryDBName = StringUtils.replace(subcategory, " ", "");
		} else {
			subcategoryDBName = subcategory;
		}
		Long oldAmount = categoryHashMap.get(uuid + subcategoryDBName);
		if (oldAmount == null) {
			oldAmount = plugin.getDatabaseManager().getMultipleAchievementAmount(player, category, subcategoryDBName);
		}
		Long newValue = oldAmount + value;

		categoryHashMap.put(uuid + subcategoryDBName, newValue);
		return newValue;
	}

	/**
	 * Returns whether player has received a specific achievement.
	 * 
	 * @param player
	 * @param name
	 * @return true if achievement received by player, false otherwise
	 */
	public boolean hasPlayerAchievement(UUID player, String name) {
		if (receivedAchievementsCache.containsEntry(player.toString(), name)) {
			return true;
		}
		if (notReceivedAchievementsCache.containsEntry(player.toString(), name)) {
			return false;
		}

		boolean received = plugin.getDatabaseManager().hasPlayerAchievement(player, name);
		if (received) {
			receivedAchievementsCache.put(player.toString(), name);
		} else {
			notReceivedAchievementsCache.put(player.toString(), name);
		}
		return received;
	}

	/**
	 * Returns the total number of achievements received by a player. Can be called asynchronously by BungeeTabListPlus,
	 * method must therefore be synchronized to avoid race conditions if a player calls /aach stats at the same time.
	 * 
	 * @param player
	 * @return
	 */
	public synchronized int getPlayerTotalAchievements(UUID player) {
		Integer totalAchievements = totalPlayerAchievementsCache.get(player.toString());
		if (totalAchievements == null) {
			totalAchievements = plugin.getDatabaseManager().getPlayerAchievementsAmount(player);
			totalPlayerAchievementsCache.put(player.toString(), totalAchievements);
		}
		return totalAchievements;
	}

	public HashMultimap<String, String> getReceivedAchievementsCache() {
		return receivedAchievementsCache;
	}

	public HashMultimap<String, String> getNotReceivedAchievementsCache() {
		return notReceivedAchievementsCache;
	}

	public Map<String, Integer> getTotalPlayerAchievementsCache() {
		return totalPlayerAchievementsCache;
	}
}
