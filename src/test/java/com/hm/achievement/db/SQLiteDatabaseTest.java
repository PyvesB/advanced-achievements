package com.hm.achievement.db;

import com.hm.achievement.exception.PluginLoadError;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.UUID;

public class SQLiteDatabaseTest {

    static SQLiteDatabaseManager db;

    final UUID testUUID = UUID.randomUUID();
    final String testAchievement = "TestAchievement";
    final String testAchievementMsg = "TestMessage";

    static void initDB() throws PluginLoadError {
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

    void sleep100ms() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    void clearDatabase() {
        String sql = "DELETE FROM achievements";

        ((SQLWriteOperation) () -> {
            Connection conn = db.getSQLConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.execute();
            }
        }).executeOperation(db.pool, db.plugin.getLogger(), "Clearing achievements table");
    }
}
