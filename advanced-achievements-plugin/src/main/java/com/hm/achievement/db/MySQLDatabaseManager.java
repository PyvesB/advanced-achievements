package com.hm.achievement.db;

import java.io.UnsupportedEncodingException;
import java.util.logging.Logger;

import javax.inject.Named;

import org.bukkit.configuration.file.YamlConfiguration;

import com.hm.achievement.config.AchievementMap;

/**
 * Class used to handle a MySQL database.
 * 
 * @author Pyves
 *
 */
public class MySQLDatabaseManager extends AbstractRemoteDatabaseManager {

	public MySQLDatabaseManager(@Named("main") YamlConfiguration mainConfig, Logger logger, AchievementMap achievementMap,
			DatabaseUpdater databaseUpdater) {
		super(mainConfig, logger, achievementMap, databaseUpdater, "com.mysql.jdbc.Driver", "mysql");
	}

	@Override
	void performPreliminaryTasks() throws ClassNotFoundException, UnsupportedEncodingException {
		super.performPreliminaryTasks();

		additionalConnectionOptions = "&useSSL=false" + additionalConnectionOptions;
	}
}
