package com.hm.achievement.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.Date;
import java.util.Collections;
import java.util.LinkedHashMap;
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
public class SQLiteDatabaseBasicTest extends SQLiteDatabaseTest {

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
	public void testGetAchievementList() {
		registerAchievement();

		List<AwardedDBAchievement> achievements = db.getPlayerAchievementsList(testUUID);
		assertEquals(1, achievements.size());
		AwardedDBAchievement found = achievements.get(0);
		AwardedDBAchievement expected = new AwardedDBAchievement(testUUID, testAchievement, testAchievementMsg,
				found.getDateAwarded(), found.getFormattedDate());
		assertEquals(expected, found);
	}

	@Test
	public void testAchievementCount() {
		registerAchievement();

		Map<UUID, Integer> expected = Collections.singletonMap(testUUID, 1);

		Map<UUID, Integer> actual = db.getPlayersAchievementsAmount();
		assertEquals(expected, actual);
	}

	@Test
	public void testAchievementDateRegistration() {
		String date = db.getPlayerAchievementDate(testUUID, testAchievement);
		assertNull(date);

		registerAchievement();

		date = db.getPlayerAchievementDate(testUUID, testAchievement);
		assertNotNull(date);
	}

	@Test
	public void testPlayerAchievementAmount() {
		registerAchievement();

		assertEquals(1, db.getPlayerAchievementsAmount(testUUID));
	}

	@Test
	public void testDeleteAchievement() {
		testPlayerAchievementAmount();

		db.deletePlayerAchievement(testUUID, testAchievement);

		assertEquals(0, db.getPlayerAchievementsAmount(testUUID));
	}

	@Test
	public void testDeleteAllAchievements() {
		registerAchievement(testUUID, testAchievement, testAchievementMsg);
		registerAchievement(testUUID, testAchievement + "2", testAchievementMsg);

		db.deleteAllPlayerAchievements(testUUID);

		assertEquals(0, db.getPlayerAchievementsAmount(testUUID));
	}

	@Test
	public void testConnectionUpdate() {
		assertEquals(0, db.getConnectionsAmount(testUUID));

		assertEquals(1, db.updateAndGetConnection(testUUID, createDateString()));
		assertEquals(2, db.updateAndGetConnection(testUUID, createDateString()));
		assertEquals(3, db.updateAndGetConnection(testUUID, createDateString()));

		assertEquals(3, db.getConnectionsAmount(testUUID));
	}

	@Test
	public void testGetTopAchievements() {
		long firstSave = 99L;

		System.out.println("Save first achievement:  " + System.currentTimeMillis());
		registerAchievement(testUUID, testAchievement, testAchievementMsg, 100L);

		long secondSave = 199L;

		UUID secondUUID = UUID.randomUUID();
		String secondAch = "TestAchievement2";

		System.out.println("Save second achievement: " + System.currentTimeMillis());
		registerAchievement(secondUUID, testAchievement, testAchievementMsg, 200L);
		System.out.println("Save third achievement:  " + System.currentTimeMillis());
		registerAchievement(secondUUID, secondAch, testAchievementMsg, 200L);

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

		List<String> expected = Collections.singletonList(testAchievement);
		List<String> achNames = db.getPlayerAchievementNamesList(testUUID);
		assertEquals(expected, achNames);
	}

	@Test
	public void testHasAchievement() {
		assertFalse(db.hasPlayerAchievement(testUUID, testAchievement));

		registerAchievement();

		assertTrue(db.hasPlayerAchievement(testUUID, testAchievement));
	}

	@Test
	public void testGetPlayerConnectionDate() {
		assertNull(db.getPlayerConnectionDate(testUUID));

		db.updateAndGetConnection(testUUID, createDateString());

		assertNotNull(db.getPlayerConnectionDate(testUUID));
	}

	@Test
	public void testClearConnection() {
		db.updateAndGetConnection(testUUID, createDateString());

		assertNotNull(db.getPlayerConnectionDate(testUUID));

		db.clearConnection(testUUID);

		assertNull(db.getPlayerConnectionDate(testUUID));
	}

	private String createDateString() {
		return new Date(System.currentTimeMillis()).toString();
	}
}
