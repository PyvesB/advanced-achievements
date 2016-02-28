package com.hm.achievement.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map.Entry;

import com.hm.achievement.AdvancedAchievements;

public class PooledRequestsSenderSync implements Runnable {

	private AdvancedAchievements plugin;

	public PooledRequestsSenderSync(AdvancedAchievements plugin, boolean init) {

		if (init == true)
			DatabasePools.databasePoolsInit(plugin.isAsyncPooledRequestsSender());
		this.plugin = plugin;
	}

	@Override
	public void run() {

		sendRequests();

	}

	/**
	 * Sends a batch of requests to the database to deal with regular events and
	 * prevent plugin from hitting server performance. Non event related
	 * categories (distances and play times) are not handled by pools.
	 * 
	 * Queries are batched for optimisation and HashMaps are cleared to prevent
	 * same writes during next task if statistics did not change.
	 */
	public void sendRequests() {

		try {
			Connection conn = plugin.getDb().getSQLConnection();
			Statement st = conn.createStatement();

			for (Entry<String, Integer> entry : DatabasePools.getDeathHashMap().entrySet())
				st.addBatch("REPLACE INTO `deaths` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");

			for (Entry<String, Integer> entry : DatabasePools.getArrowHashMap().entrySet())
				st.addBatch("REPLACE INTO `arrows` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");

			for (Entry<String, Integer> entry : DatabasePools.getSnowballHashMap().entrySet())
				st.addBatch("REPLACE INTO `snowballs` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");

			for (Entry<String, Integer> entry : DatabasePools.getEggHashMap().entrySet())
				st.addBatch("REPLACE INTO `eggs` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");

			for (Entry<String, Integer> entry : DatabasePools.getFishHashMap().entrySet())
				st.addBatch("REPLACE INTO `fish` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");

			for (Entry<String, Integer> entry : DatabasePools.getItemBreakHashMap().entrySet())
				st.addBatch("REPLACE INTO `itembreaks` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");

			for (Entry<String, Integer> entry : DatabasePools.getEatenItemsHashMap().entrySet())
				st.addBatch("REPLACE INTO `eatenitems` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");

			for (Entry<String, Integer> entry : DatabasePools.getShearHashMap().entrySet())
				st.addBatch("REPLACE INTO `shears` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");

			for (Entry<String, Integer> entry : DatabasePools.getMilkHashMap().entrySet())
				st.addBatch("REPLACE INTO `milks` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");

			for (Entry<String, Integer> entry : DatabasePools.getTradeHashMap().entrySet())
				st.addBatch("REPLACE INTO `trades` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");

			for (Entry<String, Integer> entry : DatabasePools.getAnvilHashMap().entrySet())
				st.addBatch("REPLACE INTO `anvils` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");

			for (Entry<String, Integer> entry : DatabasePools.getEnchantmentHashMap().entrySet())
				st.addBatch("REPLACE INTO `enchantments` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");

			for (Entry<String, Integer> entry : DatabasePools.getBedHashMap().entrySet())
				st.addBatch("REPLACE INTO `beds` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");

			for (Entry<String, Integer> entry : DatabasePools.getXpHashMap().entrySet())
				st.addBatch("REPLACE INTO `levels` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");

			for (Entry<String, Integer> entry : DatabasePools.getConsumedPotionsHashMap().entrySet())
				st.addBatch(
						"REPLACE INTO `consumedPotions` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");

			for (Entry<String, Integer> entry : DatabasePools.getDropHashMap().entrySet())
				st.addBatch("REPLACE INTO `drops` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");

			for (Entry<String, Integer> entry : DatabasePools.getHoePlowingHashMap().entrySet())
				st.addBatch("REPLACE INTO `hoeplowing` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");

			for (Entry<String, Integer> entry : DatabasePools.getFertiliseHashMap().entrySet())
				st.addBatch("REPLACE INTO `fertilising` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");

			for (Entry<String, Integer> entry : DatabasePools.getTameHashMap().entrySet())
				st.addBatch("REPLACE INTO `tames` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");

			for (Entry<String, Integer> entry : DatabasePools.getBrewingHashMap().entrySet())
				st.addBatch("REPLACE INTO `brewing` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");

			for (Entry<String, Integer> entry : DatabasePools.getFireworkHashMap().entrySet())
				st.addBatch("REPLACE INTO `fireworks` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");

			for (Entry<String, Integer> entry : DatabasePools.getBlockPlaceHashMap().entrySet())
				st.addBatch("REPLACE INTO `places` VALUES ('" + entry.getKey().substring(0, 36) + "',"
						+ entry.getKey().substring(36) + ", " + entry.getValue() + ")");

			for (Entry<String, Integer> entry : DatabasePools.getBlockBreakHashMap().entrySet())
				st.addBatch("REPLACE INTO `breaks` VALUES ('" + entry.getKey().substring(0, 36) + "',"
						+ entry.getKey().substring(36) + ", " + entry.getValue() + ")");

			for (Entry<String, Integer> entry : DatabasePools.getKillHashMap().entrySet())
				st.addBatch("REPLACE INTO `kills` VALUES ('" + entry.getKey().substring(0, 36) + "', '"
						+ entry.getKey().substring(36) + "', " + entry.getValue() + ")");

			for (Entry<String, Integer> entry : DatabasePools.getCraftHashMap().entrySet())
				st.addBatch("REPLACE INTO `crafts` VALUES ('" + entry.getKey().substring(0, 36) + "', '"
						+ entry.getKey().substring(36) + "', " + entry.getValue() + ")");

			st.executeBatch();

			st.close();

		} catch (SQLException e) {

			plugin.getLogger().severe("Error while sending sync pooled requests to database: " + e);
			e.printStackTrace();
		}

		// Clear entries in HashMaps.
		DatabasePools.getDeathHashMap().clear();
		DatabasePools.getArrowHashMap().clear();
		DatabasePools.getSnowballHashMap().clear();
		DatabasePools.getEggHashMap().clear();
		DatabasePools.getFishHashMap().clear();
		DatabasePools.getItemBreakHashMap().clear();
		DatabasePools.getEatenItemsHashMap().clear();
		DatabasePools.getShearHashMap().clear();
		DatabasePools.getMilkHashMap().clear();
		DatabasePools.getTradeHashMap().clear();
		DatabasePools.getAnvilHashMap().clear();
		DatabasePools.getEnchantmentHashMap().clear();
		DatabasePools.getBedHashMap().clear();
		DatabasePools.getXpHashMap().clear();
		DatabasePools.getConsumedPotionsHashMap().clear();
		DatabasePools.getDropHashMap().clear();
		DatabasePools.getHoePlowingHashMap().clear();
		DatabasePools.getFertiliseHashMap().clear();
		DatabasePools.getTameHashMap().clear();
		DatabasePools.getBrewingHashMap().clear();
		DatabasePools.getFireworkHashMap().clear();
		DatabasePools.getBlockPlaceHashMap().clear();
		DatabasePools.getBlockBreakHashMap().clear();
		DatabasePools.getKillHashMap().clear();
		DatabasePools.getCraftHashMap().clear();

	}
}
