package com.hm.achievement.db;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;

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

	public DatabasePoolsManager(AdvancedAchievements plugin) {
		this.plugin = plugin;
		normalAchievementsToPlayerStatistics = new EnumMap<>(NormalAchievements.class);
		multipleAchievementsToPlayerStatistics = new EnumMap<>(MultipleAchievements.class);
	}

	public void databasePoolsInit(boolean isAsync) {
		receivedAchievementsCache = HashMultimap.create();
		notReceivedAchievementsCache = HashMultimap.create();

		// If asynchronous task is used, ConcurrentHashMaps are necessary to
		// guarantee thread safety. Otherwise normal HashMaps are enough.
		if (isAsync) {
			for (NormalAchievements normalAchievement : NormalAchievements.values()) {
				normalAchievementsToPlayerStatistics.put(normalAchievement, new ConcurrentHashMap<String, Long>());
			}
			for (MultipleAchievements multipleAchievement : MultipleAchievements.values()) {
				multipleAchievementsToPlayerStatistics.put(multipleAchievement, new ConcurrentHashMap<String, Long>());
			}
		} else {
			for (NormalAchievements normalAchievement : NormalAchievements.values()) {
				normalAchievementsToPlayerStatistics.put(normalAchievement, new HashMap<String, Long>());
			}
			for (MultipleAchievements multipleAchievement : MultipleAchievements.values()) {
				multipleAchievementsToPlayerStatistics.put(multipleAchievement, new HashMap<String, Long>());
			}
		}
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
	protected Map<String, Long> getHashMap(MultipleAchievements category) {
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
	public long getAndIncrementStatisticAmount(NormalAchievements category, Player player, int value) {
		Map<String, Long> categoryHashMap = getHashMap(category);
		String uuid = player.getUniqueId().toString();
		Long oldAmount = categoryHashMap.get(uuid);
		if (oldAmount == null) {
			oldAmount = plugin.getDb().getNormalAchievementAmount(player, category);
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
	public long getAndIncrementStatisticAmount(MultipleAchievements category, String subcategory, Player player,
			int value) {
		Map<String, Long> categoryHashMap = getHashMap(category);
		String uuid = player.getUniqueId().toString();
		String subcategoryDBName;
		if (category == MultipleAchievements.PLAYERCOMMANDS) {
			subcategoryDBName = StringUtils.replace(subcategory, " ", "");
		} else {
			subcategoryDBName = subcategory;
		}
		Long oldAmount = categoryHashMap.get(uuid + subcategoryDBName);
		if (oldAmount == null) {
			oldAmount = plugin.getDb().getMultipleAchievementAmount(player, category, subcategoryDBName);
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
	public boolean hasPlayerAchievement(Player player, String name) {
		if (receivedAchievementsCache.containsEntry(player.getUniqueId().toString(), name)) {
			return true;
		}
		if (notReceivedAchievementsCache.containsEntry(player.getUniqueId().toString(), name)) {
			return false;
		}

		boolean received = plugin.getDb().hasPlayerAchievement(player, name);
		if (received) {
			receivedAchievementsCache.put(player.getUniqueId().toString(), name);
		} else {
			notReceivedAchievementsCache.put(player.getUniqueId().toString(), name);
		}
		return received;
	}

	public HashMultimap<String, String> getReceivedAchievementsCache() {
		return receivedAchievementsCache;
	}

	public HashMultimap<String, String> getNotReceivedAchievementsCache() {
		return notReceivedAchievementsCache;
	}
}
