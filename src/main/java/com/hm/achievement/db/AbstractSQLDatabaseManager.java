package com.hm.achievement.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

import org.apache.commons.lang3.StringUtils;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.utils.Reloadable;

/**
 * Abstract class in charge of factoring out common functionality for the database manager.
 * 
 * @author Pyves
 */
public abstract class AbstractSQLDatabaseManager implements Reloadable {

	protected final AdvancedAchievements plugin;
	// Used to do some write operations to the database asynchronously.
	protected final ExecutorService pool;
	// Connection to the database; remains opened and shared.
	protected final AtomicReference<Connection> sqlConnection;

	protected volatile String databaseAddress;
	protected volatile String databaseUser;
	protected volatile String databasePassword;
	protected volatile String tablePrefix;
	protected volatile String additionalConnectionOptions;

	private DateFormat dateFormat;
	private boolean configBookChronologicalOrder;

	public AbstractSQLDatabaseManager(AdvancedAchievements plugin) {
		this.plugin = plugin;
		// We expect to execute many short writes to the database. The pool can grow dynamically under high load and
		// allows to reuse threads.
		pool = Executors.newCachedThreadPool();
		sqlConnection = new AtomicReference<>();
	}

	@Override
	public void extractConfigurationParameters() {
		configBookChronologicalOrder = plugin.getPluginConfig().getBoolean("BookChronologicalOrder", true);
		String localeString = plugin.getPluginConfig().getString("DateLocale", "en");
		boolean dateDisplayTime = plugin.getPluginConfig().getBoolean("DateDisplayTime", false);
		Locale locale = new Locale(localeString);
		if (dateDisplayTime) {
			dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, locale);
		} else {
			dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
		}
	}

	/**
	 * Initialises the database system by extracting settings, performing setup tasks and updating schemas if necessary.
	 */
	public void initialise() {
		plugin.getLogger().info("Initialising database... ");

		tablePrefix = plugin.getPluginConfig().getString("TablePrefix", "");
		additionalConnectionOptions = plugin.getPluginConfig().getString("AdditionalConnectionOptions", "");

		try {
			performPreliminaryTasks();
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

		DatabaseUpdater databaseUpdater = new DatabaseUpdater(plugin, this);
		databaseUpdater.renameExistingTables(databaseAddress);
		databaseUpdater.initialiseTables();
		databaseUpdater.updateOldDBToMaterial();
		databaseUpdater.updateOldDBToDates();
		databaseUpdater.updateOldDBMobnameSize();
	}

	/**
	 * Performs any needed tasks before opening a connection to the database.
	 * 
	 * @throws ClassNotFoundException
	 */
	protected abstract void performPreliminaryTasks() throws ClassNotFoundException;

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
	 * 
	 * @return the cached SQL connection or a new one
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
	protected abstract Connection createSQLConnection() throws SQLException;

	/**
	 * Gets the list of all the achievements of a player, sorted by chronological or reverse ordering.
	 * 
	 * @param player
	 * @return array list with groups of 3 strings: achievement name, description and date
	 */
	public List<String> getPlayerAchievementsList(UUID player) {
		List<String> achievementsList = new ArrayList<>();
		Connection conn = getSQLConnection();
		try (Statement st = conn.createStatement()) {
			// Either oldest date to newest one or newest date to oldest one.
			String chronology = configBookChronologicalOrder ? "ASC" : "DESC";
			ResultSet rs = st.executeQuery("SELECT * FROM " + tablePrefix + "achievements WHERE playername = '"
					+ player.toString() + "' ORDER BY date " + chronology);
			Map<String, String> achievementsAndDisplayNames = plugin.getAchievementsAndDisplayNames();
			while (rs.next()) {
				// Check for names with single quotes but also two single quotes, due to a bug in versions 3.0 to 3.0.2
				// where names containing single quotes were inserted with two single quotes in the database.
				String achName = StringUtils.replace(rs.getString(2), "''", "'");
				String displayName = achievementsAndDisplayNames.get(achName);
				if (StringUtils.isNotBlank(displayName)) {
					achievementsList.add(displayName);
				} else {
					achievementsList.add(achName);
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
	 * Gets the list of names of all the achievements of a player.
	 * 
	 * @param player
	 * @return array list with Name parameters
	 */
	public List<String> getPlayerAchievementNamesList(UUID player) {
		List<String> achievementNamesList = new ArrayList<>();
		Connection conn = getSQLConnection();
		try (Statement st = conn.createStatement()) {
			ResultSet rs = st.executeQuery("SELECT achievement FROM " + tablePrefix
					+ "achievements WHERE playername = '" + player.toString() + "'");
			while (rs.next()) {
				// Check for names with single quotes but also two single quotes, due to a bug in versions 3.0 to 3.0.2
				// where names containing single quotes were inserted with two single quotes in the database.
				achievementNamesList.add(StringUtils.replace(rs.getString(1), "''", "'"));
			}
		} catch (SQLException e) {
			plugin.getLogger().log(Level.SEVERE, "SQL error while retrieving achievement names: ", e);
		}
		return achievementNamesList;
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
	 * Gets the total number of achievements received by every player; this method is provided as a convenience for
	 * other plugins.
	 * 
	 * @return map containing number of achievements for every players
	 */
	public Map<UUID, Integer> getPlayersAchievementsAmount() {
		Map<UUID, Integer> achievementAmounts = new HashMap<>();
		Connection conn = getSQLConnection();
		try (Statement st = conn.createStatement()) {
			ResultSet rs = st.executeQuery(
					"SELECT playername, COUNT(*) FROM " + tablePrefix + "achievements GROUP BY playername");
			while (rs.next()) {
				achievementAmounts.put(UUID.fromString(rs.getString(1)), rs.getInt(2));
			}
		} catch (SQLException e) {
			plugin.getLogger().log(Level.SEVERE, "SQL error while counting all player achievements: ", e);
		}
		return achievementAmounts;
	}

	/**
	 * Gets the total number of achievements received by a player, using an UUID.
	 * 
	 * @param player
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
		((SQLWriteOperation) () -> {
			String query = "REPLACE INTO " + tablePrefix + "achievements VALUES ('" + player.toString() + "',?,?,?)";
			Connection conn = getSQLConnection();
			try (PreparedStatement prep = conn.prepareStatement(query)) {
				prep.setString(1, achName);
				prep.setString(2, achMessage);
				prep.setDate(3, new java.sql.Date(new java.util.Date().getTime()));
				prep.execute();
			}
		}).executeOperation(pool, plugin.getLogger(), "SQL error while registering achievement.");
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
	 * @param category
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
			((SQLWriteOperation) () -> {
				Connection writeConn = getSQLConnection();
				String query = "REPLACE INTO " + tablePrefix + dbName + " VALUES ('" + player.toString() + "', "
						+ newConnections + ", ?)";
				try (PreparedStatement writePrep = writeConn.prepareStatement(query)) {
					writePrep.setString(1, date);
					writePrep.execute();
				}
			}).executeOperation(pool, plugin.getLogger(), "SQL error while updating connection.");
			return newConnections;
		} catch (SQLException e) {
			plugin.getLogger().log(Level.SEVERE, "SQL error while handling connection event: ", e);
		}
		return 0;
	}

	/**
	 * Deletes an achievement from a player.
	 * 
	 * @param player
	 * @param achName
	 */
	public void deletePlayerAchievement(final UUID player, final String achName) {
		((SQLWriteOperation) () -> {
			Connection conn = getSQLConnection();
			String query = "DELETE FROM " + tablePrefix + "achievements WHERE playername = '" + player.toString()
					+ "' AND achievement = ?";
			if (achName.contains("'")) {
				// Check for names with single quotes but also two single quotes, due to a bug in versions 3.0 to 3.0.2
				// where names containing single quotes were inserted with two single quotes in the database.
				query = StringUtils.replaceOnce(query, "achievement = ?", "(achievement = ? OR achievement = ?)");
			}
			try (PreparedStatement prep = conn.prepareStatement(query)) {
				prep.setString(1, achName);
				if (achName.contains("'")) {
					prep.setString(2, StringUtils.replace(achName, "'", "''"));
				}
				prep.execute();
			}
		}).executeOperation(pool, plugin.getLogger(), "SQL error while deleting achievement.");
	}

	/**
	 * Clear Connection statistics for a given player.
	 * 
	 * @param player
	 */
	public void clearConnection(final UUID player) {
		((SQLWriteOperation) () -> {
			Connection conn = getSQLConnection();
			try (Statement st = conn.createStatement()) {
				st.executeQuery(
						"DELETE FROM " + tablePrefix + "connections WHERE playername = '" + player.toString() + "'");
			}
		}).executeOperation(pool, plugin.getLogger(), "SQL error while deleting connections.");
	}

	protected String getTablePrefix() {
		return tablePrefix;
	}
}
