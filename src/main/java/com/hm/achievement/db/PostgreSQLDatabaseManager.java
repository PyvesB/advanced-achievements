package com.hm.achievement.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.NormalAchievements;

/**
 * Class used to handle a PosgreSQL database. Note that some query methods are overriden as the SQL syntax is different
 * from other database types.
 * 
 * @author Pyves
 *
 */
public class PostgreSQLDatabaseManager extends AbstractSQLDatabaseManager {

	public PostgreSQLDatabaseManager(AdvancedAchievements plugin) {
		super(plugin);
	}

	@Override
	protected void performPreliminaryTasks() throws ClassNotFoundException {
		Class.forName("org.postgresql.Driver");
		
		// Get parameters from the PostgreSQL config category.
		databaseAddress = plugin.getPluginConfig().getString("POSTGRESQL.Database",
				"jdbc:postgresql://localhost:5432/minecraft");
		databaseUser = plugin.getPluginConfig().getString("POSTGRESQL.User", "root");
		databasePassword = plugin.getPluginConfig().getString("POSTGRESQL.Password", "root");
	}

	@Override
	protected Connection createSQLConnection() throws SQLException {
		return DriverManager.getConnection(databaseAddress + "?autoReconnect=true" + additionalConnectionOptions
				+ "&user=" + databaseUser + "&password=" + databasePassword);
	}

	@Override
	public void registerAchievement(final UUID player, final String achName, final String achMessage) {
		((SQLWriteOperation) () -> {
			// PostgreSQL has no REPLACE operator. We have to use the INSERT ... ON CONFLICT construct, which is
			// available for PostgreSQL 9.5+.
			String query = "INSERT INTO " + tablePrefix + "achievements VALUES ('" + player.toString() + "',?,?,?)"
					+ " ON CONFLICT (playername,achievement) DO UPDATE SET (description,date)=(?,?)";
			Connection conn = getSQLConnection();
			try (PreparedStatement prep = conn.prepareStatement(query)) {
				prep.setString(1, achName);
				prep.setString(2, achMessage);
				prep.setDate(3, new java.sql.Date(new java.util.Date().getTime()));
				prep.setString(4, achMessage);
				prep.setDate(5, new java.sql.Date(new java.util.Date().getTime()));
				prep.execute();
			}
		}).executeOperation(pool, plugin.getLogger(), "SQL error while registering achievement.");
	}

	@Override
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
				// PostgreSQL has no REPLACE operator. We have to use the INSERT ... ON CONFLICT construct,
				// which is available for PostgreSQL 9.5+.
				String query = "INSERT INTO " + tablePrefix + dbName + " VALUES ('" + player.toString() + "', "
						+ newConnections + ", ?)" + " ON CONFLICT (playername) DO UPDATE SET (" + dbName + ",date)=('"
						+ newConnections + "', ?)";
				try (PreparedStatement writePrep = writeConn.prepareStatement(query)) {
					writePrep.setString(1, date);
					writePrep.setString(2, date);
					writePrep.execute();
				}
			}).executeOperation(pool, plugin.getLogger(), "SQL error while updating connection.");
			return newConnections;
		} catch (SQLException e) {
			plugin.getLogger().log(Level.SEVERE, "SQL error while handling connection event: ", e);
		}
		return 0;
	}
}
