package com.hm.achievement.db;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DatabasePools {

	// Statistics of the different players; keys correspond to UUIDs.
	private static Map<String, Integer> deathHashMap;
	private static Map<String, Integer> arrowHashMap;
	private static Map<String, Integer> snowballHashMap;
	private static Map<String, Integer> eggHashMap;
	private static Map<String, Integer> fishHashMap;
	private static Map<String, Integer> itemBreakHashMap;
	private static Map<String, Integer> eatenItemHashMap;
	private static Map<String, Integer> shearHashMap;
	private static Map<String, Integer> milkHashMap;
	private static Map<String, Integer> tradeHashMap;
	private static Map<String, Integer> anvilHashMap;
	private static Map<String, Integer> enchantmentHashMap;
	private static Map<String, Integer> bedHashMap;
	private static Map<String, Integer> xpHashMap;
	private static Map<String, Integer> consumedPotionHashMap;
	private static Map<String, Integer> dropHashMap;
	private static Map<String, Integer> hoePlowingHashMap;
	private static Map<String, Integer> fertiliseHashMap;
	private static Map<String, Integer> tameHashMap;
	private static Map<String, Integer> brewingHashMap;
	private static Map<String, Integer> fireworkHashMap;
	// Statistics of the different players; keys correspond to UUIDs and block/entity identifiers.
	private static Map<String, Integer> blockPlaceHashMap;
	private static Map<String, Integer> blockBreakHashMap;
	private static Map<String, Integer> killHashMap;
	private static Map<String, Integer> craftHashMap;

	public static void databasePoolsInit(boolean isAsync) {

		// If asynchronous task is used, ConcurrentHashMaps are necessary to
		// guarantee thread safety. Otherwise normal HashMaps are enough.
		if (isAsync) {
			deathHashMap = new ConcurrentHashMap<String, Integer>();
			arrowHashMap = new ConcurrentHashMap<String, Integer>();
			snowballHashMap = new ConcurrentHashMap<String, Integer>();
			eggHashMap = new ConcurrentHashMap<String, Integer>();
			fishHashMap = new ConcurrentHashMap<String, Integer>();
			itemBreakHashMap = new ConcurrentHashMap<String, Integer>();
			eatenItemHashMap = new ConcurrentHashMap<String, Integer>();
			shearHashMap = new ConcurrentHashMap<String, Integer>();
			milkHashMap = new ConcurrentHashMap<String, Integer>();
			tradeHashMap = new ConcurrentHashMap<String, Integer>();
			anvilHashMap = new ConcurrentHashMap<String, Integer>();
			enchantmentHashMap = new ConcurrentHashMap<String, Integer>();
			bedHashMap = new ConcurrentHashMap<String, Integer>();
			xpHashMap = new ConcurrentHashMap<String, Integer>();
			consumedPotionHashMap = new ConcurrentHashMap<String, Integer>();
			dropHashMap = new ConcurrentHashMap<String, Integer>();
			hoePlowingHashMap = new ConcurrentHashMap<String, Integer>();
			fertiliseHashMap = new ConcurrentHashMap<String, Integer>();
			tameHashMap = new ConcurrentHashMap<String, Integer>();
			brewingHashMap = new ConcurrentHashMap<String, Integer>();
			fireworkHashMap = new ConcurrentHashMap<String, Integer>();
			blockPlaceHashMap = new ConcurrentHashMap<String, Integer>();
			blockBreakHashMap = new ConcurrentHashMap<String, Integer>();
			killHashMap = new ConcurrentHashMap<String, Integer>();
			craftHashMap = new ConcurrentHashMap<String, Integer>();
		} else {
			deathHashMap = new HashMap<String, Integer>();
			arrowHashMap = new HashMap<String, Integer>();
			snowballHashMap = new HashMap<String, Integer>();
			eggHashMap = new HashMap<String, Integer>();
			fishHashMap = new HashMap<String, Integer>();
			itemBreakHashMap = new HashMap<String, Integer>();
			eatenItemHashMap = new HashMap<String, Integer>();
			shearHashMap = new HashMap<String, Integer>();
			milkHashMap = new HashMap<String, Integer>();
			tradeHashMap = new HashMap<String, Integer>();
			anvilHashMap = new HashMap<String, Integer>();
			enchantmentHashMap = new HashMap<String, Integer>();
			bedHashMap = new HashMap<String, Integer>();
			xpHashMap = new HashMap<String, Integer>();
			consumedPotionHashMap = new HashMap<String, Integer>();
			dropHashMap = new HashMap<String, Integer>();
			hoePlowingHashMap = new HashMap<String, Integer>();
			fertiliseHashMap = new HashMap<String, Integer>();
			tameHashMap = new HashMap<String, Integer>();
			brewingHashMap = new HashMap<String, Integer>();
			fireworkHashMap = new HashMap<String, Integer>();
			blockPlaceHashMap = new HashMap<String, Integer>();
			blockBreakHashMap = new HashMap<String, Integer>();
			killHashMap = new HashMap<String, Integer>();
			craftHashMap = new HashMap<String, Integer>();
		}
	}

	public static Map<String, Integer> getDeathHashMap() {

		return deathHashMap;
	}

	public static Map<String, Integer> getArrowHashMap() {

		return arrowHashMap;
	}

	public static Map<String, Integer> getSnowballHashMap() {

		return snowballHashMap;
	}

	public static Map<String, Integer> getEggHashMap() {

		return eggHashMap;
	}

	public static Map<String, Integer> getFishHashMap() {

		return fishHashMap;
	}

	public static Map<String, Integer> getItemBreakHashMap() {

		return itemBreakHashMap;
	}

	public static Map<String, Integer> getEatenItemsHashMap() {

		return eatenItemHashMap;
	}

	public static Map<String, Integer> getShearHashMap() {

		return shearHashMap;
	}

	public static Map<String, Integer> getMilkHashMap() {

		return milkHashMap;
	}

	public static Map<String, Integer> getTradeHashMap() {

		return tradeHashMap;
	}

	public static Map<String, Integer> getAnvilHashMap() {

		return anvilHashMap;
	}

	public static Map<String, Integer> getEnchantmentHashMap() {

		return enchantmentHashMap;
	}

	public static Map<String, Integer> getBedHashMap() {

		return bedHashMap;
	}

	public static Map<String, Integer> getXpHashMap() {

		return xpHashMap;
	}

	public static Map<String, Integer> getConsumedPotionsHashMap() {

		return consumedPotionHashMap;
	}

	public static Map<String, Integer> getDropHashMap() {

		return dropHashMap;
	}

	public static Map<String, Integer> getHoePlowingHashMap() {

		return hoePlowingHashMap;
	}

	public static Map<String, Integer> getFertiliseHashMap() {

		return fertiliseHashMap;
	}

	public static Map<String, Integer> getTameHashMap() {

		return tameHashMap;
	}

	public static Map<String, Integer> getBrewingHashMap() {

		return brewingHashMap;
	}

	public static Map<String, Integer> getFireworkHashMap() {

		return fireworkHashMap;
	}
	
	public static Map<String, Integer> getBlockPlaceHashMap() {

		return blockPlaceHashMap;
	}

	public static Map<String, Integer> getBlockBreakHashMap() {

		return blockBreakHashMap;
	}

	public static Map<String, Integer> getKillHashMap() {

		return killHashMap;
	}

	public static Map<String, Integer> getCraftHashMap() {

		return craftHashMap;
	}

}
