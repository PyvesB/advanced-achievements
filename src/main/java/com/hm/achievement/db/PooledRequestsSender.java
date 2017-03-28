package com.hm.achievement.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.bukkit.Bukkit;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;

/**
 * Class used to send the cached statistics to the database.
 * 
 * @author Pyves
 *
 */
public class PooledRequestsSender implements Runnable {

	private final AdvancedAchievements plugin;

	public PooledRequestsSender(AdvancedAchievements plugin) {
		plugin.getPoolsManager().databasePoolsInit();
		this.plugin = plugin;
	}

	/**
	 * Write cached statistics to the database, assuming asynchronous writes.
	 * 
	 * Queries must not be batched because of race conditions. Once the database entry has been updated, if the
	 * in-memory HashMap value has not changed (by a listener running on main thread) and if player has disconnected, it
	 * can be removed from the HashMap. Any thread doing a subsequent read in the database will retrieve the correct
	 * up-to-date value.
	 * 
	 * The ConcurrentHashMap cast operations are necessary to ensure compatibility with Java versions prior to Java 8
	 * (Map interface did not support remove(key, value) before then).
	 */
	@Override
	public void run() {
		for (MultipleAchievements category : MultipleAchievements.values()) {
			performRequestsForMultipleCategory(category);
		}
		for (NormalAchievements category : NormalAchievements.values()) {
			if (category != NormalAchievements.CONNECTIONS) {
				// Connections category not handled by pools.
				performRequestsForNormalCategory(category);
			}
		}
	}

	/**
	 * Write cached statistics to the database, with batched synchronous writes.
	 */
	public void sendBatchedRequests() {
		new SQLWriteOperation() {

			@Override
			protected void performWrite() throws SQLException {
				Connection conn = plugin.getDatabaseManager().getSQLConnection();
				try (Statement st = conn.createStatement()) {
					for (MultipleAchievements category : MultipleAchievements.values()) {
						batchRequestsForMultipleCategory(st, category);
					}
					for (NormalAchievements category : NormalAchievements.values()) {
						// Connections category not handled by pools.
						if (category != NormalAchievements.CONNECTIONS) {
							batchRequestsForNormalCategory(st, category);
						}
					}
					st.executeBatch();
				}
			}
		}.attemptWrites(plugin.getLogger(), "SQL error while batching statistic updates.");
	}

	/**
	 * Hands over the Multiple category statistic update to the DatabaseManager and clears cache if player is offline.
	 * 
	 * @param dbName
	 * @param categoryMap
	 */
	private void performRequestsForMultipleCategory(MultipleAchievements category) {
		Map<String, Long> categoryMap = plugin.getPoolsManager().getHashMap(category);
		for (Entry<String, Long> entry : categoryMap.entrySet()) {
			UUID player = UUID.fromString(entry.getKey().substring(0, 36));
			plugin.getDatabaseManager().updateMultipleStatistic(player, entry.getValue(), category,
					entry.getKey().substring(36));

			if (!isPlayerOnline(player)) {
				// Value will only be removed if it has not changed in the meantime (for instance player has
				// reconnected).
				((ConcurrentHashMap<String, Long>) categoryMap).remove(entry.getKey(), entry.getValue());
			}
		}
	}

	/**
	 * Hands over the Normal category statistic update to the DatabaseManager and clears cache if player is offline.
	 * 
	 * @param dbName
	 * @param categoryMap
	 */
	private void performRequestsForNormalCategory(NormalAchievements category) {
		Map<String, Long> categoryMap = plugin.getPoolsManager().getHashMap(category);
		for (Entry<String, Long> entry : categoryMap.entrySet()) {
			UUID player = UUID.fromString(entry.getKey());
			plugin.getDatabaseManager().updateNormalStatistic(player, entry.getValue(), category);

			if (!isPlayerOnline(player)) {
				// Value will only be removed if it has not changed in the meantime (for instance player has
				// reconnected).
				((ConcurrentHashMap<String, Long>) categoryMap).remove(entry.getKey(), entry.getValue());
			}
		}
	}

	/**
	 * Batches all database writes for a given Multiple category defined by the database table name and its
	 * corresponding pool map.
	 * 
	 * PostgreSQL has no REPLACE operator. We have to use the INSERT ... ON CONFLICT construct, which is available for
	 * PostgreSQL 9.5+.
	 * 
	 * @param st
	 * @param dbName
	 * @param categoryMap
	 */
	private void batchRequestsForMultipleCategory(Statement st, MultipleAchievements category) throws SQLException {
		Map<String, Long> categoryMap = plugin.getPoolsManager().getHashMap(category);
		for (Entry<String, Long> entry : categoryMap.entrySet()) {
			if (plugin.getDatabaseManager().getDatabaseType() == DatabaseType.POSTGRESQL) {
				st.addBatch("INSERT INTO " + plugin.getDatabaseManager().getTablePrefix() + category.toDBName()
						+ " VALUES ('" + entry.getKey().substring(0, 36) + "', '" + entry.getKey().substring(36) + "', "
						+ entry.getValue() + ")" + " ON CONFLICT (playername) DO UPDATE SET (" + category.toDBName()
						+ ")=(" + entry.getValue() + ")");
			} else {
				st.addBatch("REPLACE INTO " + plugin.getDatabaseManager().getTablePrefix() + category.toDBName()
						+ " VALUES ('" + entry.getKey().substring(0, 36) + "', '" + entry.getKey().substring(36) + "', "
						+ entry.getValue() + ")");
			}
		}
	}

	/**
	 * Batches all database writes for a given Normal category defined by the database table name and its corresponding
	 * pool map.
	 * 
	 * PostgreSQL has no REPLACE operator. We have to use the INSERT ... ON CONFLICT construct, which is available for
	 * PostgreSQL 9.5+.
	 * 
	 * @param st
	 * @param dbName
	 * @param categoryMap
	 */
	private void batchRequestsForNormalCategory(Statement st, NormalAchievements category) throws SQLException {
		Map<String, Long> categoryMap = plugin.getPoolsManager().getHashMap(category);
		for (Entry<String, Long> entry : categoryMap.entrySet()) {
			if (plugin.getDatabaseManager().getDatabaseType() == DatabaseType.POSTGRESQL) {
				st.addBatch("INSERT INTO " + plugin.getDatabaseManager().getTablePrefix() + category.toDBName()
						+ " VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")"
						+ " ON CONFLICT (playername) DO UPDATE SET (" + category.toDBName() + ")=(" + entry.getValue()
						+ ")");
			} else {
				st.addBatch("REPLACE INTO " + plugin.getDatabaseManager().getTablePrefix() + category.toDBName()
						+ " VALUES ('" + entry.getKey() + "', " + entry.getValue() + ")");
			}
		}
	}

	/**
	 * Checks whether the player is online by making a call on the server's main thread of execution.
	 * 
	 * @param uuidString
	 * @return
	 */
	private boolean isPlayerOnline(final UUID player) {

		// Called asynchronously, to ensure thread safety we must issue a call on the server's main thread of execution.
		Future<Boolean> onlineCheckFuture = Bukkit.getScheduler().callSyncMethod(plugin, new Callable<Boolean>() {

			@Override
			public Boolean call() {
				return Bukkit.getPlayer(player) != null;
			}
		});

		boolean playerOnline = true;
		try {
			playerOnline = onlineCheckFuture.get();
		} catch (InterruptedException | ExecutionException e) {
			plugin.getLogger().warning("Error while checking whether player is online.");

		}
		return playerOnline;
	}
}
