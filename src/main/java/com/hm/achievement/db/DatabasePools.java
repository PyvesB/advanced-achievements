package com.hm.achievement.db;

import java.util.HashMap;

public class DatabasePools {

	private static HashMap<String, Integer> arrowHashMap;
	private static HashMap<String, Integer> blockBreakHashMap;
	private static HashMap<String, Integer> blockPlaceHashMap;
	private static HashMap<String, Integer> entityDeathHashMap;
	private static HashMap<String, Integer> shearHashMap;
	private static HashMap<String, Integer> snowballHashMap;
	private static HashMap<String, Integer> eggHashMap;
	private static HashMap<String, Integer> dropHashMap;
	private static HashMap<String, Integer> hoePlowingHashMap;
	private static HashMap<String, Integer> fertiliseHashMap;

	public static void databasePoolsInit() {

		arrowHashMap = new HashMap<String, Integer>();
		blockBreakHashMap = new HashMap<String, Integer>();
		blockPlaceHashMap = new HashMap<String, Integer>();
		entityDeathHashMap = new HashMap<String, Integer>();
		shearHashMap = new HashMap<String, Integer>();
		snowballHashMap = new HashMap<String, Integer>();
		eggHashMap = new HashMap<String, Integer>();
		dropHashMap = new HashMap<String, Integer>();
		hoePlowingHashMap = new HashMap<String, Integer>();
		fertiliseHashMap = new HashMap<String, Integer>();
	}

	public static HashMap<String, Integer> getArrowHashMap() {

		return arrowHashMap;
	}

	public static void setArrowHashMap(HashMap<String, Integer> arrowHashMap) {

		DatabasePools.arrowHashMap = arrowHashMap;
	}

	public static HashMap<String, Integer> getBlockBreakHashMap() {

		return blockBreakHashMap;
	}

	public static void setBlockBreakHashMap(HashMap<String, Integer> blockBreakHashMap) {

		DatabasePools.blockBreakHashMap = blockBreakHashMap;
	}

	public static HashMap<String, Integer> getBlockPlaceHashMap() {

		return blockPlaceHashMap;
	}

	public static void setBlockPlaceHashMap(HashMap<String, Integer> blockPlaceHashMap) {

		DatabasePools.blockPlaceHashMap = blockPlaceHashMap;
	}

	public static HashMap<String, Integer> getEntityDeathHashMap() {

		return entityDeathHashMap;
	}

	public static void setEntityDeathHashMap(HashMap<String, Integer> entityDeathHashMap) {

		DatabasePools.entityDeathHashMap = entityDeathHashMap;
	}

	public static HashMap<String, Integer> getShearHashMap() {

		return shearHashMap;
	}

	public static void setShearHashMap(HashMap<String, Integer> shearHashMap) {

		DatabasePools.shearHashMap = shearHashMap;
	}

	public static HashMap<String, Integer> getSnowballHashMap() {

		return snowballHashMap;
	}

	public static void setSnowballHashMap(HashMap<String, Integer> snowballHashMap) {

		DatabasePools.snowballHashMap = snowballHashMap;
	}

	public static HashMap<String, Integer> getEggHashMap() {

		return eggHashMap;
	}

	public static void setEggHashMap(HashMap<String, Integer> eggHashMap) {

		DatabasePools.eggHashMap = eggHashMap;
	}

	public static HashMap<String, Integer> getDropHashMap() {

		return dropHashMap;
	}

	public static void setDropHashMap(HashMap<String, Integer> dropHashMap) {

		DatabasePools.dropHashMap = dropHashMap;
	}

	public static HashMap<String, Integer> getHoePlowingHashMap() {

		return hoePlowingHashMap;
	}

	public static void setHoePlowingHashMap(HashMap<String, Integer> hoePlowingHashMap) {

		DatabasePools.hoePlowingHashMap = hoePlowingHashMap;
	}

	public static HashMap<String, Integer> getFertiliseHashMap() {

		return fertiliseHashMap;
	}

	public static void setFertiliseHashMap(HashMap<String, Integer> fertiliseHashMap) {

		DatabasePools.fertiliseHashMap = fertiliseHashMap;
	}

}
