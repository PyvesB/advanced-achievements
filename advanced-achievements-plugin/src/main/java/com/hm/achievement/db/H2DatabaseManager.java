package com.hm.achievement.db;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

import javax.inject.Named;

import org.bukkit.configuration.file.YamlConfiguration;

import com.hm.achievement.AdvancedAchievements;

/**
 * Class used to handle a H2 database.
 *
 * @author Pyves
 *
 */
public class H2DatabaseManager extends AbstractFileDatabaseManager {

	public H2DatabaseManager(@Named("main") YamlConfiguration mainConfig, Logger logger, DatabaseUpdater databaseUpdater,
			AdvancedAchievements advancedAchievements, ExecutorService writeExecutor) {
		super(mainConfig, logger, databaseUpdater, advancedAchievements, "org.h2.Driver", "jdbc:h2:./"
				+ new File(advancedAchievements.getDataFolder(), "achievements")
				+ ";DATABASE_TO_UPPER=false;MODE=MySQL", "achievements.mv.db", writeExecutor);

		// Convince Maven Shade that H2 is used to prevent full exclusion during minimisation.
		@SuppressWarnings("unused")
		Class<?>[] classes = new Class<?>[] {
				org.h2.engine.Engine.class
		};
	}
}
