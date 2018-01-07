package com.hm.achievement.db;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.db.data.AwardedDBAchievement;
import com.hm.achievement.exception.PluginLoadError;
import com.hm.achievement.utils.Reloadable;
import org.apache.commons.lang3.StringUtils;

import java.sql.*;
import java.sql.Date;
import java.text.DateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

/**
 * Abstract class in charge of factoring out common functionality for the database manager.
 *
 * @author Pyves
 */
public abstract class AbstractSQLDatabaseManager implements Reloadable {

	protected final AdvancedAchievements plugin;
	// Used to do perform the database write operations asynchronously.
	protected ExecutorService pool;
	// Connection to the database; remains opened and shared.
	protected final AtomicReference<Connection> sqlConnection;

	protected volatile String databaseAddress;
	protected volatile String databaseUser;
	protected volatile String databasePassword;
	protected volatile String additionalConnectionOptions;
	protected volatile String prefix;

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
	 *
	 * @throws PluginLoadError
	 */
	public void initialise() throws PluginLoadError {
		plugin.getLogger().info("Initialising database... ");

		prefix = plugin.getPluginConfig().getString("TablePrefix", "");
		additionalConnectionOptions = plugin.getPluginConfig().getString("AdditionalConnectionOptions", "");

		try {
			performPreliminaryTasks();
		} catch (ClassNotFoundException e) {
			plugin.getLogger().severe("The JBDC driver for the chosen database type was not found.");
		}

		// Try to establish connection with database; stays opened until explicitly closed by the plugin.
		Connection conn = getSQLConnection();

		if (conn == null) {
			throw new PluginLoadError("Could not establish SQL connection. Please verify your settings in config.yml.");
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
	 * @throws PluginLoadError
	 */
	protected abstract void performPreliminaryTasks() throws ClassNotFoundException, PluginLoadError;

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
			plugin.getLogger().log(Level.SEVERE, "Error while attempting to retrieve connection to database.", e);
		}
		return sqlConnection.get();
	}

	/**
	 * Creates a new Connection object to the database.
	 *
	 * @return connection object to database
	 * @throws SQLException
	 */
	protected abstract Connection createSQLConnection() throws SQLException;

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
				ps.setObject(1, uuid);
				ps.setFetchSize(1000);
				ResultSet rs = ps.executeQuery();
				while (rs.next()) {
					// Check for names with single quotes but also two single quotes, due to a bug in versions 3.0 to
					// 3.0.2 where names containing single quotes were inserted with two single quotes in the database.
					achievementNamesList.add(StringUtils.replace(rs.getString(1), "''", "'"));
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
		// Check for names with single quotes but also two single quotes, due to a bug in versions 3.0 to 3.0.2
		// where names containing single quotes were inserted with two single quotes in the database.
		String sql = achName.contains("'")
				? "SELECT date FROM " + prefix
				+ "achievements WHERE playername = ? AND (achievement = ? OR achievement = ?)"
				: "SELECT date FROM " + prefix + "achievements WHERE playername = ? AND achievement = ?";
		return ((SQLReadOperation<String>) () -> {
			Connection conn = getSQLConnection();
			try (PreparedStatement ps = conn.prepareStatement(sql)) {
				ps.setObject(1, uuid);
				ps.setString(2, achName);
				if (achName.contains("'")) {
					ps.setString(3, StringUtils.replace(achName, "'", "''"));
				}
				ResultSet rs = ps.executeQuery();
				if (rs.next()) {
					return dateFormat.format(rs.getDate(1));
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
				ps.setObject(1, uuid);
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
				? "SELECT playername, COUNT(*) FROM " + prefix
				+ "achievements GROUP BY playername ORDER BY COUNT(*) DESC"
				: "SELECT playername, COUNT(*) FROM " + prefix
				+ "achievements WHERE date > ? GROUP BY playername ORDER BY COUNT(*) DESC";
		return ((SQLReadOperation<Map<String, Integer>>) () -> {
			Map<String, Integer> topList = new LinkedHashMap<>();
			Connection conn = getSQLConnection();
			try (PreparedStatement ps = conn.prepareStatement(sql)) {
				if (start > 0L) {
					ps.setDate(1, new Date(start));
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
	 * @param epochMs    Moment the achievement was registered at.
	 */
	protected void registerAchievement(UUID uuid, String achName, String achMessage, long epochMs) {
		String sql = "REPLACE INTO " + prefix + "achievements VALUES (?,?,?,?)";
		((SQLWriteOperation) () -> {
			Connection conn = getSQLConnection();
			try (PreparedStatement ps = conn.prepareStatement(sql)) {
				ps.setObject(1, uuid);
				ps.setString(2, achName);
				ps.setString(3, achMessage == null ? "" : achMessage);
				ps.setDate(4, new Date(epochMs));
				ps.execute();
			}
		}).executeOperation(pool, plugin.getLogger(), "registering an achievement");
	}

	/**
	 * Checks whether player has received a specific achievement.
	 *
	 * @param uuid
	 * @param achName
	 * @return true if achievement found in database, false otherwise
	 */
	public boolean hasPlayerAchievement(UUID uuid, String achName) {
		// Check for names with single quotes but also two single quotes, due to a bug in versions 3.0 to 3.0.2
		// where names containing single quotes were inserted with two single quotes in the database.
		String sql = achName.contains("'")
				? "SELECT achievement FROM " + prefix
				+ "achievements WHERE playername = ? AND (achievement = ? OR achievement = ?)"
				: "SELECT achievement FROM " + prefix + "achievements WHERE playername = ? AND achievement = ?";
		return ((SQLReadOperation<Boolean>) () -> {
			Connection conn = getSQLConnection();
			try (PreparedStatement ps = conn.prepareStatement(sql)) {
				ps.setObject(1, uuid);
				ps.setString(2, achName);
				if (achName.contains("'")) {
					ps.setString(3, StringUtils.replace(achName, "'", "''"));
				}
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
				ps.setObject(1, uuid);
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
				ps.setObject(1, uuid);
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
				ps.setObject(1, uuid);
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
				ps.setObject(1, uuid);
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
				ps.setObject(1, uuid);
				ResultSet rs = ps.executeQuery();
				int connections = rs.next() ? rs.getInt(dbName) + 1 : 1;
				String sqlWrite = "REPLACE INTO " + prefix + dbName + " VALUES (?,?,?)";
				((SQLWriteOperation) () -> {
					Connection writeConn = getSQLConnection();
					try (PreparedStatement writePrep = writeConn.prepareStatement(sqlWrite)) {
						writePrep.setObject(1, uuid);
						writePrep.setInt(2, connections);
						writePrep.setString(3, date);
						writePrep.execute();
					}
				}).executeOperation(pool, plugin.getLogger(), "updating connection date and count");
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
		// Check for names with single quotes but also two single quotes, due to a bug in versions 3.0 to 3.0.2
		// where names containing single quotes were inserted with two single quotes in the database.
		String sql = achName.contains("'")
				? "DELETE FROM " + prefix + "achievements WHERE playername = ? AND (achievement = ? OR achievement = ?)"
				: "DELETE FROM " + prefix + "achievements WHERE playername = ? AND achievement = ?";
		((SQLWriteOperation) () -> {
			Connection conn = getSQLConnection();
			try (PreparedStatement ps = conn.prepareStatement(sql)) {
				ps.setObject(1, uuid);
				ps.setString(2, achName);
				if (achName.contains("'")) {
					ps.setString(3, StringUtils.replace(achName, "'", "''"));
				}
				ps.execute();
			}
		}).executeOperation(pool, plugin.getLogger(), "deleting an achievement");
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
		}).executeOperation(pool, plugin.getLogger(), "clearing connection statistics");
	}

	protected String getPrefix() {
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
				ps.setObject(1, uuid);
				try (ResultSet rs = ps.executeQuery()) {
					while (rs.next()) {
						// Remove eventual double quotes due to a bug in versions 3.0 to 3.0.2 where names containing single
						// quotes were inserted with two single quotes in the database.
						String achName = StringUtils.replace(rs.getString(2), "''", "'");
						String displayName = plugin.getAchievementsAndDisplayNames().get(achName);
						if (StringUtils.isNotBlank(displayName)) {
							achName = displayName;
						}
						String achMsg = rs.getString(3);
						Date dateAwarded = rs.getDate(4);

						achievements.add(new AwardedDBAchievement(uuid, achName, achMsg, dateAwarded.getTime(), dateFormat.format(dateAwarded)));
					}
				}
			}
			return achievements;
		}).executeOperation("retrieving the full data of received achievements");
	}
}