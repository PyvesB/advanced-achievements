package com.hm.achievement.db;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.exception.PluginLoadError;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import test.utilities.MockUtility;

@RunWith(PowerMockRunner.class)
@PrepareForTest(JavaPlugin.class)
public class SQLiteDatabaseManagerTest {

    private SQLiteDatabaseManager db;

    @Before
    public void setUp() throws Exception {
        MockUtility mockUtility = MockUtility.setUp();
        AdvancedAchievements pluginMock = mockUtility.getPluginMock();

        db = new SQLiteDatabaseManager(pluginMock);
    }

    @Test
    public void testInitialization() throws PluginLoadError {
        db.initialise();
    }
}