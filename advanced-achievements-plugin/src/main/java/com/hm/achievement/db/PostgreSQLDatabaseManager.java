package com.hm.achievement.db;

import java.sql.*;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import javax.inject.Named;

import com.hm.achievement.category.NormalAchievements;
import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Class used to handle a PosgreSQL database. Note that some query methods are overriden as the SQL syntax is different
 * from other database types.
 * 
 * @author Pyves
 *
 */
public class PostgreSQLDatabaseManager extends AbstractRemoteDatabaseManager {

	public PostgreSQLDatabaseManager(@Named("main") CommentedYamlConfiguration mainConfig, Logger logger,
			@Named("ntd") Map<String, String> namesToDisplayNames, DatabaseUpdater databaseUpdater) {
		super(mainConfig, logger, namesToDisplayNames, databaseUpdater, "org.postgresql.ds.PGSimpleDataSource",
				"postgresql");
	}

	@Override
	public void registerAchievement(UUID uuid, String achName, String achMessage) {
		// PostgreSQL has no REPLACE operator. We have to use the INSERT ... ON CONFLICT construct, which is available
		// for PostgreSQL 9.5+.
		String sql = "INSERT INTO " + prefix + "achievements VALUES (?,?,?,?)"
				+ " ON CONFLICT (playername,achievement) DO UPDATE SET (description,date)=(?,?)";
		((SQLWriteOperation) () -> {
			try (final Connection conn = getDataSource().getConnection();
					final PreparedStatement ps = conn.prepareStatement(sql)) {
				ps.setObject(1, uuid, Types.CHAR);
				ps.setString(2, achName);
				ps.setString(3, achMessage);
				ps.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
				ps.setString(5, achMessage);
				ps.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
				ps.execute();
			}
		}).executeOperation(pool, logger, "registering an achievement");
	}

	@Override
	public int updateAndGetConnection(UUID uuid, String date) {
		final String dbName = NormalAchievements.CONNECTIONS.toDBName();
		final String sqlRead = "SELECT " + dbName + " FROM " + prefix + dbName + " WHERE playername = ?";
		final int connections = ((SQLReadOperation<Integer>) () -> {
			try (final Connection conn = getDataSource().getConnection();
					final PreparedStatement ps = conn.prepareStatement(sqlRead)) {
				ps.setString(1, uuid.toString());
				ResultSet rs = ps.executeQuery();
				return rs.next() ? rs.getInt(dbName) + 1 : 1;
			}
		}).executeOperation("handling connection event");

		// PostgreSQL has no REPLACE operator. We have to use the INSERT ... ON CONFLICT construct, which is
		// available for PostgreSQL 9.5+.
		final String sqlWrite = "INSERT INTO " + prefix + dbName + " VALUES (?,?,?)"
				+ " ON CONFLICT (playername) DO UPDATE SET (" + dbName + ",date)=(?,?)";
		((SQLWriteOperation) () -> {
			try (final Connection conn = getDataSource().getConnection();
					final PreparedStatement ps = conn.prepareStatement(sqlWrite)) {
				ps.setObject(1, uuid, Types.CHAR);
				ps.setInt(2, connections);
				ps.setString(3, date);
				ps.setInt(4, connections);
				ps.setString(5, date);
				ps.execute();
			}
		}).executeOperation(pool, logger, "updating connection date and count");

		return connections;
	}
}
