package utilities;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.PluginDescriptionFile;
import org.mockito.Mockito;

import com.hm.achievement.AdvancedAchievements;

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
			InputStream pluginYml = getClass().getClassLoader().getResourceAsStream("plugin.yml");
			when(pluginMock.getDescription()).thenReturn(new PluginDescriptionFile(pluginYml));
		} catch (InvalidDescriptionException e) {
			System.out.println("Error while setting plugin description");
		}
		return this;
	}

	public MockUtility withPluginFile(String fileName) throws Exception {
		if (pluginMock.getDataFolder() == null) {
			throw new IllegalStateException("withDataFolder needs to be called before setting configuration files");
		}
		when(pluginMock.getResource(fileName)).thenReturn(getClass().getClassLoader().getResourceAsStream(fileName));
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

	public YamlConfiguration getLoadedConfig(String name) {
		InputStream resource = pluginMock.getResource(name);
		return YamlConfiguration.loadConfiguration(new InputStreamReader(resource));
	}

}
