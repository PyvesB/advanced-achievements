package com.hm.achievement.db;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

import javax.inject.Named;

import org.bukkit.configuration.file.YamlConfiguration;

import com.hm.achievement.AdvancedAchievements;

/**
 * Class used to handle a SQLite database.
 *
 * @author Pyves
 *
 */
public class SQLiteDatabaseManager extends AbstractFileDatabaseManager {

	public SQLiteDatabaseManager(@Named("main") YamlConfiguration mainConfig, Logger logger, DatabaseUpdater databaseUpdater,
			AdvancedAchievements advancedAchievements, ExecutorService writeExecutor) {
		super(mainConfig, logger, databaseUpdater, advancedAchievements, "org.sqlite.JDBC", "jdbc:sqlite:"
				+ new File(advancedAchievements.getDataFolder(), "achievements.db"), "achievements.db", writeExecutor);
	}
}
