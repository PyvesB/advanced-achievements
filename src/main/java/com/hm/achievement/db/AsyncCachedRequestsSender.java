package com.hm.achievement.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.bukkit.Bukkit;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;

/**
 * Class used to write the modified cached statistics to the database.
 * 
 * @author Pyves
 *
 */
public class AsyncCachedRequestsSender implements Runnable {

	private final AdvancedAchievements plugin;

	public AsyncCachedRequestsSender(AdvancedAchievements plugin) {
		this.plugin = plugin;
	}

	/**
	 * Writes cached statistics to the database and cleans up the no longer relevant cached statistics.
	 * 
	 */
	@Override
	public void run() {
		sendBatchedRequests();
		cleanUpCaches();
	}

	/**
	 * Writes cached statistics to the database, with batched writes for efficiency purposes. If a failure occurs, the
	 * same queries will be attempted again.
	 * 
	 * @return
	 */
	public void sendBatchedRequests() {
		final List<String> batchedRequests = new ArrayList<>();
		for (MultipleAchievements category : MultipleAchievements.values()) {
			addRequestsForMultipleCategory(batchedRequests, category);
		}
		for (NormalAchievements category : NormalAchievements.values()) {
			addRequestsForNormalCategory(batchedRequests, category);
		}

		if (batchedRequests.isEmpty()) {
			return;
		}

		new SQLWriteOperation() {

			@Override
			protected void performWrite() throws SQLException {
				Connection conn = plugin.getDatabaseManager().getSQLConnection();
				try (Statement st = conn.createStatement()) {
					for (String request : batchedRequests) {
						st.addBatch(request);
					}
					st.executeBatch();
				}
			}
		}.attemptWrites(plugin.getLogger(), "SQL error while batching statistic updates.");
	}

	/**
	 * Adds the database queries to perform for a given Multiple category.
	 * 
	 * PostgreSQL has no REPLACE operator. We have to use the INSERT ... ON CONFLICT construct, which is available for
	 * PostgreSQL 9.5+.
	 * 
	 * @param batchedRequests
	 * @param category
	 */
	private void addRequestsForMultipleCategory(List<String> batchedRequests, MultipleAchievements category) {
		Map<String, CachedStatistic> categoryMap = plugin.getCacheManager().getHashMap(category);
		for (Entry<String, CachedStatistic> entry : categoryMap.entrySet()) {
			if (!entry.getValue().isDatabaseConsistent()) {
				// Set flag before writing to database so that concurrent updates are not wrongly marked as consistent.
				entry.getValue().prepareDatabaseWrite();
				if (plugin.getDatabaseManager().getDatabaseType() == DatabaseType.POSTGRESQL) {
					batchedRequests.add("INSERT INTO " + plugin.getDatabaseManager().getTablePrefix()
							+ category.toDBName() + " VALUES ('" + entry.getKey().substring(0, 36) + "', '"
							+ entry.getKey().substring(36) + "', " + entry.getValue().getValue()
							+ ") ON CONFLICT (playername, " + category.toSubcategoryDBName() + ") DO UPDATE SET ("
							+ category.toDBName() + ")=(" + entry.getValue().getValue() + ")");
				} else {
					batchedRequests.add("REPLACE INTO " + plugin.getDatabaseManager().getTablePrefix()
							+ category.toDBName() + " VALUES ('" + entry.getKey().substring(0, 36) + "', '"
							+ entry.getKey().substring(36) + "', " + entry.getValue().getValue() + ")");
				}
			}
		}
	}

	/**
	 * Adds the database queries to perform for a given Normal category.
	 * 
	 * PostgreSQL has no REPLACE operator. We have to use the INSERT ... ON CONFLICT construct, which is available for
	 * PostgreSQL 9.5+.
	 * 
	 * @param batchedRequests
	 * @param category
	 */
	private void addRequestsForNormalCategory(List<String> batchedRequests, NormalAchievements category) {
		Map<String, CachedStatistic> categoryMap = plugin.getCacheManager().getHashMap(category);
		for (Entry<String, CachedStatistic> entry : categoryMap.entrySet()) {
			if (!entry.getValue().isDatabaseConsistent()) {
				// Set flag before writing to database so that concurrent updates are not wrongly marked as consistent.
				entry.getValue().prepareDatabaseWrite();
				if (plugin.getDatabaseManager().getDatabaseType() == DatabaseType.POSTGRESQL) {
					batchedRequests.add("INSERT INTO " + plugin.getDatabaseManager().getTablePrefix()
							+ category.toDBName() + " VALUES ('" + entry.getKey() + "', " + entry.getValue().getValue()
							+ ") ON CONFLICT (playername) DO UPDATE SET (" + category.toDBName() + ")=("
							+ entry.getValue().getValue() + ")");
				} else {
					batchedRequests
							.add("REPLACE INTO " + plugin.getDatabaseManager().getTablePrefix() + category.toDBName()
									+ " VALUES ('" + entry.getKey() + "', " + entry.getValue().getValue() + ")");
				}
			}
		}
	}

	/**
	 * Removes the cached statistics that have been written to the database and for which the player is no longer
	 * connected.
	 */
	private void cleanUpCaches() {
		for (MultipleAchievements category : MultipleAchievements.values()) {
			Map<String, CachedStatistic> categoryMap = plugin.getCacheManager().getHashMap(category);
			cleanUpCache(categoryMap);
		}
		for (NormalAchievements category : NormalAchievements.values()) {
			Map<String, CachedStatistic> categoryMap = plugin.getCacheManager().getHashMap(category);
			cleanUpCache(categoryMap);
		}
	}

	/**
	 * Performs the aformentioned removals for a given category.
	 * 
	 * @param categoryMap
	 */
	private void cleanUpCache(final Map<String, CachedStatistic> categoryMap) {
		for (final Entry<String, CachedStatistic> entry : categoryMap.entrySet()) {
			if (entry.getValue().didPlayerDisconnect() && entry.getValue().isDatabaseConsistent()) {
				// Player was disconnected at some point in the recent past. Hand over the cleaning to the main server
				// thread.
				Bukkit.getScheduler().callSyncMethod(plugin, new Callable<Void>() {

					@Override
					public Void call() {
						// Check again whether statistic has been written to the database. This is necessary to cover
						// cases where the player may have reconnected in the meantime.
						if (entry.getValue().isDatabaseConsistent()) {
							categoryMap.remove(entry.getKey());
						} else {
							// Get player UUID, which always corresponds to the 36 first characters of the key
							// regardless of the category type.
							UUID player = UUID.fromString(entry.getKey().substring(0, 36));
							if (Bukkit.getPlayer(player) != null) {
								entry.getValue().resetDisconnection();
							}
						}
						return null;
					}
				});
			}
		}
	}
}
