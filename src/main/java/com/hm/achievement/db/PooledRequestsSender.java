package com.hm.achievement.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

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
	 * catgories (distances and play times) are not handled by pools.
	 */
	public void sendRequests() {

		try {
			Connection conn = plugin.getDb().getSQLConnection();
			Statement st = conn.createStatement();

			for (String player : DatabasePools.getArrowHashMap().keySet())
				st.addBatch("replace into `arrows` (playername, arrows) VALUES ('" + player + "', "
						+ DatabasePools.getArrowHashMap().get(player) + ")");

			for (String player : DatabasePools.getShearHashMap().keySet())
				st.addBatch("replace into `shears` (playername, shears) VALUES ('" + player + "', "
						+ DatabasePools.getShearHashMap().get(player) + ")");

			for (String player : DatabasePools.getSnowballHashMap().keySet())
				st.addBatch("replace into `snowballs` (playername, snowballs) VALUES ('" + player + "', "
						+ DatabasePools.getSnowballHashMap().get(player) + ")");

			for (String player : DatabasePools.getEggHashMap().keySet())
				st.addBatch("replace into `eggs` (playername, eggs) VALUES ('" + player + "', "
						+ DatabasePools.getEggHashMap().get(player) + ")");

			for (String player : DatabasePools.getBlockBreakHashMap().keySet())
				st.addBatch("replace into `breaks` (playername, blockid, breaks) VALUES ('" + player.substring(0, 36)
						+ "'," + player.substring(36) + ", " + DatabasePools.getBlockBreakHashMap().get(player) + ")");

			for (String player : DatabasePools.getBlockPlaceHashMap().keySet())
				st.addBatch("replace into `places` (playername, blockid, places) VALUES ('" + player.substring(0, 36)
						+ "'," + player.substring(36) + ", " + DatabasePools.getBlockPlaceHashMap().get(player) + ")");

			for (String player : DatabasePools.getEntityDeathHashMap().keySet())
				st.addBatch("replace into `kills` (playername, mobname, kills) VALUES ('" + player.substring(0, 36)
						+ "', '" + player.substring(36) + "', " + DatabasePools.getEntityDeathHashMap().get(player)
						+ ")");

			for (String player : DatabasePools.getDropHashMap().keySet())
				st.addBatch("replace into `drops` (playername, drops) VALUES ('" + player + "', "
						+ DatabasePools.getDropHashMap().get(player) + ")");

			for (String player : DatabasePools.getHoePlowingHashMap().keySet())
				st.addBatch("replace into `hoeplowing` (playername, hoeplowing) VALUES ('" + player + "', "
						+ DatabasePools.getHoePlowingHashMap().get(player) + ")");

			for (String player : DatabasePools.getFertiliseHashMap().keySet())
				st.addBatch("replace into `fertilising` (playername, fertilising) VALUES ('" + player + "', "
						+ DatabasePools.getFertiliseHashMap().get(player) + ")");
			for (String player : DatabasePools.getFireworkHashMap().keySet())
				st.addBatch("replace into `fireworks` (playername, fireworks) VALUES ('" + player + "', "
						+ DatabasePools.getFireworkHashMap().get(player) + ")");

			st.executeBatch();

			st.close();
			conn.close();

			// Clear entries in HashMaps for players who have quit the server.
			for (String playerUUID : DatabasePools.getQuitHashSet()) {

				DatabasePools.getEntityDeathHashMap().remove(playerUUID);
				DatabasePools.getBlockPlaceHashMap().remove(playerUUID);
				DatabasePools.getBlockBreakHashMap().remove(playerUUID);
				DatabasePools.getEggHashMap().remove(playerUUID);
				DatabasePools.getSnowballHashMap().remove(playerUUID);
				DatabasePools.getShearHashMap().remove(playerUUID);
				DatabasePools.getArrowHashMap().remove(playerUUID);
				DatabasePools.getDropHashMap().remove(playerUUID);
				DatabasePools.getHoePlowingHashMap().remove(playerUUID);
				DatabasePools.getFertiliseHashMap().remove(playerUUID);
				DatabasePools.getFireworkHashMap().remove(playerUUID);
			}

			DatabasePools.getQuitHashSet().clear();

		} catch (SQLException e) {

			plugin.getLogger().severe("Error while sending pooled requests to database: " + e);
			e.printStackTrace();
		}
	}
}
