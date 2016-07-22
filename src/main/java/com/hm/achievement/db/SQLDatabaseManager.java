package com.hm.achievement.db;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.hm.achievement.AdvancedAchievements;

public class SQLDatabaseManager {

	private AdvancedAchievements plugin;
	private String databaseAddress;
	private String databaseUser;
	private String databasePassword;
	private String tablePrefix;

	private byte databaseType;
	private static final byte SQLITE = 0;
	private static final byte MYSQL = 1;
	private static final byte POSTGRESQL = 2;

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
			if (databaseType == SQLITE)
				Class.forName("org.sqlite.JDBC");
			else if (databaseType == MYSQL)
				Class.forName("com.mysql.jdbc.Driver");
			else
				Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			plugin.getLogger().severe(
					"The JBDC library for your database type was not found. Please read the plugin's support for more information.");
			e.printStackTrace();
			plugin.setSuccessfulLoad(false);
		}

		// Try to establish connection with database.
		if (getSQLConnection() == null) {
			plugin.getLogger().severe("Could not establish SQL connection, disabling plugin.");
			plugin.getLogger().severe("Please verify your settings in the configuration file.");
			plugin.setOverrideDisable(true);
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

		// If a prefix is set in the config, check whether the tables with the default names exist. If so do renaming.
		if (!tablePrefix.equals("")) {
			try {
				Connection conn = getSQLConnection();
				Statement st = conn.createStatement();
				ResultSet rs;
				if (databaseType == SQLITE)
					rs = st.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='achievement'");
				else
					rs = st.executeQuery("SELECT name FROM information_schema.tables WHERE table_schema='"
							+ databaseAddress.substring(databaseAddress.lastIndexOf('/') + 1)
							+ "' AND table_name ='achievement'");
				// Table with a default name (ie. no prefix) was found; do a renaming of all tables.
				if (rs.next())
					renameTables();
				st.close();
				rs.close();
			} catch (SQLException e) {
				plugin.getLogger().severe("Error while attempting to set prefix of database tables: " + e);
				plugin.setSuccessfulLoad(false);
			}
		}

		// Check if using old database prior to version 2.4.1.
		String type = "";
		try {
			Connection conn = getSQLConnection();
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery("SELECT blockid FROM " + tablePrefix + "breaks LIMIT 1");
			type = rs.getMetaData().getColumnTypeName(1);
			st.close();
			rs.close();

		} catch (SQLException e) {
			plugin.getLogger().severe("SQL error while trying to update old DB: " + e);
		}

		// Old column type was integer for SQLite and smallint unsigned for
		// MySQL.
		if (type.equalsIgnoreCase("integer") || type.equalsIgnoreCase("smallint unsigned")) {
			plugin.getLogger().warning("Updating database tables, please wait...");
			updateOldDB(tablePrefix + "breaks");
			updateOldDB(tablePrefix + "crafts");
			updateOldDB(tablePrefix + "places");
		}

	}

	/**
	 * Load plugin configuration and set values to different parameters relevant to the database system.
	 */
	private void configurationLoad() {

		tablePrefix = plugin.getPluginConfig().getString("TablePrefix", "");
		String dataHandler = plugin.getPluginConfig().getString("DatabaseType", "sqlite");
		if (dataHandler.equalsIgnoreCase("mysql")) {

			databaseType = MYSQL;
			databaseAddress = plugin.getPluginConfig().getString("MYSQL.Database",
					"jdbc:mysql://localhost:3306/minecraft");
			databaseUser = plugin.getPluginConfig().getString("MYSQL.User", "root");
			databasePassword = plugin.getPluginConfig().getString("MYSQL.Password", "root");

		} else if (dataHandler.equalsIgnoreCase("postgresql")) {

			databaseType = POSTGRESQL;
			databaseAddress = plugin.getPluginConfig().getString("POSTGRESQL.Database",
					"jdbc:postgresql://localhost:5432/minecraft");
			databaseUser = plugin.getPluginConfig().getString("POSTGRESQL.User", "root");
			databasePassword = plugin.getPluginConfig().getString("POSTGRESQL.Password", "root");

		} else {

			databaseType = SQLITE;

		}

	}

	/**
	 * Initialise database tables by creating non existing ones. Uses configuration file to determine which ones it is
	 * relevant to try to create.
	 */
	private void initialiseTables() throws SQLException {

		Statement st = sqlConnection.createStatement();

		st.addBatch("CREATE TABLE IF NOT EXISTS " + tablePrefix
				+ "achievements (playername char(36),achievement varchar(64),description varchar(128),date char(10),PRIMARY KEY (playername, achievement))");
		st.addBatch("CREATE TABLE IF NOT EXISTS " + tablePrefix
				+ "breaks (playername char(36),blockid varchar(32),breaks INT,PRIMARY KEY(playername, blockid))");
		st.addBatch("CREATE TABLE IF NOT EXISTS " + tablePrefix
				+ "places (playername char(36),blockid varchar(32),places INT,PRIMARY KEY(playername, blockid))");
		st.addBatch("CREATE TABLE IF NOT EXISTS " + tablePrefix
				+ "kills (playername char(36),mobname varchar(32),kills INT,PRIMARY KEY (playername, mobname))");
		st.addBatch("CREATE TABLE IF NOT EXISTS " + tablePrefix
				+ "crafts (playername char(36),item varchar(32),crafts INT,PRIMARY KEY (playername, item))");
		st.addBatch("CREATE TABLE IF NOT EXISTS " + tablePrefix
				+ "deaths (playername char(36),deaths INT,PRIMARY KEY (playername))");
		st.addBatch("CREATE TABLE IF NOT EXISTS " + tablePrefix
				+ "arrows (playername char(36),arrows INT,PRIMARY KEY (playername))");
		st.addBatch("CREATE TABLE IF NOT EXISTS " + tablePrefix
				+ "snowballs (playername char(36),snowballs INT,PRIMARY KEY (playername))");
		st.addBatch("CREATE TABLE IF NOT EXISTS " + tablePrefix
				+ "eggs (playername char(36),eggs INT,PRIMARY KEY (playername))");
		st.addBatch("CREATE TABLE IF NOT EXISTS " + tablePrefix
				+ "fish (playername char(36),fish INT,PRIMARY KEY (playername))");
		st.addBatch("CREATE TABLE IF NOT EXISTS " + tablePrefix + "itembreaks (playername char(36),itembreaks INT,"
				+ "PRIMARY KEY (playername))");
		st.addBatch("CREATE TABLE IF NOT EXISTS " + tablePrefix
				+ "eatenitems (playername char(36),eatenitems INT,PRIMARY KEY (playername))");
		st.addBatch("CREATE TABLE IF NOT EXISTS " + tablePrefix
				+ "shears (playername char(36),shears INT,PRIMARY KEY (playername))");
		st.addBatch("CREATE TABLE IF NOT EXISTS " + tablePrefix
				+ "milks (playername char(36),milks INT,PRIMARY KEY (playername))");
		st.addBatch("CREATE TABLE IF NOT EXISTS " + tablePrefix
				+ "connections (playername char(36),connections INT,date varchar(10),PRIMARY KEY (playername))");
		st.addBatch("CREATE TABLE IF NOT EXISTS " + tablePrefix
				+ "trades (playername char(36),trades INT,PRIMARY KEY (playername))");
		st.addBatch("CREATE TABLE IF NOT EXISTS " + tablePrefix
				+ "anvils (playername char(36),anvils INT,PRIMARY KEY (playername))");
		st.addBatch("CREATE TABLE IF NOT EXISTS " + tablePrefix
				+ "enchantments (playername char(36),enchantments INT,PRIMARY KEY (playername))");
		st.addBatch("CREATE TABLE IF NOT EXISTS " + tablePrefix
				+ "levels (playername char(36),levels INT,PRIMARY KEY (playername))");
		st.addBatch("CREATE TABLE IF NOT EXISTS " + tablePrefix
				+ "beds (playername char(36),beds INT,PRIMARY KEY (playername))");
		st.addBatch("CREATE TABLE IF NOT EXISTS " + tablePrefix
				+ "consumedpotions (playername char(36),consumedpotions INT,PRIMARY KEY (playername))");
		st.addBatch("CREATE TABLE IF NOT EXISTS " + tablePrefix
				+ "playedtime (playername char(36),playedtime BIGINT,PRIMARY KEY (playername))");
		st.addBatch("CREATE TABLE IF NOT EXISTS " + tablePrefix
				+ "distancefoot (playername char(36),distancefoot INT,PRIMARY KEY (playername))");
		st.addBatch("CREATE TABLE IF NOT EXISTS " + tablePrefix
				+ "distancepig (playername char(36),distancepig INT,PRIMARY KEY (playername))");
		st.addBatch("CREATE TABLE IF NOT EXISTS " + tablePrefix
				+ "distancehorse (playername char(36),distancehorse INT,PRIMARY KEY (playername))");
		st.addBatch("CREATE TABLE IF NOT EXISTS " + tablePrefix
				+ "distanceminecart (playername char(36),distanceminecart INT,PRIMARY KEY (playername))");
		st.addBatch("CREATE TABLE IF NOT EXISTS " + tablePrefix
				+ "distanceboat (playername char(36),distanceboat INT,PRIMARY KEY (playername))");
		st.addBatch("CREATE TABLE IF NOT EXISTS " + tablePrefix
				+ "distancegliding (playername char(36),distancegliding INT,PRIMARY KEY (playername))");
		st.addBatch("CREATE TABLE IF NOT EXISTS " + tablePrefix
				+ "drops (playername char(36),drops INT,PRIMARY KEY (playername))");
		st.addBatch("CREATE TABLE IF NOT EXISTS " + tablePrefix
				+ "hoeplowing (playername char(36),hoeplowing INT,PRIMARY KEY (playername))");
		st.addBatch("CREATE TABLE IF NOT EXISTS " + tablePrefix
				+ "fertilising (playername char(36),fertilising INT,PRIMARY KEY (playername))");
		st.addBatch("CREATE TABLE IF NOT EXISTS " + tablePrefix
				+ "tames (playername char(36),tames INT,PRIMARY KEY (playername))");
		st.addBatch("CREATE TABLE IF NOT EXISTS " + tablePrefix
				+ "brewing (playername char(36),brewing INT,PRIMARY KEY (playername))");
		st.addBatch("CREATE TABLE IF NOT EXISTS " + tablePrefix
				+ "fireworks (playername char(36),fireworks INT,PRIMARY KEY (playername))");
		st.addBatch("CREATE TABLE IF NOT EXISTS " + tablePrefix
				+ "musicdiscs (playername char(36),musicdiscs INT,PRIMARY KEY (playername))");
		st.addBatch("CREATE TABLE IF NOT EXISTS " + tablePrefix
				+ "enderpearls (playername char(36),enderpearls INT,PRIMARY KEY (playername))");

		st.executeBatch();
		st.close();

	}

	/**
	 * Rename the database tables with the prefix given in the configuration file.
	 */
	private void renameTables() throws SQLException {

		Statement st = sqlConnection.createStatement();

		st.addBatch("ALTER TABLE achievements RENAME TO " + tablePrefix + "achievements");
		st.addBatch("ALTER TABLE breaks RENAME TO " + tablePrefix + "breaks");
		st.addBatch("ALTER TABLE places RENAME TO " + tablePrefix + "places");
		st.addBatch("ALTER TABLE kills RENAME TO " + tablePrefix + "kills");
		st.addBatch("ALTER TABLE crafts RENAME TO " + tablePrefix + "crafts");
		st.addBatch("ALTER TABLE deaths RENAME TO " + tablePrefix + "deaths");
		st.addBatch("ALTER TABLE arrows RENAME TO " + tablePrefix + "arrows");
		st.addBatch("ALTER TABLE snowballs RENAME TO " + tablePrefix + "snowballs");
		st.addBatch("ALTER TABLE eggs RENAME TO " + tablePrefix + "eggs");
		st.addBatch("ALTER TABLE fish RENAME TO " + tablePrefix + "fish");
		st.addBatch("ALTER TABLE itembreaks RENAME TO " + tablePrefix + "itembreaks");
		st.addBatch("ALTER TABLE eatenitems RENAME TO " + tablePrefix + "eatenitems");
		st.addBatch("ALTER TABLE shears RENAME TO " + tablePrefix + "shears");
		st.addBatch("ALTER TABLE milks RENAME TO " + tablePrefix + "milks");
		st.addBatch("ALTER TABLE connections RENAME TO " + tablePrefix + "connections");
		st.addBatch("ALTER TABLE trades RENAME TO " + tablePrefix + "trades");
		st.addBatch("ALTER TABLE anvils RENAME TO " + tablePrefix + "anvils");
		st.addBatch("ALTER TABLE enchantments RENAME TO " + tablePrefix + "enchantments");
		st.addBatch("ALTER TABLE levels RENAME TO " + tablePrefix + "levels");
		st.addBatch("ALTER TABLE beds RENAME TO " + tablePrefix + "beds");
		st.addBatch("ALTER TABLE consumedpotions RENAME TO " + tablePrefix + "consumedpotions");
		st.addBatch("ALTER TABLE playedtime RENAME TO " + tablePrefix + "playedtime");
		st.addBatch("ALTER TABLE distancefoot RENAME TO " + tablePrefix + "distancefoot");
		st.addBatch("ALTER TABLE distancepig RENAME TO " + tablePrefix + "distancepig");
		st.addBatch("ALTER TABLE distancehorse RENAME TO " + tablePrefix + "distancehorse");
		st.addBatch("ALTER TABLE distanceminecart RENAME TO " + tablePrefix + "distanceminecart");
		st.addBatch("ALTER TABLE distanceboat RENAME TO " + tablePrefix + "distanceboat");
		st.addBatch("ALTER TABLE distancegliding RENAME TO " + tablePrefix + "distancegliding");
		st.addBatch("ALTER TABLE drops RENAME TO " + tablePrefix + "drops");
		st.addBatch("ALTER TABLE hoeplowing RENAME TO " + tablePrefix + "hoeplowing");
		st.addBatch("ALTER TABLE fertilising RENAME TO " + tablePrefix + "fertilising");
		st.addBatch("ALTER TABLE tames RENAME TO " + tablePrefix + "tames");
		st.addBatch("ALTER TABLE brewing RENAME TO " + tablePrefix + "brewing");
		st.addBatch("ALTER TABLE fireworks RENAME TO " + tablePrefix + "fireworks");
		st.addBatch("ALTER TABLE musicdiscs RENAME TO " + tablePrefix + "musicdiscs");
		st.addBatch("ALTER TABLE enderpearls RENAME TO " + tablePrefix + "enderpearls");

		st.executeBatch();
		st.close();

	}

	/**
	 * Update the database tables for break, craft and place achievements (from int to varchar for identification
	 * column). The tables are now using material names and no longer item IDs, which are deprecated; this also allows
	 * to store extra data information, extending the number of items available for the user.
	 */
	@SuppressWarnings("deprecation")
	private void updateOldDB(String tableName) {

		try {
			Connection conn = getSQLConnection();
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery("SELECT * FROM " + tableName + "");
			ArrayList<String> uuids = new ArrayList<String>();
			ArrayList<Integer> ids = new ArrayList<Integer>();
			ArrayList<Integer> amounts = new ArrayList<Integer>();
			ArrayList<String> materials = new ArrayList<String>();
			while (rs.next()) {
				uuids.add(rs.getString(1));
				ids.add(rs.getInt(2));
				amounts.add(rs.getInt(3));
			}
			for (int id : ids)
				// Convert from ID to Material name.
				materials.add(Material.getMaterial(id).name().toLowerCase());
			conn.setAutoCommit(false); // Prevent from doing any commits before
										// entire transaction is ready.
			// Create new table.
			if (!tableName.equals("crafts"))
				st.execute("CREATE TABLE tempTable (playername char(36),blockid varchar(64)," + tableName
						+ " INT UNSIGNED,PRIMARY KEY(playername, blockid))");
			else
				st.execute(
						"CREATE TABLE tempTable (playername char(36),item varchar(64),crafts INT UNSIGNED,PRIMARY KEY(playername, item))");

			// Populate new table with contents of the old one and material
			// strings.
			PreparedStatement prep = conn.prepareStatement("INSERT INTO tempTable VALUES (?,?,?);");
			for (int i = 0; i < uuids.size(); ++i) {
				prep.setString(1, uuids.get(i));
				prep.setString(2, materials.get(i));
				prep.setInt(3, amounts.get(i));
				prep.addBatch();
			}

			prep.executeBatch();

			st.execute("DROP TABLE " + tableName + "");
			st.execute("ALTER TABLE tempTable RENAME TO " + tableName + "");
			conn.commit(); // Commit entire transaction.
			conn.setAutoCommit(true);
			st.close();
			rs.close();

		} catch (SQLException e) {
			plugin.getLogger().severe("SQL error while updating old DB: " + e);
		}
	}

	/**
	 * Retrieve SQL connection to MySQL or SQLite database.
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
	private Connection createRemoteSQLConnection() throws SQLException {

		return DriverManager.getConnection(
				databaseAddress + "?autoReconnect=true&user=" + databaseUser + "&password=" + databasePassword);
	}

	/**
	 * Get number of player's kills for a specific mob.
	 */
	public int getKills(Player player, String mobname) {

		try {
			Connection conn = getSQLConnection();
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery("SELECT kills FROM " + tablePrefix + "kills WHERE playername = '"
					+ player.getUniqueId() + "' AND mobname = '" + mobname + "'");
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
	public int getPlaces(Player player, String block) {

		try {
			Connection conn = getSQLConnection();
			int blockBreaks = 0;
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery("SELECT places FROM " + tablePrefix + "places WHERE playername = '"
					+ player.getUniqueId() + "' AND blockid = '" + block + "'");
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
	public int getBreaks(Player player, String block) {

		try {
			Connection conn = getSQLConnection();
			int blockBreaks = 0;
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery("SELECT breaks FROM " + tablePrefix + "breaks WHERE playername = '"
					+ player.getUniqueId() + "' AND blockid = '" + block + "'");
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
	 * Increment and return value of a specific craft achievement statistic.
	 */
	public int getCrafts(Player player, String item) {

		try {
			Connection conn = getSQLConnection();
			int itemCrafts = 0;
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery("SELECT crafts FROM " + tablePrefix + "crafts WHERE playername = '"
					+ player.getUniqueId() + "' AND item = '" + item + "'");
			while (rs.next()) {
				itemCrafts = rs.getInt("crafts");
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
	 * Get the list of achievements of a player.
	 */
	public ArrayList<String> getPlayerAchievementsList(Player player) {

		try {
			Connection conn = getSQLConnection();
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery(
					"SELECT * FROM " + tablePrefix + "achievements WHERE playername = '" + player.getUniqueId() + "'");
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
	 * Get the date of reception of a specific achievement.
	 */
	public String getPlayerAchievementDate(Player player, String name) {

		name = name.replace("'", "''");
		try {
			Connection conn = getSQLConnection();
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery("SELECT date FROM " + tablePrefix + "achievements WHERE playername = '"
					+ player.getUniqueId() + "' AND achievement = '" + name + "'");
			String achievementDate = null;
			if (rs.next()) {
				achievementDate = rs.getString(1);
			}
			st.close();
			rs.close();

			return achievementDate;
		} catch (SQLException e) {
			plugin.getLogger().severe("SQL error while retrieving achievement date: " + e);
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
			ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + tablePrefix + "achievements WHERE playername = '"
					+ player.getUniqueId() + "'");
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
			ResultSet rs = st.executeQuery("SELECT playername, COUNT(*) FROM " + tablePrefix
					+ "achievements GROUP BY playername ORDER BY COUNT(*) DESC LIMIT " + listLength);
			ArrayList<String> topList = new ArrayList<String>();
			while (rs.next()) {
				topList.add(rs.getString(1));
				topList.add("" + rs.getInt(2));
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
			ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM (SELECT DISTINCT playername  FROM " + tablePrefix
					+ "achievements) AS distinctPlayers");
			int players = 0;
			while (rs.next()) {
				players = rs.getInt(1);
			}
			st.close();
			rs.close();

			return players;
		} catch (SQLException e) {
			plugin.getLogger().severe("SQL error while retrieving total players: " + e);
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
			ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM (SELECT COUNT(*) number FROM " + tablePrefix
					+ "achievements GROUP BY playername) AS achGroupedByPlayer WHERE number > (SELECT COUNT(*) FROM achievements WHERE playername = '"
					+ player.getUniqueId() + "')");
			int rank = 0;
			while (rs.next()) {
				rank = rs.getInt(1) + 1;
			}
			st.close();
			rs.close();

			return rank;
		} catch (SQLException e) {
			plugin.getLogger().severe("SQL error while retrieving player rank: " + e);
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
			if (databaseType == POSTGRESQL)
				st.execute("INSERT INTO " + tablePrefix + "achievements VALUES ('" + name + "','" + achievement + "','"
						+ desc + "','" + format.format(new Date())
						+ "') ON CONFLICT (playername,achievement) DO UPDATE SET (description,date)=('" + desc + "','"
						+ format.format(new Date()) + "')");

			else
				st.execute("REPLACE INTO " + tablePrefix + "achievements VALUES ('" + name + "','" + achievement + "','"
						+ desc + "','" + format.format(new Date()) + "')");

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
			if (st.executeQuery("SELECT achievement FROM " + tablePrefix + "achievements WHERE playername = '"
					+ player.getUniqueId() + "' AND achievement = '" + name + "'").next())
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
			st.execute("DELETE FROM " + tablePrefix + "achievements WHERE playername = '" + player.getUniqueId()
					+ "' AND achievement = '" + name + "'");
			st.close();

		} catch (SQLException e) {
			plugin.getLogger().severe("SQL error while deleting achievement: " + e);
		}

	}

	/**
	 * Get the amount of a normal achievement statistic.
	 */
	public int getNormalAchievementAmount(Player player, String table) {

		try {
			Connection conn = getSQLConnection();
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery("SELECT " + table + " FROM " + tablePrefix + table + " WHERE playername = '"
					+ player.getUniqueId() + "'");
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
	 * Return player's number of connections.
	 */
	public int getConnectionsAmount(Player player) {

		final String name = player.getUniqueId().toString();
		try {
			Connection conn = getSQLConnection();
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery(
					"SELECT connections FROM " + tablePrefix + "connections WHERE playername = '" + name + "'");
			int connections = 0;
			while (rs.next()) {
				connections = rs.getInt("connections");
			}
			st.close();
			rs.close();

			return connections;
		} catch (SQLException e) {
			plugin.getLogger().severe("SQL error while retrieving connection statistics: " + e);
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
			ResultSet rs = st.executeQuery("SELECT date FROM " + tablePrefix + "connections WHERE playername = '"
					+ player.getUniqueId() + "'");
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
	 * Update player's number of connections and last connection date and return number of connections.
	 */
	public int updateAndGetConnection(Player player, final String date) {

		final String name = player.getUniqueId().toString();
		try {
			Connection conn = getSQLConnection();
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery(
					"SELECT connections FROM " + tablePrefix + "connections WHERE playername = '" + name + "'");
			int prev = 0;
			while (rs.next()) {
				prev = rs.getInt("connections");
			}
			final int newConnections = prev + 1;
			if (!plugin.isAsyncPooledRequestsSender()) {
				if (databaseType == POSTGRESQL)
					st.execute("INSERT INTO " + tablePrefix + "connections VALUES ('" + name + "', " + newConnections
							+ ", '" + date + "')" + " ON CONFLICT (playername) DO UPDATE SET (connections,date)=('"
							+ newConnections + "','" + date + "')");

				else
					st.execute("REPLACE INTO " + tablePrefix + "connections VALUES ('" + name + "', " + newConnections
							+ ", '" + date + "')");
			} else {
				new Thread() { // Avoid using Bukkit API scheduler, as a
					// reload/restart could kill the async task before
					// write to database has occured.

					@Override
					public void run() {

						Connection conn = getSQLConnection();
						Statement st;
						try {
							st = conn.createStatement();
							if (databaseType == POSTGRESQL)
								st.execute("INSERT INTO " + tablePrefix + "connections VALUES ('" + name + "', "
										+ newConnections + ", '" + date + "')"
										+ " ON CONFLICT (playername) DO UPDATE SET (connections,date)=('"
										+ newConnections + "','" + date + "')");

							else
								st.execute("REPLACE INTO " + tablePrefix + "connections VALUES ('" + name + "', "
										+ newConnections + ", '" + date + "')");
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
	public long updateAndGetPlaytime(String name, long time) {

		try {
			Connection conn = getSQLConnection();
			Statement st = conn.createStatement();
			long newPlayedTime = 0;
			if (time == 0) {
				ResultSet rs = st.executeQuery(
						"SELECT playedtime FROM " + tablePrefix + "playedtime WHERE playername = '" + name + "'");
				newPlayedTime = 0;
				while (rs.next()) {
					newPlayedTime = rs.getLong("playedtime");
				}
				rs.close();
			} else {
				if (databaseType == POSTGRESQL)
					st.execute("INSERT INTO " + tablePrefix + "playedtime VALUES ('" + name + "', " + time + ")"
							+ " ON CONFLICT (playername) DO UPDATE SET (playedtime)=('" + time + "')");

				else
					st.execute("REPLACE INTO " + tablePrefix + "playedtime VALUES ('" + name + "', " + time + ")");

			}
			st.close();

			return newPlayedTime;
		} catch (SQLException e) {
			plugin.getLogger().severe("SQL error while handling play time registration: " + e);
			return 0L;
		}

	}

	/**
	 * Update and return player's distance for a specific distance type.
	 */
	public int updateAndGetDistance(String name, int distance, String type) {

		try {
			Connection conn = getSQLConnection();
			Statement st = conn.createStatement();
			int newDistance = 0;
			if (distance == 0) {
				ResultSet rs = st.executeQuery(
						"SELECT " + type + " FROM " + tablePrefix + type + " WHERE playername = '" + name + "'");
				while (rs.next()) {
					newDistance = rs.getInt(type);
				}
				rs.close();
			} else {
				if (databaseType == POSTGRESQL)
					st.execute("INSERT INTO " + tablePrefix + type + " VALUES ('" + name + "', " + distance + ")"
							+ " ON CONFLICT (playername) DO UPDATE SET (" + type + ")=('" + distance + "')");

				else
					st.execute("REPLACE INTO " + tablePrefix + type + " VALUES ('" + name + "', " + distance + ")");
			}
			st.close();

			return newDistance;
		} catch (SQLException e) {
			plugin.getLogger().severe("SQL error while handling " + type + " registration: " + e);
			return 0;
		}

	}

	public String getTablePrefix() {

		return tablePrefix;
	}

	public boolean isPostgres() {

		return databaseType == POSTGRESQL;
	}

}
