package com.hm.achievement.db;

import com.hm.achievement.exception.PluginLoadError;

import java.util.UUID;

public class SQLiteDatabaseTest {

    SQLiteDatabaseManager db;

    final UUID testUUID = UUID.randomUUID();
    final String testAchievement = "TestAchievement";
    final String testAchievementMsg = "TestMessage";

    void initDB() throws PluginLoadError {
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

    void sleep25ms() {
        try {
            Thread.sleep(25);
        } catch (InterruptedException e) {
        }
    }
}
