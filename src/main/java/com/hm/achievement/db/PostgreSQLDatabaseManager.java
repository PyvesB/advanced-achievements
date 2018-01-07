package com.hm.achievement.db;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.NormalAchievements;

import java.sql.*;
import java.util.UUID;

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
		String sql = "INSERT INTO " + prefix + "achievements VALUES (?,?,?,?)"
				+ " ON CONFLICT (playername,achievement) DO UPDATE SET (description,date)=(?,?)";
		((SQLWriteOperation) () -> {
			Connection conn = getSQLConnection();
			try (PreparedStatement ps = conn.prepareStatement(sql)) {
				ps.setObject(1, uuid);
				ps.setString(2, achName);
				ps.setString(3, achMessage);
				ps.setDate(4, new Date(System.currentTimeMillis()));
				ps.setString(5, achMessage);
				ps.setDate(6, new Date(System.currentTimeMillis()));
				ps.execute();
			}
		}).executeOperation(pool, plugin.getLogger(), "registering an achievement");
	}

	@Override
	public int updateAndGetConnection(UUID uuid, String date) {
		String dbName = NormalAchievements.CONNECTIONS.toDBName();
		String sqlRead = "SELECT " + dbName + " FROM " + prefix + dbName + " WHERE playername = ?";
		return ((SQLReadOperation<Integer>) () -> {
			Connection conn = getSQLConnection();
			try (PreparedStatement ps = conn.prepareStatement(sqlRead)) {
				ps.setString(1, uuid.toString());
				ResultSet rs = ps.executeQuery();
				int connections = rs.next() ? rs.getInt(dbName) + 1 : 1;
				// PostgreSQL has no REPLACE operator. We have to use the INSERT ... ON CONFLICT construct, which is
				// available for PostgreSQL 9.5+.
				String sqlWrite = "INSERT INTO " + prefix + dbName + " VALUES (?,?,?)"
						+ " ON CONFLICT (playername) DO UPDATE SET (" + dbName + ",date)=(?,?)";
				((SQLWriteOperation) () -> {
					Connection writeConn = getSQLConnection();
					try (PreparedStatement writePrep = writeConn.prepareStatement(sqlWrite)) {
						writePrep.setObject(1, uuid);
						writePrep.setInt(2, connections);
						writePrep.setString(3, date);
						writePrep.setInt(4, connections);
						writePrep.setString(5, date);
						writePrep.execute();
					}
				}).executeOperation(pool, plugin.getLogger(), "updating connection date and count");
				return connections;
			}
		}).executeOperation("handling connection event");
	}
}
