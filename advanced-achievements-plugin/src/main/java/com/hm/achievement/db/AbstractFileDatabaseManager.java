package com.hm.achievement.db;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Named;

import org.bukkit.configuration.file.YamlConfiguration;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.exception.PluginLoadError;

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

	public AbstractFileDatabaseManager(@Named("main") YamlConfiguration mainConfig, Logger logger,
			DatabaseUpdater databaseUpdater, AdvancedAchievements advancedAchievements,
			String driverPath, String url, String filename, ExecutorService writeExecutor) {
		super(mainConfig, logger, databaseUpdater, driverPath, writeExecutor);
		this.advancedAchievements = advancedAchievements;
		this.url = url;
		this.filename = filename;
	}

	@Override
	void performPreliminaryTasks() throws ClassNotFoundException, PluginLoadError {
		Class.forName(driverPath);

		if (mainConfig.getBoolean("DatabaseBackup")) {
			File backup = new File(advancedAchievements.getDataFolder(), filename + ".bak");
			File database = new File(advancedAchievements.getDataFolder(), filename);
			if (database.lastModified() - backup.lastModified() > TimeUnit.DAYS.toMillis(1)) {
				logger.info("Backing up database file...");
				try {
					Files.copy(database.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {
					logger.log(Level.SEVERE, "Error while backing up database file:", e);
				}
			}
		}
	}

	@Override
	Connection createConnection() throws SQLException {
		return DriverManager.getConnection(url);
	}
}
