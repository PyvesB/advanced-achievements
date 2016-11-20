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

	private final AdvancedAchievements plugin;

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
			for (NormalAchievements category : NormalAchievements.values()) {
				// Distance and PlayedTIme achievements are handled by scheduled runnables and corresponding statistics
				// are only written to the database when player disconnects.
				if (category != NormalAchievements.PLAYEDTIME && category != NormalAchievements.CONNECTIONS
						&& !"DISTANCE".contains(category.name())) {
					performRequestsForNormalCategory(st, category);
				}
			}

			if (plugin.isAsyncPooledRequestsSender()) {
				performRequestsForMultipleCategoriesAsync(st);
			} else {
				performRequestsForMultipleCategoriesSync(st);
				st.executeBatch();
			}
		} catch (SQLException e) {
			plugin.getLogger().log(Level.SEVERE, "Error while sending async pooled requests to database: ", e);
		}
	}

	/**
	 * Deals with multiple achievement categories in async mode.
	 * 
	 * @param st
	 * @throws SQLException
	 */
	private void performRequestsForMultipleCategoriesAsync(Statement st) throws SQLException {

		for (MultipleAchievements category : MultipleAchievements.values()) {
			Map<String, Integer> categoryMap = plugin.getPoolsManager().getHashMap(category);
			for (Entry<String, Integer> entry : categoryMap.entrySet()) {
				if (plugin.getDb().isPostgres()) {
					st.execute("INSERT INTO " + plugin.getDb().getTablePrefix() + category.toDBName() + " VALUES ('"
							+ entry.getKey().substring(0, 36) + "', '" + entry.getKey().substring(36) + "', "
							+ entry.getValue() + ")" + " ON CONFLICT (playername," + category.toSubcategoryDBName()
							+ ") DO UPDATE SET (" + category.toDBName() + ")=(" + entry.getValue() + ")");
				} else {
					st.execute("REPLACE INTO " + plugin.getDb().getTablePrefix() + category.toDBName() + " VALUES ('"
							+ entry.getKey().substring(0, 36) + "', '" + entry.getKey().substring(36) + "', "
							+ entry.getValue() + ")");
				}
				((ConcurrentHashMap<String, Integer>) categoryMap).remove(entry.getKey(), entry.getValue());
			}
		}
	}

	/**
	 * Deals with multiple achievement categories in sync mode.
	 * 
	 * @param st
	 * @throws SQLException
	 */
	private void performRequestsForMultipleCategoriesSync(Statement st) throws SQLException {

		for (MultipleAchievements category : MultipleAchievements.values()) {
			Map<String, Integer> categoryMap = plugin.getPoolsManager().getHashMap(category);
			for (Entry<String, Integer> entry : categoryMap.entrySet()) {
				if (plugin.getDb().isPostgres()) {
					st.addBatch("INSERT INTO " + plugin.getDb().getTablePrefix() + category.toDBName() + " VALUES ('"
							+ entry.getKey().substring(0, 36) + "', '" + entry.getKey().substring(36) + "', "
							+ entry.getValue() + ")" + " ON CONFLICT (playername," + category.toSubcategoryDBName()
							+ ") DO UPDATE SET (" + category.toDBName() + ")=(" + entry.getValue() + ")");
				} else {
					st.addBatch("REPLACE INTO " + plugin.getDb().getTablePrefix() + category.toDBName() + " VALUES ('"
							+ entry.getKey().substring(0, 36) + "', '" + entry.getKey().substring(36) + "', "
							+ entry.getValue() + ")");
				}
			}
			categoryMap.clear();
		}
	}

	/**
	 * Deals with normal achievement categories.
	 * 
	 * @param st
	 * @param map
	 * @param categoryName
	 * @throws SQLException
	 */
	private void performRequestsForNormalCategory(Statement st, NormalAchievements category) throws SQLException {

		Map<String, Integer> categoryMap = plugin.getPoolsManager().getHashMap(category);

		if (plugin.isAsyncPooledRequestsSender()) {
			for (Entry<String, Integer> entry : categoryMap.entrySet()) {
				if (plugin.getDb().isPostgres()) {
					st.execute("INSERT INTO " + plugin.getDb().getTablePrefix() + category.toDBName() + " VALUES ('"
							+ entry.getKey() + "', " + entry.getValue() + ")"
							+ " ON CONFLICT (playername) DO UPDATE SET (" + category.toDBName() + ")=("
							+ entry.getValue() + ")");
				} else {
					st.execute("REPLACE INTO " + plugin.getDb().getTablePrefix() + category.toDBName() + " VALUES ('"
							+ entry.getKey() + "', " + entry.getValue() + ")");
				}
				((ConcurrentHashMap<String, Integer>) categoryMap).remove(entry.getKey(), entry.getValue());
			}
		} else {
			for (Entry<String, Integer> entry : categoryMap.entrySet()) {
				if (plugin.getDb().isPostgres()) {
					st.addBatch("INSERT INTO " + plugin.getDb().getTablePrefix() + category.toDBName() + " VALUES ('"
							+ entry.getKey() + "', " + entry.getValue() + ")"
							+ " ON CONFLICT (playername) DO UPDATE SET (" + category.toDBName() + ")=("
							+ entry.getValue() + ")");
				} else {
					st.addBatch("REPLACE INTO " + plugin.getDb().getTablePrefix() + category.toDBName() + " VALUES ('"
							+ entry.getKey() + "', " + entry.getValue() + ")");
				}
			}
			categoryMap.clear();
		}
	}
}
