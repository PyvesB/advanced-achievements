package com.hm.achievement.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Material;

import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.exception.PluginLoadError;
import com.hm.achievement.utils.MaterialHelper;

/**
 * Class used to update the database schema.
 * 
 * @author Pyves
 *
 */
@Singleton
public class DatabaseUpdater {

	private final Logger logger;
	private final MaterialHelper materialHelper;

	@Inject
	DatabaseUpdater(Logger logger, MaterialHelper materialHelper) {
		this.logger = logger;
		this.materialHelper = materialHelper;
	}

	/**
	 * Update the database table to use 1.13 materials rather than the old 1.12 ones.
	 * 
	 * @param databaseManager
	 */
	public void updateOldMaterialsToNewOnes(AbstractDatabaseManager databaseManager) {
		logger.info("Starting database upgrade to new Minecraft 1.13 material names, please wait...");
		updateOldMaterialsToNewOnes(databaseManager, MultipleAchievements.BREAKS);
		updateOldMaterialsToNewOnes(databaseManager, MultipleAchievements.CRAFTS);
		updateOldMaterialsToNewOnes(databaseManager, MultipleAchievements.PLACES);
		logger.info("Finished database upgrade.");
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
			Connection conn = databaseManager.getSQLConnection();
			try (Statement st = conn.createStatement()) {
				ResultSet rs = conn.getMetaData().getTables(null, null, "achievements", null);
				// If the achievements table still has its default name (ie. no prefix), but a prefix is set in the
				// configuration, do a renaming of all tables.
				if (rs.next()) {
					logger.info("Adding " + databaseManager.getPrefix() + " prefix to database table names, please wait...");
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
	 * @throws PluginLoadError
	 */
	void initialiseTables(AbstractDatabaseManager databaseManager) throws PluginLoadError {
		Connection conn = databaseManager.getSQLConnection();
		try (Statement st = conn.createStatement()) {
			st.addBatch("CREATE TABLE IF NOT EXISTS " + databaseManager.getPrefix()
					+ "achievements (playername char(36),achievement varchar(64),description varchar(128),date TIMESTAMP,PRIMARY KEY (playername, achievement))");

			for (MultipleAchievements category : MultipleAchievements.values()) {
				st.addBatch("CREATE TABLE IF NOT EXISTS " + databaseManager.getPrefix() + category.toDBName()
						+ " (playername char(36)," + category.toSubcategoryDBName() + " varchar(191)," + category.toDBName()
						+ " INT,PRIMARY KEY(playername, " + category.toSubcategoryDBName() + "))");
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
	 * Updates the database achievements table. The table is now using a timestamp type for the date column.
	 * 
	 * @param databaseManager
	 */
	void updateOldDBToTimestamps(AbstractDatabaseManager databaseManager) {
		// SQLite unaffected by this change, H2 support added with timestamp from the start.
		if (databaseManager instanceof AbstractRemoteDatabaseManager) {
			Connection conn = databaseManager.getSQLConnection();
			try (Statement st = conn.createStatement()) {
				ResultSet rs = st.executeQuery("SELECT date FROM " + databaseManager.getPrefix() + "achievements LIMIT 1");
				String type = rs.getMetaData().getColumnTypeName(1);
				// Old column type for versions prior to 5.11.0 was date.
				if ("date".equalsIgnoreCase(type)) {
					logger.info("Updating database table with timestamp datatype for achievements, please wait...");
					String query = databaseManager instanceof MySQLDatabaseManager
							? "ALTER TABLE " + databaseManager.getPrefix() + "achievements MODIFY date TIMESTAMP"
							: "ALTER TABLE " + databaseManager.getPrefix() + "achievements ALTER COLUMN date TYPE TIMESTAMP";
					st.execute(query);
				}
			} catch (SQLException e) {
				logger.log(Level.SEVERE, "Database error while updating old achievements table:", e);
			}
		}
	}

	/**
	 * Increases the size of the sub-category column of MultipleAchievements database tables to accommodate new
	 * parameters such as specificplayer-56c79b19-4500-466c-94ea-514a755fdd09 or grouped sub-categories.
	 * 
	 * @param databaseManager
	 * @param category
	 */
	void updateOldDBColumnSize(AbstractDatabaseManager databaseManager, MultipleAchievements category) {
		// SQLite ignores size for varchar datatype. H2 support was added after this was an issue.
		if (!(databaseManager instanceof AbstractFileDatabaseManager)) {
			Connection conn = databaseManager.getSQLConnection();
			try (Statement st = conn.createStatement()) {
				ResultSet rs = st.executeQuery("SELECT " + category.toSubcategoryDBName() + " FROM "
						+ databaseManager.getPrefix() + category.toDBName() + " LIMIT 1");
				if (rs.getMetaData().getPrecision(1) < 191) {
					logger.info("Updating " + category.toDBName() + " database table with extended column, please wait...");
					// Increase size of table.
					String alterOperation = databaseManager instanceof PostgreSQLDatabaseManager
							? "ALTER COLUMN " + category.toSubcategoryDBName() + " TYPE varchar(191)"
							: "MODIFY " + category.toSubcategoryDBName() + " varchar(191)";
					st.execute("ALTER TABLE " + databaseManager.getPrefix() + category.toDBName() + " " + alterOperation);
				}
			} catch (SQLException e) {
				logger.log(Level.SEVERE, "Database error while updating old " + category.toDBName() + " table:", e);
			}
		}
	}

	/**
	 * Update the database table to use 1.13 materials rather than the old 1.12 ones for a given Multiple category. This
	 * methods performs a best effort upgrade based on the functionality provided in the Bukkit.
	 * 
	 * @param databaseManager
	 * @param category
	 */
	private void updateOldMaterialsToNewOnes(AbstractDatabaseManager databaseManager, MultipleAchievements category) {
		String tableName = databaseManager.getPrefix() + category.toDBName();
		Connection conn = databaseManager.getSQLConnection();
		try (Statement st = conn.createStatement()) {
			// Create new temporary table.
			st.execute("CREATE TABLE tempTable (playername char(36)," + category.toSubcategoryDBName() + " varchar(191),"
					+ tableName + " INT UNSIGNED,PRIMARY KEY(playername, " + category.toSubcategoryDBName() + "))");
			try (PreparedStatement prep = conn.prepareStatement("INSERT INTO tempTable VALUES (?,?,?);")) {
				ResultSet rs = st.executeQuery("SELECT * FROM " + tableName + "");
				List<String> uuids = new ArrayList<>();
				List<String> materialKeys = new ArrayList<>();
				List<Integer> amounts = new ArrayList<>();

				while (rs.next()) {
					uuids.add(rs.getString(1));
					materialKeys.add(convertToNewMaterialKey(rs.getString(2)));
					amounts.add(rs.getInt(3));
				}
				// Prevent from doing any commits before entire transaction is ready.
				conn.setAutoCommit(false);

				// Populate new table with contents of the old one and material strings. Batch the insert requests.
				for (int i = 0; i < uuids.size(); ++i) {
					prep.setString(1, uuids.get(i));
					prep.setString(2, materialKeys.get(i));
					prep.setInt(3, amounts.get(i));
					prep.addBatch();
				}
				prep.executeBatch();
			} finally {
				// Delete old table.
				st.execute("DROP TABLE " + tableName);
				// Rename new table to old one.
				st.execute("ALTER TABLE tempTable RENAME TO " + tableName);
			}
			// Commit entire transaction.
			conn.commit();
			conn.setAutoCommit(true);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Database error while updating old material names to new Minecraft 1.13 ones:", e);
		}
	}

	/**
	 * Converts a material key containing pre-1.13 materials (e.g. "workbench|explosive_minecart") to an equivalent key
	 * with 1.13 materials (e.g. "crafting_table|tnt_minecart").
	 * 
	 * @param oldMaterialKey
	 * @return a key with 1.13 material names.
	 */
	private String convertToNewMaterialKey(String oldMaterialKey) {
		List<String> newMaterials = new ArrayList<>();
		for (String oldMaterial : StringUtils.split(oldMaterialKey, '|')) {
			Optional<Material> material = materialHelper.matchMaterial(StringUtils.substringBefore(oldMaterialKey, ":"),
					"the database (" + oldMaterialKey + ")");
			if (material.isPresent()) {
				String metadata = StringUtils.substringAfter(oldMaterialKey, ":");
				newMaterials.add(material.get().name().toLowerCase() + (metadata.isEmpty() ? "" : ":" + metadata));
			} else {
				newMaterials.add(oldMaterial);
			}
		}
		return StringUtils.join(newMaterials, "|");
	}
}
