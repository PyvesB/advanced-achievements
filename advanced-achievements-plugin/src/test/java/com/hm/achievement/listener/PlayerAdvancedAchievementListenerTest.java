package com.hm.achievement.listener;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.db.AbstractDatabaseManager;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.utils.PlayerAdvancedAchievementEvent;
import com.hm.achievement.utils.PlayerAdvancedAchievementEvent.PlayerAdvancedAchievementEventBuilder;
import com.hm.achievement.utils.RewardParser;

/**
 * Class for testing PlayerAdvancedAchievementListener. Currently covers AllAchievementsReceivedRewards usage.
 *
 * @author Pyves
 */
@ExtendWith(MockitoExtension.class)
class PlayerAdvancedAchievementListenerTest {

	private static final String PLUGIN_HEADER = "[HEADER]";
	private static final UUID PLAYER_UUID = UUID.randomUUID();

	@Mock
	private Server server;
	@Mock
	private Player player;
	@Mock
	private AbstractDatabaseManager abstractDatabaseManager;
	@Mock(lenient = true)
	private RewardParser rewardParser;
	@Mock
	private AdvancedAchievements plugin;

	private PlayerAdvancedAchievementListener underTest;

	@BeforeEach
	void setUp() throws Exception {
		Map<String, String> namesToDisplayNames = new HashMap<>();
		namesToDisplayNames.put("connect_1", "Good Choice");
		namesToDisplayNames.put("place_500_smooth_brick", "Stone Brick Layer");
		YamlConfiguration mainConfig = YamlConfiguration
				.loadConfiguration(new InputStreamReader(getClass().getResourceAsStream("/config-reward-reception.yml")));
		YamlConfiguration langConfig = YamlConfiguration
				.loadConfiguration(new InputStreamReader(getClass().getResourceAsStream("/lang.yml")));
		underTest = new PlayerAdvancedAchievementListener(mainConfig, langConfig, 11, mock(Logger.class),
				new StringBuilder(PLUGIN_HEADER), new CacheManager(plugin, mainConfig, abstractDatabaseManager),
				plugin, rewardParser, namesToDisplayNames, abstractDatabaseManager, null, null, null);
		underTest.extractConfigurationParameters();
		when(player.getUniqueId()).thenReturn(PLAYER_UUID);
		when(player.getName()).thenReturn("DarkPyves");
		when(plugin.getServer()).thenReturn(server);
		doReturn(Arrays.asList(player)).when(server).getOnlinePlayers();
	}

	@Test
	void itShouldGiveSpecialRewardWhenPlayerHasReceivedAllAchievements() {
		when(abstractDatabaseManager.getPlayerAchievementsAmount(PLAYER_UUID)).thenReturn(1);
		when(rewardParser.getRewardAmount("AllAchievementsReceivedRewards", "IncreaseMaxOxygen")).thenReturn(30);
		when(player.getMaximumAir()).thenReturn(100);

		PlayerAdvancedAchievementEvent event = new PlayerAdvancedAchievementEventBuilder().player(player)
				.name("connect_1").displayName("Good Choice").message("Connected for the first time!")
				.commandRewards(new String[0]).commandMessage(Collections.emptyList()).itemRewards(null)
				.moneyReward(0).experienceReward(0).maxHealthReward(0).maxOxygenReward(0).build();

		underTest.onPlayerAdvancedAchievementReception(event);

		verify(player).setMaximumAir(130);
		verify(player).sendMessage(PLUGIN_HEADER + "Congratulations, you have received all the achievements!");
		verify(player).sendMessage(PLUGIN_HEADER + "Your max oxygen has increased by 30!");
	}

	@Test
	void itShouldNotGiveSpecialRewardWhenPlayerIsMissingSomeAchievements() {
		when(abstractDatabaseManager.getPlayerAchievementsAmount(PLAYER_UUID)).thenReturn(0);

		PlayerAdvancedAchievementEvent event = new PlayerAdvancedAchievementEventBuilder().player(player)
				.name("connect_1").displayName("Good Choice").message("Connected for the first time!")
				.commandRewards(new String[0]).commandMessage(Collections.emptyList()).itemRewards(null)
				.moneyReward(0).experienceReward(0).maxHealthReward(0).maxOxygenReward(0).build();

		underTest.onPlayerAdvancedAchievementReception(event);

		verify(player, never()).setMaximumAir(anyInt());
		verify(player, never()).sendMessage(PLUGIN_HEADER + "Congratulations, you have received all the achievements!");
	}

}
