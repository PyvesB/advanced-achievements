package com.hm.achievement.db;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.exception.PluginLoadError;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import utilities.MockUtility;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Class for testing SQLite Database.
 *
 * @author Rsl1122
 */
@RunWith(MockitoJUnitRunner.class)
public class SQLiteDatabaseNullSafetyTest extends SQLiteDatabaseTest {

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
        sleep25ms();

        List<String> list = db.getPlayerAchievementsList(null);
        Map<UUID, Integer> map = db.getPlayersAchievementsAmount();

        System.out.println("Saved Achievements: " + list);
        System.out.println("Saved Achievements Map: " + map);

        assertTrue("An achievement was saved for null UUID", list.isEmpty());
        assertFalse(map.containsKey(null));
    }

    @Test
    public void testGetMethodsForNullUUIDExceptions() throws PluginLoadError, SQLException {
        initDB();

        String sql = "REPLACE INTO achievements VALUES ('" + null + "',?,?,?)";

        try (Connection connection = db.getSQLConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, testAchievement);
                ps.setString(2, testAchievementMsg);
                ps.setDate(3, new Date(System.currentTimeMillis()));
                ps.execute();
            }
        }
        sleep25ms();

        db.getPlayerAchievementsList(null);
        db.getPlayersAchievementsAmount();
        db.getPlayerAchievementsAmount(null);
        db.getPlayerAchievementNamesList(null);
        db.getPlayerAchievementDate(null, testAchievement);
    }

    @Test
    public void testRegisterNullAch() throws PluginLoadError {
        initDB();
        registerAchievement(testUUID, null, testAchievementMsg);
        sleep25ms();

        List<String> list = db.getPlayerAchievementsList(testUUID);
        Map<UUID, Integer> map = db.getPlayersAchievementsAmount();


        System.out.println("Saved Achievements: " + list);
        System.out.println("Saved Achievements Map: " + map);

        assertTrue("An achievement with name 'null' was saved", list.isEmpty());
        assertEquals(0, (int) map.getOrDefault(testUUID, 0));
    }

    @Test
    public void testRegisterNullMsg() throws PluginLoadError {
        initDB();
        registerAchievement(testUUID, testAchievement, null);

        sleep25ms();

        List<String> list = db.getPlayerAchievementsList(testUUID);
        System.out.println("Saved Achievements: " + list);

        assertEquals(3, list.size());
        String message = list.get(1);
        assertNotNull("Message was null!", message);
    }
}