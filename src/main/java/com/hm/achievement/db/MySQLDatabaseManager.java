package com.hm.achievement.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Logger;

import javax.inject.Named;

import com.hm.achievement.command.executable.ReloadCommand;
import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Class used to handle a MySQL database.
 * 
 * @author Pyves
 *
 */
public class MySQLDatabaseManager extends AbstractDatabaseManager {

	public MySQLDatabaseManager(@Named("main") CommentedYamlConfiguration mainConfig, Logger logger,
			Map<String, String> achievementsAndDisplayNames, DatabaseUpdater databaseUpdater, ReloadCommand reloadCommand) {
		super(mainConfig, logger, achievementsAndDisplayNames, databaseUpdater, reloadCommand);
	}

	@Override
	void performPreliminaryTasks() throws ClassNotFoundException {
		Class.forName("com.mysql.jdbc.Driver");

		// Get parameters from the MySQL config category.
		databaseAddress = mainConfig.getString("MYSQL.Database", "jdbc:mysql://localhost:3306/minecraft");
		databaseUser = mainConfig.getString("MYSQL.User", "root");
		databasePassword = mainConfig.getString("MYSQL.Password", "root");
	}

	@Override
	Connection createSQLConnection() throws SQLException {
		return DriverManager.getConnection(databaseAddress + "?useSSL=false&autoReconnect=true" + additionalConnectionOptions
				+ "&user=" + databaseUser + "&password=" + databasePassword);
	}
}
