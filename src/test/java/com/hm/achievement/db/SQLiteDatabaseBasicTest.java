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
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
    public static void tearDownClass() throws Exception {
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
        registerAchievement();
        sleep100ms();

        List<String> achievements = db.getPlayerAchievementsList(testUUID);
        assertFalse("List was empty", achievements.isEmpty());

        String actual = achievements.get(0);
        System.out.println("Saved Achievement: " + actual);
        assertEquals(testAchievement, actual);

    }

    @Test
    public void testAchievementCount() throws PluginLoadError {

        registerAchievement();
        sleep100ms();

        Map<UUID, Integer> map = db.getPlayersAchievementsAmount();
        assertFalse("Map was empty", map.isEmpty());

        Integer amount = map.get(testUUID);
        assertNotNull(amount);
        assertEquals(1, (int) amount);
    }

    @Test
    public void testAchievementDateRegistration() throws PluginLoadError {


        String date = db.getPlayerAchievementDate(testUUID, testAchievement);
        assertNull(date);

        registerAchievement();
        sleep100ms();

        date = db.getPlayerAchievementDate(testUUID, testAchievement);
        assertNotNull(date);
    }

    @Test
    public void testPlayerAchievementAmount() throws PluginLoadError {

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
    public void testDeleteAchievementQuotes() throws PluginLoadError {

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
    public void testConnectionUpdate() throws PluginLoadError {


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
    public void testGetTopAchievements() throws PluginLoadError {

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

    @Test
    public void testGetAchievementNameList() throws PluginLoadError {

        registerAchievement();
        sleep100ms();

        List<String> achNames = db.getPlayerAchievementNamesList(testUUID);
        assertEquals(1, achNames.size());
        assertEquals(testAchievement, achNames.get(0));
    }

    @Test
    public void testHasAchievement() throws PluginLoadError {


        assertFalse(db.hasPlayerAchievement(testUUID, testAchievement));

        registerAchievement();
        sleep100ms();

        assertTrue(db.hasPlayerAchievement(testUUID, testAchievement));
    }

    @Test
    public void testGetPlayerConnectionDate() throws PluginLoadError {


        assertNull(db.getPlayerConnectionDate(testUUID));

        db.updateAndGetConnection(testUUID, createDateString());
        sleep100ms();

        assertNotNull(db.getPlayerConnectionDate(testUUID));
    }

    @Test
    public void testClearConnection() throws PluginLoadError {


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