package com.hm.achievement.config;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import utilities.MockUtility;

public class YamlUpdaterTest {

	@ClassRule
	public static final TemporaryFolder TEMPORARY_FOLDER = new TemporaryFolder();

	private YamlUpdater underTest;

	@Before
	public void setUp() throws Exception {
		MockUtility mockUtility = MockUtility.setUp()
				.withDataFolder(TEMPORARY_FOLDER.getRoot())
				.withPluginFile("config-default.yml");
		underTest = new YamlUpdater(mockUtility.getPluginMock());
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
