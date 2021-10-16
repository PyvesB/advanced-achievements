package com.hm.achievement.db;

import static com.google.common.util.concurrent.MoreExecutors.newDirectExecutorService;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
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

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.db.data.AwardedDBAchievement;
import com.hm.achievement.db.data.ConnectionInformation;

/**
 * Class for testing H2 Database.
 *
 * @author Rsl1122
 */
@ExtendWith(MockitoExtension.class)
class H2DatabaseManagerTest {

	private static final String TEST_ACHIEVEMENT = "testachievement";
	private static final Logger LOGGER = Logger.getLogger("DBTestLogger");

	private static H2DatabaseManager db;

	private final UUID testUUID = UUID.randomUUID();

	@BeforeAll
	static void setUpClass(@TempDir Path tempDir) throws Exception {
		AdvancedAchievements plugin = mock(AdvancedAchievements.class);
		when(plugin.getDataFolder()).thenReturn(tempDir.relativize(Paths.get("").toAbsolutePath()).toFile());
		YamlConfiguration config = YamlConfiguration
				.loadConfiguration(new InputStreamReader(H2DatabaseManagerTest.class.getResourceAsStream("/config-h2.yml")));
		db = new H2DatabaseManager(config, LOGGER, new DatabaseUpdater(LOGGER), plugin, newDirectExecutorService());
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
	void testGetPlayerAchievementsList() {
		db.registerAchievement(testUUID, TEST_ACHIEVEMENT, System.currentTimeMillis());

		List<AwardedDBAchievement> achievements = db.getPlayerAchievementsList(testUUID);
		assertEquals(1, achievements.size());
		AwardedDBAchievement found = achievements.get(0);
		AwardedDBAchievement expected = new AwardedDBAchievement(testUUID, TEST_ACHIEVEMENT, found.getDateAwarded(),
				found.getFormattedDate());
		assertEquals(expected, found);
	}

	@Test
	void testGetAchievementsRecipientList() {
		db.registerAchievement(testUUID, TEST_ACHIEVEMENT, System.currentTimeMillis());

		List<AwardedDBAchievement> achievements = db.getAchievementsRecipientList(TEST_ACHIEVEMENT);
		assertEquals(1, achievements.size());
		AwardedDBAchievement found = achievements.get(0);
		AwardedDBAchievement expected = new AwardedDBAchievement(testUUID, TEST_ACHIEVEMENT, found.getDateAwarded(),
				found.getFormattedDate());
		assertEquals(expected, found);
	}

	@Test
	void testAchievementCount() {
		db.registerAchievement(testUUID, TEST_ACHIEVEMENT, System.currentTimeMillis());

		Map<UUID, Integer> expected = Collections.singletonMap(testUUID, 1);

		Map<UUID, Integer> actual = db.getPlayersAchievementsAmount();
		assertEquals(expected, actual);
	}

	@Test
	void testAchievementDateRegistration() {
		String date = db.getPlayerAchievementDate(testUUID, TEST_ACHIEVEMENT);
		assertNull(date);

		db.registerAchievement(testUUID, TEST_ACHIEVEMENT, System.currentTimeMillis());

		date = db.getPlayerAchievementDate(testUUID, TEST_ACHIEVEMENT);
		assertNotNull(date);
	}

	@Test
	void testDeleteAchievement() {
		db.registerAchievement(testUUID, TEST_ACHIEVEMENT, System.currentTimeMillis());

		db.deletePlayerAchievement(testUUID, TEST_ACHIEVEMENT);

		assertEquals(0, db.getPlayerAchievementNames(testUUID).size());
	}

	@Test
	void testDeleteAllAchievements() {
		db.registerAchievement(testUUID, TEST_ACHIEVEMENT, System.currentTimeMillis());
		db.registerAchievement(testUUID, TEST_ACHIEVEMENT + "2", System.currentTimeMillis());

		db.deleteAllPlayerAchievements(testUUID);

		assertEquals(0, db.getPlayerAchievementNames(testUUID).size());
	}

	@Test
	void testConnectionUpdate() {
		assertEquals(0, db.getNormalAchievementAmount(testUUID, NormalAchievements.CONNECTIONS));

		db.updateConnectionInformation(testUUID, 1);
		assertEquals(1, db.getNormalAchievementAmount(testUUID, NormalAchievements.CONNECTIONS));
		db.updateConnectionInformation(testUUID, 3);
		assertEquals(3, db.getNormalAchievementAmount(testUUID, NormalAchievements.CONNECTIONS));
	}

	@Test
	void testGetTopAchievements() {
		long firstSave = 99L;

		db.registerAchievement(testUUID, TEST_ACHIEVEMENT, 100L);

		long secondSave = 199L;

		UUID secondUUID = UUID.randomUUID();

		db.registerAchievement(secondUUID, TEST_ACHIEVEMENT, 200L);
		db.registerAchievement(secondUUID, TEST_ACHIEVEMENT + "2", 200L);

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
		db.registerAchievement(testUUID, TEST_ACHIEVEMENT, System.currentTimeMillis());

		Set<String> expected = Collections.singleton(TEST_ACHIEVEMENT);
		Set<String> achNames = db.getPlayerAchievementNames(testUUID);
		assertEquals(expected, achNames);
	}

	@Test
	void testGetPlayerConnectionDate() {
		ConnectionInformation connectionInformation1 = db.getConnectionInformation(testUUID);
		assertEquals(ConnectionInformation.epoch(), connectionInformation1.getDate());
		assertEquals(0, connectionInformation1.getCount());

		db.updateConnectionInformation(testUUID, 1);

		ConnectionInformation connectionInformation2 = db.getConnectionInformation(testUUID);
		assertEquals(ConnectionInformation.today(), connectionInformation2.getDate());
		assertEquals(1, connectionInformation2.getCount());
	}

	@Test
	void testClearConnection() {
		db.updateConnectionInformation(testUUID, 1);

		db.clearConnection(testUUID);

		ConnectionInformation connectionInformation = db.getConnectionInformation(testUUID);
		assertEquals(ConnectionInformation.epoch(), connectionInformation.getDate());
		assertEquals(0, connectionInformation.getCount());
	}

	@Test
	void testGetNormalAchievementAmount() {
		assertEquals(0, db.getNormalAchievementAmount(testUUID, NormalAchievements.BEDS));

		((SQLWriteOperation) () -> {
			try (PreparedStatement ps = db.getConnection()
					.prepareStatement("REPLACE INTO beds VALUES ('" + testUUID + "',5)")) {
				ps.execute();
			}
		}).executeOperation(db.writeExecutor, LOGGER, "Writing beds statistics");

		assertEquals(5, db.getNormalAchievementAmount(testUUID, NormalAchievements.BEDS));
	}

	@Test
	void testGetMultipleAchievementAmount() {
		assertEquals(0, db.getMultipleAchievementAmount(testUUID, MultipleAchievements.CRAFTS, "diamond_axe"));

		((SQLWriteOperation) () -> {
			try (PreparedStatement ps = db.getConnection()
					.prepareStatement("REPLACE INTO crafts VALUES ('" + testUUID + "','diamond_axe',7)")) {
				ps.execute();
			}
		}).executeOperation(db.writeExecutor, LOGGER, "Writing crafts statistics");

		assertEquals(7, db.getMultipleAchievementAmount(testUUID, MultipleAchievements.CRAFTS, "diamond_axe"));
	}

	@Test
	void testGetDefaultJobsRebornAchievementAmount() {
		assertEquals(1, db.getMultipleAchievementAmount(testUUID, MultipleAchievements.JOBSREBORN, "hunter"));
	}

	private void clearDatabase() {
		((SQLWriteOperation) () -> {
			try (PreparedStatement ps = db.getConnection().prepareStatement("DELETE FROM achievements")) {
				ps.execute();
			}
		}).executeOperation(db.writeExecutor, LOGGER, "Clearing achievements table");
	}
}
