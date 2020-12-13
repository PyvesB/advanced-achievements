package com.hm.achievement.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hm.achievement.AdvancedAchievements;

@ExtendWith(MockitoExtension.class)
class YamlUpdaterTest {

	@TempDir
	static File tempDir;

	@Mock
	private AdvancedAchievements plugin;

	private YamlUpdater underTest;

	@BeforeEach
	void setUp() throws Exception {
		when(plugin.getResource("config-default.yml")).thenReturn(getClass().getResourceAsStream("/config-default.yml"));
		underTest = new YamlUpdater(plugin);
	}

	@Test
	void shouldAppendMissingDefaultSectionsToUserConfiguration() throws Exception {
		when(plugin.getDataFolder()).thenReturn(tempDir);
		File userFile = createFileFromTestResource("config-missing-sections.yml");

		underTest.update("config-default.yml", userFile.getName(), YamlConfiguration.loadConfiguration(userFile));

		byte[] expectedUserConfig = Files.readAllBytes(Paths.get(getClass().getResource("/config-updated.yml").toURI()));
		byte[] actualUserConfig = Files.readAllBytes(userFile.toPath());
		assertEquals(new String(expectedUserConfig), new String(actualUserConfig));
	}

	@Test
	void shouldReloadConfigurationIfThereWereMissingSections() throws Exception {
		when(plugin.getDataFolder()).thenReturn(tempDir);
		File userFile = createFileFromTestResource("config-missing-sections.yml");
		YamlConfiguration config = YamlConfiguration.loadConfiguration(userFile);

		underTest.update("config-default.yml", userFile.getName(), config);

		assertEquals("Book created on DATE.", config.getString("book-date"));
	}

	@Test
	void shouldNotChangeUserConfigIfThereAreNoMissingKeys() throws Exception {
		File userFile = createFileFromTestResource("config-default.yml");
		long lastModified = userFile.lastModified();

		underTest.update("config-default.yml", userFile.getName(), YamlConfiguration.loadConfiguration(userFile));

		assertEquals(lastModified, userFile.lastModified());
	}

	private File createFileFromTestResource(String testResourceName) throws Exception {
		File userFile = new File(tempDir, testResourceName);
		try (FileOutputStream targetUserConfig = new FileOutputStream(userFile)) {
			Files.copy(Paths.get(getClass().getClassLoader().getResource(testResourceName).toURI()), targetUserConfig);
		}
		return userFile;
	}
}
