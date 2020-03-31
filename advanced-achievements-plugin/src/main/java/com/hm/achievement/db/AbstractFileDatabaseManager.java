package com.hm.achievement.db;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Named;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.exception.PluginLoadError;
import com.hm.mcshared.file.CommentedYamlConfiguration;
import com.hm.mcshared.file.FileManager;
import com.zaxxer.hikari.HikariConfig;

/**
 * Class used to handle a file-backed database.
 *
 * @author Pyves
 *
 */
public class AbstractFileDatabaseManager extends AbstractDatabaseManager {

	private final AdvancedAchievements advancedAchievements;
	private final String jdbcUrl;
	private final String filename;

	public AbstractFileDatabaseManager(@Named("main") CommentedYamlConfiguration mainConfig, Logger logger,
			@Named("ntd") Map<String, String> namesToDisplayNames, DatabaseUpdater databaseUpdater,
			AdvancedAchievements advancedAchievements, String dataSourceClassName, String jdbcUrl, String filename) {
		super(mainConfig, logger, namesToDisplayNames, databaseUpdater, dataSourceClassName);
		this.advancedAchievements = advancedAchievements;
		this.jdbcUrl = jdbcUrl;
		this.filename = filename;
	}

	@Override
	void performPreliminaryTasks() throws ClassNotFoundException {
		if (dataSourceClassName != null) Class.forName(dataSourceClassName);

		if (mainConfig.getBoolean("DatabaseBackup", true)) {
			File backup = new File(advancedAchievements.getDataFolder(), filename + ".bak");
			if (System.currentTimeMillis() - backup.lastModified() > TimeUnit.DAYS.toMillis(1) || backup.length() == 0L) {
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
	HikariConfig getConfig() {
		HikariConfig config = new HikariConfig();
		config.setDataSourceClassName(dataSourceClassName);
		config.setJdbcUrl(jdbcUrl);
		config.setMaximumPoolSize(1); // Allow only one connection at a time to file databases
		return config;
	}
}
