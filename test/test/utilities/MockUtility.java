package test.utilities;

import com.hm.achievement.AdvancedAchievements;
import com.hm.mcshared.file.CommentedYamlConfiguration;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.PluginDescriptionFile;
import org.powermock.api.mockito.PowerMockito;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.logging.Logger;

import static org.powermock.api.mockito.PowerMockito.when;

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

    public MockUtility initializePluginMock() {
        pluginMock = PowerMockito.mock(AdvancedAchievements.class);
        return this;
    }

    public MockUtility mockLogger() {
        Logger loggerMock = PowerMockito.mock(Logger.class);
        when(pluginMock.getLogger()).thenReturn(loggerMock);
        return this;
    }

    private void mockResourceFetching() throws Exception {
        mockPluginDescription();
        mockDataFolder();
        File configYml = new File(getClass().getResource("/config.yml").getPath());
        when(pluginMock.getResource("config.yml")).thenReturn(new FileInputStream(configYml));
    }

    private void mockDataFolder() {
        TestFolder.clearFolder();
        TestFolder.createFolder();
        File folder = TestFolder.getFolder();
        when(pluginMock.getDataFolder()).thenReturn(folder);
    }

    private void mockPluginDescription() throws InvalidDescriptionException, FileNotFoundException {
        File pluginYml = new File(getClass().getResource("/plugin.yml").getPath());
        PluginDescriptionFile desc = new PluginDescriptionFile(new FileInputStream(pluginYml));
        when(pluginMock.getDescription()).thenReturn(desc);
    }

    public MockUtility mockPluginConfig() throws Exception {
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
