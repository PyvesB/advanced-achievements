package com.hm.achievement.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;

/**
 * Class used to send the cached statistics to the database in an asynchronous manner.
 * 
 * @author Pyves
 *
 */
public class PooledRequestsSender implements Runnable {

	private AdvancedAchievements plugin;

	public PooledRequestsSender(AdvancedAchievements plugin) {

		plugin.getPoolsManager().databasePoolsInit(plugin.isAsyncPooledRequestsSender());
		this.plugin = plugin;
	}

	@Override
	public void run() {

		sendRequests();
	}

	/**
	 * Sends requests to the database to deal with regular events and prevent plugin from hitting server performance.
	 * Non event related categories (distances and play times) are not handled by pools. --- In async mode, queries must
	 * not be batched because of race conditions; a database entry must first be updated, and if cached value in HashMap
	 * has not changed (by a listener running on main thread), it can be removed from the HashMap. Any thread doing a
	 * subsequent read in the database will retrieve the correct up-to-date value.
	 * 
	 * Enumerations return elements reflecting the state of the hash table at some point at or since the creation of the
	 * enumeration, and therefore the (atomic) remove method can be used within them.
	 * 
	 * The cast operations are necessary to ensure compatibility with Java versions prior to Java 8 (Map interface did
	 * not support remove(key, value) before then). --- In sync mode, queries are batched for optimisation and HashMaps
	 * are cleared to prevent same writes during next task if statistics did not change. --- PostgreSQL has no REPLACE
	 * operator. We have to use the INSERT ... ON CONFLICT construct, which is available for PostgreSQL 9.5+.
	 */
	public void sendRequests() {

		Connection conn = plugin.getDb().getSQLConnection();
		try (Statement st = conn.createStatement()) {
			performRequestsForNormalCategory(st, plugin.getPoolsManager().getDeathHashMap(),
					NormalAchievements.DEATHS.toDBName());

			performRequestsForNormalCategory(st, plugin.getPoolsManager().getArrowHashMap(),
					NormalAchievements.ARROWS.toDBName());

			performRequestsForNormalCategory(st, plugin.getPoolsManager().getSnowballHashMap(),
					NormalAchievements.SNOWBALLS.toDBName());

			performRequestsForNormalCategory(st, plugin.getPoolsManager().getEggHashMap(),
					NormalAchievements.EGGS.toDBName());

			performRequestsForNormalCategory(st, plugin.getPoolsManager().getFishHashMap(),
					NormalAchievements.FISH.toDBName());

			performRequestsForNormalCategory(st, plugin.getPoolsManager().getItemBreakHashMap(),
					NormalAchievements.ITEMBREAKS.toDBName());

			performRequestsForNormalCategory(st, plugin.getPoolsManager().getEatenItemsHashMap(),
					NormalAchievements.EATENITEMS.toDBName());

			performRequestsForNormalCategory(st, plugin.getPoolsManager().getShearHashMap(),
					NormalAchievements.SHEARS.toDBName());

			performRequestsForNormalCategory(st, plugin.getPoolsManager().getMilkHashMap(),
					NormalAchievements.MILKS.toDBName());

			performRequestsForNormalCategory(st, plugin.getPoolsManager().getTradeHashMap(),
					NormalAchievements.TRADES.toDBName());

			performRequestsForNormalCategory(st, plugin.getPoolsManager().getAnvilHashMap(),
					NormalAchievements.ANVILS.toDBName());

			performRequestsForNormalCategory(st, plugin.getPoolsManager().getEnchantmentHashMap(),
					NormalAchievements.ENCHANTMENTS.toDBName());

			performRequestsForNormalCategory(st, plugin.getPoolsManager().getBedHashMap(),
					NormalAchievements.BEDS.toDBName());

			performRequestsForNormalCategory(st, plugin.getPoolsManager().getXpHashMap(),
					NormalAchievements.LEVELS.toDBName());

			performRequestsForNormalCategory(st, plugin.getPoolsManager().getConsumedPotionsHashMap(),
					NormalAchievements.CONSUMEDPOTIONS.toDBName());

			performRequestsForNormalCategory(st, plugin.getPoolsManager().getDropHashMap(),
					NormalAchievements.DROPS.toDBName());

			performRequestsForNormalCategory(st, plugin.getPoolsManager().getHoePlowingHashMap(),
					NormalAchievements.HOEPLOWING.toDBName());

			performRequestsForNormalCategory(st, plugin.getPoolsManager().getFertiliseHashMap(),
					NormalAchievements.FERTILISING.toDBName());

			performRequestsForNormalCategory(st, plugin.getPoolsManager().getTameHashMap(),
					NormalAchievements.TAMES.toDBName());

			performRequestsForNormalCategory(st, plugin.getPoolsManager().getBrewingHashMap(),
					NormalAchievements.BREWING.toDBName());

			performRequestsForNormalCategory(st, plugin.getPoolsManager().getFireworkHashMap(),
					NormalAchievements.FIREWORKS.toDBName());

			performRequestsForNormalCategory(st, plugin.getPoolsManager().getMusicDiscHashMap(),
					NormalAchievements.MUSICDISCS.toDBName());

			performRequestsForNormalCategory(st, plugin.getPoolsManager().getEnderPearlHashMap(),
					NormalAchievements.ENDERPEARLS.toDBName());

			if (plugin.isAsyncPooledRequestsSender())
				performRequestsForMultipleCategoriesAsync(st);
			else
				performRequestsForMultipleCategoriesSync(st);

			if (!plugin.isAsyncPooledRequestsSender())
				st.executeBatch();
		} catch (SQLException e) {
			plugin.getLogger().log(Level.SEVERE, "Error while sending async pooled requests to database: ", e);
		}

		if (!plugin.isAsyncPooledRequestsSender()) {
			// Clear entries in HashMaps; in async mode, this is done progressivily and must NOT be done at the end as
			// the main thread could have repopulated the maps in the meantime..
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

	/**
	 * Deals with multiple achievement categories in async mode.
	 * 
	 * @param st
	 * @throws SQLException
	 */
	private void performRequestsForMultipleCategoriesAsync(Statement st) throws SQLException {

		for (Entry<String, Integer> entry : plugin.getPoolsManager().getBlockPlaceHashMap().entrySet()) {
			if (plugin.getDb().isPostgres())
				st.execute("INSERT INTO " + plugin.getDb().getTablePrefix() + MultipleAchievements.PLACES.toDBName()
						+ " VALUES ('" + entry.getKey().substring(0, 36) + "', '" + entry.getKey().substring(36) + "', "
						+ entry.getValue() + ")" + " ON CONFLICT (playername,blockid) DO UPDATE SET ("
						+ MultipleAchievements.PLACES.toDBName() + ")=(" + entry.getValue() + ")");
			else
				st.execute("REPLACE INTO " + plugin.getDb().getTablePrefix() + MultipleAchievements.PLACES.toDBName()
						+ " VALUES ('" + entry.getKey().substring(0, 36) + "', '" + entry.getKey().substring(36) + "', "
						+ entry.getValue() + ")");
			((ConcurrentHashMap<String, Integer>) plugin.getPoolsManager().getBlockPlaceHashMap())
					.remove(entry.getKey(), entry.getValue());
		}

		for (Entry<String, Integer> entry : plugin.getPoolsManager().getBlockBreakHashMap().entrySet()) {
			if (plugin.getDb().isPostgres())
				st.execute("INSERT INTO " + plugin.getDb().getTablePrefix() + MultipleAchievements.BREAKS.toDBName()
						+ " VALUES ('" + entry.getKey().substring(0, 36) + "', '" + entry.getKey().substring(36) + "', "
						+ entry.getValue() + ")" + " ON CONFLICT (playername,blockid) DO UPDATE SET ("
						+ MultipleAchievements.BREAKS.toDBName() + ")=(" + entry.getValue() + ")");
			else
				st.execute("REPLACE INTO " + plugin.getDb().getTablePrefix() + MultipleAchievements.BREAKS.toDBName()
						+ " VALUES ('" + entry.getKey().substring(0, 36) + "', '" + entry.getKey().substring(36) + "', "
						+ entry.getValue() + ")");
			((ConcurrentHashMap<String, Integer>) plugin.getPoolsManager().getBlockBreakHashMap())
					.remove(entry.getKey(), entry.getValue());
		}

		for (Entry<String, Integer> entry : plugin.getPoolsManager().getKillHashMap().entrySet()) {
			if (plugin.getDb().isPostgres())
				st.execute("INSERT INTO " + plugin.getDb().getTablePrefix() + MultipleAchievements.KILLS.toDBName()
						+ " VALUES ('" + entry.getKey().substring(0, 36) + "', '" + entry.getKey().substring(36) + "', "
						+ entry.getValue() + ")" + " ON CONFLICT (playername,mobname) DO UPDATE SET ("
						+ MultipleAchievements.KILLS.toDBName() + ")=(" + entry.getValue() + ")");
			else
				st.execute("REPLACE INTO " + plugin.getDb().getTablePrefix() + MultipleAchievements.KILLS.toDBName()
						+ " VALUES ('" + entry.getKey().substring(0, 36) + "', '" + entry.getKey().substring(36) + "', "
						+ entry.getValue() + ")");
			((ConcurrentHashMap<String, Integer>) plugin.getPoolsManager().getKillHashMap()).remove(entry.getKey(),
					entry.getValue());
		}

		for (Entry<String, Integer> entry : plugin.getPoolsManager().getCraftHashMap().entrySet()) {
			if (plugin.getDb().isPostgres())
				st.execute("INSERT INTO " + plugin.getDb().getTablePrefix() + MultipleAchievements.CRAFTS.toDBName()
						+ " VALUES ('" + entry.getKey().substring(0, 36) + "', '" + entry.getKey().substring(36) + "', "
						+ entry.getValue() + ")" + " ON CONFLICT (playername,item) DO UPDATE SET ("
						+ MultipleAchievements.CRAFTS.toDBName() + ")=(" + entry.getValue() + ")");
			else
				st.execute("REPLACE INTO " + plugin.getDb().getTablePrefix() + MultipleAchievements.CRAFTS.toDBName()
						+ " VALUES ('" + entry.getKey().substring(0, 36) + "', '" + entry.getKey().substring(36) + "', "
						+ entry.getValue() + ")");
			((ConcurrentHashMap<String, Integer>) plugin.getPoolsManager().getCraftHashMap()).remove(entry.getKey(),
					entry.getValue());
		}
	}

	/**
	 * Deals with multiple achievement categories in sync mode.
	 * 
	 * @param st
	 * @throws SQLException
	 */
	private void performRequestsForMultipleCategoriesSync(Statement st) throws SQLException {

		for (Entry<String, Integer> entry : plugin.getPoolsManager().getBlockPlaceHashMap().entrySet())
			if (plugin.getDb().isPostgres())
				st.execute("INSERT INTO " + plugin.getDb().getTablePrefix() + MultipleAchievements.PLACES.toDBName()
						+ " VALUES ('" + entry.getKey().substring(0, 36) + "', '" + entry.getKey().substring(36) + "', "
						+ entry.getValue() + ")" + " ON CONFLICT (playername,blockid) DO UPDATE SET ("
						+ MultipleAchievements.PLACES.toDBName() + ")=(" + entry.getValue() + ")");
			else
				st.addBatch("REPLACE INTO " + plugin.getDb().getTablePrefix() + MultipleAchievements.PLACES.toDBName()
						+ " VALUES ('" + entry.getKey().substring(0, 36) + "', '" + entry.getKey().substring(36) + "', "
						+ entry.getValue() + ")");

		for (Entry<String, Integer> entry : plugin.getPoolsManager().getBlockBreakHashMap().entrySet())
			if (plugin.getDb().isPostgres())
				st.execute("INSERT INTO " + plugin.getDb().getTablePrefix() + MultipleAchievements.BREAKS.toDBName()
						+ " VALUES ('" + entry.getKey().substring(0, 36) + "', '" + entry.getKey().substring(36) + "', "
						+ entry.getValue() + ")" + " ON CONFLICT (playername,blockid) DO UPDATE SET ("
						+ MultipleAchievements.BREAKS.toDBName() + ")=(" + entry.getValue() + ")");
			else
				st.addBatch("REPLACE INTO " + plugin.getDb().getTablePrefix() + MultipleAchievements.BREAKS.toDBName()
						+ " VALUES ('" + entry.getKey().substring(0, 36) + "', '" + entry.getKey().substring(36) + "', "
						+ entry.getValue() + ")");

		for (Entry<String, Integer> entry : plugin.getPoolsManager().getKillHashMap().entrySet())
			if (plugin.getDb().isPostgres())
				st.execute("INSERT INTO " + plugin.getDb().getTablePrefix() + MultipleAchievements.KILLS.toDBName()
						+ " VALUES ('" + entry.getKey().substring(0, 36) + "', '" + entry.getKey().substring(36) + "', "
						+ entry.getValue() + ")" + " ON CONFLICT (playername,mobname) DO UPDATE SET ("
						+ MultipleAchievements.KILLS.toDBName() + ")=(" + entry.getValue() + ")");
			else
				st.addBatch("REPLACE INTO " + plugin.getDb().getTablePrefix() + MultipleAchievements.KILLS.toDBName()
						+ " VALUES ('" + entry.getKey().substring(0, 36) + "', '" + entry.getKey().substring(36) + "', "
						+ entry.getValue() + ")");

		for (Entry<String, Integer> entry : plugin.getPoolsManager().getCraftHashMap().entrySet())
			if (plugin.getDb().isPostgres())
				st.execute("INSERT INTO " + plugin.getDb().getTablePrefix() + MultipleAchievements.CRAFTS.toDBName()
						+ " VALUES ('" + entry.getKey().substring(0, 36) + "', '" + entry.getKey().substring(36) + "', "
						+ entry.getValue() + ")" + " ON CONFLICT (playername,item) DO UPDATE SET ("
						+ MultipleAchievements.CRAFTS.toDBName() + ")=(" + entry.getValue() + ")");
			else
				st.addBatch("REPLACE INTO " + plugin.getDb().getTablePrefix() + MultipleAchievements.CRAFTS.toDBName()
						+ " VALUES ('" + entry.getKey().substring(0, 36) + "', '" + entry.getKey().substring(36) + "', "
						+ entry.getValue() + ")");
	}

	/**
	 * Deals with normal achievement categories.
	 * 
	 * @param st
	 * @param map
	 * @param categoryName
	 * @throws SQLException
	 */
	private void performRequestsForNormalCategory(Statement st, Map<String, Integer> map, String categoryName)
			throws SQLException {

		if (plugin.isAsyncPooledRequestsSender()) {
			for (Entry<String, Integer> entry : map.entrySet()) {
				if (plugin.getDb().isPostgres())
					st.execute("INSERT INTO " + plugin.getDb().getTablePrefix() + categoryName + " VALUES ('"
							+ entry.getKey() + "', " + entry.getValue() + ")"
							+ " ON CONFLICT (playername) DO UPDATE SET (" + categoryName + ")=(" + entry.getValue()
							+ ")");
				else
					st.execute("REPLACE INTO " + plugin.getDb().getTablePrefix() + categoryName + " VALUES ('"
							+ entry.getKey() + "', " + entry.getValue() + ")");
				((ConcurrentHashMap<String, Integer>) map).remove(entry.getKey(), entry.getValue());
			}
		} else {
			for (Entry<String, Integer> entry : map.entrySet()) {
				if (plugin.getDb().isPostgres())
					st.addBatch("INSERT INTO " + plugin.getDb().getTablePrefix() + categoryName + " VALUES ('"
							+ entry.getKey() + "', " + entry.getValue() + ")"
							+ " ON CONFLICT (playername) DO UPDATE SET (" + categoryName + ")=(" + entry.getValue()
							+ ")");
				else
					st.addBatch("REPLACE INTO " + plugin.getDb().getTablePrefix() + categoryName + " VALUES ('"
							+ entry.getKey() + "', " + entry.getValue() + ")");
			}
		}
	}
}
