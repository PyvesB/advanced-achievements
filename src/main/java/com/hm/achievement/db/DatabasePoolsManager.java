package com.hm.achievement.db;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;

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

	// Statistics of the different players for normal achievements; keys correspond to UUIDs.
	private Map<String, Integer> deathHashMap;
	private Map<String, Integer> arrowHashMap;
	private Map<String, Integer> snowballHashMap;
	private Map<String, Integer> eggHashMap;
	private Map<String, Integer> fishHashMap;
	private Map<String, Integer> itemBreakHashMap;
	private Map<String, Integer> eatenItemHashMap;
	private Map<String, Integer> shearHashMap;
	private Map<String, Integer> milkHashMap;
	private Map<String, Integer> tradeHashMap;
	private Map<String, Integer> anvilHashMap;
	private Map<String, Integer> enchantmentHashMap;
	private Map<String, Integer> bedHashMap;
	private Map<String, Integer> xpHashMap;
	private Map<String, Integer> consumedPotionHashMap;
	private Map<String, Integer> dropHashMap;
	private Map<String, Integer> hoePlowingHashMap;
	private Map<String, Integer> fertiliseHashMap;
	private Map<String, Integer> tameHashMap;
	private Map<String, Integer> brewingHashMap;
	private Map<String, Integer> fireworkHashMap;
	private Map<String, Integer> musicDiscHashMap;
	private Map<String, Integer> enderPearlHashMap;
	private Map<String, Integer> petMasterGiveHashMap;
	private Map<String, Integer> petMasterReceiveHashMap;
	private Map<String, Integer> distanceFootHashMap;
	private Map<String, Integer> distanceHorseHashMap;
	private Map<String, Integer> distancePigHashMap;
	private Map<String, Integer> distanceBoatHashMap;
	private Map<String, Integer> distanceMinecartHashMap;
	private Map<String, Integer> distanceGlidingHashMap;
	private Map<String, Long> playTimeHashMap;

	// Statistics of the different players for multiple achievements; keys correspond to concatenated UUIDs and
	// block/entity identifiers.
	private Map<String, Integer> blockPlaceHashMap;
	private Map<String, Integer> blockBreakHashMap;
	private Map<String, Integer> killHashMap;
	private Map<String, Integer> craftHashMap;

	public DatabasePoolsManager(AdvancedAchievements plugin) {

		this.plugin = plugin;
	}

	public void databasePoolsInit(boolean isAsync) {

		// If asynchronous task is used, ConcurrentHashMaps are necessary to
		// guarantee thread safety. Otherwise normal HashMaps are enough.
		if (isAsync) {
			deathHashMap = new ConcurrentHashMap<>();
			arrowHashMap = new ConcurrentHashMap<>();
			snowballHashMap = new ConcurrentHashMap<>();
			eggHashMap = new ConcurrentHashMap<>();
			fishHashMap = new ConcurrentHashMap<>();
			itemBreakHashMap = new ConcurrentHashMap<>();
			eatenItemHashMap = new ConcurrentHashMap<>();
			shearHashMap = new ConcurrentHashMap<>();
			milkHashMap = new ConcurrentHashMap<>();
			tradeHashMap = new ConcurrentHashMap<>();
			anvilHashMap = new ConcurrentHashMap<>();
			enchantmentHashMap = new ConcurrentHashMap<>();
			bedHashMap = new ConcurrentHashMap<>();
			xpHashMap = new ConcurrentHashMap<>();
			consumedPotionHashMap = new ConcurrentHashMap<>();
			dropHashMap = new ConcurrentHashMap<>();
			hoePlowingHashMap = new ConcurrentHashMap<>();
			fertiliseHashMap = new ConcurrentHashMap<>();
			tameHashMap = new ConcurrentHashMap<>();
			brewingHashMap = new ConcurrentHashMap<>();
			fireworkHashMap = new ConcurrentHashMap<>();
			musicDiscHashMap = new ConcurrentHashMap<>();
			enderPearlHashMap = new ConcurrentHashMap<>();
			petMasterGiveHashMap = new ConcurrentHashMap<>();
			petMasterReceiveHashMap = new ConcurrentHashMap<>();
			blockPlaceHashMap = new ConcurrentHashMap<>();
			blockBreakHashMap = new ConcurrentHashMap<>();
			killHashMap = new ConcurrentHashMap<>();
			craftHashMap = new ConcurrentHashMap<>();
			distanceFootHashMap = new ConcurrentHashMap<>();
			distanceHorseHashMap = new ConcurrentHashMap<>();
			distancePigHashMap = new ConcurrentHashMap<>();
			distanceBoatHashMap = new ConcurrentHashMap<>();
			distanceMinecartHashMap = new ConcurrentHashMap<>();
			distanceGlidingHashMap = new ConcurrentHashMap<>();
			playTimeHashMap = new ConcurrentHashMap<>();
		} else {
			deathHashMap = new HashMap<>();
			arrowHashMap = new HashMap<>();
			snowballHashMap = new HashMap<>();
			eggHashMap = new HashMap<>();
			fishHashMap = new HashMap<>();
			itemBreakHashMap = new HashMap<>();
			eatenItemHashMap = new HashMap<>();
			shearHashMap = new HashMap<>();
			milkHashMap = new HashMap<>();
			tradeHashMap = new HashMap<>();
			anvilHashMap = new HashMap<>();
			enchantmentHashMap = new HashMap<>();
			bedHashMap = new HashMap<>();
			xpHashMap = new HashMap<>();
			consumedPotionHashMap = new HashMap<>();
			dropHashMap = new HashMap<>();
			hoePlowingHashMap = new HashMap<>();
			fertiliseHashMap = new HashMap<>();
			tameHashMap = new HashMap<>();
			brewingHashMap = new HashMap<>();
			fireworkHashMap = new HashMap<>();
			musicDiscHashMap = new HashMap<>();
			enderPearlHashMap = new HashMap<>();
			petMasterGiveHashMap = new HashMap<>();
			petMasterReceiveHashMap = new HashMap<>();
			blockPlaceHashMap = new HashMap<>();
			blockBreakHashMap = new HashMap<>();
			killHashMap = new HashMap<>();
			craftHashMap = new HashMap<>();
			distanceFootHashMap = new HashMap<>();
			distanceHorseHashMap = new HashMap<>();
			distancePigHashMap = new HashMap<>();
			distanceBoatHashMap = new HashMap<>();
			distanceMinecartHashMap = new HashMap<>();
			distanceGlidingHashMap = new HashMap<>();
			playTimeHashMap = new HashMap<>();
		}
	}

	/**
	 * Retrieves a HashMap for a NormalAchievement based on the category.
	 * 
	 * @param category
	 * @return
	 */
	public Map<String, Integer> getHashMap(NormalAchievements category) {

		switch (category) {
			case ANVILS:
				return anvilHashMap;
			case ARROWS:
				return arrowHashMap;
			case BEDS:
				return bedHashMap;
			case BREWING:
				return brewingHashMap;
			case CONNECTIONS:
				throw new IllegalArgumentException("Connections does not have a corresponding HashMap.");
			case CONSUMEDPOTIONS:
				return consumedPotionHashMap;
			case DEATHS:
				return deathHashMap;
			case DISTANCEBOAT:
				return distanceBoatHashMap;
			case DISTANCEFOOT:
				return distanceFootHashMap;
			case DISTANCEGLIDING:
				return distanceGlidingHashMap;
			case DISTANCEHORSE:
				return distanceHorseHashMap;
			case DISTANCEMINECART:
				return distanceMinecartHashMap;
			case DISTANCEPIG:
				return distancePigHashMap;
			case DROPS:
				return dropHashMap;
			case EATENITEMS:
				return eatenItemHashMap;
			case EGGS:
				return eggHashMap;
			case ENCHANTMENTS:
				return enchantmentHashMap;
			case ENDERPEARLS:
				return enderPearlHashMap;
			case FERTILISING:
				return fertiliseHashMap;
			case FIREWORKS:
				return fireworkHashMap;
			case FISH:
				return fishHashMap;
			case HOEPLOWING:
				return hoePlowingHashMap;
			case ITEMBREAKS:
				return itemBreakHashMap;
			case LEVELS:
				return xpHashMap;
			case MILKS:
				return milkHashMap;
			case MUSICDISCS:
				return musicDiscHashMap;
			case PETMASTERGIVE:
				return petMasterGiveHashMap;
			case PETMASTERRECEIVE:
				return petMasterReceiveHashMap;
			case PLAYEDTIME:
				throw new IllegalArgumentException("PlayedTime is handled by a separate function.");
			case SHEARS:
				return shearHashMap;
			case SNOWBALLS:
				return snowballHashMap;
			case TAMES:
				return tameHashMap;
			case TRADES:
				return tradeHashMap;
			default:
				return null;
		}
	}

	/**
	 * Retrieves a HashMap for a MultipleAchievement based on the category.
	 * 
	 * @param category
	 * @return
	 */
	public Map<String, Integer> getHashMap(MultipleAchievements category) {

		switch (category) {
			case BREAKS:
				return blockBreakHashMap;
			case CRAFTS:
				return craftHashMap;
			case KILLS:
				return killHashMap;
			case PLACES:
				return blockPlaceHashMap;
			default:
				return null;
		}
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
	public int getAndIncrementStatisticAmount(NormalAchievements category, Player player, int value) {

		Map<String, Integer> categoryHashMap = getHashMap(category);
		String uuid = player.getUniqueId().toString();
		Integer oldAmount = categoryHashMap.get(uuid);
		if (oldAmount == null) {
			oldAmount = plugin.getDb().getNormalAchievementAmount(player, category);
		}
		Integer newValue = oldAmount + value;

		categoryHashMap.put(uuid, newValue);
		return newValue;
	}

	/**
	 * Returns the statistic for a NormalAchievement. Calls the database if not found in the pools.
	 * 
	 * @param category
	 * @param player
	 * @return
	 */
	public int getStatisticAmount(NormalAchievements category, Player player) {

		Map<String, Integer> categoryHashMap = getHashMap(category);
		String uuid = player.getUniqueId().toString();
		Integer amount = categoryHashMap.get(uuid);
		if (amount == null) {
			return plugin.getDb().getNormalAchievementAmount(player, category);
		}
		return amount;
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
	public int getAndIncrementStatisticAmount(MultipleAchievements category, String subcategory, Player player,
			int value) {

		Map<String, Integer> categoryHashMap = getHashMap(category);
		String uuid = player.getUniqueId().toString();
		Integer oldAmount = categoryHashMap.get(uuid);
		if (oldAmount == null) {
			oldAmount = plugin.getDb().getMultipleAchievementAmount(player, category, subcategory);
		}
		Integer newValue = oldAmount + value;

		categoryHashMap.put(uuid + subcategory, newValue);
		return newValue;
	}

	/**
	 * Returns the statistic for a MultipleAchievement. Calls the database if not found in the pools.
	 * 
	 * @param category
	 * @param subcategory
	 * @param player
	 * @return
	 */
	public int getStatisticAmount(MultipleAchievements category, String subcategory, Player player) {

		Map<String, Integer> categoryHashMap = getHashMap(category);
		String uuid = player.getUniqueId().toString();
		Integer oldAmount = categoryHashMap.get(uuid);
		if (oldAmount == null) {
			oldAmount = plugin.getDb().getMultipleAchievementAmount(player, category, subcategory);
		}
		return oldAmount;
	}

	public Map<String, Long> getPlayedTimeHashMap() {

		return playTimeHashMap;
	}

	/**
	 * Returns the PlayedTime statistic.
	 * 
	 * @param category
	 * @param subcategory
	 * @param player
	 * @return
	 */
	public long getPlayerPlayTimeAmount(Player player) {

		Long amount = playTimeHashMap.get(player.getUniqueId().toString());
		if (amount == null) {
			return plugin.getDb().getPlaytimeAmount(player);
		} else {
			return amount;
		}
	}

}
