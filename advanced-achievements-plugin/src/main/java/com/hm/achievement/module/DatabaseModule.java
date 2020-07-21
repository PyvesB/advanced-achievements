package com.hm.achievement.module;

import java.util.Map;
import java.util.logging.Logger;

import javax.inject.Named;
import javax.inject.Singleton;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.db.AbstractDatabaseManager;
import com.hm.achievement.db.DatabaseUpdater;
import com.hm.achievement.db.H2DatabaseManager;
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
	AbstractDatabaseManager provideSQLDatabaseManager(@Named("main") CommentedYamlConfiguration mainConfig, Logger logger,
			@Named("ntd") Map<String, String> namesToDisplayNames, DatabaseUpdater databaseUpdater,
			AdvancedAchievements advancedAchievements) {
		String databaseType = advancedAchievements.getConfig().getString("DatabaseType", "sqlite");
		if ("mysql".equalsIgnoreCase(databaseType)) {
			return new MySQLDatabaseManager(mainConfig, logger, namesToDisplayNames, databaseUpdater);
		} else if ("postgresql".equalsIgnoreCase(databaseType)) {
			return new PostgreSQLDatabaseManager(mainConfig, logger, namesToDisplayNames, databaseUpdater);
		} else if ("h2".equalsIgnoreCase(databaseType)) {
			return new H2DatabaseManager(mainConfig, logger, namesToDisplayNames, databaseUpdater, advancedAchievements);
		} else {
			// User has specified "sqlite" or an invalid type.
			return new SQLiteDatabaseManager(mainConfig, logger, namesToDisplayNames, databaseUpdater, advancedAchievements);
		}
	}

}
