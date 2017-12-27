package com.hm.achievement.db;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.exception.PluginLoadError;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import utilities.MockUtility;

import java.util.UUID;

/**
 * Class for testing SQLite Database.
 *
 * @author Rsl1122
 */
@RunWith(MockitoJUnitRunner.class)
public class SQLiteDatabaseNullSafetyTest extends SQLiteDatabaseTest {

    private SQLiteDatabaseManager db;

    private final UUID testUUID = UUID.randomUUID();
    private final String testAchievement = "TestAchievement";
    private final String testAchievementMsg = "TestMessage";

    @Before
    public void setUp() throws Exception {
        MockUtility mockUtility = MockUtility.setUp()
                .mockLogger()
                .mockPluginConfig();
        AdvancedAchievements pluginMock = mockUtility.getPluginMock();

        db = new SQLiteDatabaseManager(pluginMock);
    }

    @After
    public void tearDown() throws Exception {
        if (db != null) {
            db.shutdown();
        }
    }

    @Test
    public void testRegisterNullUUID() throws PluginLoadError {
        initDB();
        registerAchievement(null, testAchievement, testAchievementMsg);
    }

    @Test
    public void testRegisterNullAch() throws PluginLoadError {
        initDB();
        registerAchievement(testUUID, null, testAchievementMsg);
    }

    @Test
    public void testRegisterNullMsg() throws PluginLoadError {
        initDB();
        registerAchievement(testUUID, testAchievement, null);
    }
}