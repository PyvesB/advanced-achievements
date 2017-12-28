package com.hm.achievement.db;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.exception.PluginLoadError;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import utilities.MockUtility;

import java.sql.Date;
import java.util.*;
import java.util.concurrent.Executors;

import static org.junit.Assert.*;

/**
 * Class for testing SQLite Database.
 *
 * @author Rsl1122
 */
@RunWith(MockitoJUnitRunner.class)
public class SQLiteDatabaseBasicTest extends SQLiteDatabaseTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
        MockUtility mockUtility = MockUtility.setUp()
                .mockLogger()
                .mockPluginConfig();
        AdvancedAchievements pluginMock = mockUtility.getPluginMock();

        db = new SQLiteDatabaseManager(pluginMock) {
            @Override
            protected void performPreliminaryTasks() throws ClassNotFoundException, PluginLoadError {
                super.performPreliminaryTasks();

                // Set Pool to a SingleThreadExecutor.
                pool = Executors.newSingleThreadExecutor();
            }
        };
        initDB();
    }

    @Before
    public void setUp() throws Exception {
        clearDatabase();
    }

    @AfterClass
    public static void tearDownClass() {
        if (db != null) {
            db.shutdown();
        }
    }

    @Test
    public void testInitialization() throws PluginLoadError {
        initDB();
    }

    @Test
    public void testGetAchievementList() {
        registerAchievement();
        sleep100ms();

        List<String> achievements = db.getPlayerAchievementsList(testUUID);
        assertEquals(3, achievements.size());

        List<String> expected = Arrays.asList(testAchievement, testAchievementMsg, achievements.get(2));
        assertEquals(expected, achievements);
    }

    @Test
    public void testAchievementCount() {
        registerAchievement();
        sleep100ms();

        Map<UUID, Integer> expected = Collections.singletonMap(testUUID, 1);

        Map<UUID, Integer> actual = db.getPlayersAchievementsAmount();
        assertEquals(expected, actual);
    }

    @Test
    public void testAchievementDateRegistration() {
        String date = db.getPlayerAchievementDate(testUUID, testAchievement);
        assertNull(date);

        registerAchievement();
        sleep100ms();

        date = db.getPlayerAchievementDate(testUUID, testAchievement);
        assertNotNull(date);
    }

    @Test
    public void testPlayerAchievementAmount() {
        registerAchievement();
        sleep100ms();

        assertEquals(1, db.getPlayerAchievementsAmount(testUUID));
    }

    @Test
    public void testDeleteAchievement() throws PluginLoadError {
        testPlayerAchievementAmount();

        db.deletePlayerAchievement(testUUID, testAchievement);
        sleep100ms();

        assertEquals(0, db.getPlayerAchievementsAmount(testUUID));
    }

    @Test
    public void testDeleteAchievementQuotes() {
        registerAchievement(testUUID, "'" + testAchievement + "'", testAchievementMsg);
        sleep100ms();
        registerAchievement(testUUID, "''" + testAchievement + "''", testAchievementMsg);
        sleep100ms();
        registerAchievement(testUUID, testAchievement, testAchievementMsg);
        sleep100ms();

        assertEquals(3, db.getPlayerAchievementsAmount(testUUID));

        db.deletePlayerAchievement(testUUID, "'" + testAchievement + "'");
        db.deletePlayerAchievement(testUUID, testAchievement);
        sleep100ms();

        assertEquals(0, db.getPlayerAchievementsAmount(testUUID));
    }

    @Test
    public void testConnectionUpdate() {
        assertEquals(0, db.getConnectionsAmount(testUUID));

        assertEquals(1, db.updateAndGetConnection(testUUID, createDateString()));
        sleep100ms();
        assertEquals(2, db.updateAndGetConnection(testUUID, createDateString()));
        sleep100ms();
        assertEquals(3, db.updateAndGetConnection(testUUID, createDateString()));
        sleep100ms();

        assertEquals(3, db.getConnectionsAmount(testUUID));
    }

    @Test
    public void testGetTopAchievements() {
        long firstSave = System.currentTimeMillis();

        registerAchievement(testUUID, testAchievement, testAchievementMsg);
        sleep100ms();
        sleep100ms();

        long secondSave = System.currentTimeMillis();

        UUID secondUUID = UUID.randomUUID();
        String secondAch = "TestAchievement2";

        registerAchievement(secondUUID, testAchievement, testAchievementMsg);
        sleep100ms();
        registerAchievement(secondUUID, secondAch, testAchievementMsg);
        sleep100ms();

        Map<String, Integer> expected = new LinkedHashMap<>();
        expected.put(secondUUID.toString(), 2);
        expected.put(testUUID.toString(), 1);

        Map<String, Integer> topList = db.getTopList(0);
        assertEquals(expected, topList);

        Map<String, Integer> topListFirst = db.getTopList(firstSave);
        assertEquals("Top list from first save & all top list should be the same",
                topList, topListFirst);

        expected.remove(testUUID.toString());

        Map<String, Integer> topListSecond = db.getTopList(secondSave);
        assertEquals(expected, topListSecond);
    }

    @Test
    public void testGetAchievementNameList() {
        registerAchievement();
        sleep100ms();

        List<String> expected = Collections.singletonList(testAchievement);
        List<String> achNames = db.getPlayerAchievementNamesList(testUUID);
        assertEquals(expected, achNames);
    }

    @Test
    public void testHasAchievement() {
        assertFalse(db.hasPlayerAchievement(testUUID, testAchievement));

        registerAchievement();
        sleep100ms();

        assertTrue(db.hasPlayerAchievement(testUUID, testAchievement));
    }

    @Test
    public void testGetPlayerConnectionDate() {
        assertNull(db.getPlayerConnectionDate(testUUID));

        db.updateAndGetConnection(testUUID, createDateString());
        sleep100ms();

        assertNotNull(db.getPlayerConnectionDate(testUUID));
    }

    @Test
    public void testClearConnection() {
        db.updateAndGetConnection(testUUID, createDateString());
        sleep100ms();

        assertNotNull(db.getPlayerConnectionDate(testUUID));

        db.clearConnection(testUUID);
        sleep100ms();

        assertNull(db.getPlayerConnectionDate(testUUID));
    }

    private String createDateString() {
        return new Date(System.currentTimeMillis()).toString();
    }
}