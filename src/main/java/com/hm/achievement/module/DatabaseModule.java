package com.hm.achievement.module;

import java.util.Map;
import java.util.logging.Logger;

import javax.inject.Named;
import javax.inject.Singleton;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.command.ReloadCommand;
import com.hm.achievement.db.AbstractSQLDatabaseManager;
import com.hm.achievement.db.DatabaseUpdater;
import com.hm.achievement.db.MySQLDatabaseManager;
import com.hm.achievement.db.PostgreSQLDatabaseManager;
import com.hm.achievement.db.SQLiteDatabaseManager;
import com.hm.mcshared.file.CommentedYamlConfiguration;

import dagger.Module;
import dagger.Provides;

@Module
public class DatabaseModule {

	@Provides
	@Singleton
	AbstractSQLDatabaseManager provideSQLDatabaseManager(@Named("main") CommentedYamlConfiguration mainConfig, Logger logger,
			Map<String, String> achievementsAndDisplayNames, DatabaseUpdater databaseUpdater, ReloadCommand reloadCommand,
			AdvancedAchievements advancedAchievements) {
		String dataHandler = mainConfig.getString("DatabaseType", "sqlite");
		if ("mysql".equalsIgnoreCase(dataHandler)) {
			return new MySQLDatabaseManager(mainConfig, logger, achievementsAndDisplayNames, databaseUpdater, reloadCommand);
		} else if ("postgresql".equalsIgnoreCase(dataHandler)) {
			return new PostgreSQLDatabaseManager(mainConfig, logger, achievementsAndDisplayNames, databaseUpdater,
					reloadCommand);
		} else {
			// User has specified "sqlite" or an invalid type.
			return new SQLiteDatabaseManager(mainConfig, logger, achievementsAndDisplayNames, databaseUpdater, reloadCommand,
					advancedAchievements);
		}
	}

}
