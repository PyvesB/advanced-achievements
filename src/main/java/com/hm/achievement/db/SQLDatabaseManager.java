package com.hm.achievement.db;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.hm.achievement.AdvancedAchievements;

public class SQLDatabaseManager {

	private AdvancedAchievements plugin;
	private boolean sqliteDatabase;
	private String mysqlDatabase;
	private String mysqlUser;
	private String mysqlPassword;

	private Connection sqlConnection;

	public SQLDatabaseManager(AdvancedAchievements plugin) {

		this.plugin = plugin;
	}

	/**
	 * Initialise database system and plugin settings.
	 */
	public void initialise() {

		// Load plugin settings.
		configurationLoad();

		// Check if JDBC library available.
		try {
			if (sqliteDatabase)
				Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			plugin.getLogger()
					.severe("You need the SQLite JBDC library. Please download it and put it in /lib folder.");
			e.printStackTrace();
			plugin.setSuccessfulLoad(false);
		}

		// Try to establish connection with database.
		if (getSQLConnection() == null) {
			plugin.getLogger().severe("Could not establish SQL connection. Disabling Advanced Achievement.");
			plugin.getLogger().severe("Please verify your settings in the configuration file.");
			plugin.getServer().getPluginManager().disablePlugin(plugin);
			return;
		}

		// Initialise database tables.
		try {
			initialiseTables();

		} catch (SQLException e) {
			plugin.getLogger().severe("Error while initialising database tables: " + e);
			plugin.setSuccessfulLoad(false);
		}

	}

	/**
	 * Load plugin configuration and set values to different parameters relevant
	 * to the database system.
	 */
	private void configurationLoad() {

		String dataHandler = plugin.getConfig().getString("DatabaseType", "sqlite");
		if (dataHandler.equalsIgnoreCase("mysql")) {
			sqliteDatabase = false;
			mysqlDatabase = plugin.getConfig().getString("MYSQL.Database", "jdbc:mysql://localhost:3306/minecraft");
			mysqlUser = plugin.getConfig().getString("MYSQL.User", "root");
			mysqlPassword = plugin.getConfig().getString("MYSQL.Password", "root");
		} else
			sqliteDatabase = true;

	}

	/**
	 * Initialise database tables by creating non existing ones. Uses
	 * configuration file to determine which ones it is relevant to try to
	 * create.
	 */
	private void initialiseTables() throws SQLException {

		Statement st = sqlConnection.createStatement();
		// Update old database versions.
		st.addBatch("CREATE TABLE IF NOT EXISTS `achievements` (" + "playername char(36)," + "achievement varchar(64),"
				+ "description varchar(128)," + "date varchar(10)," + "PRIMARY KEY (`playername`, `achievement`)"
				+ ")");
		st.addBatch("CREATE TABLE IF NOT EXISTS `breaks` (" + "playername char(36)," + "blockid SMALLINT UNSIGNED,"
				+ "breaks INT UNSIGNED," + "PRIMARY KEY(`playername`, `blockid`)" + ")");
		st.addBatch("CREATE TABLE IF NOT EXISTS `places` (" + "playername char(36)," + "blockid SMALLINT UNSIGNED,"
				+ "places INT UNSIGNED," + "PRIMARY KEY(`playername`, `blockid`)" + ")");
		st.addBatch("CREATE TABLE IF NOT EXISTS `kills` (" + "playername char(36)," + "mobname varchar(32),"
				+ "kills INT UNSIGNED," + "PRIMARY KEY (`playername`, `mobname`)" + ")");
		st.addBatch("CREATE TABLE IF NOT EXISTS `crafts` (" + "playername char(36)," + "item SMALLINT UNSIGNED,"
				+ "times INT UNSIGNED," + "PRIMARY KEY (`playername`, `item`)" + ")");
		st.addBatch("CREATE TABLE IF NOT EXISTS `deaths` (" + "playername char(36)," + "deaths INT UNSIGNED,"
				+ "PRIMARY KEY (`playername`)" + ")");
		st.addBatch("CREATE TABLE IF NOT EXISTS `arrows` (" + "playername char(36)," + "arrows INT UNSIGNED,"
				+ "PRIMARY KEY (`playername`)" + ")");
		st.addBatch("CREATE TABLE IF NOT EXISTS `snowballs` (" + "playername char(36)," + "snowballs INT UNSIGNED,"
				+ "PRIMARY KEY (`playername`)" + ")");
		st.addBatch("CREATE TABLE IF NOT EXISTS `eggs` (" + "playername char(36)," + "eggs INT UNSIGNED,"
				+ "PRIMARY KEY (`playername`)" + ")");
		st.addBatch("CREATE TABLE IF NOT EXISTS `fish` (" + "playername char(36)," + "fish INT UNSIGNED,"
				+ "PRIMARY KEY (`playername`)" + ")");
		st.addBatch("CREATE TABLE IF NOT EXISTS `itembreaks` (" + "playername char(36)," + "itembreaks INT UNSIGNED,"
				+ "PRIMARY KEY (`playername`)" + ")");
		st.addBatch("CREATE TABLE IF NOT EXISTS `eatenitems` (" + "playername char(36)," + "eatenitems INT UNSIGNED,"
				+ "PRIMARY KEY (`playername`)" + ")");
		st.addBatch("CREATE TABLE IF NOT EXISTS `shears` (" + "playername char(36)," + "shears INT UNSIGNED,"
				+ "PRIMARY KEY (`playername`)" + ")");
		st.addBatch("CREATE TABLE IF NOT EXISTS `milks` (" + "playername char(36)," + "milks INT UNSIGNED,"
				+ "PRIMARY KEY (`playername`)" + ")");
		st.addBatch("CREATE TABLE IF NOT EXISTS `connections` (" + "playername char(36)," + "connections INT UNSIGNED,"
				+ "date varchar(10)," + "PRIMARY KEY (`playername`)" + ")");
		st.addBatch("CREATE TABLE IF NOT EXISTS `trades` (" + "playername char(36)," + "trades INT UNSIGNED,"
				+ "PRIMARY KEY (`playername`)" + ")");
		st.addBatch("CREATE TABLE IF NOT EXISTS `anvils` (" + "playername char(36)," + "anvils INT UNSIGNED,"
				+ "PRIMARY KEY (`playername`)" + ")");
		st.addBatch("CREATE TABLE IF NOT EXISTS `enchantments` (" + "playername char(36),"
				+ "enchantments INT UNSIGNED," + "PRIMARY KEY (`playername`)" + ")");
		st.addBatch("CREATE TABLE IF NOT EXISTS `levels` (" + "playername char(36)," + "levels INT UNSIGNED,"
				+ "PRIMARY KEY (`playername`)" + ")");
		st.addBatch("CREATE TABLE IF NOT EXISTS `beds` (" + "playername char(36)," + "beds INT UNSIGNED,"
				+ "PRIMARY KEY (`playername`)" + ")");
		st.addBatch("CREATE TABLE IF NOT EXISTS `consumedpotions` (" + "playername char(36),"
				+ "consumedpotions INT UNSIGNED," + "PRIMARY KEY (`playername`)" + ")");
		st.addBatch("CREATE TABLE IF NOT EXISTS `playedtime` (" + "playername char(36)," + "playedtime INT UNSIGNED,"
				+ "PRIMARY KEY (`playername`)" + ")");
		st.addBatch("CREATE TABLE IF NOT EXISTS `distancefoot` (" + "playername char(36),"
				+ "distancefoot INT UNSIGNED," + "PRIMARY KEY (`playername`)" + ")");
		st.addBatch("CREATE TABLE IF NOT EXISTS `distancepig` (" + "playername char(36)," + "distancepig INT UNSIGNED,"
				+ "PRIMARY KEY (`playername`)" + ")");
		st.addBatch("CREATE TABLE IF NOT EXISTS `distancehorse` (" + "playername char(36),"
				+ "distancehorse INT UNSIGNED," + "PRIMARY KEY (`playername`)" + ")");
		st.addBatch("CREATE TABLE IF NOT EXISTS `distanceminecart` (" + "playername char(36),"
				+ "distanceminecart INT UNSIGNED," + "PRIMARY KEY (`playername`)" + ")");
		st.addBatch("CREATE TABLE IF NOT EXISTS `distanceboat` (" + "playername char(36),"
				+ "distanceboat INT UNSIGNED," + "PRIMARY KEY (`playername`)" + ")");
		st.addBatch("CREATE TABLE IF NOT EXISTS `drops` (" + "playername char(36)," + "drops INT UNSIGNED,"
				+ "PRIMARY KEY (`playername`)" + ")");
		st.addBatch("CREATE TABLE IF NOT EXISTS `hoeplowing` (" + "playername char(36)," + "hoeplowing INT UNSIGNED,"
				+ "PRIMARY KEY (`playername`)" + ")");
		st.addBatch("CREATE TABLE IF NOT EXISTS `fertilising` (" + "playername char(36)," + "fertilising INT UNSIGNED,"
				+ "PRIMARY KEY (`playername`)" + ")");
		st.addBatch("CREATE TABLE IF NOT EXISTS `tames` (" + "playername char(36)," + "tames INT UNSIGNED,"
				+ "PRIMARY KEY (`playername`)" + ")");
		st.addBatch("CREATE TABLE IF NOT EXISTS `brewing` (" + "playername char(36)," + "brewing INT UNSIGNED,"
				+ "PRIMARY KEY (`playername`)" + ")");
		st.addBatch("CREATE TABLE IF NOT EXISTS `fireworks` (" + "playername char(36)," + "fireworks INT UNSIGNED,"
				+ "PRIMARY KEY (`playername`)" + ")");

		st.executeBatch();
		st.close();

	}

	/**
	 * Retrieve SQL connection to MySQL or SQLite database.
	 */
	public Connection getSQLConnection() {

		// Check if Connection was not previously closed.
		try {
			if (sqlConnection == null || sqlConnection.isClosed()) {

				if (!sqliteDatabase) {
					sqlConnection = createMySQLConnection();
				} else {

					sqlConnection = createSQLiteConnection();
				}
			}
		} catch (SQLException e) {
			plugin.getLogger().severe("Error while attempting to retrieve connection to database: " + e);
			e.printStackTrace();
			plugin.setSuccessfulLoad(false);
		}

		return sqlConnection;
	}

	/**
	 * Create a new Connection object to SQLite database.
	 */
	private Connection createSQLiteConnection() throws SQLException {

		File dbfile = new File(plugin.getDataFolder(), "achievements.db");
		if (!dbfile.exists()) {
			try {
				dbfile.createNewFile();
			} catch (IOException e) {
				plugin.getLogger().severe("Error while creating database file.");
				e.printStackTrace();
				plugin.setSuccessfulLoad(false);
			}
		}
		return DriverManager.getConnection("jdbc:sqlite:" + dbfile);
	}

	/**
	 * Create a new Connection object to MySQL database.
	 */
	private Connection createMySQLConnection() throws SQLException {

		return DriverManager
				.getConnection(mysqlDatabase + "?autoReconnect=true&user=" + mysqlUser + "&password=" + mysqlPassword);
	}

	/**
	 * Get number of player's kills for a specific mob.
	 */
	public int getKills(Player player, String mobname) {

		try {
			Connection conn = getSQLConnection();
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery("SELECT kills FROM `kills` WHERE playername = '" + player.getUniqueId()
					+ "' AND mobname = '" + mobname + "'");
			int entityKills = 0;
			while (rs.next()) {
				entityKills = rs.getInt("kills");
			}

			st.close();
			rs.close();

			return entityKills;
		} catch (SQLException e) {
			plugin.getLogger().severe("SQL error while retrieving kill stats: " + e);
			return 0;
		}
	}

	/**
	 * Get number of player's places for a specific block.
	 */
	@SuppressWarnings("deprecation")
	public int getPlaces(Player player, Block block) {

		try {
			Connection conn = getSQLConnection();
			int blockBreaks = 0;
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery("SELECT places FROM `places` WHERE playername = '" + player.getUniqueId()
					+ "' AND blockid = " + block.getTypeId() + "");
			while (rs.next()) {
				blockBreaks = rs.getInt("places");
			}

			st.close();
			rs.close();

			return blockBreaks;
		} catch (SQLException e) {
			plugin.getLogger().severe("SQL error while retrieving block place stats: " + e);
			return 0;
		}
	}

	/**
	 * Get number of player's breaks for a specific block.
	 */
	@SuppressWarnings("deprecation")
	public int getBreaks(Player player, Block block) {

		try {
			Connection conn = getSQLConnection();
			int blockBreaks = 0;
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery("SELECT breaks FROM `breaks` WHERE playername = '" + player.getUniqueId()
					+ "' AND blockid = " + block.getTypeId());
			while (rs.next()) {
				blockBreaks = rs.getInt("breaks");
			}

			st.close();
			rs.close();

			return blockBreaks;
		} catch (SQLException e) {
			plugin.getLogger().severe("SQL error while retrieving block break stats: " + e);
			return 0;
		}
	}

	/**
	 * Get the list of achievements of a player.
	 */
	public ArrayList<String> getPlayerAchievementsList(Player player) {

		try {
			Connection conn = getSQLConnection();
			Statement st = conn.createStatement();
			ResultSet rs = st
					.executeQuery("SELECT * FROM `achievements` WHERE playername = '" + player.getUniqueId() + "'");
			ArrayList<String> achievementsList = new ArrayList<String>();
			while (rs.next()) {
				achievementsList.add(rs.getString(2));
				achievementsList.add(rs.getString(3));
				achievementsList.add(rs.getString(4));
			}
			st.close();
			rs.close();

			return achievementsList;
		} catch (SQLException e) {
			plugin.getLogger().severe("SQL error while retrieving achievements: " + e);
		}
		return null;
	}

	/**
	 * Get the number of achievements received by a player.
	 */
	public int getPlayerAchievementsAmount(Player player) {

		try {
			Connection conn = getSQLConnection();
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery(
					"SELECT COUNT(*) FROM `achievements` WHERE playername = '" + player.getUniqueId() + "'");
			int achievementsAmount = 0;
			if (rs.next()) {
				achievementsAmount = rs.getInt(1);
			}

			st.close();
			rs.close();

			return achievementsAmount;
		} catch (SQLException e) {
			plugin.getLogger().severe("SQL error while counting player achievements: " + e);
		}
		return 0;

	}

	/**
	 * Get the list of players with the most achievements.
	 */
	public ArrayList<String> getTopList(int listLength) {

		try {
			Connection conn = getSQLConnection();
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery(
					"SELECT playername, COUNT(*) FROM `achievements` GROUP BY playername ORDER BY COUNT(*) DESC LIMIT "
							+ listLength);
			ArrayList<String> topList = new ArrayList<String>();
			while (rs.next()) {
				topList.add(rs.getString("playername"));
				topList.add("" + rs.getInt("COUNT(*)"));
			}
			st.close();
			rs.close();

			return topList;
		} catch (SQLException e) {
			plugin.getLogger().severe("SQL error while retrieving top players: " + e);
		}
		return new ArrayList<String>();

	}

	/**
	 * Get number of players who have received at least one achievement.
	 */
	public int getTotalPlayers() {

		try {
			Connection conn = getSQLConnection();
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM (SELECT DISTINCT playername  FROM `achievements`)");
			int players = 0;
			while (rs.next()) {
				players = rs.getInt("COUNT(*)");
			}
			st.close();
			rs.close();

			return players;
		} catch (SQLException e) {
			plugin.getLogger().severe("SQL error while retrieving top players: " + e);
		}
		return 0;

	}

	/**
	 * Get the rank of a player given his number of achievements.
	 */
	public int getPlayerRank(Player player) {

		try {
			Connection conn = getSQLConnection();
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery(
					"SELECT COUNT(*) FROM `achievements` WHERE playername = '" + player.getUniqueId() + "'");
			int achievementsAmount = 0;
			if (rs.next()) {
				achievementsAmount = rs.getInt(1);
			}
			rs = st.executeQuery(
					"SELECT COUNT(*) FROM (SELECT COUNT(*) `number` FROM `achievements` GROUP BY playername) WHERE `number` >"
							+ achievementsAmount);
			int rank = 0;
			while (rs.next()) {
				rank = rs.getInt("COUNT(*)") + 1;
			}
			st.close();
			rs.close();

			return rank;
		} catch (SQLException e) {
			plugin.getLogger().severe("SQL error while retrieving top players: " + e);
		}
		return 0;

	}

	/**
	 * Register a new achievement for a player.
	 */
	public void registerAchievement(Player player, final String achievement, final String desc) {

		final String name = player.getUniqueId().toString();

		if (plugin.isAsyncPooledRequestsSender())
			new Thread() { // Avoid using Bukkit API scheduler, as a
							// reload/restart could kill the async task before
							// write to database has occured.

				@Override
				public void run() {

					registerAchievementToDB(achievement, desc, name);
				}

			}.start();
		else
			registerAchievementToDB(achievement, desc, name);

	}

	/**
	 * Write to DB to register new achievement for a player.
	 */
	private void registerAchievementToDB(String achievement, String desc, String name) {

		try {
			Connection conn = getSQLConnection();
			Statement st = conn.createStatement();
			SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
			achievement = achievement.replace("'", "''");
			desc = desc.replace("'", "''");
			st.execute("REPLACE INTO `achievements` VALUES ('" + name + "','" + achievement + "','" + desc + "','"
					+ format.format(new Date()) + "')");
			st.close();

		} catch (SQLException e) {
			plugin.getLogger().severe("SQL error while registering achievement: " + e);
		}
	}

	/**
	 * Check whether player has received a specific achievement.
	 */
	public boolean hasPlayerAchievement(Player player, String name) {

		try {
			boolean result = false;
			Connection conn = getSQLConnection();
			Statement st = conn.createStatement();
			name = name.replace("'", "''");
			if (st.executeQuery("SELECT achievement FROM `achievements` WHERE playername = '" + player.getUniqueId()
					+ "' AND achievement = '" + name + "'").next())
				result = true;
			st.close();

			return result;
		} catch (SQLException e) {
			plugin.getLogger().severe("SQL error while checking achievement: " + e);
		}
		return false;

	}

	/**
	 * Check whether player has received a specific achievement.
	 */
	public void deletePlayerAchievement(Player player, String name) {

		try {
			Connection conn = getSQLConnection();
			Statement st = conn.createStatement();
			name = name.replace("'", "''");
			st.execute("DELETE FROM `achievements` WHERE playername = '" + player.getUniqueId()
					+ "' AND achievement = '" + name + "'");
			st.close();

		} catch (SQLException e) {
			plugin.getLogger().severe("SQL error while deleting achievement: " + e);
		}

	}

	/**
	 * Increment and return value of a specific craft achievement statistic.
	 */
	@SuppressWarnings("deprecation")
	public int getCrafts(Player player, ItemStack item) {

		try {
			Connection conn = getSQLConnection();
			int itemCrafts = 0;
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery("SELECT times FROM `crafts` WHERE playername = '" + player.getUniqueId()
					+ "' AND item = " + item.getTypeId());
			while (rs.next()) {
				itemCrafts = rs.getInt("times");
			}
			st.close();
			rs.close();

			return itemCrafts;
		} catch (SQLException e) {
			plugin.getLogger().severe("SQL error while handling craft event: " + e);
			return 0;
		}

	}

	/**
	 * Get the amount of a normal achievement statistic.
	 */
	public int getNormalAchievementAmount(Player player, String table) {

		try {
			Connection conn = getSQLConnection();
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery(
					"SELECT " + table + " FROM `" + table + "` WHERE playername = '" + player.getUniqueId() + "'");
			int amount = 0;
			while (rs.next()) {
				amount = rs.getInt(table);
			}

			rs.close();

			return amount;
		} catch (SQLException e) {
			plugin.getLogger().severe("SQL error while retrieving " + table + " stats: " + e);
			return 0;
		}
	}

	/**
	 * Get a player's last connection date.
	 */
	public String getPlayerConnectionDate(Player player) {

		String date = null;
		try {
			Connection conn = getSQLConnection();
			Statement st = conn.createStatement();
			ResultSet rs = st
					.executeQuery("SELECT date FROM `connections` WHERE playername = '" + player.getUniqueId() + "'");
			while (rs.next())
				date = rs.getString("date");
			st.close();
			rs.close();

			return date;

		} catch (SQLException e) {
			plugin.getLogger().severe("SQL error while retrieving connection date stats: " + e);
			return null;
		}

		catch (NullPointerException e) {
			return null;
		}

	}

	/**
	 * Update player's number of connections and last connection date and return
	 * number of connections.
	 */
	public int updateAndGetConnection(Player player, final String date) {

		final String name = player.getUniqueId().toString();
		try {
			Connection conn = getSQLConnection();
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery(
					"SELECT connections FROM `connections` WHERE playername = '" + name + "'");
			int prev = 0;
			while (rs.next()) {
				prev = rs.getInt("connections");
			}
			final int newConnections = prev + 1;
			if (!plugin.isAsyncPooledRequestsSender())
				st.execute("REPLACE INTO `connections` VALUES ('" + name + "', " + newConnections
						+ ", '" + date + "')");
			else{
				new Thread() { // Avoid using Bukkit API scheduler, as a
					// reload/restart could kill the async task before
					// write to database has occured.

					@Override
					public void run() {

						Connection conn = getSQLConnection();
						Statement st;
						try {
							st = conn.createStatement();
							st.execute("REPLACE INTO `connections` VALUES ('" + name + "', " + newConnections
									+ ", '" + date + "')");
							st.close();
						} catch (SQLException e) {
							plugin.getLogger().severe("SQL error while handling connection event on async task: " + e);
						}
					}
				}.start();
			}
			st.close();
			rs.close();

			return newConnections;
		} catch (SQLException e) {
			plugin.getLogger().severe("SQL error while handling connection event: " + e);
			return 0;
		}

	}

	/**
	 * Update and return player's playtime.
	 */
	public long updateAndGetPlaytime(Player player, final long time) {

		final String name = player.getUniqueId().toString();
		try {
			Connection conn = getSQLConnection();
			Statement st = conn.createStatement();
			long newPlayedTime = 0;
			if (time == 0) {
				ResultSet rs = st.executeQuery("SELECT playedtime FROM `playedtime` WHERE playername = '" + name + "'");
				newPlayedTime = 0;
				while (rs.next()) {
					newPlayedTime = rs.getLong("playedtime");
				}
				rs.close();
			} else if (!plugin.isAsyncPooledRequestsSender()) {
				st.execute("REPLACE INTO `playedtime` VALUES ('" + name + "', " + time + ")");
			}
			st.close();

			// Write asynchronously to database using a new statement.
			if (time != 0 && plugin.isAsyncPooledRequestsSender()) {
				new Thread() { // Avoid using Bukkit API scheduler, as a
					// reload/restart could kill the async task before
					// write to database has occured.

					@Override
					public void run() {

						Connection conn = getSQLConnection();
						Statement st;
						try {
							st = conn.createStatement();
							st.execute("REPLACE INTO `playedtime` VALUES ('" + name + "', " + time + ")");
							st.close();
						} catch (SQLException e) {
							plugin.getLogger()
									.severe("SQL error while handling play time registration on async task: " + e);
						}
					}
				}.start();
			}

			return newPlayedTime;
		} catch (SQLException e) {
			plugin.getLogger().severe("SQL error while handling play time registration: " + e);
			return 0L;
		}

	}

	/**
	 * Update and return player's distance for a specific distance type.
	 */
	public int updateAndGetDistance(Player player, final int distance, final String type) {

		final String name = player.getUniqueId().toString();
		try {
			Connection conn = getSQLConnection();
			Statement st = conn.createStatement();
			int newDistance = 0;
			if (distance == 0) {
				ResultSet rs = st
						.executeQuery("SELECT " + type + " FROM `" + type + "` WHERE playername = '" + name + "'");
				while (rs.next()) {
					newDistance = rs.getInt(type);
				}
				rs.close();
			} else if (!plugin.isAsyncPooledRequestsSender()) {
				// Write synchronously to database using same statement.
				st.execute("REPLACE INTO `" + type + "` VALUES ('" + name + "', " + distance + ")");
			}
			st.close();

			// Write asynchronously to database using a new statement.
			if (distance != 0 && plugin.isAsyncPooledRequestsSender()) {
				new Thread() { // Avoid using Bukkit API scheduler, as a
					// reload/restart could kill the async task before
					// write to database has occured.

					@Override
					public void run() {

						Connection conn = getSQLConnection();
						Statement st;
						try {
							st = conn.createStatement();
							st.execute("REPLACE INTO `" + type + "` VALUES ('" + name + "', " + distance + ")");
							st.close();
						} catch (SQLException e) {
							plugin.getLogger()
									.severe("SQL error while handling " + type + " registration on async task: " + e);
						}
					}
				}.start();
			}
			return newDistance;
		} catch (SQLException e) {
			plugin.getLogger().severe("SQL error while handling " + type + " registration: " + e);
			return 0;
		}

	}
}
