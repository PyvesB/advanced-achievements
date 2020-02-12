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
import com.hm.achievement.exception.PluginLoadError;
import com.hm.mcshared.file.CommentedYamlConfiguration;
import com.hm.mcshared.file.FileManager;

/**
 * Class used to handle a file-backed database.
 *
 * @author Pyves
 *
 */
public class AbstractFileDatabaseManager extends AbstractDatabaseManager {

	private final AdvancedAchievements advancedAchievements;
	private final String url;
	private final String filename;

	private boolean configDatabaseBackup;

	public AbstractFileDatabaseManager(@Named("main") CommentedYamlConfiguration mainConfig, Logger logger,
			@Named("ntd") Map<String, String> namesToDisplayNames, DatabaseUpdater databaseUpdater,
			AdvancedAchievements advancedAchievements, String driverPath, String url, String filename) {
		super(mainConfig, logger, namesToDisplayNames, databaseUpdater, driverPath);
		this.advancedAchievements = advancedAchievements;
		this.url = url;
		this.filename = filename;
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		configDatabaseBackup = mainConfig.getBoolean("DatabaseBackup", true);
	}

	@Override
	void performPreliminaryTasks() throws ClassNotFoundException, PluginLoadError {
		Class.forName(driverPath);

		if (configDatabaseBackup) {
			File backup = new File(advancedAchievements.getDataFolder(), filename + ".bak");
			// Only do a daily backup for the .db file.
			if (System.currentTimeMillis() - backup.lastModified() > 86400000L || backup.length() == 0L) {
				logger.info("Backing up database file...");
				try {
					FileManager fileManager = new FileManager(filename, advancedAchievements);
					fileManager.backupFile();
				} catch (IOException e) {
					logger.log(Level.SEVERE, "Error while backing up database file:", e);
				}
			}
		}
	}

	@Override
	Connection createSQLConnection() throws SQLException {
		return DriverManager.getConnection(url);
	}
}
