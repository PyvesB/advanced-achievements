package com.hm.achievement.db;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.db.data.AwardedDBAchievement;
import com.hm.achievement.exception.PluginLoadError;
import com.hm.achievement.lifecycle.Reloadable;
import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Abstract class in charge of factoring out common functionality for the database manager.
 *
 * @author Pyves
 */
public abstract class AbstractDatabaseManager implements Reloadable {

	// Used to do perform the database write operations asynchronously.
	ExecutorService pool;
	// Connection to the database; remains opened and shared.
	final AtomicReference<Connection> sqlConnection = new AtomicReference<>();
	final CommentedYamlConfiguration mainConfig;
	final Logger logger;
	final String driverPath;

	volatile String prefix;

	private final Map<String, String> namesToDisplayNames;
	private final DatabaseUpdater databaseUpdater;

	private DateFormat dateFormat;
	private boolean configBookChronologicalOrder;

	public AbstractDatabaseManager(CommentedYamlConfiguration mainConfig, Logger logger,
			Map<String, String> namesToDisplayNames, DatabaseUpdater databaseUpdater, String driverPath) {
		this.mainConfig = mainConfig;
		this.logger = logger;
		this.namesToDisplayNames = namesToDisplayNames;
		this.databaseUpdater = databaseUpdater;
		this.driverPath = driverPath;
		// We expect to execute many short writes to the database. The pool can grow dynamically under high load and
		// allows to reuse threads.
		pool = Executors.newCachedThreadPool();
	}

	@Override
	public void extractConfigurationParameters() {
		configBookChronologicalOrder = mainConfig.getBoolean("BookChronologicalOrder", true);
		String localeString = mainConfig.getString("DateLocale", "en");
		boolean dateDisplayTime = mainConfig.getBoolean("DateDisplayTime");
		Locale locale = new Locale(localeString);
		if (dateDisplayTime) {
			dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, locale);
		} else {
			dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
		}
	}

	/**
	 * Initialises the database system by extracting settings, performing setup tasks and updating schemas if necessary.
	 *
	 * @throws PluginLoadError
	 */
	public void initialise() throws PluginLoadError {
		logger.info("Initialising database...");

		prefix = mainConfig.getString("TablePrefix", "");

		try {
			performPreliminaryTasks();
		} catch (ClassNotFoundException e) {
			logger.severe("The JBDC driver for the chosen database type was not found.");
		} catch (UnsupportedEncodingException e) {
			logger.log(Level.SEVERE, "Error while encoding the database URL:", e);
		}

		// Try to establish connection with database; stays opened until explicitly closed by the plugin.
		Connection conn = getSQLConnection();

		if (conn == null) {
			throw new PluginLoadError("Failed to establish database connection. Please verify your settings in config.yml.");
		}

		databaseUpdater.renameExistingTables(this);
		databaseUpdater.initialiseTables(this);
		databaseUpdater.updateOldDBToTimestamps(this);
		Arrays.stream(MultipleAchievements.values()).forEach(m -> databaseUpdater.updateOldDBColumnSize(this, m));
	}

	/**
	 * Performs any needed tasks before opening a connection to the database.
	 *
	 * @throws ClassNotFoundException
	 * @throws PluginLoadError
	 * @throws UnsupportedEncodingException
	 */
	abstract void performPreliminaryTasks() throws ClassNotFoundException, PluginLoadError, UnsupportedEncodingException;

	/**
	 * Shuts the thread pool down and closes connection to database.
	 */
	public void shutdown() {
		pool.shutdown();
		try {
			// Wait a few seconds for remaining tasks to execute.
			if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
				logger.warning("Some write operations could not be sent to the database during plugin shutdown.");
			}
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE, "Error while waiting for database write operations to complete:", e);
			Thread.currentThread().interrupt();
		} finally {
			try {
				Connection connection = sqlConnection.get();
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				logger.log(Level.SEVERE, "Error while closing connection to the database:", e);
			}
		}
	}

	/**
	 * Retrieves SQL connection to MySQL, PostgreSQL, H2 or SQLite database.
	 *
	 * @return the cached SQL connection or a new one
	 */
	Connection getSQLConnection() {
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
			logger.log(Level.SEVERE, "Error while attempting to retrieve a connection to the database:", e);
		}
		return sqlConnection.get();
	}

	/**
	 * Creates a new Connection object to the database.
	 *
	 * @return connection object to database
	 * @throws SQLException
	 */
	abstract Connection createSQLConnection() throws SQLException;

	/**
	 * Gets the list of names of all the achievements of a player.
	 *
	 * @param uuid
	 * @return array list with Name parameters
	 */
	public List<String> getPlayerAchievementNamesList(UUID uuid) {
		String sql = "SELECT achievement FROM " + prefix + "achievements WHERE playername = ?";
		return ((SQLReadOperation<List<String>>) () -> {
			List<String> achievementNamesList = new ArrayList<>();
			Connection conn = getSQLConnection();
			try (PreparedStatement ps = conn.prepareStatement(sql)) {
				ps.setObject(1, uuid, Types.CHAR);
				ps.setFetchSize(1000);
				ResultSet rs = ps.executeQuery();
				while (rs.next()) {
					achievementNamesList.add(rs.getString(1));
				}
			}
			return achievementNamesList;
		}).executeOperation("retrieving the names of received achievements");
	}

	/**
	 * Gets the reception date of a specific achievement.
	 *
	 * @param uuid
	 * @param achName
	 * @return date represented as a string
	 */
	public String getPlayerAchievementDate(UUID uuid, String achName) {
		String sql = "SELECT date FROM " + prefix + "achievements WHERE playername = ? AND achievement = ?";
		return ((SQLReadOperation<String>) () -> {
			Connection conn = getSQLConnection();
			try (PreparedStatement ps = conn.prepareStatement(sql)) {
				ps.setObject(1, uuid, Types.CHAR);
				ps.setString(2, achName);
				ResultSet rs = ps.executeQuery();
				if (rs.next()) {
					return dateFormat.format(new Date(rs.getTimestamp(1).getTime()));
				}
			}
			return null;
		}).executeOperation("retrieving an achievement's reception date");
	}

	/**
	 * Gets the total number of achievements received by every player; this method is provided as a convenience for
	 * other plugins.
	 *
	 * @return map containing number of achievements for every players
	 */
	public Map<UUID, Integer> getPlayersAchievementsAmount() {
		String sql = "SELECT playername, COUNT(*) FROM " + prefix + "achievements GROUP BY playername";
		return ((SQLReadOperation<Map<UUID, Integer>>) () -> {
			Map<UUID, Integer> achievementAmounts = new HashMap<>();
			Connection conn = getSQLConnection();
			try (PreparedStatement ps = conn.prepareStatement(sql)) {
				ps.setFetchSize(1000);
				try (ResultSet rs = ps.executeQuery()) {
					while (rs.next()) {
						String uuid = rs.getString(1);
						if (StringUtils.isNotEmpty(uuid)) {
							achievementAmounts.put(UUID.fromString(uuid), rs.getInt(2));
						}
					}
				}
			}
			return achievementAmounts;
		}).executeOperation("counting all players' achievements");
	}

	/**
	 * Gets the total number of achievements received by a player, using an UUID.
	 *
	 * @param uuid
	 * @return number of achievements
	 */
	public int getPlayerAchievementsAmount(UUID uuid) {
		String sql = "SELECT COUNT(*) FROM " + prefix + "achievements WHERE playername = ?";
		return ((SQLReadOperation<Integer>) () -> {
			Connection conn = getSQLConnection();
			try (PreparedStatement ps = conn.prepareStatement(sql)) {
				ps.setObject(1, uuid, Types.CHAR);
				ResultSet rs = ps.executeQuery();
				rs.next();
				return rs.getInt(1);
			}
		}).executeOperation("counting a player's achievements");
	}

	/**
	 * Constructs a mapping of players with the most achievements over a given period.
	 *
	 * @param start
	 * @return LinkedHashMap with keys corresponding to player UUIDs and values corresponding to their achievement count
	 */
	public Map<String, Integer> getTopList(long start) {
		// Either consider all the achievements or only those received after the start date.
		String sql = start == 0L
				? "SELECT playername, COUNT(*) FROM " + prefix + "achievements GROUP BY playername ORDER BY COUNT(*) DESC"
				: "SELECT playername, COUNT(*) FROM " + prefix
						+ "achievements WHERE date > ? GROUP BY playername ORDER BY COUNT(*) DESC";
		return ((SQLReadOperation<Map<String, Integer>>) () -> {
			Map<String, Integer> topList = new LinkedHashMap<>();
			Connection conn = getSQLConnection();
			try (PreparedStatement ps = conn.prepareStatement(sql)) {
				if (start > 0L) {
					ps.setTimestamp(1, new Timestamp(start));
				}
				ps.setFetchSize(1000);
				ResultSet rs = ps.executeQuery();
				while (rs.next()) {
					topList.put(rs.getString(1), rs.getInt(2));
				}
			}
			return topList;
		}).executeOperation("computing the list of top players");
	}

	/**
	 * Registers a new achievement for a player with the reception time set to now.
	 *
	 * @param uuid
	 * @param achName
	 * @param achMessage
	 */
	public void registerAchievement(UUID uuid, String achName, String achMessage) {
		registerAchievement(uuid, achName, achMessage, System.currentTimeMillis());
	}

	/**
	 * Registers a new achievement for a player.
	 *
	 * @param uuid
	 * @param achName
	 * @param achMessage
	 * @param epochMs Moment the achievement was registered at.
	 */
	void registerAchievement(UUID uuid, String achName, String achMessage, long epochMs) {
		String sql = "REPLACE INTO " + prefix + "achievements VALUES (?,?,?,?)";
		((SQLWriteOperation) () -> {
			Connection conn = getSQLConnection();
			try (PreparedStatement ps = conn.prepareStatement(sql)) {
				ps.setObject(1, uuid, Types.CHAR);
				ps.setString(2, achName);
				ps.setString(3, achMessage == null ? "" : achMessage);
				ps.setTimestamp(4, new Timestamp(epochMs));
				ps.execute();
			}
		}).executeOperation(pool, logger, "registering an achievement");
	}

	/**
	 * Checks whether player has received a specific achievement.
	 *
	 * @param uuid
	 * @param achName
	 * @return true if achievement found in database, false otherwise
	 */
	public boolean hasPlayerAchievement(UUID uuid, String achName) {
		String sql = "SELECT achievement FROM " + prefix + "achievements WHERE playername = ? AND achievement = ?";
		return ((SQLReadOperation<Boolean>) () -> {
			Connection conn = getSQLConnection();
			try (PreparedStatement ps = conn.prepareStatement(sql)) {
				ps.setObject(1, uuid, Types.CHAR);
				ps.setString(2, achName);
				return ps.executeQuery().next();
			}
		}).executeOperation("checking for an achievement");
	}

	/**
	 * Gets a player's NormalAchievement statistic.
	 *
	 * @param uuid
	 * @param category
	 * @return statistic
	 */
	public long getNormalAchievementAmount(UUID uuid, NormalAchievements category) {
		String dbName = category.toDBName();
		String sql = "SELECT " + dbName + " FROM " + prefix + dbName + " WHERE playername = ?";
		return ((SQLReadOperation<Long>) () -> {
			Connection conn = getSQLConnection();
			try (PreparedStatement ps = conn.prepareStatement(sql)) {
				ps.setObject(1, uuid, Types.CHAR);
				ResultSet rs = ps.executeQuery();
				if (rs.next()) {
					return rs.getLong(dbName);
				}
			}
			return 0L;
		}).executeOperation("retrieving " + category + " statistics");
	}

	/**
	 * Gets a player's MultipleAchievement statistic.
	 *
	 * @param uuid
	 * @param category
	 * @param subcategory
	 * @return statistic
	 */
	public long getMultipleAchievementAmount(UUID uuid, MultipleAchievements category, String subcategory) {
		String dbName = category.toDBName();
		String sql = "SELECT " + dbName + " FROM " + prefix + dbName + " WHERE playername = ? AND "
				+ category.toSubcategoryDBName() + " = ?";
		return ((SQLReadOperation<Long>) () -> {
			Connection conn = getSQLConnection();
			try (PreparedStatement ps = conn.prepareStatement(sql)) {
				ps.setObject(1, uuid, Types.CHAR);
				ps.setString(2, subcategory);
				ResultSet rs = ps.executeQuery();
				if (rs.next()) {
					return rs.getLong(dbName);
				}
			}
			return 0L;
		}).executeOperation("retrieving " + category + "." + subcategory + " statistics");
	}

	/**
	 * Returns a player's number of connections on separate days (used by GUI).
	 *
	 * @param uuid
	 * @return connections statistic
	 */
	public int getConnectionsAmount(UUID uuid) {
		String dbName = NormalAchievements.CONNECTIONS.toDBName();
		String sql = "SELECT " + dbName + " FROM " + prefix + dbName + " WHERE playername = ?";
		return ((SQLReadOperation<Integer>) () -> {
			Connection conn = getSQLConnection();
			try (PreparedStatement ps = conn.prepareStatement(sql)) {
				ps.setObject(1, uuid, Types.CHAR);
				ResultSet rs = ps.executeQuery();
				if (rs.next()) {
					return rs.getInt(dbName);
				}
			}
			return 0;
		}).executeOperation("retrieving connection statistics");
	}

	/**
	 * Gets a player's last connection date.
	 *
	 * @param uuid
	 * @return String with date
	 */
	public String getPlayerConnectionDate(UUID uuid) {
		String dbName = NormalAchievements.CONNECTIONS.toDBName();
		String sql = "SELECT date FROM " + prefix + dbName + " WHERE playername = ?";
		return ((SQLReadOperation<String>) () -> {
			Connection conn = getSQLConnection();
			try (PreparedStatement ps = conn.prepareStatement(sql)) {
				ps.setObject(1, uuid, Types.CHAR);
				ResultSet rs = ps.executeQuery();
				if (rs.next()) {
					return rs.getString("date");
				}
			}
			return null;
		}).executeOperation("retrieving a player's last connection date");
	}

	/**
	 * Updates a player's number of connections and last connection date and returns number of connections (used by
	 * Connections listener).
	 *
	 * @param uuid
	 * @param date
	 * @return connections statistic
	 */
	public int updateAndGetConnection(UUID uuid, String date) {
		String dbName = NormalAchievements.CONNECTIONS.toDBName();
		String sqlRead = "SELECT " + dbName + " FROM " + prefix + dbName + " WHERE playername = ?";
		return ((SQLReadOperation<Integer>) () -> {
			Connection conn = getSQLConnection();
			try (PreparedStatement ps = conn.prepareStatement(sqlRead)) {
				ps.setObject(1, uuid, Types.CHAR);
				ResultSet rs = ps.executeQuery();
				int connections = rs.next() ? rs.getInt(dbName) + 1 : 1;
				String sqlWrite = "REPLACE INTO " + prefix + dbName + " VALUES (?,?,?)";
				((SQLWriteOperation) () -> {
					Connection writeConn = getSQLConnection();
					try (PreparedStatement writePrep = writeConn.prepareStatement(sqlWrite)) {
						writePrep.setObject(1, uuid, Types.CHAR);
						writePrep.setInt(2, connections);
						writePrep.setString(3, date);
						writePrep.execute();
					}
				}).executeOperation(pool, logger, "updating connection date and count");
				return connections;
			}
		}).executeOperation("handling connection event");
	}

	/**
	 * Deletes an achievement from a player.
	 *
	 * @param uuid
	 * @param achName
	 */
	public void deletePlayerAchievement(UUID uuid, String achName) {
		String sql = "DELETE FROM " + prefix + "achievements WHERE playername = ? AND achievement = ?";
		((SQLWriteOperation) () -> {
			Connection conn = getSQLConnection();
			try (PreparedStatement ps = conn.prepareStatement(sql)) {
				ps.setObject(1, uuid, Types.CHAR);
				ps.setString(2, achName);
				ps.execute();
			}
		}).executeOperation(pool, logger, "deleting an achievement");
	}

	/**
	 * Deletes all achievements from a player.
	 *
	 * @param uuid
	 */
	public void deleteAllPlayerAchievements(UUID uuid) {
		String sql = "DELETE FROM " + prefix + "achievements WHERE playername = ?";
		((SQLWriteOperation) () -> {
			Connection conn = getSQLConnection();
			try (PreparedStatement ps = conn.prepareStatement(sql)) {
				ps.setObject(1, uuid, Types.CHAR);
				ps.execute();
			}
		}).executeOperation(pool, logger, "deleting all achievements");
	}

	/**
	 * Clears Connection statistics for a given player.
	 *
	 * @param uuid
	 */
	public void clearConnection(UUID uuid) {
		String sql = "DELETE FROM " + prefix + "connections WHERE playername = '" + uuid + "'";
		((SQLWriteOperation) () -> {
			Connection conn = getSQLConnection();
			try (PreparedStatement ps = conn.prepareStatement(sql)) {
				ps.execute();
			}
		}).executeOperation(pool, logger, "clearing connection statistics");
	}

	String getPrefix() {
		return prefix;
	}

	/**
	 * Returns a list of AwardedDBAchievements get by a player.
	 *
	 * @param uuid UUID of a player.
	 * @return ArrayList containing all information about achievements awarded to a player.
	 */
	public List<AwardedDBAchievement> getPlayerAchievementsList(UUID uuid) {
		// Either oldest date to newest one or newest date to oldest one.
		String sql = "SELECT * FROM " + prefix + "achievements WHERE playername = ? ORDER BY date "
				+ (configBookChronologicalOrder ? "ASC" : "DESC");
		return ((SQLReadOperation<List<AwardedDBAchievement>>) () -> {
			List<AwardedDBAchievement> achievements = new ArrayList<>();
			Connection conn = getSQLConnection();
			try (PreparedStatement ps = conn.prepareStatement(sql)) {
				ps.setFetchSize(1000);
				ps.setObject(1, uuid, Types.CHAR);
				try (ResultSet rs = ps.executeQuery()) {
					while (rs.next()) {
						String achName = rs.getString(2);
						String displayName = namesToDisplayNames.get(achName);
						if (StringUtils.isNotBlank(displayName)) {
							achName = displayName;
						}
						String achMsg = rs.getString(3);
						Timestamp dateAwarded = rs.getTimestamp(4);

						achievements.add(new AwardedDBAchievement(uuid, achName, achMsg, dateAwarded.getTime(),
								dateFormat.format(dateAwarded)));
					}
				}
			}
			return achievements;
		}).executeOperation("retrieving the full data of received achievements");
	}

	/**
	 * Retrieve matching list of achievements for a name of an achievement.
	 * <p>
	 * Limited to 1000 most recent entries to save memory.
	 *
	 * @param achievementName Name of an achievement in database format.
	 * @return List of AwardedDBAchievement objects, message field is empty to save memory.
	 */
	public List<AwardedDBAchievement> getAchievementsRecipientList(String achievementName) {
		String sql = "SELECT playername, date FROM " + prefix + "achievements WHERE achievement = ?" +
				" ORDER BY date DESC LIMIT 1000";
		return ((SQLReadOperation<List<AwardedDBAchievement>>) () -> {
			List<AwardedDBAchievement> achievements = new ArrayList<>();
			Connection conn = getSQLConnection();
			try (PreparedStatement ps = conn.prepareStatement(sql)) {
				ps.setFetchSize(1000);
				ps.setString(1, achievementName);
				try (ResultSet rs = ps.executeQuery()) {
					while (rs.next()) {
						UUID uuid;
						try {
							String uuidString = rs.getString("playername");
							uuid = UUID.fromString(uuidString);
						} catch (IllegalArgumentException improperUUIDFormatException) {
							continue;
						}
						Date dateAwarded = new Date(rs.getTimestamp("date").getTime());

						achievements.add(new AwardedDBAchievement(uuid, namesToDisplayNames.get(achievementName), "",
								dateAwarded.getTime(), dateFormat.format(dateAwarded)));
					}
				}
			}
			return achievements;
		}).executeOperation("retrieving the recipients of an achievement");
	}
}
