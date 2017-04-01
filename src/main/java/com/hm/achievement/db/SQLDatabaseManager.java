package com.hm.achievement.db;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Strings;
import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.mcshared.file.FileManager;

/**
 * Class used to deal with the database and provide functions to evaluate common queries and retrieve the relevant
 * results.
 * 
 * @author Pyves
 *
 */
public class SQLDatabaseManager {

	private final AdvancedAchievements plugin;
	// Used to do some write operations to the database asynchronously.
	private final ExecutorService pool;
	// Connection to the database; remains opened and shared.
	private final AtomicReference<Connection> sqlConnection;

	private volatile String databaseAddress;
	private volatile String databaseUser;
	private volatile String databasePassword;
	private volatile String tablePrefix;
	private volatile DatabaseType databaseType;
	private boolean achievementsChronologicalOrder;
	private DateFormat dateFormat;
	private boolean databaseBackup;

	public SQLDatabaseManager(AdvancedAchievements plugin) {
		this.plugin = plugin;
		// We expect to execute many short writes to the database. The pool can grow dynamically under high load and
		// allows to reuse threads.
		pool = Executors.newCachedThreadPool();
		sqlConnection = new AtomicReference<>();
	}

	/**
	 * Initialises database system and plugin settings.
	 */
	public void initialise() {
		// Load plugin settings.
		configurationLoad();

		// Check if JDBC library for the specified database system is available.
		try {
			if (databaseType == DatabaseType.SQLITE) {
				Class.forName("org.sqlite.JDBC");
			} else if (databaseType == DatabaseType.MYSQL) {
				Class.forName("com.mysql.jdbc.Driver");
			} else {
				Class.forName("org.postgresql.Driver");
			}
		} catch (ClassNotFoundException e) {
			plugin.getLogger().severe(
					"The JBDC library for your database type was not found. Please read the plugin's support for more information.");
			plugin.setSuccessfulLoad(false);
		}

		if (databaseBackup && databaseType == DatabaseType.SQLITE) {
			File backup = new File(plugin.getDataFolder(), "achievements.db.bak");
			// Only do a daily backup for the .db file.
			if (System.currentTimeMillis() - backup.lastModified() > 86400000L || backup.length() == 0L) {
				plugin.getLogger().info("Backing up database file...");
				try {
					FileManager fileManager = new FileManager("achievements.db", plugin);
					fileManager.backupFile();
				} catch (IOException e) {
					plugin.getLogger().log(Level.SEVERE, "Error while backing up database file:", e);
					plugin.setSuccessfulLoad(false);
				}
			}
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

		DatabaseUpdater databaseUpdater = new DatabaseUpdater(plugin, this);
		databaseUpdater.renameExistingTables(databaseAddress);
		databaseUpdater.initialiseTables();
		databaseUpdater.updateOldDBToMaterial();
		databaseUpdater.updateOldDBToDates();
		databaseUpdater.updateOldDBMobnameSize();
	}

	/**
	 * Loads plugin configuration and sets parameters relevant to the database system.
	 */
	private void configurationLoad() {
		achievementsChronologicalOrder = plugin.getPluginConfig().getBoolean("BookChronologicalOrder", true);
		tablePrefix = plugin.getPluginConfig().getString("TablePrefix", "");
		databaseBackup = plugin.getPluginConfig().getBoolean("DatabaseBackup", true);

		String localeString = plugin.getPluginConfig().getString("DateLocale", "en");
		boolean dateDisplayTime = plugin.getPluginConfig().getBoolean("DateDisplayTime", false);
		Locale locale = new Locale(localeString);
		if (dateDisplayTime) {
			dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, locale);
		} else {
			dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
		}

		String dataHandler = plugin.getPluginConfig().getString("DatabaseType", "sqlite");
		if ("mysql".equalsIgnoreCase(dataHandler)) {
			// Get parameters from the MySQL config category.
			databaseType = DatabaseType.MYSQL;
			databaseAddress = plugin.getPluginConfig().getString("MYSQL.Database",
					"jdbc:mysql://localhost:3306/minecraft");
			databaseUser = plugin.getPluginConfig().getString("MYSQL.User", "root");
			databasePassword = plugin.getPluginConfig().getString("MYSQL.Password", "root");
		} else if ("postgresql".equalsIgnoreCase(dataHandler)) {
			// Get parameters from the PostgreSQL config category.
			databaseType = DatabaseType.POSTGRESQL;
			databaseAddress = plugin.getPluginConfig().getString("POSTGRESQL.Database",
					"jdbc:postgresql://localhost:5432/minecraft");
			databaseUser = plugin.getPluginConfig().getString("POSTGRESQL.User", "root");
			databasePassword = plugin.getPluginConfig().getString("POSTGRESQL.Password", "root");
		} else {
			// No extra parameters to retrieve!
			databaseType = DatabaseType.SQLITE;
		}
	}

	/**
	 * Shuts the thread pool down and closes connection to database.
	 */
	public void shutdown() {
		pool.shutdown();
		try {
			// Wait a few seconds for remaining tasks to execute.
			if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
				plugin.getLogger().warning("Some write operations were not sent to the database.");
			}
		} catch (InterruptedException e) {
			plugin.getLogger().log(Level.SEVERE, "Error awaiting for pool to terminate its tasks.", e);
			Thread.currentThread().interrupt();
		} finally {
			try {
				Connection connection = sqlConnection.get();
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				plugin.getLogger().log(Level.SEVERE, "Error while closing connection to database.", e);
			}
		}
	}

	/**
	 * Retrieves SQL connection to MySQL, PostgreSQL or SQLite database.
	 */
	protected Connection getSQLConnection() {
		Connection oldConnection = sqlConnection.get();
		try {
			// Check if Connection was not previously closed.
			if (oldConnection == null || oldConnection.isClosed()) {
				Connection newConnection = createSQLConnection();
				if (!sqlConnection.compareAndSet(oldConnection, newConnection)) {
					newConnection.close();
				}
			}
		} catch (SQLException e) {
			plugin.getLogger().log(Level.SEVERE, "Error while attempting to retrieve connection to database: ", e);
			plugin.setSuccessfulLoad(false);
		}
		return sqlConnection.get();
	}

	/**
	 * Creates a new Connection object tothe database.
	 * 
	 * @return connection object to database
	 * @throws SQLException
	 */
	private Connection createSQLConnection() throws SQLException {
		if (databaseType == DatabaseType.SQLITE) {
			File dbfile = new File(plugin.getDataFolder(), "achievements.db");
			try {
				if (dbfile.createNewFile()) {
					plugin.getLogger().info("Successfully created database file.");
				}
			} catch (IOException e) {
				plugin.getLogger().log(Level.SEVERE, "Error while creating database file: ", e);
				plugin.setSuccessfulLoad(false);
			}
			return DriverManager.getConnection("jdbc:sqlite:" + dbfile);
		} else if (databaseType == DatabaseType.MYSQL) {
			return DriverManager.getConnection(databaseAddress + "?useSSL=false&autoReconnect=true&user=" + databaseUser
					+ "&password=" + databasePassword);
		} else {
			return DriverManager.getConnection(
					databaseAddress + "?autoReconnect=true&user=" + databaseUser + "&password=" + databasePassword);
		}
	}

	/**
	 * Gets the list of all the achievements of a player.
	 * 
	 * @param player
	 * @return array list with groups of 3 strings: achievement name, description and date
	 */
	public List<String> getPlayerAchievementsList(UUID player) {
		List<String> achievementsList = new ArrayList<>();
		Connection conn = getSQLConnection();
		try (Statement st = conn.createStatement()) {
			// Either oldest date to newest one or newest date to oldest one.
			String chronology = achievementsChronologicalOrder ? "ASC" : "DESC";
			ResultSet rs = st.executeQuery("SELECT * FROM " + tablePrefix + "achievements WHERE playername = '"
					+ player.toString() + "' ORDER BY date " + chronology);
			Map<String, String> achievementsAndDisplayNames = plugin.getAchievementsAndDisplayNames();
			while (rs.next()) {
				// Check for names with single quotes but also two single quotes, due to a bug in versions 3.0 to 3.0.2
				// where names containing single quotes were inserted with two single quotes in the database.
				String achName = StringUtils.replace(rs.getString(2), "''", "'");
				String displayName = achievementsAndDisplayNames.get(achName);
				if (Strings.isNullOrEmpty(displayName)) {
					achievementsList.add(achName);
				} else {
					achievementsList.add(displayName);
				}
				achievementsList.add(rs.getString(3));
				achievementsList.add(dateFormat.format(rs.getDate(4)));
			}
		} catch (SQLException e) {
			plugin.getLogger().log(Level.SEVERE, "SQL error while retrieving achievements: ", e);
		}
		return achievementsList;
	}

	/**
	 * Gets the date of reception of a specific achievement.
	 * 
	 * @param player
	 * @param achName
	 * @return date represented as a string
	 */
	public String getPlayerAchievementDate(UUID player, String achName) {
		String achDate = null;
		String query = "SELECT date FROM " + tablePrefix + "achievements WHERE playername = '" + player.toString()
				+ "' AND achievement = ?";
		if (achName.contains("'")) {
			// Check for names with single quotes but also two single quotes, due to a bug in versions 3.0 to 3.0.2
			// where names containing single quotes were inserted with two single quotes in the database.
			query = StringUtils.replaceOnce(query, "achievement = ?", "(achievement = ? OR achievement = ?)");
		}
		Connection conn = getSQLConnection();
		try (PreparedStatement prep = conn.prepareStatement(query)) {
			prep.setString(1, achName);
			if (achName.contains("'")) {
				prep.setString(2, StringUtils.replace(achName, "'", "''"));
			}
			ResultSet rs = prep.executeQuery();
			if (rs.next()) {
				achDate = dateFormat.format(rs.getDate(1));
			}
		} catch (SQLException e) {
			plugin.getLogger().log(Level.SEVERE, "SQL error while retrieving achievement date: ", e);
		}
		return achDate;
	}

	/**
	 * Gets the total number of achievements received by a player, using an UUID; this method is provided as a
	 * convenience for other plugins.
	 * 
	 * @param uuid
	 * @return number of achievements
	 */
	public int getPlayerAchievementsAmount(UUID player) {
		int achievementsAmount = 0;
		Connection conn = getSQLConnection();
		try (Statement st = conn.createStatement()) {
			ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + tablePrefix + "achievements WHERE playername = '"
					+ player.toString() + "'");
			if (rs.next()) {
				achievementsAmount = rs.getInt(1);
			}
		} catch (SQLException e) {
			plugin.getLogger().log(Level.SEVERE, "SQL error while counting player achievements: ", e);
		}
		return achievementsAmount;
	}

	/**
	 * Gets the list of players with the most achievements over a given period.
	 * 
	 * @param listLength
	 * @param start
	 * @return list with player UUIDs
	 */
	public List<String> getTopList(int listLength, long start) {
		List<String> topList = new ArrayList<>(2 * listLength);
		String query;
		if (start == 0L) {
			// We consider all the achievements; no date comparison.
			query = "SELECT playername, COUNT(*) FROM " + tablePrefix
					+ "achievements GROUP BY playername ORDER BY COUNT(*) DESC LIMIT " + listLength;
		} else {
			// We only consider achievement received after the start date; do date comparisons.
			query = "SELECT playername, COUNT(*) FROM " + tablePrefix
					+ "achievements WHERE date > ? GROUP BY playername ORDER BY COUNT(*) DESC LIMIT " + listLength;
		}
		Connection conn = getSQLConnection();
		try (PreparedStatement prep = conn.prepareStatement(query)) {
			if (start > 0L) {
				prep.setDate(1, new java.sql.Date(start));
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
	 * Gets number of players who have received at least one achievement after start date.
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
	 * Gets the rank of a player given his number of achievements received after start date.
	 * 
	 * @param player
	 * @param start
	 * @return player's rank
	 */
	public int getPlayerRank(UUID player, long start) {
		int rank = 0;
		String query;
		if (start == 0L) {
			// We consider all the achievements; no date comparison.
			query = "SELECT COUNT(*) FROM (SELECT COUNT(*) number FROM " + tablePrefix
					+ "achievements GROUP BY playername) AS achGroupedByPlayer WHERE number > (SELECT COUNT(*) FROM "
					+ tablePrefix + "achievements WHERE playername = '" + player.toString() + "')";
		} else {
			// We only consider achievement received after the start date; do date comparisons.
			query = "SELECT COUNT(*) FROM (SELECT COUNT(*) number FROM " + tablePrefix
					+ "achievements WHERE date > ? GROUP BY playername) AS achGroupedByPlayer WHERE number > (SELECT COUNT(*) FROM "
					+ tablePrefix + "achievements WHERE playername = '" + player.toString() + "' AND date > ?)";
		}
		Connection conn = getSQLConnection();
		try (PreparedStatement prep = conn.prepareStatement(query)) {
			if (start > 0L) {
				prep.setDate(1, new java.sql.Date(start));
				prep.setDate(2, new java.sql.Date(start));
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
	 * Registers a new achievement for a player; this method will distinguish between asynchronous and synchronous
	 * processing.
	 * 
	 * @param player
	 * @param achName
	 * @param achMessage
	 */
	public void registerAchievement(final UUID player, final String achName, final String achMessage) {
		new SQLWriteOperation() {

			@Override
			protected void performWrite() throws SQLException {
				String query;
				if (databaseType == DatabaseType.POSTGRESQL) {
					// PostgreSQL has no REPLACE operator. We have to use the INSERT ... ON CONFLICT construct, which is
					// available for PostgreSQL 9.5+.
					query = "INSERT INTO " + tablePrefix + "achievements VALUES ('" + player.toString()
							+ "',?,?,?) ON CONFLICT (playername,achievement) DO UPDATE SET (description,date)=(?,?)";
				} else {
					query = "REPLACE INTO " + tablePrefix + "achievements VALUES ('" + player.toString() + "',?,?,?)";
				}
				Connection conn = getSQLConnection();
				try (PreparedStatement prep = conn.prepareStatement(query)) {
					prep.setString(1, achName);
					prep.setString(2, achMessage);
					prep.setDate(3, new java.sql.Date(new java.util.Date().getTime()));
					if (databaseType == DatabaseType.POSTGRESQL) {
						prep.setString(4, achMessage);
						prep.setDate(5, new java.sql.Date(new java.util.Date().getTime()));
					}
					prep.execute();
				}
			}
		}.executeOperation(pool, plugin.getLogger(), "SQL error while registering achievement.");
	}

	/**
	 * Checks whether player has received a specific achievement. Access through PoolsManager.
	 * 
	 * @param player
	 * @param achName
	 * @return true if achievement found in database, false otherwise
	 */
	public boolean hasPlayerAchievement(UUID player, String achName) {
		boolean result = false;
		String query = "SELECT achievement FROM " + tablePrefix + "achievements WHERE playername = '"
				+ player.toString() + "' AND achievement = ?";
		if (achName.contains("'")) {
			// Check for names with single quotes but also two single quotes, due to a bug in versions 3.0 to 3.0.2
			// where names containing single quotes were inserted with two single quotes in the database.
			query = StringUtils.replaceOnce(query, "achievement = ?", "(achievement = ? OR achievement = ?)");
		}
		Connection conn = getSQLConnection();
		try (PreparedStatement prep = conn.prepareStatement(query)) {
			prep.setString(1, achName);
			if (achName.contains("'")) {
				prep.setString(2, StringUtils.replace(achName, "'", "''"));
			}
			if (prep.executeQuery().next()) {
				result = true;
			}
		} catch (SQLException e) {
			plugin.getLogger().log(Level.SEVERE, "SQL error while checking achievement: ", e);
		}
		return result;
	}

	/**
	 * Gets the amount of a NormalAchievement statistic.
	 * 
	 * @param player
	 * @param table
	 * @return statistic
	 */
	public long getNormalAchievementAmount(UUID player, NormalAchievements category) {
		long amount = 0;
		String dbName = category.toDBName();
		Connection conn = getSQLConnection();
		try (Statement st = conn.createStatement()) {
			ResultSet rs = st.executeQuery("SELECT " + dbName + " FROM " + tablePrefix + dbName
					+ " WHERE playername = '" + player.toString() + "'");
			while (rs.next()) {
				amount = rs.getLong(dbName);
			}
		} catch (SQLException e) {
			plugin.getLogger().log(Level.SEVERE, "SQL error while retrieving " + dbName + " stats: ", e);
		}
		return amount;
	}

	/**
	 * Gets the amount of a MultipleAchievement statistic.
	 * 
	 * @param player
	 * @param category
	 * @param subcategory
	 * @return statistic
	 */
	public long getMultipleAchievementAmount(UUID player, MultipleAchievements category, String subcategory) {
		long amount = 0;
		String dbName = category.toDBName();
		Connection conn = getSQLConnection();
		try (PreparedStatement prep = conn.prepareStatement("SELECT " + dbName + " FROM " + tablePrefix + dbName
				+ " WHERE playername = '" + player.toString() + "' AND " + category.toSubcategoryDBName() + " = ?")) {
			prep.setString(1, subcategory);
			ResultSet rs = prep.executeQuery();
			while (rs.next()) {
				amount = rs.getLong(dbName);
			}
		} catch (SQLException e) {
			plugin.getLogger().log(Level.SEVERE, "SQL error while retrieving " + dbName + " stats: ", e);
		}
		return amount;
	}

	/**
	 * Returns player's number of connections on separate days (used by GUI).
	 * 
	 * @param player
	 * @return connections statistic
	 */
	public int getConnectionsAmount(UUID player) {
		String dbName = NormalAchievements.CONNECTIONS.toDBName();
		int connections = 0;
		Connection conn = getSQLConnection();
		try (Statement st = conn.createStatement()) {
			ResultSet rs = st.executeQuery("SELECT " + dbName + " FROM " + tablePrefix + dbName
					+ " WHERE playername = '" + player.toString() + "'");
			while (rs.next()) {
				connections = rs.getInt(dbName);
			}
		} catch (SQLException e) {
			plugin.getLogger().log(Level.SEVERE, "SQL error while retrieving connection statistics: ", e);
		}
		return connections;
	}

	/**
	 * Gets a player's last connection date.
	 * 
	 * @param player
	 * @return String with date
	 */
	public String getPlayerConnectionDate(UUID player) {
		String date = null;
		Connection conn = getSQLConnection();
		try (Statement st = conn.createStatement()) {
			ResultSet rs = st.executeQuery("SELECT date FROM " + tablePrefix + NormalAchievements.CONNECTIONS.toDBName()
					+ " WHERE playername = '" + player.toString() + "'");
			while (rs.next()) {
				date = rs.getString("date");
			}
		} catch (SQLException e) {
			plugin.getLogger().log(Level.SEVERE, "SQL error while retrieving connection date stats: ", e);
		}
		return date;
	}

	/**
	 * Updates player's number of connections and last connection date and returns number of connections (used by
	 * Connections listener).
	 * 
	 * @param player
	 * @param date
	 * @return connections statistic
	 */
	public int updateAndGetConnection(final UUID player, final String date) {
		final String dbName = NormalAchievements.CONNECTIONS.toDBName();
		Connection conn = getSQLConnection();
		try (PreparedStatement prep = conn
				.prepareStatement("SELECT " + dbName + " FROM " + tablePrefix + dbName + " WHERE playername = ?")) {
			prep.setString(1, player.toString());
			ResultSet rs = prep.executeQuery();
			int prev = 0;
			while (rs.next()) {
				prev = rs.getInt(dbName);
			}
			final int newConnections = prev + 1;
			new SQLWriteOperation() {

				@Override
				protected void performWrite() throws SQLException {
					Connection conn = getSQLConnection();
					String query;
					if (databaseType == DatabaseType.POSTGRESQL) {
						// PostgreSQL has no REPLACE operator. We have to use the INSERT ... ON CONFLICT construct,
						// which is available for PostgreSQL 9.5+.
						query = "INSERT INTO " + tablePrefix + dbName + " VALUES ('" + player.toString() + "', "
								+ newConnections + ", ?)" + " ON CONFLICT (playername) DO UPDATE SET (" + dbName
								+ ",date)=('" + newConnections + "', ?)";
					} else {
						query = "REPLACE INTO " + tablePrefix + dbName + " VALUES ('" + player.toString() + "', "
								+ newConnections + ", ?)";
					}
					try (PreparedStatement prep = conn.prepareStatement(query)) {
						if (databaseType == DatabaseType.POSTGRESQL) {
							prep.setString(1, date);
							prep.setString(2, date);
						} else {
							prep.setString(1, date);
						}
						prep.execute();
					}
				}
			}.executeOperation(pool, plugin.getLogger(), "SQL error while updating connection.");
			return newConnections;
		} catch (SQLException e) {
			plugin.getLogger().log(Level.SEVERE, "SQL error while handling connection event: ", e);
		}
		return 0;
	}

	/**
	 * Updates statistic for a Normal category.
	 * 
	 * @param player
	 * @param statistic
	 * @param dbName
	 * @return statistic
	 */
	protected void updateNormalStatistic(final UUID player, final long statistic, final NormalAchievements category) {
		new SQLWriteOperation() {

			@Override
			protected void performWrite() throws SQLException {
				Connection conn = getSQLConnection();
				try (Statement st = conn.createStatement()) {
					// Update statistic.
					if (databaseType == DatabaseType.POSTGRESQL) {
						// PostgreSQL has no REPLACE operator. We have to use the INSERT ... ON CONFLICT construct,
						// which is available for PostgreSQL 9.5+.
						st.execute("INSERT INTO " + tablePrefix + category.toDBName() + " VALUES ('" + player + "', "
								+ statistic + ")" + " ON CONFLICT (playername) DO UPDATE SET (" + category.toDBName()
								+ ")=('" + statistic + "')");
					} else {
						st.execute("REPLACE INTO " + tablePrefix + category.toDBName() + " VALUES ('" + player + "', "
								+ statistic + ")");
					}
				}
			}
		}.executeOperation(pool, plugin.getLogger(), "SQL error while handling " + category.toDBName() + " update.");
	}

	/**
	 * Updates statistic for a Multiple category.
	 * 
	 * @param player
	 * @param statistic
	 * @param dbName
	 * @return statistic
	 */
	protected void updateMultipleStatistic(final UUID player, final long statistic, final MultipleAchievements category,
			final String subcategory) {
		new SQLWriteOperation() {

			@Override
			protected void performWrite() throws SQLException {
				Connection conn = getSQLConnection();
				String query;
				if (databaseType == DatabaseType.POSTGRESQL) {
					// PostgreSQL has no REPLACE operator. We have to use the INSERT ... ON CONFLICT construct,
					// which is available for PostgreSQL 9.5+.
					query = "INSERT INTO " + tablePrefix + category.toDBName() + " VALUES ('" + player.toString()
							+ "', ?, " + statistic + ")" + " ON CONFLICT (playername, " + category.toSubcategoryDBName()
							+ ") DO UPDATE SET (" + category.toDBName() + ")=('" + statistic + "')";
				} else {
					query = "REPLACE INTO " + tablePrefix + category.toDBName() + " VALUES ('" + player.toString()
							+ "', ?, " + statistic + ")";
				}
				try (PreparedStatement prep = conn.prepareStatement(query)) {
					// Update statistic.
					prep.setString(1, subcategory);
					prep.execute();
				}
			}
		}.executeOperation(pool, plugin.getLogger(), "SQL error while handling " + category.toDBName() + " update.");
	}

	/**
	 * Deletes an achievement from a player.
	 * 
	 * @param player
	 * @param achName
	 */
	public void deletePlayerAchievement(final UUID player, final String achName) {
		new SQLWriteOperation() {

			@Override
			protected void performWrite() throws SQLException {
				Connection conn = getSQLConnection();
				String query = "DELETE FROM " + tablePrefix + "achievements WHERE playername = '" + player.toString()
						+ "' AND achievement = ?";
				if (achName.contains("'")) {
					// Check for names with single quotes but also two single quotes, due to a bug in versions 3.0 to
					// 3.0.2 where names containing single quotes were inserted with two single quotes in the database.
					query = StringUtils.replaceOnce(query, "achievement = ?", "(achievement = ? OR achievement = ?)");
				}
				try (PreparedStatement prep = conn.prepareStatement(query)) {
					prep.setString(1, achName);
					if (achName.contains("'")) {
						prep.setString(2, StringUtils.replace(achName, "'", "''"));
					}
					prep.execute();
				}
			}
		}.executeOperation(pool, plugin.getLogger(), "SQL error while deleting achievement.");
	}

	/**
	 * Clear Connection statistics for a given player.
	 * 
	 * @param player
	 */
	public void clearConnection(final UUID player) {
		new SQLWriteOperation() {

			@Override
			protected void performWrite() throws SQLException {
				Connection conn = getSQLConnection();
				try (Statement st = conn.createStatement()) {
					st.executeQuery("DELETE FROM " + tablePrefix + "connections WHERE playername = '"
							+ player.toString() + "'");
				}
			}
		}.executeOperation(pool, plugin.getLogger(), "SQL error while deleting connections.");
	}

	public DatabaseType getDatabaseType() {
		return databaseType;
	}

	protected String getTablePrefix() {
		return tablePrefix;
	}
}
