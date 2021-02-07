package com.hm.achievement.listener;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.config.AchievementMap;
import com.hm.achievement.config.RewardParser;
import com.hm.achievement.db.AbstractDatabaseManager;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.domain.Achievement;
import com.hm.achievement.domain.Achievement.AchievementBuilder;
import com.hm.achievement.domain.Reward;
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
	@Mock(lenient = true)
	private RewardParser rewardParser;
	@Mock
	private AdvancedAchievements plugin;

	private PlayerAdvancedAchievementListener underTest;

	@BeforeEach
	void setUp() throws Exception {
		AchievementMap achievementMap = new AchievementMap();
		achievementMap.put(new AchievementBuilder().name("connect_1").displayName("Good Choice").build());
		achievementMap.put(new AchievementBuilder().name("place_500_smooth_brick").displayName("Stone Brick Layer").build());
		YamlConfiguration mainConfig = YamlConfiguration
				.loadConfiguration(new InputStreamReader(getClass().getResourceAsStream("/config-reward-reception.yml")));
		YamlConfiguration langConfig = YamlConfiguration
				.loadConfiguration(new InputStreamReader(getClass().getResourceAsStream("/lang.yml")));
		underTest = new PlayerAdvancedAchievementListener(mainConfig, langConfig, 11, mock(Logger.class),
				new StringBuilder(PLUGIN_HEADER), new CacheManager(plugin, achievementMap, abstractDatabaseManager),
				plugin, rewardParser, achievementMap, abstractDatabaseManager, null, null, null);
		underTest.extractConfigurationParameters();
		when(player.getUniqueId()).thenReturn(PLAYER_UUID);
		when(player.getName()).thenReturn("DarkPyves");
		when(plugin.getServer()).thenReturn(server);
		doReturn(Arrays.asList(player)).when(server).getOnlinePlayers();
	}

	@Test
	void itShouldGiveSpecialRewardWhenPlayerHasReceivedAllAchievements() {
		Set<String> receivedAchievements = new HashSet<>();
		receivedAchievements.add("some_ach");
		when(abstractDatabaseManager.getPlayerAchievementNames(PLAYER_UUID)).thenReturn(receivedAchievements);
		Reward reward = new Reward(Arrays.asList(), Arrays.asList("Your max oxygen in PLAYER_WORLD has increased by 30!"),
				p -> p.setMaximumAir(130));
		when(rewardParser.parseRewards("AllAchievementsReceivedRewards")).thenReturn(Arrays.asList(reward));
		when(player.getWorld()).thenReturn(world);
		when(world.getName()).thenReturn("Nether");
		when(player.getLocation()).thenReturn(new Location(world, 0, 0, 0));
		Achievement achievement = new AchievementBuilder()
				.name("connect_1")
				.displayName("Good Choice")
				.message("Connected for the first time!")
				.build();

		underTest.onPlayerAdvancedAchievementReception(new PlayerAdvancedAchievementEvent(player, achievement));

		verify(player).setMaximumAir(130);
		verify(player).sendMessage(PLUGIN_HEADER + "Congratulations, you have received all the achievements!");
		verify(player).sendMessage(PLUGIN_HEADER + "Your max oxygen in Nether has increased by 30!");
	}

	@Test
	void itShouldNotGiveSpecialRewardWhenPlayerIsMissingSomeAchievements() {
		when(abstractDatabaseManager.getPlayerAchievementNames(PLAYER_UUID)).thenReturn(new HashSet<>());
		Reward reward = new Reward(Arrays.asList(), Arrays.asList("Your max oxygen has increased by 30!"),
				p -> p.setMaximumAir(130));
		when(rewardParser.parseRewards("AllAchievementsReceivedRewards")).thenReturn(Arrays.asList(reward));
		Achievement achievement = new AchievementBuilder()
				.name("connect_1")
				.displayName("Good Choice")
				.message("Connected for the first time!")
				.build();

		underTest.onPlayerAdvancedAchievementReception(new PlayerAdvancedAchievementEvent(player, achievement));

		verify(player, never()).setMaximumAir(anyInt());
		verify(player, never()).sendMessage(PLUGIN_HEADER + "Congratulations, you have received all the achievements!");
	}

}
