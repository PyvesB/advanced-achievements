package com.hm.achievement.db;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.exception.PluginLoadError;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import utilities.MockUtility;

import java.util.Date;
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
public class SQLiteDatabaseBasicTest extends SQLiteDatabaseTest {

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
    public void testInitialization() throws PluginLoadError {
        initDB();
    }

    @Test
    public void testGetAchievementList() throws PluginLoadError {
        initDB();
        registerAchievement();
        sleep25ms();

        List<String> achievements = db.getPlayerAchievementsList(testUUID);
        assertFalse("List was empty", achievements.isEmpty());

        String actual = achievements.get(0);
        System.out.println("Saved Achievement: " + actual);
        assertEquals(testAchievement, actual);

    }

    @Test
    public void testAchievementCount() throws PluginLoadError {
        initDB();
        registerAchievement();
        sleep25ms();

        Map<UUID, Integer> map = db.getPlayersAchievementsAmount();
        assertFalse("Map was empty", map.isEmpty());

        Integer amount = map.get(testUUID);
        assertNotNull(amount);
        assertEquals(1, (int) amount);
    }

    @Test
    public void testAchievementDateRegistration() throws PluginLoadError {
        initDB();

        String date = db.getPlayerAchievementDate(testUUID, testAchievement);
        assertNull(date);

        registerAchievement();
        sleep25ms();

        date = db.getPlayerAchievementDate(testUUID, testAchievement);
        assertNotNull(date);
    }

    @Test
    public void testPlayerAchievementAmount() throws PluginLoadError {
        initDB();
        registerAchievement();
        sleep25ms();

        assertEquals(1, db.getPlayerAchievementsAmount(testUUID));
    }

    @Test
    public void testDeleteAchievement() throws PluginLoadError {
        testPlayerAchievementAmount();

        db.deletePlayerAchievement(testUUID, testAchievement);
        sleep25ms();

        assertEquals(0, db.getPlayerAchievementsAmount(testUUID));
    }

    @Test
    public void testDeleteAchievementQuotes() throws PluginLoadError {
        initDB();
        registerAchievement(testUUID, "'" + testAchievement + "'", testAchievementMsg);
        sleep25ms();
        registerAchievement(testUUID, "''" + testAchievement + "''", testAchievementMsg);
        sleep25ms();
        registerAchievement(testUUID, testAchievement, testAchievementMsg);
        sleep25ms();

        assertEquals(3, db.getPlayerAchievementsAmount(testUUID));

        db.deletePlayerAchievement(testUUID, "'" + testAchievement + "'");
        db.deletePlayerAchievement(testUUID, testAchievement);
        sleep25ms();

        assertEquals(0, db.getPlayerAchievementsAmount(testUUID));
    }

    @Test
    public void testConnectionUpdate() throws PluginLoadError {
        initDB();

        assertEquals(0, db.getConnectionsAmount(testUUID));

        assertEquals(1, db.updateAndGetConnection(testUUID, new Date().toString()));
        sleep25ms();
        assertEquals(2, db.updateAndGetConnection(testUUID, new Date().toString()));
        sleep25ms();
        assertEquals(3, db.updateAndGetConnection(testUUID, new Date().toString()));
        sleep25ms();

        assertEquals(3, db.getConnectionsAmount(testUUID));
    }

    @Test
    public void testGetTopAchievements() throws PluginLoadError {
        initDB();
        long firstSave = System.currentTimeMillis();

        registerAchievement(testUUID, testAchievement, testAchievementMsg);

        sleep25ms();
        sleep25ms();
        long secondSave = System.currentTimeMillis();

        UUID secondUUID = UUID.randomUUID();
        String secondAch = "TestAchievement2";

        registerAchievement(secondUUID, testAchievement, testAchievementMsg);
        sleep25ms();
        registerAchievement(secondUUID, secondAch, testAchievementMsg);
        sleep25ms();

        Map<String, Integer> topList = db.getTopList(0);
        assertEquals(2, topList.size());
        assertEquals(2, (int) topList.get(secondUUID.toString()));
        assertEquals(1, (int) topList.get(testUUID.toString()));

        Map<String, Integer> topListFirst = db.getTopList(firstSave);
        assertEquals("Top list from first save & all top list should be the same",
                topList, topListFirst);

        Map<String, Integer> topListSecond = db.getTopList(secondSave);
        assertFalse("Top list from moment before the second save should not include first uuid",
                topListSecond.containsKey(testUUID.toString()));
    }
}