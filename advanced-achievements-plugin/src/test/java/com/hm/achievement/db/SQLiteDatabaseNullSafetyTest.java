package com.hm.achievement.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.hm.achievement.db.data.AwardedDBAchievement;

import utilities.MockUtility;

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
				.withPluginDescription()
				.withLogger()
				.withDataFolder(temporaryFolder.getRoot())
				.withPluginFile("config.yml");
		initDB(mockUtility);
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
	public void testRegisterNullAch() {
		registerAchievement(testUUID, null, testAchievementMsg);

		List<AwardedDBAchievement> list = db.getPlayerAchievementsList(testUUID);
		Map<UUID, Integer> map = db.getPlayersAchievementsAmount();

		System.out.println("Saved Achievements: " + list);
		System.out.println("Saved Achievements Map: " + map);

		assertEquals(1, list.size());
		assertEquals(1, (int) map.getOrDefault(testUUID, 0));
	}

	@Test
	public void testRegisterNullMsg() {
		registerAchievement(testUUID, testAchievement, null);

		List<AwardedDBAchievement> list = db.getPlayerAchievementsList(testUUID);
		System.out.println("Saved Achievements: " + list);

		assertEquals(1, list.size());
		String message = list.get(0).getMessage();
		assertNotNull("Message was null!", message);
	}
}
