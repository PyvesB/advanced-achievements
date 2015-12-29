package com.hm.achievement.db;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.hm.achievement.AdvancedAchievements;

public class SQLDatabaseManager {

	private AdvancedAchievements plugin;

	/**
	 * Retrieve SQL connection to database.
	 */
	public Connection getSQLConnection() {

		String dataHandler = plugin.getConfig().getString("DatabaseType", "sqlite");
		String mysqlDatabase = plugin.getConfig().getString("MYSQL.Database", "jdbc:mysql://localhost:3306/minecraft");
		String mysqlUser = plugin.getConfig().getString("MYSQL.User", "root");
		String mysqlPassword = plugin.getConfig().getString("MYSQL.Password", "root");
		if (dataHandler.equalsIgnoreCase("mysql")) {
			try {
				return DriverManager.getConnection(mysqlDatabase + "?autoReconnect=true&user=" + mysqlUser
						+ "&password=" + mysqlPassword);
			} catch (SQLException e) {
				plugin.getLogger().severe("Error while attempting to retrieve connection to database: " + e);
				e.printStackTrace();
				plugin.setSuccessfulLoad(false);
			}
			return null;
		} else {

			File dbfile = new File(plugin.getDataFolder(), "achievements.db");
			if (!dbfile.exists()) {
				try {
					dbfile.createNewFile();
					Class.forName("org.sqlite.JDBC");
					Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbfile);
					Statement st = conn.createStatement();
					// Important: "desc" keyword only allowed in SQLite.
					// "description" used in MySQL.
					st.execute("CREATE TABLE IF NOT EXISTS `achievements` (" + "playername char(36),"
							+ "achievement varchar(64)," + "desc varchar(128)," + "date varchar(10),"
							+ "PRIMARY KEY (`playername`, `achievement`)" + ")");
					initialiseTables(st);
					st.close();
					return conn;
				} catch (IOException e) {
					plugin.getLogger().severe("Error while creating database file.");
					e.printStackTrace();
					plugin.setSuccessfulLoad(false);
				} catch (SQLException e) {
					plugin.getLogger().severe("SQLite exception on initialize: " + e);
					e.printStackTrace();
					plugin.setSuccessfulLoad(false);
				} catch (ClassNotFoundException e) {
					plugin.getLogger().severe(
							"You need the SQLite JBDC library. Please download it and put it in /lib folder.");
					e.printStackTrace();
					plugin.setSuccessfulLoad(false);
				}
			}
			try {
				Class.forName("org.sqlite.JDBC");
				Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbfile);
				return conn;

			} catch (SQLException e) {
				plugin.getLogger().severe("SQLite exception on initialize: " + e);
				e.printStackTrace();
				plugin.setSuccessfulLoad(false);
			} catch (ClassNotFoundException e) {
				plugin.getLogger().severe(
						"You need the SQLite JBDC library. Please download it and put it in /lib folder.");
				e.printStackTrace();
				plugin.setSuccessfulLoad(false);
			}
		}
		return null;
	}

	/**
	 * Initialise database system.
	 */
	public void initialise(AdvancedAchievements plugin) {

		this.plugin = plugin;
		Connection conn = getSQLConnection();
		if (conn == null) {
			plugin.getLogger().severe("Could not establish SQL connection. Disabling Advanced Achievement.");
			plugin.getLogger().severe("Please verify your settings in the configuration file.");
			plugin.getServer().getPluginManager().disablePlugin(plugin);
			return;

		}
		try {
			if (plugin.getConfig().getString("DatabaseType", "sqlite").equalsIgnoreCase("mysql")) {
				Statement st = conn.createStatement();
				st.execute("CREATE TABLE IF NOT EXISTS `achievements` (" + "playername char(36),"
						+ "achievement varchar(64)," + "description varchar(128)," + "date varchar(10),"
						+ "PRIMARY KEY (`playername`, `achievement`)" + ")");
				initialiseTables(st);
				st.close();
			} else {
				// Update old SQLite database versions.
				// Added in version 1.3:
				if (plugin.getDatabaseVersion() == 1) {
					Statement st = conn.createStatement();
					st.execute("CREATE TABLE IF NOT EXISTS `trades` (" + "playername char(36),"
							+ "trades INT UNSIGNED," + "PRIMARY KEY (`playername`)" + ")");
					st.execute("CREATE TABLE IF NOT EXISTS `anvils` (" + "playername char(36),"
							+ "anvils INT UNSIGNED," + "PRIMARY KEY (`playername`)" + ")");
					st.execute("CREATE TABLE IF NOT EXISTS `enchantments` (" + "playername char(36),"
							+ "enchantments INT UNSIGNED," + "PRIMARY KEY (`playername`)" + ")");

					st.close();
					plugin.setDatabaseVersion(2);
				}
				// Added in version 1.4:
				if (plugin.getDatabaseVersion() == 2) {
					Statement st = conn.createStatement();
					st.execute("CREATE TABLE IF NOT EXISTS `eggs` (" + "playername char(36)," + "eggs INT UNSIGNED,"
							+ "PRIMARY KEY (`playername`)" + ")");
					st.execute("CREATE TABLE IF NOT EXISTS `levels` (" + "playername char(36),"
							+ "levels INT UNSIGNED," + "PRIMARY KEY (`playername`)" + ")");
					st.execute("CREATE TABLE IF NOT EXISTS `beds` (" + "playername char(36)," + "beds INT UNSIGNED,"
							+ "PRIMARY KEY (`playername`)" + ")");
					st.close();
					plugin.setDatabaseVersion(3);

				}
				// Added in version 1.5:
				if (plugin.getDatabaseVersion() == 3) {
					Statement st = conn.createStatement();
					st.execute("CREATE TABLE IF NOT EXISTS `consumedpotions` (" + "playername char(36),"
							+ "consumedpotions INT UNSIGNED," + "PRIMARY KEY (`playername`)" + ")");
					st.close();
					plugin.setDatabaseVersion(4);

				}
				// Added in version 2.0:
				if (plugin.getDatabaseVersion() == 4) {
					Statement st = conn.createStatement();
					st.execute("CREATE TABLE IF NOT EXISTS `playedtime` (" + "playername char(36),"
							+ "playedtime INT UNSIGNED," + "PRIMARY KEY (`playername`)" + ")");
					st.execute("CREATE TABLE IF NOT EXISTS `distancefoot` (" + "playername char(36),"
							+ "distancefoot INT UNSIGNED," + "PRIMARY KEY (`playername`)" + ")");
					st.execute("CREATE TABLE IF NOT EXISTS `distancepig` (" + "playername char(36),"
							+ "distancepig INT UNSIGNED," + "PRIMARY KEY (`playername`)" + ")");
					st.execute("CREATE TABLE IF NOT EXISTS `distancehorse` (" + "playername char(36),"
							+ "distancehorse INT UNSIGNED," + "PRIMARY KEY (`playername`)" + ")");
					st.execute("CREATE TABLE IF NOT EXISTS `distanceminecart` (" + "playername char(36),"
							+ "distanceminecart INT UNSIGNED," + "PRIMARY KEY (`playername`)" + ")");
					st.execute("CREATE TABLE IF NOT EXISTS `distanceboat` (" + "playername char(36),"
							+ "distanceboat INT UNSIGNED," + "PRIMARY KEY (`playername`)" + ")");
					st.execute("CREATE TABLE IF NOT EXISTS `drops` (" + "playername char(36)," + "drops INT UNSIGNED,"
							+ "PRIMARY KEY (`playername`)" + ")");
					st.execute("CREATE TABLE IF NOT EXISTS `hoeplowing` (" + "playername char(36),"
							+ "hoeplowing INT UNSIGNED," + "PRIMARY KEY (`playername`)" + ")");
					st.execute("CREATE TABLE IF NOT EXISTS `fertilising` (" + "playername char(36),"
							+ "fertilising INT UNSIGNED," + "PRIMARY KEY (`playername`)" + ")");
					st.close();
					plugin.setDatabaseVersion(5);
				}
				// Added in version 2.2:
				if (plugin.getDatabaseVersion() == 5) {
					Statement st = conn.createStatement();
					st.execute("CREATE TABLE IF NOT EXISTS `tames` (" + "playername char(36)," + "tames INT UNSIGNED,"
							+ "PRIMARY KEY (`playername`)" + ")");
					st.close();
					plugin.setDatabaseVersion(6);
				}
				// Version 2.2 fix:
				if (plugin.getDatabaseVersion() == 6) {
					Statement st = conn.createStatement();
					st.execute("CREATE TABLE IF NOT EXISTS `brewing` (" + "playername char(36),"
							+ "brewing INT UNSIGNED," + "PRIMARY KEY (`playername`)" + ")");
					st.close();
					plugin.setDatabaseVersion(7);
				}
			}
			conn.close();

		} catch (SQLException e) {
			plugin.getLogger().severe("Error while initialising database: " + e);
			plugin.setSuccessfulLoad(false);
		}

	}

	/**
	 * Initialise database tables.
	 */
	private void initialiseTables(Statement st) throws SQLException {

		st.execute("CREATE TABLE IF NOT EXISTS `breaks` (" + "playername char(36)," + "blockid SMALLINT UNSIGNED,"
				+ "breaks INT UNSIGNED," + "PRIMARY KEY(`playername`, `blockid`)" + ")");
		st.execute("CREATE TABLE IF NOT EXISTS `places` (" + "playername char(36)," + "blockid SMALLINT UNSIGNED,"
				+ "places INT UNSIGNED," + "PRIMARY KEY(`playername`, `blockid`)" + ")");
		st.execute("CREATE TABLE IF NOT EXISTS `kills` (" + "playername char(36)," + "mobname varchar(32),"
				+ "kills INT UNSIGNED," + "PRIMARY KEY (`playername`, `mobname`)" + ")");
		st.execute("CREATE TABLE IF NOT EXISTS `crafts` (" + "playername char(36)," + "item SMALLINT UNSIGNED,"
				+ "times INT UNSIGNED," + "PRIMARY KEY (`playername`, `item`)" + ")");
		st.execute("CREATE TABLE IF NOT EXISTS `deaths` (" + "playername char(36)," + "deaths INT UNSIGNED,"
				+ "PRIMARY KEY (`playername`)" + ")");
		st.execute("CREATE TABLE IF NOT EXISTS `arrows` (" + "playername char(36)," + "arrows INT UNSIGNED,"
				+ "PRIMARY KEY (`playername`)" + ")");
		st.execute("CREATE TABLE IF NOT EXISTS `snowballs` (" + "playername char(36)," + "snowballs INT UNSIGNED,"
				+ "PRIMARY KEY (`playername`)" + ")");
		st.execute("CREATE TABLE IF NOT EXISTS `eggs` (" + "playername char(36)," + "eggs INT UNSIGNED,"
				+ "PRIMARY KEY (`playername`)" + ")");
		st.execute("CREATE TABLE IF NOT EXISTS `fish` (" + "playername char(36)," + "fish INT UNSIGNED,"
				+ "PRIMARY KEY (`playername`)" + ")");
		st.execute("CREATE TABLE IF NOT EXISTS `itembreaks` (" + "playername char(36)," + "itembreaks INT UNSIGNED,"
				+ "PRIMARY KEY (`playername`)" + ")");
		st.execute("CREATE TABLE IF NOT EXISTS `eatenitems` (" + "playername char(36)," + "eatenitems INT UNSIGNED,"
				+ "PRIMARY KEY (`playername`)" + ")");
		st.execute("CREATE TABLE IF NOT EXISTS `shears` (" + "playername char(36)," + "shears INT UNSIGNED,"
				+ "PRIMARY KEY (`playername`)" + ")");
		st.execute("CREATE TABLE IF NOT EXISTS `milks` (" + "playername char(36)," + "milks INT UNSIGNED,"
				+ "PRIMARY KEY (`playername`)" + ")");
		st.execute("CREATE TABLE IF NOT EXISTS `connections` (" + "playername char(36)," + "connections INT UNSIGNED,"
				+ "date varchar(10)," + "PRIMARY KEY (`playername`)" + ")");
		st.execute("CREATE TABLE IF NOT EXISTS `trades` (" + "playername char(36)," + "trades INT UNSIGNED,"
				+ "PRIMARY KEY (`playername`)" + ")");
		st.execute("CREATE TABLE IF NOT EXISTS `anvils` (" + "playername char(36)," + "anvils INT UNSIGNED,"
				+ "PRIMARY KEY (`playername`)" + ")");
		st.execute("CREATE TABLE IF NOT EXISTS `enchantments` (" + "playername char(36),"
				+ "enchantments INT UNSIGNED," + "PRIMARY KEY (`playername`)" + ")");
		st.execute("CREATE TABLE IF NOT EXISTS `levels` (" + "playername char(36)," + "levels INT UNSIGNED,"
				+ "PRIMARY KEY (`playername`)" + ")");
		st.execute("CREATE TABLE IF NOT EXISTS `beds` (" + "playername char(36)," + "beds INT UNSIGNED,"
				+ "PRIMARY KEY (`playername`)" + ")");
		st.execute("CREATE TABLE IF NOT EXISTS `consumedpotions` (" + "playername char(36),"
				+ "consumedpotions INT UNSIGNED," + "PRIMARY KEY (`playername`)" + ")");
		st.execute("CREATE TABLE IF NOT EXISTS `playedtime` (" + "playername char(36)," + "playedtime INT UNSIGNED,"
				+ "PRIMARY KEY (`playername`)" + ")");
		st.execute("CREATE TABLE IF NOT EXISTS `distancefoot` (" + "playername char(36),"
				+ "distancefoot INT UNSIGNED," + "PRIMARY KEY (`playername`)" + ")");
		st.execute("CREATE TABLE IF NOT EXISTS `distancepig` (" + "playername char(36)," + "distancepig INT UNSIGNED,"
				+ "PRIMARY KEY (`playername`)" + ")");
		st.execute("CREATE TABLE IF NOT EXISTS `distancehorse` (" + "playername char(36),"
				+ "distancehorse INT UNSIGNED," + "PRIMARY KEY (`playername`)" + ")");
		st.execute("CREATE TABLE IF NOT EXISTS `distanceminecart` (" + "playername char(36),"
				+ "distanceminecart INT UNSIGNED," + "PRIMARY KEY (`playername`)" + ")");
		st.execute("CREATE TABLE IF NOT EXISTS `distanceboat` (" + "playername char(36),"
				+ "distanceboat INT UNSIGNED," + "PRIMARY KEY (`playername`)" + ")");
		st.execute("CREATE TABLE IF NOT EXISTS `drops` (" + "playername char(36)," + "drops INT UNSIGNED,"
				+ "PRIMARY KEY (`playername`)" + ")");
		st.execute("CREATE TABLE IF NOT EXISTS `hoeplowing` (" + "playername char(36)," + "hoeplowing INT UNSIGNED,"
				+ "PRIMARY KEY (`playername`)" + ")");
		st.execute("CREATE TABLE IF NOT EXISTS `fertilising` (" + "playername char(36)," + "fertilising INT UNSIGNED,"
				+ "PRIMARY KEY (`playername`)" + ")");
		st.execute("CREATE TABLE IF NOT EXISTS `tames` (" + "playername char(36)," + "tames INT UNSIGNED,"
				+ "PRIMARY KEY (`playername`)" + ")");
		st.execute("CREATE TABLE IF NOT EXISTS `brewing` (" + "playername char(36)," + "brewing INT UNSIGNED,"
				+ "PRIMARY KEY (`playername`)" + ")");
	}

	/**
	 * Get number of player's kills for a specific mob.
	 */
	public Integer getKills(Player player, String mobname) {

		try {
			Connection conn = getSQLConnection();
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery("SELECT kills from `kills` WHERE playername = '" + player.getUniqueId()
					+ "' AND mobname = '" + mobname + "'");
			Integer entityKills = 0;
			while (rs.next()) {
				entityKills = rs.getInt("kills");
			}

			st.close();
			rs.close();
			conn.close();
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
	public Integer getPlaces(Player player, Block block) {

		try {
			Connection conn = getSQLConnection();
			Integer blockBreaks = 0;
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery("SELECT places from `places` WHERE playername = '" + player.getUniqueId()
					+ "' AND blockid = " + block.getTypeId() + "");
			while (rs.next()) {
				blockBreaks = rs.getInt("places");
			}

			st.close();
			rs.close();
			conn.close();
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
	public Integer getBreaks(Player player, Block block) {

		try {
			Connection conn = getSQLConnection();
			Integer blockBreaks = 0;
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery("SELECT breaks FROM `breaks` WHERE playername = '" + player.getUniqueId()
					+ "' AND blockid = " + block.getTypeId());
			while (rs.next()) {
				blockBreaks = rs.getInt("breaks");
			}

			st.close();
			rs.close();
			conn.close();
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
			ResultSet rs = st.executeQuery("SELECT * FROM `achievements` WHERE playername = '" + player.getUniqueId()
					+ "'");
			ArrayList<String> achievementsList = new ArrayList<String>();
			while (rs.next()) {
				achievementsList.add(rs.getString("achievement"));
				if (plugin.getConfig().getString("DatabaseType", "sqlite").equalsIgnoreCase("mysql"))
					achievementsList.add(rs.getString("description"));
				else
					achievementsList.add(rs.getString("desc"));
				achievementsList.add(rs.getString("date"));
			}
			st.close();
			rs.close();
			conn.close();

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
			ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM `achievements` WHERE playername = '"
					+ player.getUniqueId() + "'");
			int achievementsAmount = 0;
			if (rs.next()) {
				achievementsAmount = rs.getInt(1);
			}

			st.close();
			rs.close();
			conn.close();
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
			ResultSet rs = st
					.executeQuery("SELECT playername, COUNT(*) FROM `achievements` GROUP BY playername ORDER BY COUNT(*) DESC LIMIT "
							+ listLength);
			ArrayList<String> topList = new ArrayList<String>();
			while (rs.next()) {
				topList.add(rs.getString("playername"));
				topList.add("" + rs.getInt("COUNT(*)"));
			}
			st.close();
			rs.close();
			conn.close();
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
			ResultSet rs = st
					.executeQuery("SELECT COUNT(*) FROM (SELECT COUNT(*)  FROM `achievements` GROUP BY playername)");
			int players = 0;
			while (rs.next()) {
				players = rs.getInt("COUNT(*)");
			}
			st.close();
			rs.close();
			conn.close();
			return players;
		} catch (SQLException e) {
			plugin.getLogger().severe("SQL error while retrieving top players: " + e);
		}
		return 0;

	}

	/**
	 * Get the rank of a player given his number of achievements.
	 */
	public int getRank(int numberAchievements) {

		try {
			Connection conn = getSQLConnection();
			Statement st = conn.createStatement();
			ResultSet rs = st
					.executeQuery("SELECT COUNT(*) FROM (SELECT COUNT(*) `number` FROM `achievements` GROUP BY playername) WHERE `number` >"
							+ numberAchievements);
			int rank = 0;
			while (rs.next()) {
				rank = rs.getInt("COUNT(*)") + 1;
			}
			st.close();
			rs.close();
			conn.close();
			return rank;
		} catch (SQLException e) {
			plugin.getLogger().severe("SQL error while retrieving top players: " + e);
		}
		return 0;

	}

	/**
	 * Register a new achievement for a player.
	 */
	public void registerAchievement(Player player, String achievement, String desc, String date) {

		try {
			Connection conn = getSQLConnection();
			Statement st = conn.createStatement();
			achievement = achievement.replace("'", "''");
			desc = desc.replace("'", "''");
			if (plugin.getConfig().getString("DatabaseType", "sqlite").equalsIgnoreCase("mysql"))
				st.execute("replace into `achievements` (playername, achievement, description, date) VALUES ('"
						+ player.getUniqueId() + "','" + achievement + "','" + desc + "','" + date + "')");
			else
				st.execute("replace into `achievements` (playername, achievement, desc, date) VALUES ('"
						+ player.getUniqueId() + "','" + achievement + "','" + desc + "','" + date + "')");
			st.close();
			conn.close();

		} catch (SQLException e) {
			plugin.getLogger().severe("SQL error while registering achievement: " + e);
		}

	}

	/**
	 * Check whether player has received a specific achievement.
	 */
	public boolean hasPlayerAchievement(Player player, String name) {

		try {
			Connection conn = getSQLConnection();
			Statement st = conn.createStatement();
			name = name.replace("'", "''");
			ResultSet rs = st.executeQuery("SELECT achievement FROM `achievements` WHERE playername = '"
					+ player.getUniqueId() + "' AND achievement = '" + name + "'");
			String hasAchievement = "";
			while (rs.next())
				hasAchievement = rs.getString("achievement");
			st.close();
			rs.close();
			conn.close();
			return (hasAchievement != "");
		} catch (SQLException e) {
			plugin.getLogger().severe("SQL error while checking achievement: " + e);
		}
		return false;

	}

	/**
	 * Increment and return value of a normal achievement statistic.
	 */
	public Integer incrementAndGetNormalAchievement(Player player, String table) {

		try {
			Connection conn = getSQLConnection();
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery("SELECT " + table + " from `" + table + "` WHERE playername = '"
					+ player.getUniqueId() + "'");
			Integer prev = 0;
			while (rs.next()) {
				prev = rs.getInt(table);
			}
			Integer amount = prev + 1;
			st.execute("replace into `" + table + "` (playername, " + table + ") VALUES ('" + player.getUniqueId()
					+ "', " + amount + ")");
			st.close();
			rs.close();
			conn.close();
			return amount;
		} catch (SQLException e) {
			plugin.getLogger().severe("SQL error while handling " + table + " event: " + e);
			return 0;
		}
	}

	/**
	 * Increment and return value of a specific craft achievement statistic.
	 */
	@SuppressWarnings("deprecation")
	public Integer updateAndGetCraft(Player player, ItemStack item, int amount) {

		try {
			Connection conn = getSQLConnection();
			Integer itemCrafts = 0;
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery("SELECT times FROM `crafts` WHERE playername = '" + player.getUniqueId()
					+ "' AND item = " + item.getTypeId());
			while (rs.next()) {
				itemCrafts = rs.getInt("times");
			}
			Integer newCrafts = itemCrafts + amount;
			st.execute("replace into `crafts` (playername, item, times) VALUES ('" + player.getUniqueId() + "',"
					+ item.getTypeId() + ", " + newCrafts + ")");
			st.close();
			rs.close();
			conn.close();
			return newCrafts;
		} catch (SQLException e) {
			plugin.getLogger().severe("SQL error while handling craft event: " + e);
			return 0;
		}

	}

	/**
	 * Increment and return value of player's max level.
	 */
	public Integer incrementAndGetMaxLevel(Player player) {

		try {
			Connection conn = getSQLConnection();
			Statement st = conn.createStatement();

			Integer newLevels = player.getLevel() + 1;
			st.execute("replace into `levels` (playername, levels) VALUES ('" + player.getUniqueId() + "', "
					+ newLevels + ")");
			st.close();
			conn.close();
			return newLevels;
		} catch (SQLException e) {
			plugin.getLogger().severe("SQL error while handling XP event: " + e);
			return 0;
		}
	}

	/**
	 * Get the amount of a normal achievement statistic.
	 */
	public Integer getNormalAchievementAmount(Player player, String table) {

		try {
			Connection conn = getSQLConnection();
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery("SELECT " + table + " from `" + table + "` WHERE playername = '"
					+ player.getUniqueId() + "'");
			Integer amount = 0;
			while (rs.next()) {
				amount = rs.getInt(table);
			}

			rs.close();
			conn.close();
			return amount;
		} catch (SQLException e) {
			plugin.getLogger().severe("SQL error while retrieving " + table + " stats: " + e);
			return 0;
		}
	}

	/**
	 * Get a player's last connection date.
	 */
	public String getConnectionDate(Player player) {

		String date = null;
		try {
			Connection conn = getSQLConnection();
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery("SELECT date from `connections` WHERE playername = '" + player.getUniqueId()
					+ "'");
			while (rs.next())
				date = rs.getString("date");
			st.close();
			rs.close();
			conn.close();

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
	public Integer updateAndGetConnection(Player player, String date) {

		try {
			Connection conn = getSQLConnection();
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery("SELECT connections from `connections` WHERE playername = '"
					+ player.getUniqueId() + "'");
			Integer prev = 0;
			while (rs.next()) {
				prev = rs.getInt("connections");
			}
			Integer newConnections = prev + 1;
			st.execute("replace into `connections` (playername, connections, date) VALUES ('" + player.getUniqueId()
					+ "', " + newConnections + ", '" + date + "')");
			st.close();
			rs.close();
			conn.close();
			return newConnections;
		} catch (SQLException e) {
			plugin.getLogger().severe("SQL error while handling connection event: " + e);
			return 0;
		}

	}

	/**
	 * Update and return player's playtime.
	 */
	public Long updateAndGetPlaytime(Player player, Long time) {

		try {
			Connection conn = getSQLConnection();
			Statement st = conn.createStatement();
			long newPlayedTime = 0;
			if (time == 0) {
				ResultSet rs = st.executeQuery("SELECT playedtime from `playedtime` WHERE playername = '"
						+ player.getUniqueId() + "'");
				newPlayedTime = 0;
				while (rs.next()) {
					newPlayedTime = rs.getLong("playedtime");
				}
				rs.close();
			} else
				st.execute("replace into `playedtime` (playername, playedtime) VALUES ('" + player.getUniqueId()
						+ "', " + time + ")");
			st.close();
			conn.close();
			return newPlayedTime;
		} catch (SQLException e) {
			plugin.getLogger().severe("SQL error while handling play time registration: " + e);
			return (long) 0;
		}

	}

	/**
	 * Update and return player's distance for a specific distance type.
	 */
	public Integer updateAndGetDistance(Player player, Integer distance, String type) {

		try {
			Connection conn = getSQLConnection();
			Statement st = conn.createStatement();
			int newDistance = 0;
			if (distance == 0) {
				ResultSet rs = st.executeQuery("SELECT " + type + " from `" + type + "` WHERE playername = '"
						+ player.getUniqueId() + "'");
				while (rs.next()) {
					newDistance = rs.getInt(type);
				}
				rs.close();
			} else
				st.execute("replace into `" + type + "` (playername, " + type + ") VALUES ('" + player.getUniqueId()
						+ "', " + distance + ")");
			st.close();
			conn.close();
			return newDistance;
		} catch (SQLException e) {
			plugin.getLogger().severe("SQL error while handling " + type + " registration: " + e);
			return 0;
		}

	}
}
