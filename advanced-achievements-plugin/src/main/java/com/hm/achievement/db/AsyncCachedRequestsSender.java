package com.hm.achievement.db;

import java.sql.BatchUpdateException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;

/**
 * Class used to write the modified cached statistics to the database.
 * 
 * @author Pyves
 *
 */
public class AsyncCachedRequestsSender implements Runnable {

	private final Logger logger;
	private final CacheManager cacheManager;
	private final AbstractDatabaseManager databaseManager;

	@Inject
	public AsyncCachedRequestsSender(Logger logger, CacheManager cacheManager, AbstractDatabaseManager databaseManager) {
		this.logger = logger;
		this.cacheManager = cacheManager;
		this.databaseManager = databaseManager;
	}

	/**
	 * Writes cached statistics to the database, with batched writes for efficiency purposes. If a failure occurs, the
	 * same queries will be attempted again.
	 */
	@Override
	public void run() {
		sendBatchedRequests();
		cacheManager.cleanStaleCaches();
	}

	/**
	 * Writes cached statistics to the database, with batched writes for efficiency purposes. If a failure occurs, the
	 * same queries will be attempted again.
	 */
	public void sendBatchedRequests() {
		List<String> batchedRequests = new ArrayList<>();
		for (MultipleAchievements category : MultipleAchievements.values()) {
			addRequestsForMultipleCategory(batchedRequests, category);
		}
		for (NormalAchievements category : NormalAchievements.values()) {
			addRequestsForNormalCategory(batchedRequests, category);
		}

		if (!batchedRequests.isEmpty()) {
			((SQLWriteOperation) () -> {
				try (Statement st = databaseManager.getConnection().createStatement()) {
					for (String request : batchedRequests) {
						st.addBatch(request);
					}
					st.executeBatch();
				} catch (BatchUpdateException e) { // Attempt to solve issue #309.
					databaseManager.getConnection().close();
					throw e;
				}
			}).attemptWrites(logger, "batching statistic updates");
		}
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
		Map<SubcategoryUUID, CachedStatistic> categoryMap = cacheManager.getHashMap(category);
		for (Entry<SubcategoryUUID, CachedStatistic> entry : categoryMap.entrySet()) {
			CachedStatistic statistic = entry.getValue();
			if (!statistic.isDatabaseConsistent()) {
				// Set flag before writing to database so that concurrent updates are not wrongly marked as consistent.
				statistic.prepareDatabaseWrite();
				UUID uuid = entry.getKey().getUUID();
				String subcategory = StringUtils.replace(entry.getKey().getSubcategory(), "'", "''");
				if (databaseManager instanceof PostgreSQLDatabaseManager) {
					batchedRequests.add("INSERT INTO " + databaseManager.getPrefix() + category.toDBName() + " VALUES ('"
							+ uuid + "', '" + subcategory + "', " + statistic.getValue() + ") ON CONFLICT (playername, "
							+ category.toSubcategoryDBName() + ") DO UPDATE SET (" + category.toDBName() + ")=("
							+ statistic.getValue() + ")");
				} else {
					batchedRequests.add("REPLACE INTO " + databaseManager.getPrefix() + category.toDBName() + " VALUES ('"
							+ uuid + "', '" + subcategory + "', " + statistic.getValue() + ")");
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
		Map<UUID, CachedStatistic> categoryMap = cacheManager.getHashMap(category);
		for (Entry<UUID, CachedStatistic> entry : categoryMap.entrySet()) {
			CachedStatistic statistic = entry.getValue();
			if (!statistic.isDatabaseConsistent()) {
				// Set flag before writing to database so that concurrent updates are not wrongly marked as consistent.
				statistic.prepareDatabaseWrite();
				UUID uuid = entry.getKey();
				if (databaseManager instanceof PostgreSQLDatabaseManager) {
					batchedRequests.add("INSERT INTO " + databaseManager.getPrefix() + category.toDBName() + " VALUES ('"
							+ uuid + "', " + statistic.getValue() + ") ON CONFLICT (playername) DO UPDATE SET ("
							+ category.toDBName() + ")=(" + statistic.getValue() + ")");
				} else {
					batchedRequests.add("REPLACE INTO " + databaseManager.getPrefix() + category.toDBName() + " VALUES ('"
							+ uuid + "', " + statistic.getValue() + ")");
				}
			}
		}
	}

}
