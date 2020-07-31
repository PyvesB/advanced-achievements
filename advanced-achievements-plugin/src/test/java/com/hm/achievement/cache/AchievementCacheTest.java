package com.hm.achievement.cache;

import com.hm.achievement.achievement.Achievement;
import com.hm.achievement.db.AchievementCache;
import com.hm.mcshared.file.CommentedYamlConfiguration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import utilities.MockUtility;

import java.util.Set;

public class AchievementCacheTest {

	private AchievementCache uut;

	@ClassRule
	public static final TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Before
	public void setup() throws Exception {
		MockUtility mockUtility = MockUtility.setUp()
				.withPluginDescription()
				.withLogger()
				.withDataFolder(temporaryFolder.getRoot())
				.withPluginFile("config.yml");
		CommentedYamlConfiguration mainConfig = mockUtility.getLoadedConfig("config.yml");
		uut = new AchievementCache(mainConfig);
	}

	@Test
	public void load_none_dataIsLoaded() {
		uut.load();

		Assert.assertTrue(uut.getCache().size() > 0);
	}

	@Test
	public void getByCategory_categoryExists_SetOfAchievementsReturned() {
		uut.load();

		Set<Achievement> breakAchievements = uut.getByCategory("breaks");

		Assert.assertTrue(breakAchievements.size() > 0);

	}

	@Test
	public void getByCategory_categoryDoesntExists_EmptySetReturned() {
		uut.load();

		Set<Achievement> doesNotExist = uut.getByCategory("categoryThatDoesDefentlyDoesntExists");

		Assert.assertEquals(0, doesNotExist.size());
	}

	@Test
	public void getByName_nameExists_AchievementReturned() {
		uut.load();

		Achievement achievement = uut.getByName("snowballs_1000");

		Assert.assertNotNull(achievement);
	}

	public void getByName_nameDoesntExist_NullReturned() {
		uut.load();

		Achievement achievement = uut.getByName("BigPPAchievementThatDoesntExist");

		Assert.assertNull(achievement);
	}
}
