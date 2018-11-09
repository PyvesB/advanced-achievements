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
 * Class used to handle a MySQL database.
 * 
 * @author Pyves
 *
 */
public class MySQLDatabaseManager extends AbstractDatabaseManager {

	public MySQLDatabaseManager(@Named("main") CommentedYamlConfiguration mainConfig, Logger logger,
			@Named("ntd") Map<String, String> namesToDisplayNames, DatabaseUpdater databaseUpdater) {
		super(mainConfig, logger, namesToDisplayNames, databaseUpdater);
	}

	@Override
	void performPreliminaryTasks() throws ClassNotFoundException, UnsupportedEncodingException {
		Class.forName("com.mysql.jdbc.Driver");

		// Get parameters from the MySQL config category.
		databaseAddress = getDatabaseConfig("DatabaseAddress", "Database", "jdbc:mysql://localhost:3306/minecraft");
		databaseUser = URLEncoder.encode(getDatabaseConfig("DatabaseUser", "User", "root"), UTF_8.name());
		databasePassword = URLEncoder.encode(getDatabaseConfig("DatabasePassword", "Password", "root"), UTF_8.name());
	}

	@Override
	Connection createSQLConnection() throws SQLException {
		return DriverManager.getConnection(databaseAddress + "?useSSL=false&autoReconnect=true" + additionalConnectionOptions
				+ "&user=" + databaseUser + "&password=" + databasePassword);
	}

	private String getDatabaseConfig(String newName, String oldName, String defaultValue) {
		return mainConfig.getString(newName, mainConfig.getString("MYSQL." + oldName, defaultValue));
	}
}
