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
	 * @param size
	 */
	public void updateOldMaterialsToNewOnes(AbstractDatabaseManager databaseManager, int size) {
		logger.info("Starting database upgrade to new Minecraft 1.13 material names, please wait...");
		updateOldMaterialsToNewOnes(databaseManager, MultipleAchievements.BREAKS, size);
		updateOldMaterialsToNewOnes(databaseManager, MultipleAchievements.CRAFTS, size);
		updateOldMaterialsToNewOnes(databaseManager, MultipleAchievements.PLACES, size);
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
			try (ResultSet rs = conn.getMetaData().getTables(null, null, "achievements", null)) {
				// If the achievements table still has its default name (ie. no prefix), but a prefix is set in the
				// configuration, do a renaming of all tables.
				if (rs.next()) {
					logger.info("Adding " + databaseManager.getPrefix() + " prefix to database table names, please wait...");
					try (Statement st = conn.createStatement()) {
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
		Connection conn = databaseManager.getSQLConnection();
		try (Statement st = conn.createStatement()) {
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
		// SQLite ignores size for varchar datatype. H2 support was added after this was an issue.
		if (!(databaseManager instanceof AbstractFileDatabaseManager)) {
			Connection conn = databaseManager.getSQLConnection();
			try (Statement st = conn.createStatement();
					ResultSet rs = st.executeQuery("SELECT " + category.toSubcategoryDBName() + " FROM "
							+ databaseManager.getPrefix() + category.toDBName() + " LIMIT 1")) {
				if (rs.getMetaData().getPrecision(1) < size) {
					logger.info("Updating " + category.toDBName() + " database table with extended column, please wait...");
					// Increase size of table.
					String alterOperation = databaseManager instanceof PostgreSQLDatabaseManager
							? "ALTER COLUMN " + category.toSubcategoryDBName() + " TYPE varchar(" + size + ")"
							: "MODIFY " + category.toSubcategoryDBName() + " varchar(" + size + ")";
					st.execute("ALTER TABLE " + databaseManager.getPrefix() + category.toDBName() + " " + alterOperation);
				}
			} catch (SQLException e) {
				logger.log(Level.SEVERE, "Database error while updating old " + category.toDBName() + " table:", e);
			}
		}
	}

	/**
	 * Removes achievement descriptions from database storage.
	 * 
	 * @param databaseManager
	 */
	void removeAchievementDescriptions(AbstractDatabaseManager databaseManager) {
		Connection conn = databaseManager.getSQLConnection();
		try (ResultSet rs = conn.getMetaData().getColumns(null, null, databaseManager.getPrefix() + "achievements",
				"description")) {
			if (rs.next()) {
				logger.info("Removing descriptions from database storage, please wait...");
				try (Statement st = conn.createStatement()) {
					// SQLite does not support dropping columns: create new table and copy contents over.
					if (databaseManager instanceof SQLiteDatabaseManager) {
						st.execute(
								"CREATE TABLE tempTable (playername char(36),achievement varchar(64),date TIMESTAMP,PRIMARY KEY (playername, achievement))");
						try (PreparedStatement prep = conn.prepareStatement("INSERT INTO tempTable VALUES (?,?,?);");
								ResultSet achievements = st.executeQuery("SELECT * FROM achievements")) {
							while (achievements.next()) {
								prep.setString(1, achievements.getString(1));
								prep.setString(2, achievements.getString(2));
								prep.setTimestamp(3, achievements.getTimestamp(4));
								prep.addBatch();
							}
							prep.executeBatch();
							st.execute("DROP TABLE achievements");
							st.execute("ALTER TABLE tempTable RENAME TO achievements");
						}
					} else {
						st.execute("ALTER TABLE " + databaseManager.getPrefix() + "achievements DROP COLUMN description");
					}
				}
			}
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Database error while removing descriptions:", e);
		}
	}

	/**
	 * Update the database table to use 1.13 materials rather than the old 1.12 ones for a given Multiple category. This
	 * methods performs a best effort upgrade based on the functionality provided in the Bukkit.
	 * 
	 * @param databaseManager
	 * @param category
	 * @param size
	 */
	private void updateOldMaterialsToNewOnes(AbstractDatabaseManager databaseManager, MultipleAchievements category,
			int size) {
		String tableName = databaseManager.getPrefix() + category.toDBName();
		Connection conn = databaseManager.getSQLConnection();
		try (Statement st = conn.createStatement()) {
			// Create new temporary table.
			st.execute("CREATE TABLE tempTable (playername char(36)," + category.toSubcategoryDBName() + " varchar(" + size
					+ ")," + tableName + " INT UNSIGNED,PRIMARY KEY(playername, " + category.toSubcategoryDBName() + "))");
			try (PreparedStatement prep = conn.prepareStatement("INSERT INTO tempTable VALUES (?,?,?);");
					ResultSet rs = st.executeQuery("SELECT * FROM " + tableName + "")) {
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
