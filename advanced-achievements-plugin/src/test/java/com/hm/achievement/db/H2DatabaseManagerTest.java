package com.hm.achievement.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.common.util.concurrent.MoreExecutors;
import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.db.data.AwardedDBAchievement;

/**
 * Class for testing H2 Database.
 *
 * @author Rsl1122
 */
@ExtendWith(MockitoExtension.class)
class H2DatabaseManagerTest {

	private static final String TEST_ACHIEVEMENT = "testachievement";

	private static H2DatabaseManager db;

	private final UUID testUUID = UUID.randomUUID();

	@BeforeAll
	static void setUpClass(@TempDir Path tempDir) throws Exception {
		AdvancedAchievements plugin = mock(AdvancedAchievements.class);
		when(plugin.getDataFolder()).thenReturn(tempDir.relativize(Paths.get("").toAbsolutePath()).toFile());
		Logger logger = Logger.getLogger("DBTestLogger");
		YamlConfiguration config = YamlConfiguration
				.loadConfiguration(new InputStreamReader(H2DatabaseManagerTest.class.getResourceAsStream("/config-h2.yml")));
		db = new H2DatabaseManager(config, logger, new DatabaseUpdater(logger, null), plugin) {

			@Override
			public void extractConfigurationParameters() {
				super.extractConfigurationParameters();
				pool = MoreExecutors.newDirectExecutorService();
			}
		};
		db.initialise();
		db.extractConfigurationParameters();
	}

	@BeforeEach
	void setUp() {
		clearDatabase();
	}

	@AfterAll
	static void tearDownClass() {
		db.shutdown();
	}

	@Test
	void testGetAchievementList() {
		registerAchievement();

		List<AwardedDBAchievement> achievements = db.getPlayerAchievementsList(testUUID);
		assertEquals(1, achievements.size());
		AwardedDBAchievement found = achievements.get(0);
		AwardedDBAchievement expected = new AwardedDBAchievement(testUUID, TEST_ACHIEVEMENT, found.getDateAwarded(),
				found.getFormattedDate());
		assertEquals(expected, found);
	}

	@Test
	void testAchievementCount() {
		registerAchievement();

		Map<UUID, Integer> expected = Collections.singletonMap(testUUID, 1);

		Map<UUID, Integer> actual = db.getPlayersAchievementsAmount();
		assertEquals(expected, actual);
	}

	@Test
	void testAchievementDateRegistration() {
		String date = db.getPlayerAchievementDate(testUUID, TEST_ACHIEVEMENT);
		assertNull(date);

		registerAchievement();

		date = db.getPlayerAchievementDate(testUUID, TEST_ACHIEVEMENT);
		assertNotNull(date);
	}

	@Test
	void testDeleteAchievement() {
		registerAchievement();

		db.deletePlayerAchievement(testUUID, TEST_ACHIEVEMENT);

		assertEquals(0, db.getPlayerAchievementNames(testUUID).size());
	}

	@Test
	void testDeleteAllAchievements() {
		registerAchievement(testUUID, TEST_ACHIEVEMENT);
		registerAchievement(testUUID, TEST_ACHIEVEMENT + "2");

		db.deleteAllPlayerAchievements(testUUID);

		assertEquals(0, db.getPlayerAchievementNames(testUUID).size());
	}

	@Test
	void testConnectionUpdate() {
		assertEquals(0, db.getNormalAchievementAmount(testUUID, NormalAchievements.CONNECTIONS));

		assertEquals(1, db.updateAndGetConnection(testUUID, 1));
		assertEquals(3, db.updateAndGetConnection(testUUID, 2));
		assertEquals(4, db.updateAndGetConnection(testUUID, 1));

		assertEquals(4, db.getNormalAchievementAmount(testUUID, NormalAchievements.CONNECTIONS));
	}

	@Test
	void testGetTopAchievements() {
		long firstSave = 99L;

		System.out.println("Save first achievement:  " + System.currentTimeMillis());
		registerAchievement(testUUID, TEST_ACHIEVEMENT, 100L);

		long secondSave = 199L;

		UUID secondUUID = UUID.randomUUID();
		String secondAch = "TestAchievement2";

		System.out.println("Save second achievement: " + System.currentTimeMillis());
		registerAchievement(secondUUID, TEST_ACHIEVEMENT, 200L);
		System.out.println("Save third achievement:  " + System.currentTimeMillis());
		registerAchievement(secondUUID, secondAch, 200L);

		Map<String, Integer> expected = new LinkedHashMap<>();
		expected.put(secondUUID.toString(), 2);
		expected.put(testUUID.toString(), 1);

		Map<String, Integer> topList = db.getTopList(0);
		assertEquals(expected, topList);

		Map<String, Integer> topListFirst = db.getTopList(firstSave);
		assertEquals(topList, topListFirst, "Top list from first save & all top list should be the same");

		expected.remove(testUUID.toString());

		Map<String, Integer> topListSecond = db.getTopList(secondSave);
		assertEquals(expected, topListSecond);
	}

	@Test
	void testGetAchievementNameList() {
		registerAchievement();

		Set<String> expected = Collections.singleton(TEST_ACHIEVEMENT);
		Set<String> achNames = db.getPlayerAchievementNames(testUUID);
		assertEquals(expected, achNames);
	}

	@Test
	void testHasAchievement() {
		assertFalse(db.hasPlayerAchievement(testUUID, TEST_ACHIEVEMENT));

		registerAchievement();

		assertTrue(db.hasPlayerAchievement(testUUID, TEST_ACHIEVEMENT));
	}

	@Test
	void testGetPlayerConnectionDate() {
		assertFalse(db.hasPlayerConnectedToday(testUUID));

		db.updateAndGetConnection(testUUID, 1);

		assertTrue(db.hasPlayerConnectedToday(testUUID));
	}

	@Test
	void testClearConnection() {
		db.updateAndGetConnection(testUUID, 1);

		assertTrue(db.hasPlayerConnectedToday(testUUID));

		db.clearConnection(testUUID);

		assertFalse(db.hasPlayerConnectedToday(testUUID));
	}

	private void registerAchievement() {
		registerAchievement(testUUID, TEST_ACHIEVEMENT);
	}

	private void registerAchievement(UUID uuid, String ach) {
		registerAchievement(uuid, ach, System.currentTimeMillis());
	}

	private void registerAchievement(UUID uuid, String ach, long date) {
		System.out.println("Saving test achievement: " + uuid + " | " + ach);
		db.registerAchievement(uuid, ach, date);
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
