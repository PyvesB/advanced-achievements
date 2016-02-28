package com.hm.achievement.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.hm.achievement.AdvancedAchievements;

public class PooledRequestsSenderAsync implements Runnable {

	private AdvancedAchievements plugin;

	public PooledRequestsSenderAsync(AdvancedAchievements plugin, boolean init) {

		if (init == true)
			DatabasePools.databasePoolsInit(plugin.isAsyncPooledRequestsSender());
		this.plugin = plugin;
	}

	@Override
	public void run() {

		sendRequests();

	}

	/**
	 * Sends requests to the database to deal with regular events and prevent
	 * plugin from hitting server performance. Non event related categories
	 * (distances and play times) are not handled by pools.
	 * 
	 * Queries must not be batched because of race conditions; a database entry
	 * must first be updated, and if cached value in HashMap has not changed (by
	 * a listener running on main thread), it can be removed from the HashMap.
	 * Any thread doing a subsequent read in the database will retrieve the
	 * correct value.
	 * 
	 * Enumerations return elements reflecting the state of the hash table at
	 * some point at or since the creation of the enumeration, and therefore the
	 * (atomic) remove method can be used within them.
	 * 
	 * The cast operations are necessary to ensure compatibility with Java
	 * versions prior to Java 8 (Map interface did not support remove(key,
	 * value) before then).
	 */
	public void sendRequests() {

		try {
			Connection conn = plugin.getDb().getSQLConnection();
			Statement st = conn.createStatement();

			for (Entry<String, Integer> entry : DatabasePools.getDeathHashMap().entrySet()) {
				st.execute("REPLACE INTO `deaths` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) DatabasePools.getDeathHashMap()).remove(entry.getKey(),
						entry.getValue());
			}

			for (Entry<String, Integer> entry : DatabasePools.getArrowHashMap().entrySet()) {
				st.execute("REPLACE INTO `arrows` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) DatabasePools.getArrowHashMap()).remove(entry.getKey(),
						entry.getValue());
			}

			for (Entry<String, Integer> entry : DatabasePools.getSnowballHashMap().entrySet()) {
				st.execute("REPLACE INTO `snowballs` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) DatabasePools.getSnowballHashMap()).remove(entry.getKey(),
						entry.getValue());
			}

			for (Entry<String, Integer> entry : DatabasePools.getEggHashMap().entrySet()) {
				st.execute("REPLACE INTO `eggs` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) DatabasePools.getEggHashMap()).remove(entry.getKey(),
						entry.getValue());
			}

			for (Entry<String, Integer> entry : DatabasePools.getFishHashMap().entrySet()) {
				st.execute("REPLACE INTO `fish` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) DatabasePools.getFishHashMap()).remove(entry.getKey(),
						entry.getValue());
			}

			for (Entry<String, Integer> entry : DatabasePools.getItemBreakHashMap().entrySet()) {
				st.execute("REPLACE INTO `itembreaks` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) DatabasePools.getItemBreakHashMap()).remove(entry.getKey(),
						entry.getValue());
			}

			for (Entry<String, Integer> entry : DatabasePools.getEatenItemsHashMap().entrySet()) {
				st.execute("REPLACE INTO `eatenitems` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) DatabasePools.getEatenItemsHashMap()).remove(entry.getKey(),
						entry.getValue());
			}

			for (Entry<String, Integer> entry : DatabasePools.getShearHashMap().entrySet()) {
				st.execute("REPLACE INTO `shears` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) DatabasePools.getShearHashMap()).remove(entry.getKey(),
						entry.getValue());
			}

			for (Entry<String, Integer> entry : DatabasePools.getMilkHashMap().entrySet()) {
				st.execute("REPLACE INTO `milks` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) DatabasePools.getMilkHashMap()).remove(entry.getKey(),
						entry.getValue());
			}

			for (Entry<String, Integer> entry : DatabasePools.getTradeHashMap().entrySet()) {
				st.execute("REPLACE INTO `trades` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) DatabasePools.getTradeHashMap()).remove(entry.getKey(),
						entry.getValue());
			}

			for (Entry<String, Integer> entry : DatabasePools.getAnvilHashMap().entrySet()) {
				st.execute("REPLACE INTO `anvils` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) DatabasePools.getAnvilHashMap()).remove(entry.getKey(),
						entry.getValue());
			}

			for (Entry<String, Integer> entry : DatabasePools.getEnchantmentHashMap().entrySet()) {
				st.execute("REPLACE INTO `enchantments` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) DatabasePools.getEnchantmentHashMap()).remove(entry.getKey(),
						entry.getValue());
			}

			for (Entry<String, Integer> entry : DatabasePools.getBedHashMap().entrySet()) {
				st.execute("REPLACE INTO `beds` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) DatabasePools.getBedHashMap()).remove(entry.getKey(),
						entry.getValue());
			}

			for (Entry<String, Integer> entry : DatabasePools.getXpHashMap().entrySet()) {
				st.execute("REPLACE INTO `levels` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) DatabasePools.getXpHashMap()).remove(entry.getKey(),
						entry.getValue());
			}

			for (Entry<String, Integer> entry : DatabasePools.getConsumedPotionsHashMap().entrySet()) {
				st.execute(
						"REPLACE INTO `consumedPotions` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) DatabasePools.getConsumedPotionsHashMap()).remove(entry.getKey(),
						entry.getValue());
			}

			for (Entry<String, Integer> entry : DatabasePools.getDropHashMap().entrySet()) {
				st.execute("REPLACE INTO `drops` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) DatabasePools.getDropHashMap()).remove(entry.getKey(),
						entry.getValue());
			}

			for (Entry<String, Integer> entry : DatabasePools.getHoePlowingHashMap().entrySet()) {
				st.execute("REPLACE INTO `hoeplowing` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) DatabasePools.getHoePlowingHashMap()).remove(entry.getKey(),
						entry.getValue());
			}

			for (Entry<String, Integer> entry : DatabasePools.getFertiliseHashMap().entrySet()) {
				st.execute("REPLACE INTO `fertilising` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) DatabasePools.getFertiliseHashMap()).remove(entry.getKey(),
						entry.getValue());
			}

			for (Entry<String, Integer> entry : DatabasePools.getTameHashMap().entrySet()) {
				st.execute("REPLACE INTO `tames` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) DatabasePools.getTameHashMap()).remove(entry.getKey(),
						entry.getValue());
			}

			for (Entry<String, Integer> entry : DatabasePools.getBrewingHashMap().entrySet()) {
				st.execute("REPLACE INTO `brewing` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) DatabasePools.getBrewingHashMap()).remove(entry.getKey(),
						entry.getValue());
			}

			for (Entry<String, Integer> entry : DatabasePools.getFireworkHashMap().entrySet()) {
				st.execute("REPLACE INTO `fireworks` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) DatabasePools.getFireworkHashMap()).remove(entry.getKey(),
						entry.getValue());
			}

			for (Entry<String, Integer> entry : DatabasePools.getBlockPlaceHashMap().entrySet()) {
				st.execute("REPLACE INTO `places` VALUES ('" + entry.getKey().substring(0, 36) + "',"
						+ entry.getKey().substring(36) + ", " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) DatabasePools.getBlockPlaceHashMap()).remove(entry.getKey(),
						entry.getValue());
			}

			for (Entry<String, Integer> entry : DatabasePools.getBlockBreakHashMap().entrySet()) {
				st.execute("REPLACE INTO `breaks` VALUES ('" + entry.getKey().substring(0, 36) + "',"
						+ entry.getKey().substring(36) + ", " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) DatabasePools.getBlockBreakHashMap()).remove(entry.getKey(),
						entry.getValue());
			}

			for (Entry<String, Integer> entry : DatabasePools.getKillHashMap().entrySet()) {
				st.execute("REPLACE INTO `kills` VALUES ('" + entry.getKey().substring(0, 36) + "', '"
						+ entry.getKey().substring(36) + "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) DatabasePools.getKillHashMap()).remove(entry.getKey(),
						entry.getValue());
			}

			for (Entry<String, Integer> entry : DatabasePools.getCraftHashMap().entrySet()) {
				st.execute("REPLACE INTO `crafts` VALUES ('" + entry.getKey().substring(0, 36) + "', '"
						+ entry.getKey().substring(36) + "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) DatabasePools.getCraftHashMap()).remove(entry.getKey(),
						entry.getValue());
			}

			st.close();

		} catch (SQLException e) {

			plugin.getLogger().severe("Error while sending async pooled requests to database: " + e);
			e.printStackTrace();
		}

	}
}
