package com.hm.achievement.listener;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.config.AchievementMap;
import com.hm.achievement.db.AbstractDatabaseManager;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.domain.Achievement;
import com.hm.achievement.domain.Achievement.AchievementBuilder;
import com.hm.achievement.utils.FancyMessageSender;
import com.hm.achievement.utils.PlayerAdvancedAchievementEvent;

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
	private World world;
	@Mock
	private AbstractDatabaseManager abstractDatabaseManager;
	@Mock
	private AdvancedAchievements plugin;

	private PlayerAdvancedAchievementListener underTest;

	@Test
	void itShouldRegisterNewAchievementInDatabase() {
		AchievementMap achievementMap = new AchievementMap();
		achievementMap.put(new AchievementBuilder().name("connect_1").displayName("Good Choice").build());
		achievementMap.put(new AchievementBuilder().name("place_500_smooth_brick").displayName("Stone Brick Layer").build());
		YamlConfiguration mainConfig = YamlConfiguration
				.loadConfiguration(new InputStreamReader(getClass().getResourceAsStream("/config-reception.yml")));
		YamlConfiguration langConfig = YamlConfiguration
				.loadConfiguration(new InputStreamReader(getClass().getResourceAsStream("/lang.yml")));
		underTest = new PlayerAdvancedAchievementListener(mainConfig, langConfig, mock(Logger.class),
				new StringBuilder(PLUGIN_HEADER), new CacheManager(plugin, abstractDatabaseManager), plugin, null,
				achievementMap, abstractDatabaseManager, null, new FancyMessageSender(16));
		underTest.extractConfigurationParameters();
		when(player.getUniqueId()).thenReturn(PLAYER_UUID);
		when(player.getName()).thenReturn("DarkPyves");
		when(plugin.getServer()).thenReturn(server);
		doReturn(Arrays.asList(player)).when(server).getOnlinePlayers();
		Set<String> receivedAchievements = new HashSet<>();
		receivedAchievements.add("connect_1");
		when(abstractDatabaseManager.getPlayerAchievementNames(PLAYER_UUID)).thenReturn(receivedAchievements);
		Achievement achievement = new AchievementBuilder()
				.name("connect_1")
				.displayName("Good Choice")
				.message("Connected for the first time!")
				.build();

		underTest.onPlayerAdvancedAchievementReception(new PlayerAdvancedAchievementEvent(player, achievement));

		verify(abstractDatabaseManager).registerAchievement(eq(PLAYER_UUID), eq("connect_1"), anyLong());
	}

}
