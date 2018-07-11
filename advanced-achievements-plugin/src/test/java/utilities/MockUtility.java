package utilities;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.PluginDescriptionFile;
import org.mockito.Mockito;

import com.hm.achievement.AdvancedAchievements;
import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Utility class for Mocking the main AdvancedAchievements in order to complete testing.
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
		pluginMock = mock(AdvancedAchievements.class);
		return this;
	}

	public MockUtility withDataFolder(File folder) {
		when(pluginMock.getDataFolder()).thenReturn(folder);
		return this;
	}

	public MockUtility withPluginDescription() {
		try {
			File pluginYml = new File(getClass().getResource("/plugin.yml").getPath());
			PluginDescriptionFile desc = new PluginDescriptionFile(new FileInputStream(pluginYml));
			when(pluginMock.getDescription()).thenReturn(desc);
		} catch (FileNotFoundException | InvalidDescriptionException e) {
			System.out.println("Error while setting plugin description");
		}
		return this;
	}

	public MockUtility withPluginFile(String fileName) throws Exception {
		if (pluginMock.getDataFolder() == null) {
			throw new IllegalStateException("withDataFolder needs to be called before setting configuration files");
		}
		try {
			File file = new File(getClass().getResource("/" + fileName).getPath());
			when(pluginMock.getResource(fileName)).thenReturn(new FileInputStream(file));
		} catch (NullPointerException e) {
			System.out.println("File is missing! " + fileName + " (MockUtility.withPluginFile)");
		}
		return this;
	}

	public MockUtility withLogger() {
		Logger testLogger = Logger.getLogger("TestLogger");
		when(pluginMock.getLogger()).thenReturn(testLogger);
		return this;
	}

	public MockUtility mockServer() {
		when(pluginMock.getServer()).thenReturn(mock(Server.class, Mockito.RETURNS_DEEP_STUBS));
		return this;
	}

	public MockUtility withOnlinePlayers(Player... players) {
		Server serverMock = pluginMock.getServer();
		if (serverMock == null) {
			throw new IllegalStateException("mockServer needs to be called before withOnlinePlayers");
		}
		doReturn(Arrays.asList(players)).when(serverMock).getOnlinePlayers();
		return this;
	}

	public AdvancedAchievements getPluginMock() {
		return pluginMock;
	}

	public CommentedYamlConfiguration getLoadedConfig(String name) throws IOException, InvalidConfigurationException {
		CommentedYamlConfiguration config = new CommentedYamlConfiguration(name, pluginMock);
		config.loadConfiguration();
		return config;
	}

}
