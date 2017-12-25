package test.utilities;

import com.hm.achievement.AdvancedAchievements;

/**
 * Utility class for Mocking plugin components prior tests in order to complete testing.
 */
public class MockUtility {

    private AdvancedAchievements pluginMock;

    private MockUtility() {

    }

    public static MockUtility setUp() {
        return new MockUtility().initializePluginMock();
    }

    public MockUtility initializePluginMock() {
        // TODO Mock AdvancedAchievements
        return this;
    }

    public MockUtility mockLogger() {
        // TODO Mock logger
        return this;
    }

    public AdvancedAchievements getPluginMock() {
        return pluginMock;
    }
}
