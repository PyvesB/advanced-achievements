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

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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
public class SQLiteDatabaseNullSafetyTest extends SQLiteDatabaseTest {

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
    public void testRegisterNullUUID() {
        registerAchievement(null, testAchievement, testAchievementMsg);
        sleep100ms();

        List<String> list = db.getPlayerAchievementsList(null);
        Map<UUID, Integer> map = db.getPlayersAchievementsAmount();

        System.out.println("Saved Achievements: " + list);
        System.out.println("Saved Achievements Map: " + map);

        assertTrue("An achievement was saved for null UUID", list.isEmpty());
        assertFalse(map.containsKey(null));
    }

    @Test
    public void testGetMethodsForNullUUIDExceptions() throws SQLException {
        addNullUUIDtoDB();
        sleep100ms();

        db.getPlayerAchievementsList(null);
        db.getPlayersAchievementsAmount();
        db.getPlayerAchievementsAmount(null);
        db.getPlayerAchievementNamesList(null);
        db.getPlayerAchievementDate(null, testAchievement);
    }

    private void addNullUUIDtoDB() {
        String sql = "REPLACE INTO achievements VALUES ('" + null + "',?,?,?)";

        ((SQLWriteOperation) () -> {
            Connection conn = db.getSQLConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, testAchievement);
                ps.setString(2, testAchievementMsg);
                ps.setDate(3, new Date(System.currentTimeMillis()));
                ps.execute();
            }
        }).executeOperation(db.pool, db.plugin.getLogger(), "registering an achievement with null UUID");
    }

    @Test
    public void testRegisterNullAch() {
        registerAchievement(testUUID, null, testAchievementMsg);
        sleep100ms();

        List<String> list = db.getPlayerAchievementsList(testUUID);
        Map<UUID, Integer> map = db.getPlayersAchievementsAmount();


        System.out.println("Saved Achievements: " + list);
        System.out.println("Saved Achievements Map: " + map);

        assertTrue("An achievement with name 'null' was saved", list.isEmpty());
        assertEquals(0, (int) map.getOrDefault(testUUID, 0));
    }

    @Test
    public void testRegisterNullMsg() {
        registerAchievement(testUUID, testAchievement, null);

        sleep100ms();

        List<String> list = db.getPlayerAchievementsList(testUUID);
        System.out.println("Saved Achievements: " + list);

        assertEquals(3, list.size());
        String message = list.get(1);
        assertNotNull("Message was null!", message);
    }
}