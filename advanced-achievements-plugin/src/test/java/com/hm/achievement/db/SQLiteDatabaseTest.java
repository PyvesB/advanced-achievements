package com.hm.achievement.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Collections;
import java.util.UUID;
import java.util.logging.Logger;

import com.google.common.util.concurrent.MoreExecutors;

import utilities.MockUtility;

public class SQLiteDatabaseTest {

	static SQLiteDatabaseManager db;

	final UUID testUUID = UUID.randomUUID();
	final String testAchievement = "TestAchievement";
	final String testAchievementMsg = "TestMessage";

	static void initDB(MockUtility mockUtility) throws Exception {
		Logger logger = Logger.getLogger("DBTestLogger");
		db = new SQLiteDatabaseManager(mockUtility.getLoadedConfig("config.yml"), logger, Collections.emptyMap(),
				new DatabaseUpdater(logger, null), mockUtility.getPluginMock()) {

			@Override
			public void extractConfigurationParameters() {
				super.extractConfigurationParameters();
				pool = MoreExecutors.newDirectExecutorService();
			}
		};
		db.initialise();
		db.extractConfigurationParameters();
	}

	void registerAchievement() {
		registerAchievement(testUUID, testAchievement, testAchievementMsg);
	}

	void registerAchievement(UUID uuid, String ach, String msg) {
		System.out.println("Saving test achievement: " + uuid + " | " + ach + " | " + msg);
		db.registerAchievement(uuid, ach, msg);
	}

	void registerAchievement(UUID uuid, String ach, String msg, long date) {
		System.out.println("Saving test achievement: " + uuid + " | " + ach + " | " + msg);
		db.registerAchievement(uuid, ach, msg, date);
	}

	void clearDatabase() {
		String sql = "DELETE FROM achievements";

		((SQLWriteOperation) () -> {
			Connection conn = db.getSQLConnection();
			try (PreparedStatement ps = conn.prepareStatement(sql)) {
				ps.execute();
			}
		}).executeOperation(db.pool, null, "Clearing achievements table");
	}
}
