package com.hm.achievement.db;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.regex.Pattern;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.google.common.base.Strings;
import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;

/**
 * Class used to deal with the database and provide functions to evaluate common queries and retrieve the relevant
 * results.
 * 
 * @author Pyves
 *
 */
public class SQLDatabaseManager {

	private AdvancedAchievements plugin;
	private String databaseAddress;
	private String databaseUser;
	private String databasePassword;
	private String tablePrefix;
	private boolean achievementsChronologicalOrder;
	private byte databaseType;

	// Connection to the database; remains opened and shared except when plugin disabled.
	private Connection sqlConnection;

	// Used to do some write operations to the database asynchronously.
	private final ExecutorService pool;

	private static final byte SQLITE = 0;
	private static final byte MYSQL = 1;
	private static final byte POSTGRESQL = 2;

	public SQLDatabaseManager(AdvancedAchievements plugin) {

		this.plugin = plugin;
		// We expect to execute many short writes to the database. The pool can grow dynamically under high load.
		pool = Executors.newCachedThreadPool();
	}

	/**
	 * Initialise database system and plugin settings.
	 */
	public void initialise() {

		// Load plugin settings.
		configurationLoad();

		// Check if JDBC library for the specified database system is available.
		try {
			if (databaseType == SQLITE)
				Class.forName("org.sqlite.JDBC");
			else if (databaseType == MYSQL)
				Class.forName("com.mysql.jdbc.Driver");
			else
				Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			plugin.getLogger().severe(
					"The JBDC library for your database type was not found. Please read the plugin's support for more information.");
			plugin.setSuccessfulLoad(false);
		}

		// Try to establish connection with database; stays opened until explicitely closed by the plugin..
		Connection conn = getSQLConnection();

		if (conn == null) {
			plugin.getLogger().severe("Could not establish SQL connection, disabling plugin.");
			plugin.getLogger().severe("Please verify your settings in the configuration file.");
			plugin.setOverrideDisable(true);
			plugin.getServer().getPluginManager().disablePlugin(plugin);
			return;
		}

		// If a prefix is set in the config, check whether the tables with the default names exist. If so do renaming.
		if (!"".equals(tablePrefix)) {
			try (Statement st = conn.createStatement()) {
				ResultSet rs;
				if (databaseType == SQLITE) {
					rs = st.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='achievements'");
				} else if (databaseType == MYSQL) {
					rs = st.executeQuery("SELECT table_name FROM information_schema.tables WHERE table_schema='"
							+ databaseAddress.substring(databaseAddress.lastIndexOf('/') + 1)
							+ "' AND table_name ='achievements'");
				} else {
					rs = st.executeQuery(
							"SELECT 1 FROM pg_catalog.pg_class c JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace WHERE n.nspname = 'public' AND c.relname = 'achievements' AND c.relkind = 'r'");
				}
				// Table achievements still has its default name (ie. no prefix), but a prefix is set in the
				// configuration; do a renaming of all tables.
				if (rs.next())
					renameTables();
			} catch (SQLException e) {
				plugin.getLogger().log(Level.SEVERE, "Error while attempting to set prefix of database tables: ", e);
				plugin.setSuccessfulLoad(false);
			}
		}

		// Initialise database tables (ie. create if they don't exist).
		try {
			initialiseTables();
		} catch (SQLException e) {
			plugin.getLogger().log(Level.SEVERE, "Error while initialising database tables: ", e);
			plugin.setSuccessfulLoad(false);
		}

		// Check if using old database prior to version 2.4.1.
		String type = "";
		try (Statement st = conn.createStatement()) {
			ResultSet rs = st.executeQuery(
					"SELECT blockid FROM " + tablePrefix + MultipleAchievements.BREAKS.toDBName() + " LIMIT 1");
			type = rs.getMetaData().getColumnTypeName(1);
		} catch (SQLException e) {
			plugin.getLogger().log(Level.SEVERE, "SQL error while trying to update old DB: ", e);
		}

		// Old column type for versions prior to 2.4.1 was integer for SQLite and smallint unsigned for MySQL.
		if ("integer".equalsIgnoreCase(type) || "smallint unsigned".equalsIgnoreCase(type)) {
			plugin.getLogger().warning("Updating database tables, please wait...");
			updateOldDBToMaterial(tablePrefix + MultipleAchievements.BREAKS.toDBName());
			updateOldDBToMaterial(tablePrefix + MultipleAchievements.CRAFTS.toDBName());
			updateOldDBToMaterial(tablePrefix + MultipleAchievements.PLACES.toDBName());
		}

		// Check if using old database prior to version 3.0.
		try (Statement st = conn.createStatement()) {
			ResultSet rs = st.executeQuery("SELECT date FROM " + tablePrefix + "achievements LIMIT 1");
			type = rs.getMetaData().getColumnTypeName(1);
		} catch (SQLException e) {
			plugin.getLogger().log(Level.SEVERE, "SQL error while trying to update old DB: ", e);
		}
		// Old column type for versions prior to 3.0 was text for SQLite, char for MySQL and varchar for PostgreSQL
		// (even though PostgreSQL was not supported on versions prior to 3.0, we still support the upgrade for it in
		// case a user imports anither database into PostgreSQL without doing the table upgrade beforehand).
		if ("text".equalsIgnoreCase(type) || "char".equalsIgnoreCase(type) || "varchar".equalsIgnoreCase(type)) {
			plugin.getLogger().warning("Updating database tables, please wait...");
			updateOldDBToDates();
		}
	}

	/**
	 * Load plugin configuration and set parameters relevant to the database system.
	 */
	private void configurationLoad() {

		achievementsChronologicalOrder = plugin.getPluginConfig().getBoolean("BookChronologicalOrder", true);
		tablePrefix = plugin.getPluginConfig().getString("TablePrefix", "");

		String dataHandler = plugin.getPluginConfig().getString("DatabaseType", "sqlite");
		if ("mysql".equalsIgnoreCase(dataHandler)) {
			// Get parameters from the MySQL config category.
			databaseType = MYSQL;
			databaseAddress = plugin.getPluginConfig().getString("MYSQL.Database",
					"jdbc:mysql://localhost:3306/minecraft");
			databaseUser = plugin.getPluginConfig().getString("MYSQL.User", "root");
			databasePassword = plugin.getPluginConfig().getString("MYSQL.Password", "root");
		} else if ("postgresql".equalsIgnoreCase(dataHandler)) {
			// Get parameters from the PostgreSQL config category.
			databaseType = POSTGRESQL;
			databaseAddress = plugin.getPluginConfig().getString("POSTGRESQL.Database",
					"jdbc:postgresql://localhost:5432/minecraft");
			databaseUser = plugin.getPluginConfig().getString("POSTGRESQL.User", "root");
			databasePassword = plugin.getPluginConfig().getString("POSTGRESQL.Password", "root");
		} else {
			// No extra parameters to retrieve!
			databaseType = SQLITE;
		}
	}

	/**
	 * Initialise database tables by creating non existing ones. We batch the requests to send a unique batch to the
	 * database.
	 * 
	 * @throws SQLException
	 */
	private void initialiseTables() throws SQLException {

		try (Statement st = sqlConnection.createStatement()) {
			st.addBatch("CREATE TABLE IF NOT EXISTS " + tablePrefix
					+ "achievements (playername char(36),achievement varchar(64),description varchar(128),date DATE,PRIMARY KEY (playername, achievement))");
			st.addBatch("CREATE TABLE IF NOT EXISTS " + tablePrefix + MultipleAchievements.BREAKS.toDBName()
					+ " (playername char(36),blockid varchar(32)," + MultipleAchievements.BREAKS.toDBName()
					+ " INT,PRIMARY KEY(playername, blockid))");
			st.addBatch("CREATE TABLE IF NOT EXISTS " + tablePrefix + MultipleAchievements.PLACES.toDBName()
					+ " (playername char(36),blockid varchar(32)," + MultipleAchievements.PLACES.toDBName()
					+ " INT,PRIMARY KEY(playername, blockid))");
			st.addBatch("CREATE TABLE IF NOT EXISTS " + tablePrefix + MultipleAchievements.KILLS.toDBName()
					+ " (playername char(36),mobname varchar(32)," + MultipleAchievements.KILLS.toDBName()
					+ " INT,PRIMARY KEY (playername, mobname))");
			st.addBatch("CREATE TABLE IF NOT EXISTS " + tablePrefix + MultipleAchievements.CRAFTS.toDBName()
					+ " (playername char(36),item varchar(32)," + MultipleAchievements.CRAFTS.toDBName()
					+ " INT,PRIMARY KEY (playername, item))");

			for (NormalAchievements category : NormalAchievements.values()) {
				if (category == NormalAchievements.PLAYEDTIME) {
					st.addBatch("CREATE TABLE IF NOT EXISTS " + tablePrefix + category.toDBName()
							+ " (playername char(36)," + category.toDBName() + " BIGINT,PRIMARY KEY (playername))");
				} else if (category == NormalAchievements.CONNECTIONS) {
					st.addBatch(
							"CREATE TABLE IF NOT EXISTS " + tablePrefix + category.toDBName() + " (playername char(36),"
									+ category.toDBName() + " INT,date varchar(10),PRIMARY KEY (playername))");
				} else {
					st.addBatch("CREATE TABLE IF NOT EXISTS " + tablePrefix + category.toDBName()
							+ " (playername char(36)," + category.toDBName() + " INT,PRIMARY KEY (playername))");
				}
			}
			st.executeBatch();
		}
	}

	/**
	 * Rename the database tables with the prefix given in the configuration file. This method is only used and only
	 * works if the tables had the default name. It does not support multiple successive table renamings.
	 * 
	 * @throws SQLException
	 */
	private void renameTables() throws SQLException {

		try (Statement st = sqlConnection.createStatement()) {
			st.addBatch("ALTER TABLE achievements RENAME TO " + tablePrefix + "achievements");
			for (NormalAchievements category : NormalAchievements.values()) {
				st.addBatch("ALTER TABLE " + category.toDBName() + " RENAME TO " + tablePrefix + category.toDBName());
			}
			for (MultipleAchievements category : MultipleAchievements.values()) {
				st.addBatch("ALTER TABLE " + category.toDBName() + " RENAME TO " + tablePrefix + category.toDBName());
			}
			st.executeBatch();
		}
	}

	/**
	 * Update the database tables for break, craft and place achievements (from int to varchar for identification
	 * column). The tables are now using material names and no longer item IDs, which are deprecated; this also allows
	 * to store extra data information, extending the number of items available for the user.
	 * 
	 * @param tableName
	 */
	@SuppressWarnings("deprecation")
	private void updateOldDBToMaterial(String tableName) {

		Connection conn = getSQLConnection();
		try (Statement st = conn.createStatement();
				PreparedStatement prep = conn.prepareStatement("INSERT INTO tempTable VALUES (?,?,?);")) {
			ResultSet rs = st.executeQuery("SELECT * FROM " + tableName + "");
			ArrayList<String> uuids = new ArrayList<>();
			ArrayList<Integer> ids = new ArrayList<>();
			ArrayList<Integer> amounts = new ArrayList<>();

			while (rs.next()) {
				uuids.add(rs.getString(1));
				ids.add(rs.getInt(2));
				amounts.add(rs.getInt(3));
			}

			// Preallocate space in array containing the values in the new format.
			ArrayList<String> materials = new ArrayList<>(ids.size());

			for (int id : ids) {
				// Convert from ID to Material name.
				materials.add(Material.getMaterial(id).name().toLowerCase());
			}
			// Prevent from doing any commits before entire transaction is ready.
			conn.setAutoCommit(false);

			// Create new table.
			if (!MultipleAchievements.CRAFTS.toDBName().equals(tableName))
				st.execute("CREATE TABLE tempTable (playername char(36),blockid varchar(64)," + tableName
						+ " INT UNSIGNED,PRIMARY KEY(playername, blockid))");
			else
				st.execute("CREATE TABLE tempTable (playername char(36),item varchar(64)," + tableName
						+ " INT UNSIGNED,PRIMARY KEY(playername, item))");

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
	 * Update the database tables achievements. The table is now using a date type for the date column (previously was
	 * char type). We also increase the number of chars allowed for the achievement names and descriptions.
	 */
	private void updateOldDBToDates() {

		// Early versions of the plugin added colors to the date. We have to get rid of them by using a regex pattern,
		// else parsing will fail.
		final Pattern regexPattern = Pattern.compile("&([a-f]|[0-9]){1}");
		// Old date format, which was stored as a string.
		final SimpleDateFormat oldFormat = new SimpleDateFormat("dd/MM/yyyy");

		Connection conn = getSQLConnection();
		try (Statement st = conn.createStatement();
				PreparedStatement prep = conn.prepareStatement("INSERT INTO tempTable VALUES (?,?,?,?);")) {
			// Load entire achievements table into memory.
			ResultSet rs = st.executeQuery("SELECT * FROM " + tablePrefix + "achievements");
			ArrayList<String> uuids = new ArrayList<>();
			ArrayList<String> achs = new ArrayList<>();
			ArrayList<String> descs = new ArrayList<>();
			ArrayList<String> oldDates = new ArrayList<>();

			// Parse entire table into arrays.
			while (rs.next()) {
				uuids.add(rs.getString(1));
				achs.add(rs.getString(2));
				descs.add(rs.getString(3));
				oldDates.add(rs.getString(4));
			}

			// Preallocate space in array containing the values in the new format.
			ArrayList<java.sql.Date> newDates = new ArrayList<>(oldDates.size());

			try {
				for (String date : oldDates) {
					// Convert to SQL date format.
					newDates.add((new java.sql.Date(
							oldFormat.parse(date.replaceAll(regexPattern.pattern(), "")).getTime())));
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
			st.execute("DROP TABLE " + tablePrefix + "achievements");
			// Rename new table to old one.
			st.execute("ALTER TABLE tempTable RENAME TO " + tablePrefix + "achievements");
			// Commit entire transaction.
			conn.commit();
			conn.setAutoCommit(true);
		} catch (SQLException e) {
			plugin.getLogger().log(Level.SEVERE, "SQL error while updating old DB (strings to dates): ", e);
		}
	}

	/**
	 * Retrieve SQL connection to MySQL, PostgreSQL or SQLite database.
	 */
	public Connection getSQLConnection() {

		// Check if Connection was not previously closed.
		try {
			if (sqlConnection == null || sqlConnection.isClosed()) {

				if (databaseType == MYSQL || databaseType == POSTGRESQL) {
					sqlConnection = createRemoteSQLConnection();
				} else {
					sqlConnection = createSQLiteConnection();
				}
			}
		} catch (SQLException e) {
			plugin.getLogger().log(Level.SEVERE, "Error while attempting to retrieve connection to database: ", e);
			plugin.setSuccessfulLoad(false);
		}
		return sqlConnection;
	}

	/**
	 * Create a new Connection object to SQLite database.
	 * 
	 * @return connection object to database
	 * @throws SQLException
	 */
	private Connection createSQLiteConnection() throws SQLException {

		File dbfile = new File(plugin.getDataFolder(), "achievements.db");
		try {
			if (dbfile.createNewFile())
				plugin.getLogger().info("Successfully created database file.");
		} catch (IOException e) {
			plugin.getLogger().log(Level.SEVERE, "Error while creating database file: ", e);
			plugin.setSuccessfulLoad(false);
		}
		return DriverManager.getConnection("jdbc:sqlite:" + dbfile);
	}

	/**
	 * Create a new Connection object to MySQL or PostgreSQL database.
	 * 
	 * @return connection object to database
	 * @throws SQLException
	 */
	private Connection createRemoteSQLConnection() throws SQLException {

		return DriverManager.getConnection(
				databaseAddress + "?autoReconnect=true&user=" + databaseUser + "&password=" + databasePassword);
	}

	/**
	 * Get the list of all the achievements of a player.
	 * 
	 * @param player
	 * @return array list with groups of 3 strings: achievement name, description and date
	 */
	public List<String> getPlayerAchievementsList(Player player) {

		ArrayList<String> achievementsList = new ArrayList<>();
		Connection conn = getSQLConnection();
		try (Statement st = conn.createStatement()) {
			ResultSet rs;
			if (achievementsChronologicalOrder) {
				// Oldest date to newest one.
				rs = st.executeQuery("SELECT * FROM " + tablePrefix + "achievements WHERE playername = '"
						+ player.getUniqueId() + "' ORDER BY date ASC");
			} else {
				// Newest date to oldest one.
				rs = st.executeQuery("SELECT * FROM " + tablePrefix + "achievements WHERE playername = '"
						+ player.getUniqueId() + "' ORDER BY date DESC");
			}

			Map<String, String> achievementsAndDisplayNames = plugin.getAchievementsAndDisplayNames();
			while (rs.next()) {
				String achName = rs.getString(2);
				String displayName = achievementsAndDisplayNames.getOrDefault(achName, "");

				if (Strings.isNullOrEmpty(displayName)) {
					achievementsList.add(achName);
				} else {
					achievementsList.add(displayName);
				}
				achievementsList.add(rs.getString(3));
				achievementsList.add(rs.getDate(4).toString());
			}
		} catch (SQLException e) {
			plugin.getLogger().log(Level.SEVERE, "SQL error while retrieving achievements: ", e);
		}
		return achievementsList;
	}

	/**
	 * Get the date of reception of a specific achievement.
	 * 
	 * @param player
	 * @param name
	 * @return date represented as a string
	 */
	public String getPlayerAchievementDate(Player player, String name) {

		// We double apostrophes to avoid breaking the query.
		String dbName = name.replace("'", "''");
		String achievementDate = null;
		Connection conn = getSQLConnection();
		try (Statement st = conn.createStatement()) {
			ResultSet rs = st.executeQuery("SELECT date FROM " + tablePrefix + "achievements WHERE playername = '"
					+ player.getUniqueId() + "' AND achievement = '" + dbName + "'");

			if (rs.next()) {
				achievementDate = rs.getDate(1).toString();
			}
		} catch (SQLException e) {
			plugin.getLogger().log(Level.SEVERE, "SQL error while retrieving achievement date: ", e);
		}
		return achievementDate;
	}

	/**
	 * Get the total number of achievements received by a player.
	 * 
	 * @param player
	 * @return number of achievements
	 */
	public int getPlayerAchievementsAmount(Player player) {

		int achievementsAmount = 0;
		Connection conn = getSQLConnection();
		try (Statement st = conn.createStatement()) {
			ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + tablePrefix + "achievements WHERE playername = '"
					+ player.getUniqueId() + "'");
			if (rs.next()) {
				achievementsAmount = rs.getInt(1);
			}
		} catch (SQLException e) {
			plugin.getLogger().log(Level.SEVERE, "SQL error while counting player achievements: ", e);
		}
		return achievementsAmount;
	}

	/**
	 * Get the list of players with the most achievements over a given period.
	 * 
	 * @param listLength
	 * @param start
	 * @return list with player UUIDs
	 */
	public List<String> getTopList(int listLength, long start) {

		ArrayList<String> topList = new ArrayList<>();
		String query;
		if (start == 0L) {
			// We consider all the achievements; no date comparison.
			query = "SELECT playername, COUNT(*) FROM " + tablePrefix
					+ "achievements GROUP BY playername ORDER BY COUNT(*) DESC LIMIT ?";
		} else {
			// We only consider achievement received after the start date; do date comparisons.
			query = "SELECT playername, COUNT(*) FROM " + tablePrefix
					+ "achievements WHERE date > ? GROUP BY playername ORDER BY COUNT(*) DESC LIMIT ?";
		}

		Connection conn = getSQLConnection();
		try (PreparedStatement prep = conn.prepareStatement(query)) {
			if (start == 0L) {
				prep.setInt(1, listLength);
			} else {
				prep.setDate(1, new java.sql.Date(start));
				prep.setInt(2, listLength);
			}
			prep.execute();
			ResultSet rs = prep.getResultSet();
			while (rs.next()) {
				topList.add(rs.getString(1));
				topList.add(Integer.toString(rs.getInt(2)));
			}
		} catch (SQLException e) {
			plugin.getLogger().log(Level.SEVERE, "SQL error while retrieving top players: ", e);
		}
		return topList;
	}

	/**
	 * Get number of players who have received at least one achievement after start date.
	 * 
	 * @param start
	 * @return list with player UUIDs
	 */
	public int getTotalPlayers(long start) {

		int players = 0;
		String query;
		if (start == 0L) {
			// We consider all the achievements; no date comparison.
			query = "SELECT COUNT(*) FROM (SELECT DISTINCT playername  FROM " + tablePrefix
					+ "achievements) AS distinctPlayers";
		} else {
			// We only consider achievement received after the start date; do date comparisons.
			query = "SELECT COUNT(*) FROM (SELECT DISTINCT playername  FROM " + tablePrefix
					+ "achievements WHERE date > ?) AS distinctPlayers";
		}

		Connection conn = getSQLConnection();
		try (PreparedStatement prep = conn.prepareStatement(query)) {
			if (start > 0L) {
				prep.setDate(1, new java.sql.Date(start));
			}
			prep.execute();
			ResultSet rs = prep.getResultSet();
			while (rs.next()) {
				players = rs.getInt(1);
			}
		} catch (SQLException e) {
			plugin.getLogger().log(Level.SEVERE, "SQL error while retrieving total players: ", e);
		}
		return players;
	}

	/**
	 * Get the rank of a player given his number of achievements received after start date.
	 * 
	 * @param player
	 * @param start
	 * @return player's rank
	 */
	public int getPlayerRank(Player player, long start) {

		int rank = 0;
		String query;
		if (start == 0L) {
			// We consider all the achievements; no date comparison.
			query = "SELECT COUNT(*) FROM (SELECT COUNT(*) number FROM " + tablePrefix
					+ "achievements GROUP BY playername) AS achGroupedByPlayer WHERE number > (SELECT COUNT(*) FROM "
					+ tablePrefix + "achievements WHERE playername = ?)";
		} else {
			// We only consider achievement received after the start date; do date comparisons.
			query = "SELECT COUNT(*) FROM (SELECT COUNT(*) number FROM " + tablePrefix
					+ "achievements WHERE date > ? GROUP BY playername) AS achGroupedByPlayer WHERE number > (SELECT COUNT(*) FROM "
					+ tablePrefix + "achievements WHERE playername = ? AND date > ?)";
		}

		Connection conn = getSQLConnection();
		try (PreparedStatement prep = conn.prepareStatement(query)) {
			if (start == 0L) {
				prep.setString(1, player.getUniqueId().toString());
			} else {
				prep.setDate(1, new java.sql.Date(start));
				prep.setString(2, player.getUniqueId().toString());
				prep.setDate(3, new java.sql.Date(start));
			}
			prep.execute();
			ResultSet rs = prep.getResultSet();
			while (rs.next()) {
				// Rank of a player corresponds to number of players with more achievements + 1.
				rank = rs.getInt(1) + 1;
			}
		} catch (SQLException e) {
			plugin.getLogger().log(Level.SEVERE, "SQL error while retrieving player rank: ", e);
		}
		return rank;
	}

	/**
	 * Register a new achievement for a player; this method will distinguish between asynchronous and synchronous
	 * processing.
	 * 
	 * @param player
	 * @param achievement
	 * @param desc
	 */
	public void registerAchievement(Player player, final String achievement, final String desc) {

		final String name = player.getUniqueId().toString();

		if (plugin.isAsyncPooledRequestsSender()) {
			// Avoid using Bukkit API scheduler, as a reload/restart could kill the async task before write to database
			// has occurred; pools also allow to reuse threads.
			pool.execute(new Runnable() {

				@Override
				public void run() {

					registerAchievementToDB(achievement, desc, name);
				}
			});
		} else {
			registerAchievementToDB(achievement, desc, name);
		}
	}

	/**
	 * Write to DB to register new achievement for a player. Method executed on main server thread or in parallel.
	 * 
	 * @param achievement
	 * @param desc
	 * @param name
	 */
	private void registerAchievementToDB(String achievement, String desc, String name) {

		String query;
		if (databaseType == POSTGRESQL) {
			// PostgreSQL has no REPLACE operator. We have to use the INSERT ... ON CONFLICT construct, which is
			// available for PostgreSQL 9.5+.
			query = "INSERT INTO " + tablePrefix
					+ "achievements VALUES (?,?,?,?) ON CONFLICT (playername,achievement) DO UPDATE SET (description,date)=(?,?)";
		} else {
			query = "REPLACE INTO " + tablePrefix + "achievements VALUES (?,?,?,?)";
		}
		Connection conn = getSQLConnection();
		try (PreparedStatement prep = conn.prepareStatement(query)) {
			prep.setString(1, name);
			prep.setString(2, achievement);
			prep.setString(3, desc);
			prep.setDate(4, new java.sql.Date(new java.util.Date().getTime()));
			if (databaseType == POSTGRESQL) {
				prep.setString(5, desc);
				prep.setDate(6, new java.sql.Date(new java.util.Date().getTime()));
			}
			prep.execute();
		} catch (SQLException e) {
			plugin.getLogger().log(Level.SEVERE, "SQL error while registering achievement: ", e);
		}
	}

	/**
	 * Check whether player has received a specific achievement.
	 * 
	 * @param player
	 * @param name
	 * @return true if achievement found in database, false otherwise
	 */
	public boolean hasPlayerAchievement(Player player, String name) {

		boolean result = false;
		String query;
		if (name.contains("'")) {
			// We check for names with single quotes, but also two single quotes. This is due to a bug in versions
			// 3.0 to 3.0.2 where names containing single quotes were inserted with two single quotes in the
			// database.
			query = "SELECT achievement FROM " + tablePrefix + "achievements WHERE playername = '"
					+ player.getUniqueId() + "' AND (achievement = ? OR achievement = ?)";
		} else {
			query = "SELECT achievement FROM " + tablePrefix + "achievements WHERE playername = '"
					+ player.getUniqueId() + "' AND achievement = ?";
		}

		Connection conn = getSQLConnection();
		try (PreparedStatement prep = conn.prepareStatement(query)) {
			if (name.contains("'")) {
				prep.setString(1, name);
				prep.setString(2, name.replace("'", "''"));
			} else {
				prep.setString(1, name);
			}
			if (prep.executeQuery().next())
				result = true;
		} catch (SQLException e) {
			plugin.getLogger().log(Level.SEVERE, "SQL error while checking achievement: ", e);
		}
		return result;
	}

	/**
	 * Delete an achievement from a player.
	 * 
	 * @param player
	 * @param ach
	 */
	public void deletePlayerAchievement(Player player, final String ach) {

		final String name = player.getUniqueId().toString();
		if (plugin.isAsyncPooledRequestsSender()) {
			// Avoid using Bukkit API scheduler, as a reload/restart could kill the async task before write to
			// database has occurred; pools also allow to reuse threads.
			pool.execute(new Runnable() {

				@Override
				public void run() {

					deletePlayerAchievementFromDB(ach, name);
				}
			});
		} else {
			deletePlayerAchievementFromDB(ach, name);
		}
	}

	/**
	 * Delete an achievement from a player in the DB. Method executed on main server thread or in parallel.
	 * 
	 * @param player
	 * @param name
	 */
	private void deletePlayerAchievementFromDB(final String ach, final String name) {

		Connection conn = getSQLConnection();
		try (PreparedStatement prep = conn.prepareStatement(
				"DELETE FROM " + tablePrefix + "achievements WHERE playername = ? AND achievement = ?")) {
			prep.setString(1, name);
			prep.setString(2, ach);
			prep.execute();
		} catch (SQLException e) {
			plugin.getLogger().log(Level.SEVERE, "SQL error while deleting achievement: ", e);
		}
	}

	/**
	 * Get number of player's kills for a specific mob.
	 * 
	 * @param player
	 * @param mobname
	 * @return kill statistic
	 */
	public int getKills(Player player, String mobname) {

		int entityKills = 0;
		Connection conn = getSQLConnection();
		try (Statement st = conn.createStatement()) {
			ResultSet rs = st.executeQuery("SELECT " + MultipleAchievements.KILLS.toDBName() + " FROM " + tablePrefix
					+ MultipleAchievements.KILLS.toDBName() + " WHERE playername = '" + player.getUniqueId()
					+ "' AND mobname = '" + mobname + "'");

			while (rs.next()) {
				entityKills = rs.getInt(MultipleAchievements.KILLS.toDBName());
			}
		} catch (SQLException e) {
			plugin.getLogger().log(Level.SEVERE, "SQL error while retrieving kill stats: ", e);
		}
		return entityKills;
	}

	/**
	 * Get number of player's places for a specific block.
	 * 
	 * @param player
	 * @param block
	 * @return place statistics
	 */
	public int getPlaces(Player player, String block) {

		int blockBreaks = 0;
		Connection conn = getSQLConnection();
		try (Statement st = conn.createStatement()) {
			ResultSet rs = st.executeQuery("SELECT " + MultipleAchievements.PLACES.toDBName() + " FROM " + tablePrefix
					+ MultipleAchievements.PLACES.toDBName() + " WHERE playername = '" + player.getUniqueId()
					+ "' AND blockid = '" + block + "'");
			while (rs.next()) {
				blockBreaks = rs.getInt(MultipleAchievements.PLACES.toDBName());
			}
		} catch (SQLException e) {
			plugin.getLogger().log(Level.SEVERE, "SQL error while retrieving block place stats: ", e);
		}
		return blockBreaks;
	}

	/**
	 * Get number of player's breaks for a specific block.
	 * 
	 * @param player
	 * @param block
	 * @return breaks statistics
	 */
	public int getBreaks(Player player, String block) {

		int blockBreaks = 0;
		Connection conn = getSQLConnection();
		try (Statement st = conn.createStatement()) {
			ResultSet rs = st.executeQuery("SELECT " + MultipleAchievements.BREAKS.toDBName() + " FROM " + tablePrefix
					+ MultipleAchievements.BREAKS.toDBName() + " WHERE playername = '" + player.getUniqueId()
					+ "' AND blockid = '" + block + "'");
			while (rs.next()) {
				blockBreaks = rs.getInt(MultipleAchievements.BREAKS.toDBName());
			}
		} catch (SQLException e) {
			plugin.getLogger().log(Level.SEVERE, "SQL error while retrieving block break stats: ", e);
		}
		return blockBreaks;
	}

	/**
	 * Get number of player's crafts for a specific object.
	 * 
	 * @param player
	 * @param item
	 * @return craft statistics
	 */
	public int getCrafts(Player player, String item) {

		int itemCrafts = 0;
		Connection conn = getSQLConnection();
		try (Statement st = conn.createStatement()) {
			ResultSet rs = st.executeQuery("SELECT " + MultipleAchievements.CRAFTS.toDBName() + " FROM " + tablePrefix
					+ MultipleAchievements.CRAFTS.toDBName() + " WHERE playername = '" + player.getUniqueId()
					+ "' AND item = '" + item + "'");
			while (rs.next()) {
				itemCrafts = rs.getInt(MultipleAchievements.CRAFTS.toDBName());
			}
		} catch (SQLException e) {
			plugin.getLogger().log(Level.SEVERE, "SQL error while handling craft event: ", e);
		}
		return itemCrafts;
	}

	/**
	 * Get the time played by a player in millis.
	 * 
	 * @param player
	 * @param table
	 * @return statistic
	 */
	public long getPlaytime(Player player) {

		long amount = 0;
		Connection conn = getSQLConnection();
		try (Statement st = conn.createStatement()) {
			ResultSet rs = st.executeQuery("SELECT " + NormalAchievements.PLAYEDTIME.toDBName() + " FROM " + tablePrefix
					+ NormalAchievements.PLAYEDTIME.toDBName() + " WHERE playername = '" + player.getUniqueId() + "'");
			while (rs.next()) {
				amount = rs.getLong(NormalAchievements.PLAYEDTIME.toDBName());
			}
		} catch (SQLException e) {
			plugin.getLogger().log(Level.SEVERE, "SQL error while retrieving playedtime stats: ", e);
		}
		return amount;
	}

	/**
	 * Get the amount of a normal achievement statistic.
	 * 
	 * @param player
	 * @param table
	 * @return statistic
	 */
	public int getNormalAchievementAmount(Player player, String table) {

		int amount = 0;
		Connection conn = getSQLConnection();
		try (Statement st = conn.createStatement()) {
			ResultSet rs = st.executeQuery("SELECT " + table + " FROM " + tablePrefix + table + " WHERE playername = '"
					+ player.getUniqueId() + "'");
			while (rs.next()) {
				amount = rs.getInt(table);
			}
		} catch (SQLException e) {
			plugin.getLogger().log(Level.SEVERE, "SQL error while retrieving " + table + " stats: ", e);
		}
		return amount;
	}

	/**
	 * Return player's number of connections on separate days (used by GUI).
	 * 
	 * @param player
	 * @return connections statistic
	 */
	public int getConnectionsAmount(Player player) {

		final String name = player.getUniqueId().toString();
		int connections = 0;
		Connection conn = getSQLConnection();
		try (Statement st = conn.createStatement()) {
			ResultSet rs = st.executeQuery("SELECT " + NormalAchievements.CONNECTIONS.toDBName() + " FROM "
					+ tablePrefix + NormalAchievements.CONNECTIONS.toDBName() + " WHERE playername = '" + name + "'");
			while (rs.next()) {
				connections = rs.getInt(NormalAchievements.CONNECTIONS.toDBName());
			}
		} catch (SQLException e) {
			plugin.getLogger().log(Level.SEVERE, "SQL error while retrieving connection statistics: ", e);
		}
		return connections;
	}

	/**
	 * Get a player's last connection date.
	 * 
	 * @param player
	 * @return String with date
	 */
	public String getPlayerConnectionDate(Player player) {

		String date = null;
		Connection conn = getSQLConnection();
		try (Statement st = conn.createStatement()) {
			ResultSet rs = st.executeQuery("SELECT date FROM " + tablePrefix + NormalAchievements.CONNECTIONS.toDBName()
					+ " WHERE playername = '" + player.getUniqueId() + "'");
			while (rs.next())
				date = rs.getString("date");
		} catch (SQLException e) {
			plugin.getLogger().log(Level.SEVERE, "SQL error while retrieving connection date stats: ", e);
		}
		return date;
	}

	/**
	 * Update player's number of connections and last connection date and return number of connections (used by
	 * Connections listener).
	 * 
	 * @param player
	 * @param date
	 * @return connections statistic
	 */
	public int updateAndGetConnection(Player player, final String date) {

		final String name = player.getUniqueId().toString();
		Connection conn = getSQLConnection();
		try (Statement st = conn.createStatement()) {
			ResultSet rs = st.executeQuery("SELECT " + NormalAchievements.CONNECTIONS.toDBName() + " FROM "
					+ tablePrefix + NormalAchievements.CONNECTIONS.toDBName() + " WHERE playername = '" + name + "'");
			int prev = 0;
			while (rs.next()) {
				prev = rs.getInt(NormalAchievements.CONNECTIONS.toDBName());
			}
			final int newConnections = prev + 1;
			if (plugin.isAsyncPooledRequestsSender()) {
				// Avoid using Bukkit API scheduler, as a reload/restart could kill the async task before write to
				// database has occurred; pools also allow to reuse threads.
				pool.execute(new Runnable() {

					@Override
					public void run() {

						updateConnection(date, name, newConnections);
					}
				});
			} else {
				updateConnection(date, name, newConnections);
			}
			return newConnections;
		} catch (SQLException e) {
			plugin.getLogger().log(Level.SEVERE, "SQL error while handling connection event: ", e);
		}
		return 0;
	}

	/**
	 * Update player's number of connections and last connection date.
	 * 
	 * @param date
	 * @param name
	 * @param newConnections
	 */
	private void updateConnection(final String date, final String name, final int newConnections) {

		Connection conn = getSQLConnection();
		try (Statement st = conn.createStatement()) {
			if (databaseType == POSTGRESQL) {
				// PostgreSQL has no REPLACE operator. We have to use the INSERT ... ON CONFLICT
				// construct, which is available for PostgreSQL 9.5+.
				st.execute("INSERT INTO " + tablePrefix + NormalAchievements.CONNECTIONS.toDBName() + " VALUES ('"
						+ name + "', " + newConnections + ", '" + date + "')"
						+ " ON CONFLICT (playername) DO UPDATE SET (" + NormalAchievements.CONNECTIONS.toDBName()
						+ ",date)=('" + newConnections + "','" + date + "')");
			} else {
				st.execute("REPLACE INTO " + tablePrefix + NormalAchievements.CONNECTIONS.toDBName() + " VALUES ('"
						+ name + "', " + newConnections + ", '" + date + "')");
			}
		} catch (SQLException e) {
			plugin.getLogger().log(Level.SEVERE, "SQL error while updating connection: ", e);
		}
	}

	/**
	 * Updates player's playtime.
	 * 
	 * @param name
	 * @param time
	 * @return play time statistic
	 */
	public void updatePlaytime(String name, long time) {

		Connection conn = getSQLConnection();
		try (Statement st = conn.createStatement()) {
			if (databaseType == POSTGRESQL) {
				// PostgreSQL has no REPLACE operator. We have to use the INSERT ... ON CONFLICT
				// construct, which is available for PostgreSQL 9.5+.
				st.execute("INSERT INTO " + tablePrefix + NormalAchievements.PLAYEDTIME.toDBName() + " VALUES ('" + name
						+ "', " + time + ")" + " ON CONFLICT (playername) DO UPDATE SET ("
						+ NormalAchievements.PLAYEDTIME.toDBName() + ")=('" + time + "')");
			} else {
				st.execute("REPLACE INTO " + tablePrefix + NormalAchievements.PLAYEDTIME.toDBName() + " VALUES ('"
						+ name + "', " + time + ")");
			}
		} catch (SQLException e) {
			plugin.getLogger().log(Level.SEVERE, "SQL error while handling play time update: ", e);
		}
	}

	/**
	 * Updates player's distance for a specific distance type.
	 * 
	 * @param name
	 * @param distance
	 * @param type
	 * @return distance statistic
	 */
	public void updateDistance(String name, int distance, String type) {

		Connection conn = getSQLConnection();
		try (Statement st = conn.createStatement()) {
			// Update statistic.
			if (databaseType == POSTGRESQL) {
				// PostgreSQL has no REPLACE operator. We have to use the INSERT ... ON CONFLICT
				// construct, which is available for PostgreSQL 9.5+.
				st.execute("INSERT INTO " + tablePrefix + type + " VALUES ('" + name + "', " + distance + ")"
						+ " ON CONFLICT (playername) DO UPDATE SET (" + type + ")=('" + distance + "')");
			} else {
				st.execute("REPLACE INTO " + tablePrefix + type + " VALUES ('" + name + "', " + distance + ")");
			}
		} catch (SQLException e) {
			plugin.getLogger().log(Level.SEVERE, "SQL error while handling " + type + " update: ", e);
		}
	}

	public String getTablePrefix() {

		return tablePrefix;
	}

	public boolean isPostgres() {

		return databaseType == POSTGRESQL;
	}

}
