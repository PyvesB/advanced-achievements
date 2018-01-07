package com.hm.achievement.db;

import com.google.common.util.concurrent.MoreExecutors;
import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.db.data.AwardedDBAchievement;
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
	public void testRegisterNullUUID() {
		registerAchievement(null, testAchievement, testAchievementMsg);

		List<AwardedDBAchievement> list = db.getCleanPlayerAchievementsList(testUUID);
		Map<UUID, Integer> map = db.getPlayersAchievementsAmount();

		System.out.println("Saved Achievements: " + list);
		System.out.println("Saved Achievements Map: " + map);

		assertTrue("An achievement was saved for null UUID", list.isEmpty());
		assertFalse(map.containsKey(null));
	}

	@Test
	public void testGetMethodsForNullUUIDExceptions() {
		addNullUUIDtoDB();

		db.getCleanPlayerAchievementsList(null);
		db.getPlayersAchievementsAmount();
		db.getPlayerAchievementsAmount(null);
		db.getPlayerAchievementNamesList(null);
		db.getPlayerAchievementDate(null, testAchievement);
	}

	private void addNullUUIDtoDB() {
		String sql = "REPLACE INTO achievements VALUES (?,?,?,?)";

		((SQLWriteOperation) () -> {
			Connection conn = db.getSQLConnection();
			try (PreparedStatement ps = conn.prepareStatement(sql)) {
				ps.setObject(1, null);
				ps.setString(2, testAchievement);
				ps.setString(3, testAchievementMsg);
				ps.setDate(4, new Date(System.currentTimeMillis()));
				ps.execute();
			}
		}).executeOperation(db.pool, db.plugin.getLogger(), "registering an achievement with null UUID");
	}

	@Test
	public void testRegisterNullAch() {
		registerAchievement(testUUID, null, testAchievementMsg);

		List<AwardedDBAchievement> list = db.getCleanPlayerAchievementsList(testUUID);
		Map<UUID, Integer> map = db.getPlayersAchievementsAmount();

		System.out.println("Saved Achievements: " + list);
		System.out.println("Saved Achievements Map: " + map);

		assertEquals(1, list.size());
		assertEquals(1, (int) map.getOrDefault(testUUID, 0));
	}

	@Test
	public void testRegisterNullMsg() {
		registerAchievement(testUUID, testAchievement, null);

		List<AwardedDBAchievement> list = db.getCleanPlayerAchievementsList(testUUID);
		System.out.println("Saved Achievements: " + list);

		assertEquals(1, list.size());
		String message = list.get(0).getMessage();
		assertNotNull("Message was null!", message);
	}
}