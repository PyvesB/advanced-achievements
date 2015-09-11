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

public class SQLDatabases {

	private static AdvancedAchievements plugin;

	public Connection getSQLConnection() {

		String dataHandler = plugin.getConfig().getString("DatabaseType", "sqlite");
		String mysqlDatabase = plugin.getConfig().getString("MYSQL.Database", "jdbc:mysql://localhost:3306/minecraft");
		String mysqlUser = plugin.getConfig().getString("MYSQL.User", "root");
		String mysqlPassword = plugin.getConfig().getString("MYSQL.Password", "root");
		if (dataHandler.equalsIgnoreCase("mysql")) {
			try {
				return DriverManager.getConnection(mysqlDatabase + "?autoReconnect=true&user=" + mysqlUser
						+ "&password=" + mysqlPassword);
			} catch (SQLException ex) {
				plugin.getLogger().severe("Unable to retreive connection" + ex);
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
					st.execute("CREATE TABLE IF NOT EXISTS `breaks` (" + "playername char(36),"
							+ "blockid SMALLINT UNSIGNED," + "breaks INT UNSIGNED,"
							+ "PRIMARY KEY(`playername`, `blockid`)" + ")");
					st.execute("CREATE TABLE IF NOT EXISTS `places` (" + "playername char(36),"
							+ "blockid SMALLINT UNSIGNED," + "places INT UNSIGNED,"
							+ "PRIMARY KEY(`playername`, `blockid`)" + ")");
					st.execute("CREATE TABLE IF NOT EXISTS `kills` (" + "playername char(36)," + "mobname varchar(32),"
							+ "kills INT UNSIGNED," + "PRIMARY KEY (`playername`, `mobname`)" + ")");
					st.execute("CREATE TABLE IF NOT EXISTS `crafts` (" + "playername char(36),"
							+ "item SMALLINT UNSIGNED," + "times INT UNSIGNED," + "PRIMARY KEY (`playername`, `item`)"
							+ ")");
					st.execute("CREATE TABLE IF NOT EXISTS `achievements` (" + "playername char(36),"
							+ "achievement varchar(64)," + "desc varchar(128)," + "date varchar(10),"
							+ "PRIMARY KEY (`playername`, `achievement`)" + ")");
					st.execute("CREATE TABLE IF NOT EXISTS `deaths` (" + "playername char(36),"
							+ "deaths INT UNSIGNED," + "PRIMARY KEY (`playername`)" + ")");
					st.execute("CREATE TABLE IF NOT EXISTS `arrows` (" + "playername char(36),"
							+ "arrows INT UNSIGNED," + "PRIMARY KEY (`playername`)" + ")");
					st.execute("CREATE TABLE IF NOT EXISTS `snowballs` (" + "playername char(36),"
							+ "snowballs INT UNSIGNED," + "PRIMARY KEY (`playername`)" + ")");
					st.execute("CREATE TABLE IF NOT EXISTS `eggs` (" + "playername char(36)," + "eggs INT UNSIGNED,"
							+ "PRIMARY KEY (`playername`)" + ")");
					st.execute("CREATE TABLE IF NOT EXISTS `fish` (" + "playername char(36)," + "fish INT UNSIGNED,"
							+ "PRIMARY KEY (`playername`)" + ")");
					st.execute("CREATE TABLE IF NOT EXISTS `itembreaks` (" + "playername char(36),"
							+ "itembreaks INT UNSIGNED," + "PRIMARY KEY (`playername`)" + ")");
					st.execute("CREATE TABLE IF NOT EXISTS `eatenitems` (" + "playername char(36),"
							+ "eatenitems INT UNSIGNED," + "PRIMARY KEY (`playername`)" + ")");
					st.execute("CREATE TABLE IF NOT EXISTS `shears` (" + "playername char(36),"
							+ "shears INT UNSIGNED," + "PRIMARY KEY (`playername`)" + ")");
					st.execute("CREATE TABLE IF NOT EXISTS `milks` (" + "playername char(36)," + "milks INT UNSIGNED,"
							+ "PRIMARY KEY (`playername`)" + ")");
					st.execute("CREATE TABLE IF NOT EXISTS `connections` (" + "playername char(36),"
							+ "connections INT UNSIGNED," + "date varchar(10)," + "PRIMARY KEY (`playername`)" + ")");
					st.execute("CREATE TABLE IF NOT EXISTS `trades` (" + "playername char(36),"
							+ "trades INT UNSIGNED," + "PRIMARY KEY (`playername`)" + ")");
					st.execute("CREATE TABLE IF NOT EXISTS `anvils` (" + "playername char(36),"
							+ "anvils INT UNSIGNED," + "PRIMARY KEY (`playername`)" + ")");
					st.execute("CREATE TABLE IF NOT EXISTS `enchantments` (" + "playername char(36),"
							+ "enchantments INT UNSIGNED," + "PRIMARY KEY (`playername`)" + ")");
					st.execute("CREATE TABLE IF NOT EXISTS `levels` (" + "playername char(36),"
							+ "levels INT UNSIGNED," + "PRIMARY KEY (`playername`)" + ")");
					st.execute("CREATE TABLE IF NOT EXISTS `beds` (" + "playername char(36)," + "beds INT UNSIGNED,"
							+ "PRIMARY KEY (`playername`)" + ")");
					st.execute("CREATE TABLE IF NOT EXISTS `consumedpotions` (" + "playername char(36),"
							+ "consumedpotions INT UNSIGNED," + "PRIMARY KEY (`playername`)" + ")");
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
					return conn;
				} catch (IOException ex) {
					plugin.getLogger().severe("File write error: achievements.db");
				} catch (SQLException ex) {
					plugin.getLogger().severe("SQLite exception on initialize" + ex);
				} catch (ClassNotFoundException ex) {
					plugin.getLogger().severe("You need the SQLite JBDC library. Google it. Put it in /lib folder.");
				}
			}
			try {
				Class.forName("org.sqlite.JDBC");
				Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbfile);
				return conn;

			} catch (SQLException ex) {
				plugin.getLogger().severe("SQLite exception on initialize" + ex);
			} catch (ClassNotFoundException ex) {
				plugin.getLogger().severe("You need the SQLite library." + ex);
			}
		}
		return null;
	}

	public void initialise(AdvancedAchievements plugin) {

		SQLDatabases.plugin = plugin;
		Connection conn = getSQLConnection();
		if (conn == null) {
			plugin.getLogger().severe("[Achievement] Could not establish SQL connection. Disabling Achievement");
			plugin.getLogger().severe("[Achievement] Adjust Settings in Config or set MySql: False");
			plugin.getServer().getPluginManager().disablePlugin(plugin);
			return;

		}
		try {
			if (plugin.getConfig().getString("DatabaseType", "sqlite").equalsIgnoreCase("mysql")) {
				Statement st = conn.createStatement();
				st.execute("CREATE TABLE IF NOT EXISTS `breaks` (" + "playername char(36),"
						+ "blockid SMALLINT UNSIGNED," + "breaks INT UNSIGNED,"
						+ "PRIMARY KEY(`playername`, `blockid`)" + ")");
				st.execute("CREATE TABLE IF NOT EXISTS `places` (" + "playername char(36),"
						+ "blockid SMALLINT UNSIGNED," + "places INT UNSIGNED,"
						+ "PRIMARY KEY(`playername`, `blockid`)" + ")");
				st.execute("CREATE TABLE IF NOT EXISTS `kills` (" + "playername char(36)," + "mobname varchar(32),"
						+ "kills INT UNSIGNED," + "PRIMARY KEY (`playername`, `mobname`)" + ")");
				st.execute("CREATE TABLE IF NOT EXISTS `crafts` (" + "playername char(36)," + "item SMALLINT UNSIGNED,"
						+ "times INT UNSIGNED," + "PRIMARY KEY (`playername`, `item`)" + ")");
				st.execute("CREATE TABLE IF NOT EXISTS `achievements` (" + "playername char(36),"
						+ "achievement varchar(64)," + "description varchar(128)," + "date varchar(10),"
						+ "PRIMARY KEY (`playername`, `achievement`)" + ")");
				st.execute("CREATE TABLE IF NOT EXISTS `deaths` (" + "playername char(36)," + "deaths INT UNSIGNED,"
						+ "PRIMARY KEY (`playername`)" + ")");
				st.execute("CREATE TABLE IF NOT EXISTS `arrows` (" + "playername char(36)," + "arrows INT UNSIGNED,"
						+ "PRIMARY KEY (`playername`)" + ")");
				st.execute("CREATE TABLE IF NOT EXISTS `snowballs` (" + "playername char(36),"
						+ "snowballs INT UNSIGNED," + "PRIMARY KEY (`playername`)" + ")");
				st.execute("CREATE TABLE IF NOT EXISTS `eggs` (" + "playername char(36)," + "eggs INT UNSIGNED,"
						+ "PRIMARY KEY (`playername`)" + ")");
				st.execute("CREATE TABLE IF NOT EXISTS `fish` (" + "playername char(36)," + "fish INT UNSIGNED,"
						+ "PRIMARY KEY (`playername`)" + ")");
				st.execute("CREATE TABLE IF NOT EXISTS `itembreaks` (" + "playername char(36),"
						+ "itembreaks INT UNSIGNED," + "PRIMARY KEY (`playername`)" + ")");
				st.execute("CREATE TABLE IF NOT EXISTS `eatenitems` (" + "playername char(36),"
						+ "eatenitems INT UNSIGNED," + "PRIMARY KEY (`playername`)" + ")");
				st.execute("CREATE TABLE IF NOT EXISTS `shears` (" + "playername char(36)," + "shears INT UNSIGNED,"
						+ "PRIMARY KEY (`playername`)" + ")");
				st.execute("CREATE TABLE IF NOT EXISTS `milks` (" + "playername char(36)," + "milks INT UNSIGNED,"
						+ "PRIMARY KEY (`playername`)" + ")");
				st.execute("CREATE TABLE IF NOT EXISTS `connections` (" + "playername char(36),"
						+ "connections INT UNSIGNED," + "date varchar(10)," + "PRIMARY KEY (`playername`)" + ")");
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
			}

			else {

				// Update old SQLite database versions.
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

				if (plugin.getDatabaseVersion() == 3) {
					Statement st = conn.createStatement();
					st.execute("CREATE TABLE IF NOT EXISTS `consumedpotions` (" + "playername char(36),"
							+ "consumedpotions INT UNSIGNED," + "PRIMARY KEY (`playername`)" + ")");
					st.close();
					plugin.setDatabaseVersion(4);

				}
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
			}
			conn.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@SuppressWarnings("deprecation")
	public Integer registerCraft(Player player, ItemStack item) {

		try {
			Connection conn = getSQLConnection();
			Integer itemCrafts = 0;
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery("SELECT times FROM `crafts` WHERE playername = '" + player.getUniqueId()
					+ "' AND item = " + item.getTypeId());
			while (rs.next()) {
				itemCrafts = rs.getInt("times");
			}
			Integer newCrafts = itemCrafts + 1;
			st.execute("replace into `crafts` (playername, item, times) VALUES ('" + player.getUniqueId() + "',"
					+ item.getTypeId() + ", " + newCrafts + ")");
			st.close();
			rs.close();
			conn.close();
			return newCrafts;
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}

	}

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
			e.printStackTrace();
			return 0;
		}
	}

	@SuppressWarnings("deprecation")
	public Integer getPlace(Player player, Block block) {

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
			e.printStackTrace();
			return 0;
		}
	}

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
			e.printStackTrace();
			return 0;
		}
	}

	public ArrayList<String> getAchievements(Player player) {

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
			e.printStackTrace();
		}
		return null;
	}

	public int countAchievements(Player player) {

		try {
			Connection conn = getSQLConnection();
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM `achievements` WHERE playername = '"
					+ player.getUniqueId() + "'");
			int numberAchievements = 0;
			if (rs.next()) {
				numberAchievements = rs.getInt(1);
			}

			st.close();
			rs.close();
			conn.close();
			return numberAchievements;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;

	}

	public ArrayList<String> getTop(int topList) {

		try {
			Connection conn = getSQLConnection();
			Statement st = conn.createStatement();
			ResultSet rs = st
					.executeQuery("SELECT playername, COUNT(*) FROM `achievements` GROUP BY playername ORDER BY COUNT(*) DESC LIMIT "
							+ topList);
			ArrayList<String> achievementsList = new ArrayList<String>();
			while (rs.next()) {
				achievementsList.add(rs.getString("playername"));
				achievementsList.add("" + rs.getInt("COUNT(*)"));
			}
			st.close();
			rs.close();
			conn.close();
			return achievementsList;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return new ArrayList<String>();

	}

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
			e.printStackTrace();
		}

	}

	public boolean hasAchievement(Player player, String name) {

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
			e.printStackTrace();
		}
		return false;

	}

	public Integer registerDeath(Player player) {

		try {
			Connection conn = getSQLConnection();
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery("SELECT deaths from `deaths` WHERE playername = '" + player.getUniqueId()
					+ "'");
			Integer prev = 0;
			while (rs.next()) {
				prev = rs.getInt("deaths");
			}
			Integer newDeaths = prev + 1;
			st.execute("replace into `deaths` (playername, deaths) VALUES ('" + player.getUniqueId() + "', "
					+ newDeaths + ")");
			st.close();
			rs.close();
			conn.close();
			return newDeaths;
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}

	public Integer getArrows(Player player) {

		try {
			Connection conn = getSQLConnection();
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery("SELECT arrows from `arrows` WHERE playername = '" + player.getUniqueId()
					+ "'");
			Integer arrows = 0;
			while (rs.next()) {
				arrows = rs.getInt("arrows");
			}

			rs.close();
			conn.close();
			return arrows;
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}

	public Integer getSnowballs(Player player) {

		try {
			Connection conn = getSQLConnection();
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery("SELECT snowballs from `snowballs` WHERE playername = '"
					+ player.getUniqueId() + "'");
			Integer snowballs = 0;
			while (rs.next()) {
				snowballs = rs.getInt("snowballs");
			}

			st.close();
			rs.close();
			conn.close();
			return snowballs;
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}

	public Integer getDrops(Player player) {

		try {
			Connection conn = getSQLConnection();
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery("SELECT drops from `drops` WHERE playername = '" + player.getUniqueId()
					+ "'");
			Integer drops = 0;
			while (rs.next()) {
				drops = rs.getInt("drops");
			}

			st.close();
			rs.close();
			conn.close();
			return drops;
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	public Integer getHoePlowing(Player player) {

		try {
			Connection conn = getSQLConnection();
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery("SELECT hoeplowing from `hoeplowing` WHERE playername = '" + player.getUniqueId()
					+ "'");
			Integer drops = 0;
			while (rs.next()) {
				drops = rs.getInt("hoeplowing");
			}

			st.close();
			rs.close();
			conn.close();
			return drops;
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	public Integer getFertilising(Player player) {

		try {
			Connection conn = getSQLConnection();
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery("SELECT fertilising from `fertilising` WHERE playername = '" + player.getUniqueId()
					+ "'");
			Integer drops = 0;
			while (rs.next()) {
				drops = rs.getInt("fertilising");
			}

			st.close();
			rs.close();
			conn.close();
			return drops;
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}

	public Integer getEggs(Player player) {

		try {
			Connection conn = getSQLConnection();
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery("SELECT eggs from `eggs` WHERE playername = '" + player.getUniqueId() + "'");
			Integer eggs = 0;
			while (rs.next()) {
				eggs = rs.getInt("eggs");
			}

			st.close();
			rs.close();
			conn.close();
			return eggs;
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}

	public Integer registerFish(Player player) {

		try {
			Connection conn = getSQLConnection();
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery("SELECT fish from `fish` WHERE playername = '" + player.getUniqueId() + "'");
			Integer prev = 0;
			while (rs.next()) {
				prev = rs.getInt("fish");
			}
			Integer newFish = prev + 1;
			st.execute("replace into `fish` (playername, fish) VALUES ('" + player.getUniqueId() + "', " + newFish
					+ ")");
			st.close();
			rs.close();
			conn.close();
			return newFish;
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}

	public Integer registerItemBreak(Player player) {

		try {
			Connection conn = getSQLConnection();
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery("SELECT itembreaks from `itembreaks` WHERE playername = '"
					+ player.getUniqueId() + "'");
			Integer prev = 0;
			while (rs.next()) {
				prev = rs.getInt("itembreaks");
			}
			Integer newItemBreaks = prev + 1;
			st.execute("replace into `itembreaks` (playername, itembreaks) VALUES ('" + player.getUniqueId() + "', "
					+ newItemBreaks + ")");
			st.close();
			rs.close();
			conn.close();
			return newItemBreaks;
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}

	public Integer registerEatenItem(Player player) {

		try {
			Connection conn = getSQLConnection();
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery("SELECT eatenitems from `eatenitems` WHERE playername = '"
					+ player.getUniqueId() + "'");
			Integer prev = 0;
			while (rs.next()) {
				prev = rs.getInt("eatenitems");
			}
			Integer newEatenItems = prev + 1;
			st.execute("replace into `eatenitems` (playername, eatenitems) VALUES ('" + player.getUniqueId() + "', "
					+ newEatenItems + ")");
			st.close();
			rs.close();
			conn.close();
			return newEatenItems;
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}

	public Integer getShear(Player player) {

		try {
			Connection conn = getSQLConnection();
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery("SELECT shears from `shears` WHERE playername = '" + player.getUniqueId()
					+ "'");
			Integer shears = 0;
			while (rs.next()) {
				shears = rs.getInt("shears");
			}

			st.close();
			rs.close();
			conn.close();
			return shears;
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}

	public Integer registerMilk(Player player) {

		try {
			Connection conn = getSQLConnection();
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery("SELECT milks from `milks` WHERE playername = '" + player.getUniqueId()
					+ "'");
			Integer prev = 0;
			while (rs.next()) {
				prev = rs.getInt("milks");
			}
			Integer newMilks = prev + 1;
			st.execute("replace into `milks` (playername, milks) VALUES ('" + player.getUniqueId() + "', " + newMilks
					+ ")");
			st.close();
			rs.close();
			conn.close();
			return newMilks;
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}

	public Integer registerConnection(Player player, String format) {

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
					+ "', " + newConnections + ", '" + format + "')");
			st.close();
			rs.close();
			conn.close();
			return newConnections;
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}

	}

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
			e.printStackTrace();
			return null;
		}

		catch (NullPointerException e) {
			return null;
		}

	}

	public Integer registerTrade(Player player) {

		try {
			Connection conn = getSQLConnection();
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery("SELECT trades from `trades` WHERE playername = '" + player.getUniqueId()
					+ "'");
			Integer prev = 0;
			while (rs.next()) {
				prev = rs.getInt("trades");
			}
			Integer newTrades = prev + 1;
			st.execute("replace into `trades` (playername, trades) VALUES ('" + player.getUniqueId() + "', "
					+ newTrades + ")");
			st.close();
			rs.close();
			conn.close();
			return newTrades;
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}

	public Integer registerAnvil(Player player) {

		try {
			Connection conn = getSQLConnection();
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery("SELECT anvils from `anvils` WHERE playername = '" + player.getUniqueId()
					+ "'");
			Integer prev = 0;
			while (rs.next()) {
				prev = rs.getInt("anvils");
			}
			Integer newAnvils = prev + 1;
			st.execute("replace into `anvils` (playername, anvils) VALUES ('" + player.getUniqueId() + "', "
					+ newAnvils + ")");
			st.close();
			rs.close();
			conn.close();
			return newAnvils;
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}

	public Integer registerEnchantment(Player player) {

		try {
			Connection conn = getSQLConnection();
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery("SELECT enchantments from `enchantments` WHERE playername = '"
					+ player.getUniqueId() + "'");
			Integer prev = 0;
			while (rs.next()) {
				prev = rs.getInt("enchantments");
			}
			Integer newEnchantments = prev + 1;
			st.execute("replace into `enchantments` (playername, enchantments) VALUES ('" + player.getUniqueId()
					+ "', " + newEnchantments + ")");
			st.close();
			rs.close();
			conn.close();
			return newEnchantments;
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}

	public Integer registerXP(Player player) {

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
			e.printStackTrace();
			return 0;
		}
	}

	public Integer registerBed(Player player) {

		try {
			Connection conn = getSQLConnection();
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery("SELECT beds from `beds` WHERE playername = '" + player.getUniqueId() + "'");
			Integer prev = 0;
			while (rs.next()) {
				prev = rs.getInt("beds");
			}
			Integer newBeds = prev + 1;
			st.execute("replace into `beds` (playername, beds) VALUES ('" + player.getUniqueId() + "', " + newBeds
					+ ")");
			st.close();
			rs.close();
			conn.close();
			return newBeds;
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}

	public Integer registerPotions(Player player) {

		try {
			Connection conn = getSQLConnection();
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery("SELECT consumedpotions from `consumedpotions` WHERE playername = '"
					+ player.getUniqueId() + "'");
			Integer prev = 0;
			while (rs.next()) {
				prev = rs.getInt("consumedpotions");
			}
			Integer newConsumedPotions = prev + 1;
			st.execute("replace into `consumedpotions` (playername, consumedpotions) VALUES ('" + player.getUniqueId()
					+ "', " + newConsumedPotions + ")");
			st.close();
			rs.close();
			conn.close();
			return newConsumedPotions;
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}

	public Long registerPlaytime(Player player, Long time) {

		try {
			Connection conn = getSQLConnection();
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery("SELECT playedtime from `playedtime` WHERE playername = '"
					+ player.getUniqueId() + "'");
			Integer prev = 0;
			while (rs.next()) {
				prev = rs.getInt("playedtime");
			}
			Long newPlayedTime = prev + time;
			if (time != 0)
				st.execute("replace into `playedtime` (playername, playedtime) VALUES ('" + player.getUniqueId()
						+ "', " + newPlayedTime + ")");
			st.close();
			rs.close();
			conn.close();
			return newPlayedTime;
		} catch (SQLException e) {
			e.printStackTrace();
			return (long) 0;
		}

	}

	public Long registerDistanceFoot(Player player, Long distance) {

		try {
			Connection conn = getSQLConnection();
			Statement st = conn.createStatement();
			Long newDistance = (long) 0;
			if (distance == 0) {
				ResultSet rs = st.executeQuery("SELECT distancefoot from `distancefoot` WHERE playername = '"
						+ player.getUniqueId() + "'");
				while (rs.next()) {
					newDistance = rs.getLong("distancefoot");
				}
				rs.close();
			} else if (distance != 0)
				st.execute("replace into `distancefoot` (playername, distancefoot) VALUES ('" + player.getUniqueId()
						+ "', " + distance + ")");
			st.close();
			conn.close();
			return newDistance;
		} catch (SQLException e) {
			e.printStackTrace();
			return (long) 0;
		}

	}

	public Long registerDistancePig(Player player, Long distance) {

		try {
			Connection conn = getSQLConnection();
			Statement st = conn.createStatement();
			Long newDistance = (long) 0;
			if (distance == 0) {
				ResultSet rs = st.executeQuery("SELECT distancepig from `distancepig` WHERE playername = '"
						+ player.getUniqueId() + "'");
				while (rs.next()) {
					newDistance = rs.getLong("distancepig");
				}
				rs.close();
			} else if (distance != 0)
				st.execute("replace into `distancepig` (playername, distancepig) VALUES ('" + player.getUniqueId()
						+ "', " + distance + ")");
			st.close();
			conn.close();
			return newDistance;
		} catch (SQLException e) {
			e.printStackTrace();
			return (long) 0;
		}

	}

	public Long registerDistanceHorse(Player player, Long distance) {

		try {
			Connection conn = getSQLConnection();
			Statement st = conn.createStatement();
			Long newDistance = (long) 0;
			if (distance == 0) {
				ResultSet rs = st.executeQuery("SELECT distancehorse from `distancehorse` WHERE playername = '"
						+ player.getUniqueId() + "'");
				while (rs.next()) {
					newDistance = rs.getLong("distancehorse");
				}
				rs.close();
			} else if (distance != 0)
				st.execute("replace into `distancehorse` (playername, distancehorse) VALUES ('" + player.getUniqueId()
						+ "', " + distance + ")");
			st.close();
			conn.close();
			return newDistance;
		} catch (SQLException e) {
			e.printStackTrace();
			return (long) 0;
		}

	}

	public Long registerDistanceMinecart(Player player, Long distance) {

		try {
			Connection conn = getSQLConnection();
			Statement st = conn.createStatement();
			Long newDistance = (long) 0;
			if (distance == 0) {
				ResultSet rs = st.executeQuery("SELECT distanceminecart from `distanceminecart` WHERE playername = '"
						+ player.getUniqueId() + "'");
				while (rs.next()) {
					newDistance = rs.getLong("distanceminecart");
				}
				rs.close();
			} else if (distance != 0)
				st.execute("replace into `distanceminecart` (playername, distanceminecart) VALUES ('"
						+ player.getUniqueId() + "', " + distance + ")");
			st.close();
			conn.close();
			return newDistance;
		} catch (SQLException e) {
			e.printStackTrace();
			return (long) 0;
		}

	}

	public Long registerDistanceBoat(Player player, Long distance) {

		try {
			Connection conn = getSQLConnection();
			Statement st = conn.createStatement();
			Long newDistance = (long) 0;
			if (distance == 0) {
				ResultSet rs = st.executeQuery("SELECT distanceboat from `distanceboat` WHERE playername = '"
						+ player.getUniqueId() + "'");
				while (rs.next()) {
					newDistance = rs.getLong("distanceboat");
				}
				rs.close();
			} else if (distance != 0)
				st.execute("replace into `distanceboat` (playername, distanceboat) VALUES ('" + player.getUniqueId()
						+ "', " + distance + ")");
			st.close();
			conn.close();
			return newDistance;
		} catch (SQLException e) {
			e.printStackTrace();
			return (long) 0;
		}

	}

}
