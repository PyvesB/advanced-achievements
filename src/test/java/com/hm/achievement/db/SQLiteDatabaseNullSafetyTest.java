package com.hm.achievement.db;

import com.google.common.util.concurrent.MoreExecutors;
import com.hm.achievement.AdvancedAchievements;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import utilities.MockUtility;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
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

    @ClassRule
    public static final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @BeforeClass
    public static void setUpClass() throws Exception {
        MockUtility mockUtility = MockUtility.setUp()
                .mockLogger()
                .mockDataFolder(temporaryFolder.getRoot())
                .mockPluginConfig();
        AdvancedAchievements pluginMock = mockUtility.getPluginMock();

        db = new SQLiteDatabaseManager(pluginMock) {
            @Override
            public void extractConfigurationParameters() {
                super.extractConfigurationParameters();
                pool = MoreExecutors.newDirectExecutorService();
            }
        };
        initDB();
    }

    @Before
    public void setUp() {
        clearDatabase();
    }

    @AfterClass
    public static void tearDownClass() {
        if (db != null) {
            db.shutdown();
        }
    }

    @Test
    @Ignore("Can be enabled later")
    public void testRegisterNullUUID() {
        registerAchievement(null, testAchievement, testAchievementMsg);


        List<String> list = db.getPlayerAchievementsList(null);
        Map<UUID, Integer> map = db.getPlayersAchievementsAmount();

        System.out.println("Saved Achievements: " + list);
        System.out.println("Saved Achievements Map: " + map);

        assertTrue("An achievement was saved for null UUID", list.isEmpty());
        assertFalse(map.containsKey(null));
    }

    @Test
    @Ignore("Can be enabled later")
    public void testGetMethodsForNullUUIDExceptions() {
        addNullUUIDtoDB();

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
    @Ignore("Can be enabled later")
    public void testRegisterNullAch() {
        registerAchievement(testUUID, null, testAchievementMsg);

        List<String> list = db.getPlayerAchievementsList(testUUID);
        Map<UUID, Integer> map = db.getPlayersAchievementsAmount();

        System.out.println("Saved Achievements: " + list);
        System.out.println("Saved Achievements Map: " + map);

        assertTrue("An achievement with name 'null' was saved", list.isEmpty());
        assertEquals(0, (int) map.getOrDefault(testUUID, 0));
    }

    @Test
    @Ignore("Can be enabled later")
    public void testRegisterNullMsg() {
        registerAchievement(testUUID, testAchievement, null);

        List<String> list = db.getPlayerAchievementsList(testUUID);
        System.out.println("Saved Achievements: " + list);

        assertEquals(3, list.size());
        String message = list.get(1);
        assertNotNull("Message was null!", message);
    }
}