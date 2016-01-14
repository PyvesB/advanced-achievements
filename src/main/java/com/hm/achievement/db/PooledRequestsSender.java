package com.hm.achievement.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map.Entry;

import com.hm.achievement.AdvancedAchievements;

public class PooledRequestsSender implements Runnable {

	private AdvancedAchievements plugin;

	public PooledRequestsSender(AdvancedAchievements plugin, boolean init) {

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
				st.addBatch("REPLACE INTO `arrows` (playername, arrows) VALUES ('" + entry.getKey() + "', "
						+ entry.getValue() + ")");

			for (Entry<String, Integer> entry : DatabasePools.getShearHashMap().entrySet())
				st.addBatch("REPLACE INTO `shears` (playername, shears) VALUES ('" + entry.getKey() + "', "
						+ entry.getValue() + ")");

			for (Entry<String, Integer> entry : DatabasePools.getEatenItemsHashMap().entrySet())
				st.addBatch("REPLACE INTO `eatenitems` (playername, eatenitems) VALUES ('" + entry.getKey() + "', "
						+ entry.getValue() + ")");

			for (Entry<String, Integer> entry : DatabasePools.getSnowballHashMap().entrySet())
				st.addBatch("REPLACE INTO `snowballs` (playername, snowballs) VALUES ('" + entry.getKey() + "', "
						+ entry.getValue() + ")");

			for (Entry<String, Integer> entry : DatabasePools.getEggHashMap().entrySet())
				st.addBatch("REPLACE INTO `eggs` (playername, eggs) VALUES ('" + entry.getKey() + "', "
						+ entry.getValue() + ")");

			for (Entry<String, Integer> entry : DatabasePools.getDropHashMap().entrySet())
				st.addBatch("REPLACE INTO `drops` (playername, drops) VALUES ('" + entry.getKey() + "', "
						+ entry.getValue() + ")");

			for (Entry<String, Integer> entry : DatabasePools.getHoePlowingHashMap().entrySet())
				st.addBatch("REPLACE INTO `hoeplowing` (playername, hoeplowing) VALUES ('" + entry.getKey() + "', "
						+ entry.getValue() + ")");

			for (Entry<String, Integer> entry : DatabasePools.getFertiliseHashMap().entrySet())
				st.addBatch("REPLACE INTO `fertilising` (playername, fertilising) VALUES ('" + entry.getKey() + "', "
						+ entry.getValue() + ")");

			for (Entry<String, Integer> entry : DatabasePools.getFireworkHashMap().entrySet())
				st.addBatch("REPLACE INTO `fireworks` (playername, fireworks) VALUES ('" + entry.getKey() + "', "
						+ entry.getValue() + ")");

			for (Entry<String, Integer> entry : DatabasePools.getBlockBreakHashMap().entrySet())
				st.addBatch("REPLACE INTO `breaks` (playername, blockid, breaks) VALUES ('"
						+ entry.getKey().substring(0, 36) + "'," + entry.getKey().substring(36) + ", "
						+ entry.getValue() + ")");

			for (Entry<String, Integer> entry : DatabasePools.getBlockPlaceHashMap().entrySet())
				st.addBatch("REPLACE INTO `places` (playername, blockid, places) VALUES ('"
						+ entry.getKey().substring(0, 36) + "'," + entry.getKey().substring(36) + ", "
						+ entry.getValue() + ")");

			for (Entry<String, Integer> entry : DatabasePools.getEntityDeathHashMap().entrySet())
				st.addBatch(
						"REPLACE INTO `kills` (playername, mobname, kills) VALUES ('" + entry.getKey().substring(0, 36)
								+ "', '" + entry.getKey().substring(36) + "', " + entry.getValue() + ")");

			st.executeBatch();

			st.close();
			conn.close();

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
