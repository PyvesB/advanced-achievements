package com.hm.achievement.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map.Entry;

import com.hm.achievement.AdvancedAchievements;

public class PooledRequestsSenderAsync implements Runnable {

	private AdvancedAchievements plugin;

	public PooledRequestsSenderAsync(AdvancedAchievements plugin, boolean init) {

		if (init == true)
			DatabasePools.databasePoolsInit(plugin.isAsyncPooledRequestsSender());
		this.plugin = plugin;
	}

	public void run() {

		sendRequests();

	}

	/**
	 * Sends a batch of requests to the database to deal with regular events and
	 * prevent plugin from hitting server performance. Non event related
	 * categories (distances and play times) are not handled by pools.
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
	 */
	public void sendRequests() {

		try {
			Connection conn = plugin.getDb().getSQLConnection();
			Statement st = conn.createStatement();

			for (Entry<String, Integer> entry : DatabasePools.getArrowHashMap().entrySet()) {
				st.execute(
						"INSERT OR REPLACE INTO `arrows` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");
				DatabasePools.getArrowHashMap().remove(entry.getKey(), entry.getValue());
			}

			for (Entry<String, Integer> entry : DatabasePools.getShearHashMap().entrySet()) {
				st.execute(
						"INSERT OR REPLACE INTO `shears` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");
				DatabasePools.getShearHashMap().remove(entry.getKey(), entry.getValue());
			}

			for (Entry<String, Integer> entry : DatabasePools.getEatenItemsHashMap().entrySet()) {
				st.execute("INSERT OR REPLACE INTO `eatenitems` VALUES ('" + entry.getKey() + "', "
						+ entry.getValue() + ")");
				DatabasePools.getEatenItemsHashMap().remove(entry.getKey(), entry.getValue());
			}

			for (Entry<String, Integer> entry : DatabasePools.getSnowballHashMap().entrySet()) {
				st.execute("INSERT OR REPLACE INTO `snowballs` VALUES ('" + entry.getKey() + "', "
						+ entry.getValue() + ")");
				DatabasePools.getSnowballHashMap().remove(entry.getKey(), entry.getValue());
			}

			for (Entry<String, Integer> entry : DatabasePools.getEggHashMap().entrySet()) {
				st.execute(
						"INSERT OR REPLACE INTO `eggs` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");
				DatabasePools.getEggHashMap().remove(entry.getKey(), entry.getValue());
			}

			for (Entry<String, Integer> entry : DatabasePools.getDropHashMap().entrySet()) {
				st.execute(
						"INSERT OR REPLACE INTO `drops` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");
				DatabasePools.getDropHashMap().remove(entry.getKey(), entry.getValue());
			}

			for (Entry<String, Integer> entry : DatabasePools.getHoePlowingHashMap().entrySet()) {
				st.execute("INSERT OR REPLACE INTO `hoeplowing` VALUES ('" + entry.getKey() + "', "
						+ entry.getValue() + ")");
				DatabasePools.getHoePlowingHashMap().remove(entry.getKey(), entry.getValue());
			}

			for (Entry<String, Integer> entry : DatabasePools.getFertiliseHashMap().entrySet()) {
				st.execute("INSERT OR REPLACE INTO `fertilising` VALUES ('" + entry.getKey() + "', "
						+ entry.getValue() + ")");
				DatabasePools.getFertiliseHashMap().remove(entry.getKey(), entry.getValue());
			}

			for (Entry<String, Integer> entry : DatabasePools.getFireworkHashMap().entrySet()) {
				st.execute("INSERT OR REPLACE INTO `fireworks` VALUES ('" + entry.getKey() + "', "
						+ entry.getValue() + ")");
				DatabasePools.getFireworkHashMap().remove(entry.getKey(), entry.getValue());
			}

			for (Entry<String, Integer> entry : DatabasePools.getBlockBreakHashMap().entrySet()) {
				st.execute("INSERT OR REPLACE INTO `breaks` VALUES ('" + entry.getKey().substring(0, 36) + "',"
						+ entry.getKey().substring(36) + ", " + entry.getValue() + ")");
				DatabasePools.getBlockBreakHashMap().remove(entry.getKey(), entry.getValue());
			}

			for (Entry<String, Integer> entry : DatabasePools.getBlockPlaceHashMap().entrySet()) {
				st.execute("INSERT OR REPLACE INTO `places` VALUES ('" + entry.getKey().substring(0, 36) + "',"
						+ entry.getKey().substring(36) + ", " + entry.getValue() + ")");
				DatabasePools.getBlockPlaceHashMap().remove(entry.getKey(), entry.getValue());
			}

			for (Entry<String, Integer> entry : DatabasePools.getEntityDeathHashMap().entrySet()) {
				st.execute("INSERT OR REPLACE INTO `kills` VALUES ('" + entry.getKey().substring(0, 36) + "', '"
						+ entry.getKey().substring(36) + "', " + entry.getValue() + ")");
				DatabasePools.getEntityDeathHashMap().remove(entry.getKey(), entry.getValue());
			}

			st.close();

		} catch (SQLException e) {

			plugin.getLogger().severe("Error while sending async pooled requests to database: " + e);
			e.printStackTrace();
		}

	}
}
