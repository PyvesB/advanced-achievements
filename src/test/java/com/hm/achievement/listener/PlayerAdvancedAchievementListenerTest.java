package com.hm.achievement.listener;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.anyInt;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.lang.ListenerLang;
import com.hm.achievement.utils.PlayerAdvancedAchievementEvent;
import com.hm.achievement.utils.PlayerAdvancedAchievementEvent.PlayerAdvancedAchievementEventBuilder;

import utilities.MockUtility;

/**
 * Class for testing PlayerAdvancedAchievementListener. Currently covers AllAchievementsReceivedRewards usage.
 *
 * @author Pyves
 */
@RunWith(MockitoJUnitRunner.class)
public class PlayerAdvancedAchievementListenerTest {

	private static final String PLUGIN_HEADER = "[HEADER]";
	private static final UUID PLAYER_UUID = UUID.randomUUID();

	@ClassRule
	public static final TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Mock
	private Player player;

	private AdvancedAchievements plugin;

	private PlayerAdvancedAchievementListener underTest;

	@Before
	public void setUp() throws Exception {
		Map<String, String> achievementsAndDisplayNames = new HashMap<>();
		achievementsAndDisplayNames.put("connect_1", "Good Choice");
		achievementsAndDisplayNames.put("place_500_smooth_brick", "Stone Brick Layer");
		MockUtility mockUtility = MockUtility.setUp()
				.withLogger()
				.withCacheManager()
				.mockDatabaseManager()
				.mockServer()
				.withOnlinePlayers(player)
				.withRewardParser()
				.withAchievementsAndDisplayNames(achievementsAndDisplayNames)
				.withPluginDescription()
				.withDataFolder(temporaryFolder.getRoot())
				.withPluginConfig("config-reward-reception.yml")
				.withPluginLang()
				.withChatHeader(PLUGIN_HEADER);
		plugin = mockUtility.getPluginMock();

		underTest = new PlayerAdvancedAchievementListener(plugin);
		underTest.extractConfigurationParameters();
		when(player.getUniqueId()).thenReturn(PLAYER_UUID);
		when(player.getName()).thenReturn("DarkPyves");
	}

	@Test
	public void itShouldGiveSpecialRewardWhenPlayerHasReceivedAllAchievements() {
		when(plugin.getDatabaseManager().getPlayerAchievementsAmount(PLAYER_UUID)).thenReturn(1);
		when(player.getMaximumAir()).thenReturn(100);

		PlayerAdvancedAchievementEvent event = new PlayerAdvancedAchievementEventBuilder().player(player)
				.name("connect_1").displayName("Good Choice").message("Connected for the first time!")
				.commandRewards(new String[0]).commandMessage(null).itemReward(null).moneyReward(0).experienceReward(0)
				.maxHealthReward(0).maxOxygenReward(0).build();

		underTest.onPlayerAdvancedAchievementReception(event);

		verify(player).setMaximumAir(130);
		verify(player).sendMessage(PLUGIN_HEADER + ListenerLang.ALL_ACHIEVEMENTS_RECEIVED.toLangDefault());
		verify(player).sendMessage(PLUGIN_HEADER
				+ ListenerLang.INCREASE_MAX_OXYGEN_REWARD_RECEIVED.toLangDefault().replace("AMOUNT", "30"));
	}

	@Test
	public void itShouldNotGiveSpecialRewardWhenPlayerIsMissingSomeAchievements() {
		when(plugin.getDatabaseManager().getPlayerAchievementsAmount(PLAYER_UUID)).thenReturn(0);

		PlayerAdvancedAchievementEvent event = new PlayerAdvancedAchievementEventBuilder().player(player)
				.name("connect_1").displayName("Good Choice").message("Connected for the first time!")
				.commandRewards(new String[0]).commandMessage(null).itemReward(null).moneyReward(0).experienceReward(0)
				.maxHealthReward(0).maxOxygenReward(0).build();

		underTest.onPlayerAdvancedAchievementReception(event);

		verify(player, never()).setMaximumAir(anyInt());
		verify(player, never()).sendMessage(PLUGIN_HEADER + ListenerLang.ALL_ACHIEVEMENTS_RECEIVED.toLangDefault());
	}

}
