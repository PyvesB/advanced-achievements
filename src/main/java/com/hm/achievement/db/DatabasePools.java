package com.hm.achievement.db;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DatabasePools {

	// Statistics of the different players; keys correspond to UUIDs.
	private static Map<String, Integer> arrowHashMap;
	private static Map<String, Integer> blockBreakHashMap;
	private static Map<String, Integer> blockPlaceHashMap;
	private static Map<String, Integer> entityDeathHashMap;
	private static Map<String, Integer> eatenItemsHashMap;
	private static Map<String, Integer> shearHashMap;
	private static Map<String, Integer> snowballHashMap;
	private static Map<String, Integer> eggHashMap;
	private static Map<String, Integer> dropHashMap;
	private static Map<String, Integer> hoePlowingHashMap;
	private static Map<String, Integer> fertiliseHashMap;
	private static Map<String, Integer> fireworkHashMap;

	public static void databasePoolsInit(boolean isAsync) {

		// If asynchronous task is used, ConcurrentHashMaps are necessary to
		// guarantee thread safety. Otherwise normal HashMaps are enough.
		if (isAsync) {
			arrowHashMap = new ConcurrentHashMap<String, Integer>();
			blockBreakHashMap = new ConcurrentHashMap<String, Integer>();
			blockPlaceHashMap = new ConcurrentHashMap<String, Integer>();
			entityDeathHashMap = new ConcurrentHashMap<String, Integer>();
			eatenItemsHashMap = new ConcurrentHashMap<String, Integer>();
			shearHashMap = new ConcurrentHashMap<String, Integer>();
			snowballHashMap = new ConcurrentHashMap<String, Integer>();
			eggHashMap = new ConcurrentHashMap<String, Integer>();
			dropHashMap = new ConcurrentHashMap<String, Integer>();
			hoePlowingHashMap = new ConcurrentHashMap<String, Integer>();
			fertiliseHashMap = new ConcurrentHashMap<String, Integer>();
			fireworkHashMap = new ConcurrentHashMap<String, Integer>();
		} else {
			arrowHashMap = new HashMap<String, Integer>();
			blockBreakHashMap = new HashMap<String, Integer>();
			blockPlaceHashMap = new HashMap<String, Integer>();
			entityDeathHashMap = new HashMap<String, Integer>();
			eatenItemsHashMap = new HashMap<String, Integer>();
			shearHashMap = new HashMap<String, Integer>();
			snowballHashMap = new HashMap<String, Integer>();
			eggHashMap = new HashMap<String, Integer>();
			dropHashMap = new HashMap<String, Integer>();
			hoePlowingHashMap = new HashMap<String, Integer>();
			fertiliseHashMap = new HashMap<String, Integer>();
			fireworkHashMap = new HashMap<String, Integer>();
		}
	}

	public static Map<String, Integer> getArrowHashMap() {

		return arrowHashMap;
	}

	public static Map<String, Integer> getBlockBreakHashMap() {

		return blockBreakHashMap;
	}

	public static Map<String, Integer> getBlockPlaceHashMap() {

		return blockPlaceHashMap;
	}

	public static Map<String, Integer> getEntityDeathHashMap() {

		return entityDeathHashMap;
	}

	public static Map<String, Integer> getEatenItemsHashMap() {

		return eatenItemsHashMap;
	}

	public static void setEatenItemsHashMap(Map<String, Integer> eatenItemsHashMap) {

		DatabasePools.eatenItemsHashMap = eatenItemsHashMap;
	}

	public static Map<String, Integer> getShearHashMap() {

		return shearHashMap;
	}

	public static Map<String, Integer> getSnowballHashMap() {

		return snowballHashMap;
	}

	public static Map<String, Integer> getEggHashMap() {

		return eggHashMap;
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

	public static Map<String, Integer> getFireworkHashMap() {

		return fireworkHashMap;
	}

}
