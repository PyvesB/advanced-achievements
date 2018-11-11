package com.hm.achievement.db;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Logger;

import javax.inject.Named;

import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Class used to handle a remote (in the sense not managed by the plugin) database.
 * 
 * @author Pyves
 *
 */
public class AbstractRemoteDatabaseManager extends AbstractDatabaseManager {

	volatile String databaseAddress;
	volatile String databaseUser;
	volatile String databasePassword;
	volatile String additionalConnectionOptions;

	private final String oldConfigPath;

	public AbstractRemoteDatabaseManager(@Named("main") CommentedYamlConfiguration mainConfig, Logger logger,
			@Named("ntd") Map<String, String> namesToDisplayNames, DatabaseUpdater databaseUpdater, String driverPath,
			String oldConfigPath) {
		super(mainConfig, logger, namesToDisplayNames, databaseUpdater, driverPath);
		this.oldConfigPath = oldConfigPath;
	}

	@Override
	void performPreliminaryTasks() throws ClassNotFoundException, UnsupportedEncodingException {
		Class.forName(driverPath);

		databaseAddress = getDatabaseConfig("DatabaseAddress", "Database", "");
		databaseUser = URLEncoder.encode(getDatabaseConfig("DatabaseUser", "User", "root"), UTF_8.name());
		databasePassword = URLEncoder.encode(getDatabaseConfig("DatabasePassword", "Password", "root"), UTF_8.name());
		additionalConnectionOptions = mainConfig.getString("AdditionalConnectionOptions", "");
	}

	@Override
	Connection createSQLConnection() throws SQLException {
		return DriverManager.getConnection(databaseAddress + "?autoReconnect=true" + additionalConnectionOptions + "&user="
				+ databaseUser + "&password=" + databasePassword);
	}

	private String getDatabaseConfig(String newName, String oldName, String defaultValue) {
		return mainConfig.getString(newName, mainConfig.getString(oldConfigPath + "." + oldName, defaultValue));
	}
}
