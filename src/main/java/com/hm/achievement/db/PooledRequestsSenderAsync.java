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
			plugin.getPoolsManager().databasePoolsInit(plugin.isAsyncPooledRequestsSender());
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

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getDeathHashMap().entrySet()) {
				st.execute("REPLACE INTO `deaths` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) plugin.getPoolsManager().getDeathHashMap()).remove(entry.getKey(),
						entry.getValue());
			}

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getArrowHashMap().entrySet()) {
				st.execute("REPLACE INTO `arrows` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) plugin.getPoolsManager().getArrowHashMap()).remove(entry.getKey(),
						entry.getValue());
			}

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getSnowballHashMap().entrySet()) {
				st.execute("REPLACE INTO `snowballs` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) plugin.getPoolsManager().getSnowballHashMap())
						.remove(entry.getKey(), entry.getValue());
			}

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getEggHashMap().entrySet()) {
				st.execute("REPLACE INTO `eggs` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) plugin.getPoolsManager().getEggHashMap()).remove(entry.getKey(),
						entry.getValue());
			}

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getFishHashMap().entrySet()) {
				st.execute("REPLACE INTO `fish` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) plugin.getPoolsManager().getFishHashMap()).remove(entry.getKey(),
						entry.getValue());
			}

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getItemBreakHashMap().entrySet()) {
				st.execute("REPLACE INTO `itembreaks` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) plugin.getPoolsManager().getItemBreakHashMap())
						.remove(entry.getKey(), entry.getValue());
			}

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getEatenItemsHashMap().entrySet()) {
				st.execute("REPLACE INTO `eatenitems` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) plugin.getPoolsManager().getEatenItemsHashMap())
						.remove(entry.getKey(), entry.getValue());
			}

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getShearHashMap().entrySet()) {
				st.execute("REPLACE INTO `shears` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) plugin.getPoolsManager().getShearHashMap()).remove(entry.getKey(),
						entry.getValue());
			}

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getMilkHashMap().entrySet()) {
				st.execute("REPLACE INTO `milks` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) plugin.getPoolsManager().getMilkHashMap()).remove(entry.getKey(),
						entry.getValue());
			}

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getTradeHashMap().entrySet()) {
				st.execute("REPLACE INTO `trades` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) plugin.getPoolsManager().getTradeHashMap()).remove(entry.getKey(),
						entry.getValue());
			}

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getAnvilHashMap().entrySet()) {
				st.execute("REPLACE INTO `anvils` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) plugin.getPoolsManager().getAnvilHashMap()).remove(entry.getKey(),
						entry.getValue());
			}

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getEnchantmentHashMap().entrySet()) {
				st.execute("REPLACE INTO `enchantments` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) plugin.getPoolsManager().getEnchantmentHashMap())
						.remove(entry.getKey(), entry.getValue());
			}

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getBedHashMap().entrySet()) {
				st.execute("REPLACE INTO `beds` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) plugin.getPoolsManager().getBedHashMap()).remove(entry.getKey(),
						entry.getValue());
			}

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getXpHashMap().entrySet()) {
				st.execute("REPLACE INTO `levels` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) plugin.getPoolsManager().getXpHashMap()).remove(entry.getKey(),
						entry.getValue());
			}

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getConsumedPotionsHashMap().entrySet()) {
				st.execute(
						"REPLACE INTO `consumedpotions` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) plugin.getPoolsManager().getConsumedPotionsHashMap())
						.remove(entry.getKey(), entry.getValue());
			}

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getDropHashMap().entrySet()) {
				st.execute("REPLACE INTO `drops` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) plugin.getPoolsManager().getDropHashMap()).remove(entry.getKey(),
						entry.getValue());
			}

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getHoePlowingHashMap().entrySet()) {
				st.execute("REPLACE INTO `hoeplowing` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) plugin.getPoolsManager().getHoePlowingHashMap())
						.remove(entry.getKey(), entry.getValue());
			}

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getFertiliseHashMap().entrySet()) {
				st.execute("REPLACE INTO `fertilising` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) plugin.getPoolsManager().getFertiliseHashMap())
						.remove(entry.getKey(), entry.getValue());
			}

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getTameHashMap().entrySet()) {
				st.execute("REPLACE INTO `tames` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) plugin.getPoolsManager().getTameHashMap()).remove(entry.getKey(),
						entry.getValue());
			}

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getBrewingHashMap().entrySet()) {
				st.execute("REPLACE INTO `brewing` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) plugin.getPoolsManager().getBrewingHashMap())
						.remove(entry.getKey(), entry.getValue());
			}

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getFireworkHashMap().entrySet()) {
				st.execute("REPLACE INTO `fireworks` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) plugin.getPoolsManager().getFireworkHashMap())
						.remove(entry.getKey(), entry.getValue());
			}

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getMusicDiscHashMap().entrySet()) {
				st.execute("REPLACE INTO `musicdiscs` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) plugin.getPoolsManager().getMusicDiscHashMap())
						.remove(entry.getKey(), entry.getValue());
			}

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getEnderPearlHashMap().entrySet()) {
				st.execute("REPLACE INTO `enderpearls` VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) plugin.getPoolsManager().getEnderPearlHashMap())
						.remove(entry.getKey(), entry.getValue());
			}

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getBlockPlaceHashMap().entrySet()) {
				st.execute("REPLACE INTO `places` VALUES ('" + entry.getKey().substring(0, 36) + "', '"
						+ entry.getKey().substring(36) + "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) plugin.getPoolsManager().getBlockPlaceHashMap())
						.remove(entry.getKey(), entry.getValue());
			}

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getBlockBreakHashMap().entrySet()) {
				st.execute("REPLACE INTO `breaks` VALUES ('" + entry.getKey().substring(0, 36) + "', '"
						+ entry.getKey().substring(36) + "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) plugin.getPoolsManager().getBlockBreakHashMap())
						.remove(entry.getKey(), entry.getValue());
			}

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getKillHashMap().entrySet()) {
				st.execute("REPLACE INTO `kills` VALUES ('" + entry.getKey().substring(0, 36) + "', '"
						+ entry.getKey().substring(36) + "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) plugin.getPoolsManager().getKillHashMap()).remove(entry.getKey(),
						entry.getValue());
			}

			for (Entry<String, Integer> entry : plugin.getPoolsManager().getCraftHashMap().entrySet()) {
				st.execute("REPLACE INTO `crafts` VALUES ('" + entry.getKey().substring(0, 36) + "', '"
						+ entry.getKey().substring(36) + "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) plugin.getPoolsManager().getCraftHashMap()).remove(entry.getKey(),
						entry.getValue());
			}

			st.close();

		} catch (SQLException e) {

			plugin.getLogger().severe("Error while sending async pooled requests to database: " + e);
			e.printStackTrace();
		}

	}
}
