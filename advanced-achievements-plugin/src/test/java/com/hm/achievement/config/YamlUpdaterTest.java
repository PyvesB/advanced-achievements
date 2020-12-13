package com.hm.achievement.config;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.hm.achievement.AdvancedAchievements;

@RunWith(MockitoJUnitRunner.class)
public class YamlUpdaterTest {

	@ClassRule
	public static final TemporaryFolder TEMPORARY_FOLDER = new TemporaryFolder();

	@Mock
	private AdvancedAchievements plugin;

	private YamlUpdater underTest;

	@Before
	public void setUp() throws Exception {
		when(plugin.getDataFolder()).thenReturn(TEMPORARY_FOLDER.getRoot());
		when(plugin.getResource("config-default.yml")).thenReturn(getClass().getResourceAsStream("/config-default.yml"));
		underTest = new YamlUpdater(plugin);
	}

	@Test
	public void shouldAppendMissingDefaultSectionsToUserConfiguration() throws Exception {
		File userFile = createFileFromTestResource("config-missing-sections.yml");

		underTest.update("config-default.yml", userFile.getName(), YamlConfiguration.loadConfiguration(userFile));

		byte[] expectedUserConfig = Files.readAllBytes(Paths.get(getClass().getResource("/config-updated.yml").toURI()));
		byte[] actualUserConfig = Files.readAllBytes(userFile.toPath());
		assertEquals(new String(expectedUserConfig), new String(actualUserConfig));
	}

	@Test
	public void shouldReloadConfigurationIfThereWereMissingSections() throws Exception {
		File userFile = createFileFromTestResource("config-missing-sections.yml");
		YamlConfiguration config = YamlConfiguration.loadConfiguration(userFile);

		underTest.update("config-default.yml", userFile.getName(), config);

		assertEquals("Book created on DATE.", config.getString("book-date"));
	}

	@Test
	public void shouldNotChangeUserConfigIfThereAreNoMissingKeys() throws Exception {
		File userFile = createFileFromTestResource("config-default.yml");
		long lastModified = userFile.lastModified();

		underTest.update("config-default.yml", userFile.getName(), YamlConfiguration.loadConfiguration(userFile));

		assertEquals(lastModified, userFile.lastModified());
	}

	private File createFileFromTestResource(String testResourceName) throws Exception {
		File userFile = TEMPORARY_FOLDER.newFile();
		try (FileOutputStream targetUserConfig = new FileOutputStream(userFile)) {
			Files.copy(Paths.get(getClass().getClassLoader().getResource(testResourceName).toURI()), targetUserConfig);
		}
		return userFile;
	}
}
