package utilities;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doReturn;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.PluginDescriptionFile;
import org.mockito.Mockito;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.db.DatabaseCacheManager;
import com.hm.achievement.db.SQLiteDatabaseManager;
import com.hm.achievement.utils.RewardParser;
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
		pluginMock = Mockito.mock(AdvancedAchievements.class);
		return this;
	}

	public MockUtility withLogger() {
		Logger testLogger = Logger.getLogger("TestLogger");
		given(pluginMock.getLogger()).willReturn(testLogger);
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

	public MockUtility withPluginConfig(String fileName) throws Exception {
		withPluginFile(fileName);
		CommentedYamlConfiguration config = new CommentedYamlConfiguration(fileName, pluginMock);
		when(pluginMock.getPluginConfig()).thenReturn(config);
		return this;
	}

	public MockUtility withPluginLang() throws Exception {
		withPluginFile("lang.yml");
		CommentedYamlConfiguration lang = new CommentedYamlConfiguration("lang.yml", pluginMock);
		when(pluginMock.getPluginLang()).thenReturn(lang);
		return this;
	}

	private void withPluginFile(String fileName) throws Exception {
		if (pluginMock.getDataFolder() == null) {
			throw new IllegalStateException("withDataFolder needs to be called before setting configuration files");
		}
		try {
			File file = new File(getClass().getResource("/" + fileName).getPath());
			when(pluginMock.getResource(fileName)).thenReturn(new FileInputStream(file));
		} catch (NullPointerException e) {
			System.out.println("File is missing! " + fileName + " (MockUtility.withPluginFile)");
		}
	}

	public MockUtility mockServer() {
		when(pluginMock.getServer()).thenReturn(Mockito.mock(Server.class, Mockito.RETURNS_DEEP_STUBS));
		return this;
	}

	public MockUtility withCacheManager() {
		when(pluginMock.getCacheManager()).thenReturn(new DatabaseCacheManager(pluginMock));
		return this;
	}

	public MockUtility mockDatabaseManager() {
		when(pluginMock.getDatabaseManager()).thenReturn(Mockito.mock(SQLiteDatabaseManager.class));
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

	public MockUtility withRewardParser() {
		Server serverMock = pluginMock.getServer();
		if (serverMock == null) {
			throw new IllegalStateException("mockServer needs to be called before withRewardParser");
		}
		when(serverMock.getPluginManager().getPlugin("Vault")).thenReturn(null);
		RewardParser rewardParser = new RewardParser(pluginMock);
		when(pluginMock.getRewardParser()).thenReturn(rewardParser);
		return this;
	}

	public MockUtility withAchievementsAndDisplayNames(Map<String, String> achievementsAndDisplayNames) {
		when(pluginMock.getAchievementsAndDisplayNames()).thenReturn(achievementsAndDisplayNames);
		return this;
	}

	public MockUtility withChatHeader(String header) {
		when(pluginMock.getChatHeader()).thenReturn(header);
		return this;
	}

	public AdvancedAchievements getPluginMock() {
		return pluginMock;
	}

}
