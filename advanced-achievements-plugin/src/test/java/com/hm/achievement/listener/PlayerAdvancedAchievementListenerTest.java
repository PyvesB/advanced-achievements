package com.hm.achievement.listener;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.entity.Player;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.db.AbstractDatabaseManager;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.lang.ListenerLang;
import com.hm.achievement.utils.PlayerAdvancedAchievementEvent;
import com.hm.achievement.utils.PlayerAdvancedAchievementEvent.PlayerAdvancedAchievementEventBuilder;
import com.hm.achievement.utils.RewardParser;
import com.hm.mcshared.file.CommentedYamlConfiguration;

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
	@Mock
	private AbstractDatabaseManager abstractDatabaseManager;
	@Mock
	private RewardParser rewardParser;

	private AdvancedAchievements plugin;

	private PlayerAdvancedAchievementListener underTest;

	@Before
	public void setUp() throws Exception {
		Map<String, String> namesToDisplayNames = new HashMap<>();
		namesToDisplayNames.put("connect_1", "Good Choice");
		namesToDisplayNames.put("place_500_smooth_brick", "Stone Brick Layer");
		MockUtility mockUtility = MockUtility.setUp()
				.mockServer()
				.withLogger()
				.withOnlinePlayers(player)
				.withPluginDescription()
				.withDataFolder(temporaryFolder.getRoot())
				.withPluginFile("config-reward-reception.yml")
				.withPluginFile("lang.yml");
		plugin = mockUtility.getPluginMock();

		CommentedYamlConfiguration mainConfig = mockUtility.getLoadedConfig("config-reward-reception.yml");
		underTest = new PlayerAdvancedAchievementListener(mainConfig, mockUtility.getLoadedConfig("lang.yml"), 11,
				mock(Logger.class), new StringBuilder(PLUGIN_HEADER),
				new CacheManager(plugin, mainConfig, abstractDatabaseManager),
				plugin, rewardParser, namesToDisplayNames, abstractDatabaseManager, null, null, null);
		underTest.extractConfigurationParameters();
		when(player.getUniqueId()).thenReturn(PLAYER_UUID);
		when(player.getName()).thenReturn("DarkPyves");
	}

	@Test
	public void itShouldGiveSpecialRewardWhenPlayerHasReceivedAllAchievements() {
		when(abstractDatabaseManager.getPlayerAchievementsAmount(PLAYER_UUID)).thenReturn(1);
		when(rewardParser.getRewardAmount("AllAchievementsReceivedRewards", "IncreaseMaxOxygen")).thenReturn(30);
		when(player.getMaximumAir()).thenReturn(100);

		PlayerAdvancedAchievementEvent event = new PlayerAdvancedAchievementEventBuilder().player(player)
				.name("connect_1").displayName("Good Choice").message("Connected for the first time!")
				.commandRewards(new String[0]).commandMessage(Collections.emptyList()).itemRewards(null)
				.moneyReward(0).experienceReward(0).maxHealthReward(0).maxOxygenReward(0).build();

		underTest.onPlayerAdvancedAchievementReception(event);

		verify(player).setMaximumAir(130);
		verify(player).sendMessage(PLUGIN_HEADER + ListenerLang.ALL_ACHIEVEMENTS_RECEIVED.toLangDefault());
		verify(player).sendMessage(PLUGIN_HEADER
				+ ListenerLang.INCREASE_MAX_OXYGEN_REWARD_RECEIVED.toLangDefault().replace("AMOUNT", "30"));
	}

	@Test
	public void itShouldNotGiveSpecialRewardWhenPlayerIsMissingSomeAchievements() {
		when(abstractDatabaseManager.getPlayerAchievementsAmount(PLAYER_UUID)).thenReturn(0);

		PlayerAdvancedAchievementEvent event = new PlayerAdvancedAchievementEventBuilder().player(player)
				.name("connect_1").displayName("Good Choice").message("Connected for the first time!")
				.commandRewards(new String[0]).commandMessage(Collections.emptyList()).itemRewards(null)
				.moneyReward(0).experienceReward(0).maxHealthReward(0).maxOxygenReward(0).build();

		underTest.onPlayerAdvancedAchievementReception(event);

		verify(player, never()).setMaximumAir(anyInt());
		verify(player, never()).sendMessage(PLUGIN_HEADER + ListenerLang.ALL_ACHIEVEMENTS_RECEIVED.toLangDefault());
	}

}
