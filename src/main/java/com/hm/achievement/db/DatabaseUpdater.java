package com.hm.achievement.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Pattern;

import org.bukkit.Material;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;

/**
 * Class used to update the database schema.
 * 
 * @author Pyves
 *
 */
public class DatabaseUpdater {

	private final AdvancedAchievements plugin;
	private final SQLDatabaseManager sqlDatabaseManager;

	protected DatabaseUpdater(AdvancedAchievements plugin, SQLDatabaseManager sqlDatabaseManager) {
		this.plugin = plugin;
		this.sqlDatabaseManager = sqlDatabaseManager;
	}

	/**
	 * Renames the database tables with the prefix given in the configuration file. This method is only used and only
	 * works if the tables had the default name. It does not support multiple successive table renamings.
	 * 
	 * @throws SQLException
	 */
	protected void renameExistingTables(String databaseAddress) {
		// If a prefix is set in the config, check whether the tables with the default names exist. If so do renaming.
		if (!"".equals(sqlDatabaseManager.getTablePrefix())) {
			Connection conn = sqlDatabaseManager.getSQLConnection();
			try (Statement st = conn.createStatement()) {
				ResultSet rs;
				if (sqlDatabaseManager.getDatabaseType() == DatabaseType.SQLITE) {
					rs = st.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='achievements'");
				} else if (sqlDatabaseManager.getDatabaseType() == DatabaseType.MYSQL) {
					rs = st.executeQuery("SELECT table_name FROM information_schema.tables WHERE table_schema='"
							+ databaseAddress.substring(databaseAddress.lastIndexOf('/') + 1)
							+ "' AND table_name ='achievements'");
				} else {
					rs = st.executeQuery(
							"SELECT 1 FROM pg_catalog.pg_class c JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace WHERE n.nspname = 'public' AND c.relname = 'achievements' AND c.relkind = 'r'");
				}

				// Table achievements still has its default name (ie. no prefix), but a prefix is set in the
				// configuration; do a renaming of all tables.
				if (rs.next()) {
					st.addBatch("ALTER TABLE achievements RENAME TO " + sqlDatabaseManager.getTablePrefix()
							+ "achievements");
					for (NormalAchievements category : NormalAchievements.values()) {
						st.addBatch("ALTER TABLE " + category.toDBName() + " RENAME TO "
								+ sqlDatabaseManager.getTablePrefix() + category.toDBName());
					}
					for (MultipleAchievements category : MultipleAchievements.values()) {
						st.addBatch("ALTER TABLE " + category.toDBName() + " RENAME TO "
								+ sqlDatabaseManager.getTablePrefix() + category.toDBName());
					}
					st.executeBatch();
				}
			} catch (SQLException e) {
				plugin.getLogger().log(Level.SEVERE, "Error while attempting to set prefix of database tables: ", e);
				plugin.setSuccessfulLoad(false);
			}
		}
	}

	/**
	 * Initialises database tables by creating non existing ones. We batch the requests to send a unique batch to the
	 * database.
	 * 
	 * @throws SQLException
	 */
	protected void initialiseTables() {
		Connection conn = sqlDatabaseManager.getSQLConnection();
		try (Statement st = conn.createStatement()) {
			st.addBatch("CREATE TABLE IF NOT EXISTS " + sqlDatabaseManager.getTablePrefix()
					+ "achievements (playername char(36),achievement varchar(64),description varchar(128),date DATE,PRIMARY KEY (playername, achievement))");

			for (MultipleAchievements category : MultipleAchievements.values()) {
				st.addBatch("CREATE TABLE IF NOT EXISTS " + sqlDatabaseManager.getTablePrefix() + category.toDBName()
						+ " (playername char(36)," + category.toSubcategoryDBName() + " varchar(51),"
						+ category.toDBName() + " INT,PRIMARY KEY(playername, " + category.toSubcategoryDBName()
						+ "))");
			}

			for (NormalAchievements category : NormalAchievements.values()) {
				if (category == NormalAchievements.CONNECTIONS) {
					st.addBatch("CREATE TABLE IF NOT EXISTS " + sqlDatabaseManager.getTablePrefix()
							+ category.toDBName() + " (playername char(36)," + category.toDBName()
							+ " INT,date varchar(10),PRIMARY KEY (playername))");
				} else {
					st.addBatch("CREATE TABLE IF NOT EXISTS " + sqlDatabaseManager.getTablePrefix()
							+ category.toDBName() + " (playername char(36)," + category.toDBName()
							+ " BIGINT,PRIMARY KEY (playername))");
				}
			}
			st.executeBatch();
		} catch (SQLException e) {
			plugin.getLogger().log(Level.SEVERE, "Error while initialising database tables: ", e);
			plugin.setSuccessfulLoad(false);
		}
	}

	/**
	 * Update the database tables for Breaks, Crafts and Places achievements (from int to varchar for identification
	 * column). The tables are now using material names and no longer item IDs, which are deprecated; this also allows
	 * to store extra data information, extending the number of items available for the user.
	 */
	protected void updateOldDBToMaterial() {
		Connection conn = sqlDatabaseManager.getSQLConnection();
		String type = "";
		try (Statement st = conn.createStatement()) {
			ResultSet rs = st.executeQuery("SELECT blockid FROM " + sqlDatabaseManager.getTablePrefix()
					+ MultipleAchievements.BREAKS.toDBName() + " LIMIT 1");
			type = rs.getMetaData().getColumnTypeName(1);
		} catch (SQLException e) {
			plugin.getLogger().log(Level.SEVERE, "SQL error while trying to update old DB: ", e);
		}

		// Old column type for versions prior to 2.4.1 was integer for SQLite and smallint unsigned for MySQL.
		if ("integer".equalsIgnoreCase(type) || "smallint unsigned".equalsIgnoreCase(type)) {
			plugin.getLogger().warning("Updating database tables with Material names, please wait...");
			updateOldDBToMaterial(MultipleAchievements.BREAKS);
			updateOldDBToMaterial(MultipleAchievements.CRAFTS);
			updateOldDBToMaterial(MultipleAchievements.PLACES);
		}
	}

	/**
	 * Update the database tables for a MultipleAchievements category.
	 * 
	 * @param category
	 */
	@SuppressWarnings("deprecation")
	private void updateOldDBToMaterial(MultipleAchievements category) {
		String tableName = sqlDatabaseManager.getTablePrefix() + category.toDBName();
		Connection conn = sqlDatabaseManager.getSQLConnection();
		try (Statement st = conn.createStatement();
				PreparedStatement prep = conn.prepareStatement("INSERT INTO tempTable VALUES (?,?,?);")) {
			ResultSet rs = st.executeQuery("SELECT * FROM " + tableName + "");
			List<String> uuids = new ArrayList<>();
			List<Integer> ids = new ArrayList<>();
			List<Integer> amounts = new ArrayList<>();

			while (rs.next()) {
				uuids.add(rs.getString(1));
				ids.add(rs.getInt(2));
				amounts.add(rs.getInt(3));
			}

			// Preallocate space in array containing the values in the new format.
			List<String> materials = new ArrayList<>(ids.size());

			for (int id : ids) {
				// Convert from ID to Material name.
				materials.add(Material.getMaterial(id).name().toLowerCase());
			}
			// Prevent from doing any commits before entire transaction is ready.
			conn.setAutoCommit(false);

			// Create new table.
			st.execute("CREATE TABLE tempTable (playername char(36)," + category.toSubcategoryDBName() + " varchar(64),"
					+ tableName + " INT UNSIGNED,PRIMARY KEY(playername, " + category.toSubcategoryDBName() + "))");

			// Populate new table with contents of the old one and material strings. Batch the insert requests.
			for (int i = 0; i < uuids.size(); ++i) {
				prep.setString(1, uuids.get(i));
				prep.setString(2, materials.get(i));
				prep.setInt(3, amounts.get(i));
				prep.addBatch();
			}

			prep.executeBatch();
			// Delete old table.
			st.execute("DROP TABLE " + tableName);
			// Rename new table to old one.
			st.execute("ALTER TABLE tempTable RENAME TO " + tableName);
			// Commit entire transaction.
			conn.commit();
			conn.setAutoCommit(true);
		} catch (SQLException e) {
			plugin.getLogger().log(Level.SEVERE, "SQL error while updating old DB (ids to material): ", e);
		}
	}

	/**
	 * Updates the database achievements table. The table is now using a date type for the date column. We also increase
	 * the number of chars allowed for the achievement names and descriptions.
	 */
	protected void updateOldDBToDates() {
		Connection conn = sqlDatabaseManager.getSQLConnection();
		try (Statement st = conn.createStatement()) {
			ResultSet rs = st
					.executeQuery("SELECT date FROM " + sqlDatabaseManager.getTablePrefix() + "achievements LIMIT 1");
			String type = rs.getMetaData().getColumnTypeName(1);
			// Old column type for versions prior to 3.0 was text for SQLite, char for MySQL and varchar for PostgreSQL
			// (even though PostgreSQL was not supported on versions prior to 3.0, we still support the upgrade for it
			// in case a user imports another database into PostgreSQL without doing the table upgrade beforehand).
			if ("text".equalsIgnoreCase(type) || "char".equalsIgnoreCase(type) || "varchar".equalsIgnoreCase(type)) {
				plugin.getLogger()
						.warning("Updating database table with date datatype for achievements, please wait...");
				try (PreparedStatement prep = conn.prepareStatement("INSERT INTO tempTable VALUES (?,?,?,?);")) {
					// Early versions of the plugin added colors to the date. We have to get rid of them by using a
					// regex pattern, else parsing will fail.
					final Pattern regexPattern = Pattern.compile("&([a-f]|[0-9]){1}");
					// Old date format, which was stored as a string.
					final SimpleDateFormat oldFormat = new SimpleDateFormat("dd/MM/yyyy");
					// Load entire achievements table into memory.
					rs = st.executeQuery("SELECT * FROM " + sqlDatabaseManager.getTablePrefix() + "achievements");
					List<String> uuids = new ArrayList<>();
					List<String> achs = new ArrayList<>();
					List<String> descs = new ArrayList<>();
					List<String> oldDates = new ArrayList<>();

					// Parse entire table into arrays.
					while (rs.next()) {
						uuids.add(rs.getString(1));
						achs.add(rs.getString(2));
						descs.add(rs.getString(3));
						oldDates.add(rs.getString(4));
					}

					// Preallocate space in array containing the values in the new format.
					List<java.sql.Date> newDates = new ArrayList<>(oldDates.size());

					try {
						for (String date : oldDates) {
							// Convert to SQL date format.
							newDates.add(new java.sql.Date(
									oldFormat.parse(regexPattern.matcher(date).replaceAll("")).getTime()));
						}
					} catch (ParseException e) {
						plugin.getLogger().log(Level.SEVERE, "Error while parsing dates: ", e);
					}
					// Prevent from doing any commits before entire transaction is ready.
					conn.setAutoCommit(false);

					// Create new table.
					st.execute(
							"CREATE TABLE tempTable (playername char(36),achievement varchar(64),description varchar(128),date DATE,PRIMARY KEY (playername, achievement))");

					// Populate new table with contents of the old one and date values.
					for (int i = 0; i < uuids.size(); ++i) {
						prep.setString(1, uuids.get(i));
						prep.setString(2, achs.get(i));
						prep.setString(3, descs.get(i));
						prep.setDate(4, newDates.get(i));
						prep.addBatch();
					}
					prep.executeBatch();

					// Delete old table.
					st.execute("DROP TABLE " + sqlDatabaseManager.getTablePrefix() + "achievements");
					// Rename new table to old one.
					st.execute(
							"ALTER TABLE tempTable RENAME TO " + sqlDatabaseManager.getTablePrefix() + "achievements");
					// Commit entire transaction.
					conn.commit();
					conn.setAutoCommit(true);
				}
			}
		} catch (SQLException e) {
			plugin.getLogger().log(Level.SEVERE, "SQL error while updating old DB (strings to dates): ", e);
		}
	}

	/**
	 * Increases size of the mobname column of the kills table to accommodate new parameters such as
	 * specificplayer-56c79b19-4500-466c-94ea-514a755fdd09.
	 */
	protected void updateOldDBMobnameSize() {
		Connection conn = sqlDatabaseManager.getSQLConnection();
		// SQLite ignores size for varchar datatype.
		if (sqlDatabaseManager.getDatabaseType() != DatabaseType.SQLITE) {
			int size = 51;
			try (Statement st = conn.createStatement()) {
				ResultSet rs = st
						.executeQuery("SELECT mobname FROM " + sqlDatabaseManager.getTablePrefix() + "kills LIMIT 1");
				size = rs.getMetaData().getPrecision(1);
				// Old kills table prior to version 4.2.1 contained a capacity of only 32 chars.
				if (size == 32) {
					plugin.getLogger().warning("Updating database table with extended mobname column, please wait...");
					// Increase size of table.
					if (sqlDatabaseManager.getDatabaseType() == DatabaseType.POSTGRESQL) {
						st.execute("ALTER TABLE " + sqlDatabaseManager.getTablePrefix()
								+ "kills ALTER COLUMN mobname TYPE varchar(51)");
					} else {
						st.execute("ALTER TABLE " + sqlDatabaseManager.getTablePrefix()
								+ "kills MODIFY mobname varchar(51)");
					}
				}
			} catch (SQLException e) {
				plugin.getLogger().log(Level.SEVERE, "SQL error while trying to update old kills table: ", e);
			}
		}
	}
}
