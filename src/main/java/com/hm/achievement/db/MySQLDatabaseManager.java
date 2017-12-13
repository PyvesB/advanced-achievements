package com.hm.achievement.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.hm.achievement.AdvancedAchievements;

/**
 * Class used to handle a MySQL database.
 * 
 * @author Pyves
 *
 */
public class MySQLDatabaseManager extends AbstractSQLDatabaseManager {

	public MySQLDatabaseManager(AdvancedAchievements plugin) {
		super(plugin);
	}

	@Override
	protected void performPreliminaryTasks() throws ClassNotFoundException {
		Class.forName("com.mysql.jdbc.Driver");

		// Get parameters from the MySQL config category.
		databaseAddress = plugin.getPluginConfig().getString("MYSQL.Database", "jdbc:mysql://localhost:3306/minecraft");
		databaseUser = plugin.getPluginConfig().getString("MYSQL.User", "root");
		databasePassword = plugin.getPluginConfig().getString("MYSQL.Password", "root");
	}

	@Override
	protected Connection createSQLConnection() throws SQLException {
		return DriverManager.getConnection(databaseAddress + "?useSSL=false&autoReconnect=true"
				+ additionalConnectionOptions + "&user=" + databaseUser + "&password=" + databasePassword);
	}
}
