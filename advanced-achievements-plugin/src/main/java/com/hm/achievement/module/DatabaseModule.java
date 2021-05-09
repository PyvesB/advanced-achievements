package com.hm.achievement.module;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import javax.inject.Named;
import javax.inject.Singleton;

import org.bukkit.configuration.file.YamlConfiguration;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.db.AbstractDatabaseManager;
import com.hm.achievement.db.DatabaseUpdater;
import com.hm.achievement.db.H2DatabaseManager;
import com.hm.achievement.db.MySQLDatabaseManager;
import com.hm.achievement.db.PostgreSQLDatabaseManager;
import com.hm.achievement.db.SQLiteDatabaseManager;

import dagger.Module;
import dagger.Provides;

@Module
public class DatabaseModule {

	@Provides
	@Singleton
	ExecutorService provideWriteExecutor() {
		// Used to do perform the database write operations asynchronously. We expect to execute many short writes to
		// the database. The pool can grow dynamically under high load and allows to reuse threads.
		return Executors.newCachedThreadPool();
	}

	@Provides
	@Singleton
	AbstractDatabaseManager provideSQLDatabaseManager(@Named("main") YamlConfiguration mainConfig, Logger logger,
			DatabaseUpdater databaseUpdater, AdvancedAchievements advancedAchievements, ExecutorService writeExecutor) {
		String databaseType = advancedAchievements.getConfig().getString("DatabaseType", "sqlite");
		if ("mysql".equalsIgnoreCase(databaseType)) {
			return new MySQLDatabaseManager(mainConfig, logger, databaseUpdater, writeExecutor);
		} else if ("postgresql".equalsIgnoreCase(databaseType)) {
			return new PostgreSQLDatabaseManager(mainConfig, logger, databaseUpdater, writeExecutor);
		} else if ("h2".equalsIgnoreCase(databaseType)) {
			return new H2DatabaseManager(mainConfig, logger, databaseUpdater, advancedAchievements, writeExecutor);
		} else {
			// User has specified "sqlite" or an invalid type.
			return new SQLiteDatabaseManager(mainConfig, logger, databaseUpdater, advancedAchievements, writeExecutor);
		}
	}

}
