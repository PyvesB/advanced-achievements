package com.hm.achievement.db;

import java.io.File;
import java.util.Map;
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

	public H2DatabaseManager(@Named("main") YamlConfiguration mainConfig, Logger logger,
			@Named("ntd") Map<String, String> namesToDisplayNames, DatabaseUpdater databaseUpdater,
			AdvancedAchievements advancedAchievements) {
		super(mainConfig, logger, namesToDisplayNames, databaseUpdater, advancedAchievements, "org.h2.Driver", "jdbc:h2:./"
				+ new File(advancedAchievements.getDataFolder(), "achievements") + ";DATABASE_TO_UPPER=false;MODE=MySQL",
				"achievements.mv.db");
	}
}
