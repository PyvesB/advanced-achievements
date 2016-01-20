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
			DatabasePools.databasePoolsInit();
		this.plugin = plugin;
	}

	public void run() {

		sendRequests();

	}

	/**
	 * Sends a batch of requests to the database to deal with regular events and
	 * prevent plugin from hitting server performance. Non event related
	 * categories (distances and play times) are not handled by pools.
	 */
	public void sendRequests() {

		try {
			Connection conn = plugin.getDb().getSQLConnection();
			Statement st = conn.createStatement();

			for (Entry<String, Integer> entry : DatabasePools.getArrowHashMap().entrySet())
				st.addBatch(
						"INSERT OR REPLACE INTO `arrows` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");

			for (Entry<String, Integer> entry : DatabasePools.getShearHashMap().entrySet())
				st.addBatch(
						"INSERT OR REPLACE INTO `shears` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");

			for (Entry<String, Integer> entry : DatabasePools.getEatenItemsHashMap().entrySet())
				st.addBatch("INSERT OR REPLACE INTO `eatenitems` VALUES ('" + entry.getKey() + "', " + entry.getValue()
						+ ")");

			for (Entry<String, Integer> entry : DatabasePools.getSnowballHashMap().entrySet())
				st.addBatch("INSERT OR REPLACE INTO `snowballs` VALUES ('" + entry.getKey() + "', " + entry.getValue()
						+ ")");

			for (Entry<String, Integer> entry : DatabasePools.getEggHashMap().entrySet())
				st.addBatch(
						"INSERT OR REPLACE INTO `eggs` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");

			for (Entry<String, Integer> entry : DatabasePools.getDropHashMap().entrySet())
				st.addBatch(
						"INSERT OR REPLACE INTO `drops` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");

			for (Entry<String, Integer> entry : DatabasePools.getHoePlowingHashMap().entrySet())
				st.addBatch("INSERT OR REPLACE INTO `hoeplowing` VALUES ('" + entry.getKey() + "', " + entry.getValue()
						+ ")");

			for (Entry<String, Integer> entry : DatabasePools.getFertiliseHashMap().entrySet())
				st.addBatch("INSERT OR REPLACE INTO `fertilising` VALUES ('" + entry.getKey() + "', " + entry.getValue()
						+ ")");

			for (Entry<String, Integer> entry : DatabasePools.getFireworkHashMap().entrySet())
				st.addBatch("INSERT OR REPLACE INTO `fireworks` VALUES ('" + entry.getKey() + "', " + entry.getValue()
						+ ")");

			for (Entry<String, Integer> entry : DatabasePools.getBlockBreakHashMap().entrySet())
				st.addBatch("INSERT OR REPLACE INTO `breaks` VALUES ('" + entry.getKey().substring(0, 36) + "',"
						+ entry.getKey().substring(36) + ", " + entry.getValue() + ")");

			for (Entry<String, Integer> entry : DatabasePools.getBlockPlaceHashMap().entrySet())
				st.addBatch("INSERT OR REPLACE INTO `places` VALUES ('" + entry.getKey().substring(0, 36) + "',"
						+ entry.getKey().substring(36) + ", " + entry.getValue() + ")");

			for (Entry<String, Integer> entry : DatabasePools.getEntityDeathHashMap().entrySet())
				st.addBatch("INSERT OR REPLACE INTO `kills` VALUES ('" + entry.getKey().substring(0, 36) + "', '"
						+ entry.getKey().substring(36) + "', " + entry.getValue() + ")");

			st.executeBatch();

			st.close();

		} catch (SQLException e) {

			plugin.getLogger().severe("Error while sending pooled requests to database: " + e);
			e.printStackTrace();
		}

		// Clear entries in HashMaps.
		DatabasePools.getEntityDeathHashMap().clear();
		DatabasePools.getBlockPlaceHashMap().clear();
		DatabasePools.getBlockBreakHashMap().clear();
		DatabasePools.getEatenItemsHashMap().clear();
		DatabasePools.getEggHashMap().clear();
		DatabasePools.getSnowballHashMap().clear();
		DatabasePools.getShearHashMap().clear();
		DatabasePools.getArrowHashMap().clear();
		DatabasePools.getDropHashMap().clear();
		DatabasePools.getHoePlowingHashMap().clear();
		DatabasePools.getFertiliseHashMap().clear();
		DatabasePools.getFireworkHashMap().clear();

	}
}
