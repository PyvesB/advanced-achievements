package com.hm.achievement.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.google.common.util.concurrent.MoreExecutors;
import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.db.data.AwardedDBAchievement;

/**
 * Class for testing H2 Database.
 *
 * @author Rsl1122
 */
@RunWith(MockitoJUnitRunner.class)
public class H2DatabaseManagerTest {

	private static final String TEST_ACHIEVEMENT = "TestAchievement";
	private static final String TEST_MESSAGE = "TestMessage";

	@ClassRule
	public static final TemporaryFolder temporaryFolder = new TemporaryFolder();

	private static H2DatabaseManager db;

	private final UUID testUUID = UUID.randomUUID();

	@BeforeClass
	public static void setUpClass() throws Exception {
		AdvancedAchievements plugin = mock(AdvancedAchievements.class);
		File relativeTempFolder = temporaryFolder.getRoot().toPath().relativize(Paths.get("").toAbsolutePath()).toFile();
		when(plugin.getDataFolder()).thenReturn(relativeTempFolder);
		Logger logger = Logger.getLogger("DBTestLogger");
		YamlConfiguration config = YamlConfiguration
				.loadConfiguration(new InputStreamReader(H2DatabaseManagerTest.class.getResourceAsStream("/config-h2.yml")));
		db = new H2DatabaseManager(config, logger, Collections.emptyMap(), new DatabaseUpdater(logger, null), plugin) {

			@Override
			public void extractConfigurationParameters() {
				super.extractConfigurationParameters();
				pool = MoreExecutors.newDirectExecutorService();
			}
		};
		db.initialise();
		db.extractConfigurationParameters();
	}

	@Before
	public void setUp() {
		clearDatabase();
	}

	@AfterClass
	public static void tearDownClass() {
		db.shutdown();
	}

	@Test
	public void testGetAchievementList() {
		registerAchievement();

		List<AwardedDBAchievement> achievements = db.getPlayerAchievementsList(testUUID);
		assertEquals(1, achievements.size());
		AwardedDBAchievement found = achievements.get(0);
		AwardedDBAchievement expected = new AwardedDBAchievement(testUUID, TEST_ACHIEVEMENT, TEST_MESSAGE,
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
		String date = db.getPlayerAchievementDate(testUUID, TEST_ACHIEVEMENT);
		assertNull(date);

		registerAchievement();

		date = db.getPlayerAchievementDate(testUUID, TEST_ACHIEVEMENT);
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

		db.deletePlayerAchievement(testUUID, TEST_ACHIEVEMENT);

		assertEquals(0, db.getPlayerAchievementsAmount(testUUID));
	}

	@Test
	public void testDeleteAllAchievements() {
		registerAchievement(testUUID, TEST_ACHIEVEMENT, TEST_MESSAGE);
		registerAchievement(testUUID, TEST_ACHIEVEMENT + "2", TEST_MESSAGE);

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
		registerAchievement(testUUID, TEST_ACHIEVEMENT, TEST_MESSAGE, 100L);

		long secondSave = 199L;

		UUID secondUUID = UUID.randomUUID();
		String secondAch = "TestAchievement2";

		System.out.println("Save second achievement: " + System.currentTimeMillis());
		registerAchievement(secondUUID, TEST_ACHIEVEMENT, TEST_MESSAGE, 200L);
		System.out.println("Save third achievement:  " + System.currentTimeMillis());
		registerAchievement(secondUUID, secondAch, TEST_MESSAGE, 200L);

		Map<String, Integer> expected = new LinkedHashMap<>();
		expected.put(secondUUID.toString(), 2);
		expected.put(testUUID.toString(), 1);

		Map<String, Integer> topList = db.getTopList(0);
		assertEquals(expected, topList);

		Map<String, Integer> topListFirst = db.getTopList(firstSave);
		assertEquals("Top list from first save & all top list should be the same", topList, topListFirst);

		expected.remove(testUUID.toString());

		Map<String, Integer> topListSecond = db.getTopList(secondSave);
		assertEquals(expected, topListSecond);
	}

	@Test
	public void testGetAchievementNameList() {
		registerAchievement();

		List<String> expected = Collections.singletonList(TEST_ACHIEVEMENT);
		List<String> achNames = db.getPlayerAchievementNamesList(testUUID);
		assertEquals(expected, achNames);
	}

	@Test
	public void testHasAchievement() {
		assertFalse(db.hasPlayerAchievement(testUUID, TEST_ACHIEVEMENT));

		registerAchievement();

		assertTrue(db.hasPlayerAchievement(testUUID, TEST_ACHIEVEMENT));
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

	private void registerAchievement() {
		registerAchievement(testUUID, TEST_ACHIEVEMENT, TEST_MESSAGE);
	}

	private void registerAchievement(UUID uuid, String ach, String msg) {
		System.out.println("Saving test achievement: " + uuid + " | " + ach + " | " + msg);
		db.registerAchievement(uuid, ach, msg);
	}

	private void registerAchievement(UUID uuid, String ach, String msg, long date) {
		System.out.println("Saving test achievement: " + uuid + " | " + ach + " | " + msg);
		db.registerAchievement(uuid, ach, msg, date);
	}

	private void clearDatabase() {
		String sql = "DELETE FROM achievements";

		((SQLWriteOperation) () -> {
			Connection conn = db.getSQLConnection();
			try (PreparedStatement ps = conn.prepareStatement(sql)) {
				ps.execute();
			}
		}).executeOperation(db.pool, null, "Clearing achievements table");
	}
}
