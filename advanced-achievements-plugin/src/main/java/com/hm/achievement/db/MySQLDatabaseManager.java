package com.hm.achievement.db;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.logging.Logger;

import javax.inject.Named;

import com.hm.achievement.exception.PluginLoadError;
import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Class used to handle a MySQL database.
 * 
 * @author Pyves
 *
 */
public class MySQLDatabaseManager extends AbstractRemoteDatabaseManager {

	public MySQLDatabaseManager(@Named("main") CommentedYamlConfiguration mainConfig, Logger logger,
			@Named("ntd") Map<String, String> namesToDisplayNames, DatabaseUpdater databaseUpdater) {
		super(mainConfig, logger, namesToDisplayNames, databaseUpdater, null, "mysql");
	}

	@Override
	void performPreliminaryTasks() throws ClassNotFoundException, PluginLoadError {
		super.performPreliminaryTasks();

		additionalConnectionOptions = "&useSSL=false" + additionalConnectionOptions;
	}
}
