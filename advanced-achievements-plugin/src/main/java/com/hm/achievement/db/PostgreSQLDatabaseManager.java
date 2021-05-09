package com.hm.achievement.db;

import java.io.UnsupportedEncodingException;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

import javax.inject.Named;

import org.bukkit.configuration.file.YamlConfiguration;

import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.db.data.ConnectionInformation;

/**
 * Class used to handle a PosgreSQL database. Note that some query methods are overriden as the SQL syntax is different
 * from other database types.
 * 
 * @author Pyves
 *
 */
public class PostgreSQLDatabaseManager extends AbstractRemoteDatabaseManager {

	public PostgreSQLDatabaseManager(@Named("main") YamlConfiguration mainConfig, Logger logger,
			DatabaseUpdater databaseUpdater, ExecutorService writeExecutor) {
		super(mainConfig, logger, databaseUpdater, "org.postgresql.Driver", "postgresql", writeExecutor);
	}

	@Override
	void performPreliminaryTasks() throws ClassNotFoundException, UnsupportedEncodingException {
		super.performPreliminaryTasks();

		// Convince Maven Shade that PostgreSQL is used to prevent full exclusion during minimisation.
		@SuppressWarnings("unused")
		Class<?>[] classes = new Class<?>[] {
				org.postgresql.Driver.class
		};
	}

	@Override
	public void registerAchievement(UUID uuid, String achName, long time) {
		((SQLWriteOperation) () -> {
			// PostgreSQL has no REPLACE operator. We have to use the INSERT ... ON CONFLICT construct, which is
			// available for PostgreSQL 9.5+.
			String sql = "INSERT INTO " + prefix + "achievements VALUES (?,?,?) ON CONFLICT (playername,achievement) "
					+ "DO UPDATE SET (date)=(?)";
			try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
				ps.setString(1, uuid.toString());
				ps.setString(2, achName);
				ps.setTimestamp(3, new Timestamp(time));
				ps.setTimestamp(4, new Timestamp(time));
				ps.execute();
			}
		}).executeOperation(writeExecutor, logger, "registering an achievement");
	}

	@Override
	public void updateConnectionInformation(UUID uuid, long connections) {
		((SQLWriteOperation) () -> {
			// PostgreSQL has no REPLACE operator. We have to use the INSERT ... ON CONFLICT construct, which is
			// available for PostgreSQL 9.5+.
			String sql = "INSERT INTO " + prefix + NormalAchievements.CONNECTIONS.toDBName() + " VALUES (?,?,?) ON"
					+ " CONFLICT (playername) DO UPDATE SET (" + NormalAchievements.CONNECTIONS.toDBName() + ",date)=(?,?)";
			try (PreparedStatement writePrep = getConnection().prepareStatement(sql)) {
				String date = ConnectionInformation.today();
				writePrep.setString(1, uuid.toString());
				writePrep.setLong(2, connections);
				writePrep.setString(3, date);
				writePrep.setLong(4, connections);
				writePrep.setString(5, date);
				writePrep.execute();
			}
		}).executeOperation(writeExecutor, logger, "updating connection date and count");
	}
}
