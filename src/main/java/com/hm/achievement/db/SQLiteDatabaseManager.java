package com.hm.achievement.db;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Named;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.command.ReloadCommand;
import com.hm.achievement.exception.PluginLoadError;
import com.hm.mcshared.file.CommentedYamlConfiguration;
import com.hm.mcshared.file.FileManager;

/**
 * Class used to handle a SQLite database.
 * 
 * @author Pyves
 *
 */
public class SQLiteDatabaseManager extends AbstractSQLDatabaseManager {

	private final AdvancedAchievements advancedAchievements;
	private boolean configDatabaseBackup;

	public SQLiteDatabaseManager(@Named("main") CommentedYamlConfiguration mainConfig, Logger logger,
			Map<String, String> achievementsAndDisplayNames, DatabaseUpdater databaseUpdater, ReloadCommand reloadCommand,
			AdvancedAchievements advancedAchievements) {
		super(mainConfig, logger, achievementsAndDisplayNames, databaseUpdater, reloadCommand);
		this.advancedAchievements = advancedAchievements;
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		configDatabaseBackup = mainConfig.getBoolean("DatabaseBackup", true);
	}

	@Override
	void performPreliminaryTasks() throws ClassNotFoundException, PluginLoadError {
		Class.forName("org.sqlite.JDBC");

		if (configDatabaseBackup) {
			File backup = new File(advancedAchievements.getDataFolder(), "achievements.db.bak");
			// Only do a daily backup for the .db file.
			if (System.currentTimeMillis() - backup.lastModified() > 86400000L || backup.length() == 0L) {
				logger.info("Backing up database file...");
				try {
					FileManager fileManager = new FileManager("achievements.db", advancedAchievements);
					fileManager.backupFile();
				} catch (IOException e) {
					logger.log(Level.SEVERE, "Error while backing up database file:", e);
				}
			}
		}

		File dbfile = new File(advancedAchievements.getDataFolder(), "achievements.db");
		try {
			if (dbfile.createNewFile()) {
				logger.info("Successfully created new SQLite database file.");
			}
		} catch (IOException e) {
			throw new PluginLoadError("Error while creating database file.", e);
		}
	}

	@Override
	Connection createSQLConnection() throws SQLException {
		File dbfile = new File(advancedAchievements.getDataFolder(), "achievements.db");
		return DriverManager.getConnection("jdbc:sqlite:" + dbfile);
	}
}
