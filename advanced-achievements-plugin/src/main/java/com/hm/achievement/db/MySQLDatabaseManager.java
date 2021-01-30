package com.hm.achievement.db;

import java.io.UnsupportedEncodingException;
import java.util.logging.Logger;

import javax.inject.Named;

import org.bukkit.configuration.file.YamlConfiguration;

/**
 * Class used to handle a MySQL database.
 * 
 * @author Pyves
 *
 */
public class MySQLDatabaseManager extends AbstractRemoteDatabaseManager {

	public MySQLDatabaseManager(@Named("main") YamlConfiguration mainConfig, Logger logger,
			DatabaseUpdater databaseUpdater) {
		super(mainConfig, logger, databaseUpdater, "com.mysql.jdbc.Driver", "mysql");
	}

	@Override
	void performPreliminaryTasks() throws ClassNotFoundException, UnsupportedEncodingException {
		super.performPreliminaryTasks();

		additionalConnectionOptions = "&useSSL=false" + additionalConnectionOptions;
	}
}
