package com.hm.achievement.placeholder;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.achievement.Achievement;
import com.hm.achievement.db.AbstractDatabaseManager;
import com.hm.achievement.db.AchievementCache;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.gui.CategoryGUI;
import com.hm.mcshared.file.CommentedYamlConfiguration;
import org.bukkit.entity.Player;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import utilities.MockUtility;

import java.util.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PlaceholderTest {

	private AchievementPlaceholderHook uut;

	private Player player;
	private UUID uuid;

	private AbstractDatabaseManager dbManager;
	private AdvancedAchievements advancedAchievements;
	private CommentedYamlConfiguration mainConfig;
	private CacheManager cacheManager;
	private AchievementCache achievementCache;
	private Map<String, String> displayNames;
	private CategoryGUI categoryGUI;

	@ClassRule
	public static final TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Before
	public void setup() throws Exception {
		MockUtility mockUtility = MockUtility.setUp()
				.withPluginDescription()
				.withLogger()
				.withDataFolder(temporaryFolder.getRoot())
				.withPluginFile("config.yml");
		mainConfig = mockUtility.getLoadedConfig("config.yml");
		advancedAchievements = mock(AdvancedAchievements.class);
		dbManager = mock(AbstractDatabaseManager.class);
		cacheManager = mock(CacheManager.class);
		achievementCache = mock(AchievementCache.class);
		categoryGUI = mock(CategoryGUI.class);

		displayNames = new HashMap<>();
		player = mock(Player.class);
		uuid = UUID.randomUUID();
		uut = new AchievementPlaceholderHook(advancedAchievements, mainConfig, dbManager, cacheManager, achievementCache,
				displayNames, categoryGUI);

	}

	@Test
	public void getCompleted_regexAndCategoryMatchesSingleAndIsCompleted_returnsOne() {
		String category = "breakCategory";
		Achievement match = new Achievement("breakTest", "breakDisplay", "a message", "Break test x times", 10, category,
				"");
		Achievement noMatch = new Achievement("soulsand", "fishingDisplay", "a message", "fishing test x times", 3, category,
				"");
		Achievement notCompleted = new Achievement("breakTest3", "breakDisplay", "a message", "Break test x times", 10,
				category, "");
		Map<UUID, Set<String>> completedAchievmenetCache = new HashMap<>();
		Set<String> completed = new HashSet<>();
		completed.add("breakTest");
		completed.add("breakTest2");
		completedAchievmenetCache.put(uuid, completed);

		when(player.getUniqueId()).thenReturn(uuid);
		when(cacheManager.getReceivedAchievementsCache()).thenReturn(completedAchievmenetCache);
		when(achievementCache.getByCategory(category))
				.thenReturn(new HashSet<>(Arrays.asList(match, noMatch, notCompleted)));
		String regex = "^break.*";
		String result = uut.onPlaceholderRequest(player, "goalscompleted_" + category + "_" + regex);

		Assert.assertEquals("1", result);
	}

	@Test
	public void getCompleted_categoryDoesntMatch_returnsZero() {
		String category = "breakCategory";
		Map<UUID, Set<String>> completedAchievmenetCache = new HashMap<>();
		Set<String> completed = new HashSet<>();
		completedAchievmenetCache.put(uuid, completed);

		when(player.getUniqueId()).thenReturn(uuid);
		when(cacheManager.getReceivedAchievementsCache()).thenReturn(completedAchievmenetCache);
		when(achievementCache.getByCategory(category)).thenReturn(new HashSet<>());
		String regex = "^break.*";
		String result = uut.onPlaceholderRequest(player, "goalscompleted_" + category + "_" + regex);

		Assert.assertEquals("0", result);
	}

	@Test
	public void getCompleted_regexDoesntMatch_returnsZero() {
		String category = "breakCategory";
		Achievement match = new Achievement("SomebreakTest", "breakDisplay", "a message", "Break test x times", 10, category,
				"");
		Achievement noMatch = new Achievement("soulsand", "fishingDisplay", "a message", "fishing test x times", 3, category,
				"");
		Achievement notCompleted = new Achievement("AnotherbreakTest", "breakDisplay", "a message", "Break test x times", 10,
				category, "");
		Map<UUID, Set<String>> completedAchievmenetCache = new HashMap<>();
		Set<String> completed = new HashSet<>();
		completed.add("SomebreakTest");
		completed.add("AnotherbreakTest");
		completed.add("soulsand");
		completedAchievmenetCache.put(uuid, completed);

		when(player.getUniqueId()).thenReturn(uuid);
		when(cacheManager.getReceivedAchievementsCache()).thenReturn(completedAchievmenetCache);
		when(achievementCache.getByCategory(category))
				.thenReturn(new HashSet<>(Arrays.asList(match, noMatch, notCompleted)));
		String regex = "^break.*";
		String result = uut.onPlaceholderRequest(player, "goalscompleted_" + category + "_" + regex);

		Assert.assertEquals("0", result);
	}

	@Test
	public void getCompleted_isNotCompleted_returnsZero() {
		String category = "breakCategory";
		Achievement match = new Achievement("breakTest", "breakDisplay", "a message", "Break test x times", 10, category,
				"");
		Achievement noMatch = new Achievement("soulsand", "fishingDisplay", "a message", "fishing test x times", 3, category,
				"");
		Achievement notCompleted = new Achievement("breakTest", "breakDisplay", "a message", "Break test x times", 10,
				category, "");
		Map<UUID, Set<String>> completedAchievmenetCache = new HashMap<>();
		Set<String> completed = new HashSet<>();
		completed.add("soulsand");
		completedAchievmenetCache.put(uuid, completed);

		when(player.getUniqueId()).thenReturn(uuid);
		when(cacheManager.getReceivedAchievementsCache()).thenReturn(completedAchievmenetCache);
		when(achievementCache.getByCategory(category))
				.thenReturn(new HashSet<>(Arrays.asList(match, noMatch, notCompleted)));
		String regex = "^break.*";
		String result = uut.onPlaceholderRequest(player, "goalscompleted_" + category + "_" + regex);

		Assert.assertEquals("0", result);
	}
}
