package com.hm.achievement.db;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.exception.PluginLoadError;
import com.hm.mcshared.file.FileManager;

/**
 * Class used to handle a SQLite database.
 * 
 * @author Pyves
 *
 */
public class SQLiteDatabaseManager extends AbstractSQLDatabaseManager {

	private boolean configDatabaseBackup;

	public SQLiteDatabaseManager(AdvancedAchievements plugin) {
		super(plugin);
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		configDatabaseBackup = plugin.getPluginConfig().getBoolean("DatabaseBackup", true);
	}

	@Override
	protected void performPreliminaryTasks() throws ClassNotFoundException, PluginLoadError {
		Class.forName("org.sqlite.JDBC");

		if (configDatabaseBackup) {
			File backup = new File(plugin.getDataFolder(), "achievements.db.bak");
			// Only do a daily backup for the .db file.
			if (System.currentTimeMillis() - backup.lastModified() > 86400000L || backup.length() == 0L) {
				plugin.getLogger().info("Backing up database file...");
				try {
					FileManager fileManager = new FileManager("achievements.db", plugin);
					fileManager.backupFile();
				} catch (IOException e) {
					plugin.getLogger().log(Level.SEVERE, "Error while backing up database file.", e);
				}
			}
		}

		File dbfile = new File(plugin.getDataFolder(), "achievements.db");
		try {
			if (dbfile.createNewFile()) {
				plugin.getLogger().info("Successfully created database file.");
			}
		} catch (IOException e) {
			throw new PluginLoadError("Error while creating database file.", e);
		}
	}

	@Override
	protected Connection createSQLConnection() throws SQLException {
		File dbfile = new File(plugin.getDataFolder(), "achievements.db");
		return DriverManager.getConnection("jdbc:sqlite:" + dbfile);
	}
}
