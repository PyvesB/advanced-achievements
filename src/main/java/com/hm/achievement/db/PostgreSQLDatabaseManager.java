package com.hm.achievement.db;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

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
	public void registerAchievement(UUID uuid, String achName, String achMessage) {
		// PostgreSQL has no REPLACE operator. We have to use the INSERT ... ON CONFLICT construct, which is available
		// for PostgreSQL 9.5+.
		String sql = "INSERT INTO " + prefix + "achievements VALUES ('" + uuid + "',?,?,?)"
				+ " ON CONFLICT (playername,achievement) DO UPDATE SET (description,date)=(?,?)";
		((SQLWriteOperation) () -> {
			Connection conn = getSQLConnection();
			try (PreparedStatement ps = conn.prepareStatement(sql)) {
				ps.setString(1, achName);
				ps.setString(2, achMessage);
				ps.setDate(3, new Date(System.currentTimeMillis()));
				ps.setString(4, achMessage);
				ps.setDate(5, new Date(System.currentTimeMillis()));
				ps.execute();
			}
		}).executeOperation(pool, plugin.getLogger(), "SQL error while registering achievement.");
	}

	@Override
	public int updateAndGetConnection(UUID uuid, String date) {
		String dbName = NormalAchievements.CONNECTIONS.toDBName();
		String sqlRead = "SELECT " + dbName + " FROM " + prefix + dbName + " WHERE playername = ?";
		return ((SQLReadOperation<Integer>) () -> {
			Connection conn = getSQLConnection();
			try (PreparedStatement ps = conn.prepareStatement(sqlRead)) {
				ps.setString(1, uuid.toString());
				int connections = 1;
				ResultSet rs = ps.executeQuery();
				if (rs.next()) {
					connections += rs.getInt(dbName);
				}
				// PostgreSQL has no REPLACE operator. We have to use the INSERT ... ON CONFLICT construct, which is
				// available for PostgreSQL 9.5+.
				String sqlWrite = "INSERT INTO " + prefix + dbName + " VALUES ('" + uuid + "', " + connections + ", ?)"
						+ " ON CONFLICT (playername) DO UPDATE SET (" + dbName + ",date)=('" + connections + "', ?)";
				((SQLWriteOperation) () -> {
					Connection writeConn = getSQLConnection();
					try (PreparedStatement writePrep = writeConn.prepareStatement(sqlWrite)) {
						writePrep.setString(1, date);
						writePrep.setString(2, date);
						writePrep.execute();
					}
				}).executeOperation(pool, plugin.getLogger(), "SQL error while updating connection.");
				return connections;
			}
		}).executeOperation("SQL error while updating connection");
	}
}
