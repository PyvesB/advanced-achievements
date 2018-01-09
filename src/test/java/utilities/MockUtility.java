package utilities;

import com.hm.achievement.AdvancedAchievements;
import com.hm.mcshared.file.CommentedYamlConfiguration;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.PluginDescriptionFile;
import org.mockito.Mockito;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.logging.Logger;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

/**
 * Utility class for Mocking plugin components prior tests in order to complete testing.
 *
 * @author Rsl1122
 */
public class MockUtility {

	private AdvancedAchievements pluginMock;


	private MockUtility() {
	}

	public static MockUtility setUp() {
		return new MockUtility().initializePluginMock();
	}

	private MockUtility initializePluginMock() {
		pluginMock = Mockito.mock(AdvancedAchievements.class);
		return this;
	}

	public MockUtility mockLogger() {
		Logger testLogger = Logger.getLogger("TestLogger");
		given(pluginMock.getLogger()).willReturn(testLogger);
		return this;
	}

	public void mockResourceFetching() throws Exception {
		mockPluginDescription();
		String[] files = {
				"config.yml",
				"gui.yml",
				"lang.yml"
		};
		for (String fileName : files) {
			try {
				File file = new File(getClass().getResource("/" + fileName).getPath());
				when(pluginMock.getResource(fileName)).thenReturn(new FileInputStream(file));
			} catch (NullPointerException e) {
				System.out.println("File is missing! " + fileName + " (MockUtility.mockResourceFetching)");
			}
		}
	}

	public MockUtility mockDataFolder(File folder) {
		when(pluginMock.getDataFolder()).thenReturn(folder);
		return this;
	}

	private void mockPluginDescription() throws InvalidDescriptionException, FileNotFoundException {
		File pluginYml = new File(getClass().getResource("/plugin.yml").getPath());
		PluginDescriptionFile desc = new PluginDescriptionFile(new FileInputStream(pluginYml));
		when(pluginMock.getDescription()).thenReturn(desc);
	}

	public MockUtility mockPluginConfig() throws Exception {
		if (pluginMock.getDataFolder() == null) {
			throw new IllegalStateException("mockDataFolder needs to be called before mockPluginConfig");
		}
		mockResourceFetching();
		mockPluginDescription();

		CommentedYamlConfiguration config = new CommentedYamlConfiguration("config.yml", pluginMock);
		when(pluginMock.getPluginConfig()).thenReturn(config);
		return this;
	}

	public AdvancedAchievements getPluginMock() {
		return pluginMock;
	}
}
