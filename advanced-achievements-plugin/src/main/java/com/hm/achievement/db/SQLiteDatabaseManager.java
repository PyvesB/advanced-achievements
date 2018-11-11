package com.hm.achievement.db;

import java.io.File;
import java.util.Map;
import java.util.logging.Logger;

import javax.inject.Named;

import com.hm.achievement.AdvancedAchievements;
import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Class used to handle a SQLite database.
 *
 * @author Pyves
 *
 */
public class SQLiteDatabaseManager extends AbstractFileDatabaseManager {

	public SQLiteDatabaseManager(@Named("main") CommentedYamlConfiguration mainConfig, Logger logger,
			@Named("ntd") Map<String, String> namesToDisplayNames, DatabaseUpdater databaseUpdater,
			AdvancedAchievements advancedAchievements) {
		super(mainConfig, logger, namesToDisplayNames, databaseUpdater, advancedAchievements, "org.sqlite.JDBC",
				"jdbc:sqlite:" + new File(advancedAchievements.getDataFolder(), "achievements.db"), "achievements.db");
	}
}
