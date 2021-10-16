package com.hm.achievement.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;

import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.exception.PluginLoadError;

/**
 * Class used to update the database schema.
 * 
 * @author Pyves
 *
 */
@Singleton
public class DatabaseUpdater {

	private final Logger logger;

	@Inject
	DatabaseUpdater(Logger logger) {
		this.logger = logger;
	}

	/**
	 * Renames the database tables with the prefix given in the configuration file. This method is only used and only
	 * works if the tables had the default name. It does not support multiple successive table renamings.
	 * 
	 * @param databaseManager
	 * @throws PluginLoadError
	 */
	void renameExistingTables(AbstractDatabaseManager databaseManager) throws PluginLoadError {
		// If a prefix is set in the config, check whether the tables with the default names exist. If so do renaming.
		if (StringUtils.isNotBlank(databaseManager.getPrefix())) {
			try (ResultSet rs = databaseManager.getConnection().getMetaData().getTables(null, null, "achievements", null)) {
				// If the achievements table still has its default name (ie. no prefix), but a prefix is set in the
				// configuration, do a renaming of all tables.
				if (rs.next()) {
					logger.info("Adding " + databaseManager.getPrefix() + " prefix to database table names, please wait...");
					try (Statement st = databaseManager.getConnection().createStatement()) {
						st.addBatch("ALTER TABLE achievements RENAME TO " + databaseManager.getPrefix() + "achievements");
						for (NormalAchievements category : NormalAchievements.values()) {
							st.addBatch("ALTER TABLE " + category.toDBName() + " RENAME TO " + databaseManager.getPrefix()
									+ category.toDBName());
						}
						for (MultipleAchievements category : MultipleAchievements.values()) {
							st.addBatch("ALTER TABLE " + category.toDBName() + " RENAME TO " + databaseManager.getPrefix()
									+ category.toDBName());
						}
						st.executeBatch();
					}
				}
			} catch (SQLException e) {
				throw new PluginLoadError("Error while setting prefix of database tables.", e);
			}
		}
	}

	/**
	 * Initialises database tables by creating non existing ones. We batch the requests to send a unique batch to the
	 * database.
	 * 
	 * @param databaseManager
	 * @param size
	 * @throws PluginLoadError
	 */
	void initialiseTables(AbstractDatabaseManager databaseManager, int size) throws PluginLoadError {
		try (Statement st = databaseManager.getConnection().createStatement()) {
			st.addBatch("CREATE TABLE IF NOT EXISTS " + databaseManager.getPrefix()
					+ "achievements (playername char(36),achievement varchar(64),date TIMESTAMP,PRIMARY KEY (playername, achievement))");

			for (MultipleAchievements category : MultipleAchievements.values()) {
				st.addBatch("CREATE TABLE IF NOT EXISTS " + databaseManager.getPrefix() + category.toDBName()
						+ " (playername char(36)," + category.toSubcategoryDBName() + " varchar(" + size + "),"
						+ category.toDBName() + " INT,PRIMARY KEY(playername, " + category.toSubcategoryDBName() + "))");
			}

			for (NormalAchievements category : NormalAchievements.values()) {
				if (category == NormalAchievements.CONNECTIONS) {
					st.addBatch("CREATE TABLE IF NOT EXISTS " + databaseManager.getPrefix() + category.toDBName()
							+ " (playername char(36)," + category.toDBName()
							+ " INT,date varchar(10),PRIMARY KEY (playername))");
				} else {
					st.addBatch("CREATE TABLE IF NOT EXISTS " + databaseManager.getPrefix() + category.toDBName()
							+ " (playername char(36)," + category.toDBName() + " BIGINT,PRIMARY KEY (playername))");
				}
			}
			st.executeBatch();
		} catch (SQLException e) {
			throw new PluginLoadError("Error while initialising database tables.", e);
		}
	}

	/**
	 * Increases the size of the sub-category column of MultipleAchievements database tables to accommodate new
	 * parameters such as specificplayer-56c79b19-4500-466c-94ea-514a755fdd09 or grouped sub-categories.
	 * 
	 * @param databaseManager
	 * @param category
	 * @param size
	 */
	void updateOldDBColumnSize(AbstractDatabaseManager databaseManager, MultipleAchievements category, int size) {
		// SQLite ignores size for varchar datatype.
		if (!(databaseManager instanceof SQLiteDatabaseManager)) {
			try (Statement st = databaseManager.getConnection().createStatement();
					ResultSet rs = st.executeQuery("SELECT " + category.toSubcategoryDBName() + " FROM "
							+ databaseManager.getPrefix() + category.toDBName() + " LIMIT 1")) {
				if (rs.getMetaData().getPrecision(1) < size) {
					logger.info("Changing " + category.toDBName() + " database column size to " + size + ", please wait...");
					String alterOperation = databaseManager instanceof MySQLDatabaseManager
							? "MODIFY " + category.toSubcategoryDBName() + " varchar(" + size + ")"
							: "ALTER COLUMN " + category.toSubcategoryDBName() + " TYPE varchar(" + size + ")";
					st.execute("ALTER TABLE " + databaseManager.getPrefix() + category.toDBName() + " " + alterOperation);
				}
			} catch (SQLException e) {
				logger.log(Level.SEVERE, "Database error while updating old " + category.toDBName() + " table:", e);
			}
		}
	}
}
