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
			plugin.getPoolsManager().databasePoolsInit(plugin.isAsyncPooledRequestsSender());
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

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getDeathHashMap().entrySet())
				st.addBatch("REPLACE INTO `deaths` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getArrowHashMap().entrySet())
				st.addBatch("REPLACE INTO `arrows` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getSnowballHashMap().entrySet())
				st.addBatch("REPLACE INTO `snowballs` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getEggHashMap().entrySet())
				st.addBatch("REPLACE INTO `eggs` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getFishHashMap().entrySet())
				st.addBatch("REPLACE INTO `fish` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getItemBreakHashMap().entrySet())
				st.addBatch("REPLACE INTO `itembreaks` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getEatenItemsHashMap().entrySet())
				st.addBatch("REPLACE INTO `eatenitems` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getShearHashMap().entrySet())
				st.addBatch("REPLACE INTO `shears` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getMilkHashMap().entrySet())
				st.addBatch("REPLACE INTO `milks` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getTradeHashMap().entrySet())
				st.addBatch("REPLACE INTO `trades` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getAnvilHashMap().entrySet())
				st.addBatch("REPLACE INTO `anvils` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getEnchantmentHashMap().entrySet())
				st.addBatch("REPLACE INTO `enchantments` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getBedHashMap().entrySet())
				st.addBatch("REPLACE INTO `beds` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getXpHashMap().entrySet())
				st.addBatch("REPLACE INTO `levels` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getConsumedPotionsHashMap().entrySet())
				st.addBatch(
						"REPLACE INTO `consumedpotions` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getDropHashMap().entrySet())
				st.addBatch("REPLACE INTO `drops` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getHoePlowingHashMap().entrySet())
				st.addBatch("REPLACE INTO `hoeplowing` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getFertiliseHashMap().entrySet())
				st.addBatch("REPLACE INTO `fertilising` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getTameHashMap().entrySet())
				st.addBatch("REPLACE INTO `tames` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getBrewingHashMap().entrySet())
				st.addBatch("REPLACE INTO `brewing` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getFireworkHashMap().entrySet())
				st.addBatch("REPLACE INTO `fireworks` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getBlockPlaceHashMap().entrySet())
				st.addBatch("REPLACE INTO `places` VALUES ('" + entry.getKey().substring(0, 36) + "', '"
						+ entry.getKey().substring(36) + "', " + entry.getValue() + ")");

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getBlockBreakHashMap().entrySet())
				st.addBatch("REPLACE INTO `breaks` VALUES ('" + entry.getKey().substring(0, 36) + "', '"
						+ entry.getKey().substring(36) + "', " + entry.getValue() + ")");

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getKillHashMap().entrySet())
				st.addBatch("REPLACE INTO `kills` VALUES ('" + entry.getKey().substring(0, 36) + "', '"
						+ entry.getKey().substring(36) + "', " + entry.getValue() + ")");

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getCraftHashMap().entrySet())
				st.addBatch("REPLACE INTO `crafts` VALUES ('" + entry.getKey().substring(0, 36) + "', '"
						+ entry.getKey().substring(36) + "', " + entry.getValue() + ")");

			st.executeBatch();

			st.close();

		} catch (SQLException e) {

			plugin.getLogger().severe("Error while sending sync pooled requests to database: " + e);
			e.printStackTrace();
		}

		// Clear entries in HashMaps.
		plugin.getPoolsManager().getDeathHashMap().clear();
		plugin.getPoolsManager().getArrowHashMap().clear();
		plugin.getPoolsManager().getSnowballHashMap().clear();
		plugin.getPoolsManager().getEggHashMap().clear();
		plugin.getPoolsManager().getFishHashMap().clear();
		plugin.getPoolsManager().getItemBreakHashMap().clear();
		plugin.getPoolsManager().getEatenItemsHashMap().clear();
		plugin.getPoolsManager().getShearHashMap().clear();
		plugin.getPoolsManager().getMilkHashMap().clear();
		plugin.getPoolsManager().getTradeHashMap().clear();
		plugin.getPoolsManager().getAnvilHashMap().clear();
		plugin.getPoolsManager().getEnchantmentHashMap().clear();
		plugin.getPoolsManager().getBedHashMap().clear();
		plugin.getPoolsManager().getXpHashMap().clear();
		plugin.getPoolsManager().getConsumedPotionsHashMap().clear();
		plugin.getPoolsManager().getDropHashMap().clear();
		plugin.getPoolsManager().getHoePlowingHashMap().clear();
		plugin.getPoolsManager().getFertiliseHashMap().clear();
		plugin.getPoolsManager().getTameHashMap().clear();
		plugin.getPoolsManager().getBrewingHashMap().clear();
		plugin.getPoolsManager().getFireworkHashMap().clear();
		plugin.getPoolsManager().getBlockPlaceHashMap().clear();
		plugin.getPoolsManager().getBlockBreakHashMap().clear();
		plugin.getPoolsManager().getKillHashMap().clear();
		plugin.getPoolsManager().getCraftHashMap().clear();

	}
}
