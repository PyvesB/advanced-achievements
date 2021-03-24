package com.hm.achievement.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.domain.Reward;
import com.hm.achievement.utils.MaterialHelper;

import net.milkbowl.vault.economy.Economy;

@ExtendWith(MockitoExtension.class)
class RewardParserTest {

	@Mock
	private Economy economy;
	@Mock
	private Player player;
	@Mock
	private AdvancedAchievements advancedAchievements;
	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private Server server;
	private YamlConfiguration mainConfig;
	private YamlConfiguration langConfig;
	private RewardParser underTest;

	@BeforeEach
	void setUp() throws Exception {
		when(advancedAchievements.getServer()).thenReturn(server);
		when(server.getPluginManager().isPluginEnabled("Vault")).thenReturn(true);
		when(server.getServicesManager().getRegistration(Economy.class).getProvider()).thenReturn(economy);

		mainConfig = new YamlConfiguration();
		langConfig = new YamlConfiguration();
		langConfig.load(Paths.get(getClass().getClassLoader().getResource("reward-parser/lang.yml").toURI()).toFile());
		underTest = new RewardParser(mainConfig, langConfig, advancedAchievements,
				new MaterialHelper(Logger.getGlobal(), 16), 16);
	}

	@Test
	void shouldParseMoneyRewardSingular() throws Exception {
		mainConfig.load(Paths.get(getClass().getClassLoader().getResource("reward-parser/money-1.yml").toURI()).toFile());
		when(economy.currencyNameSingular()).thenReturn("coin");

		List<Reward> rewards = underTest.parseRewards("");

		assertEquals(1, rewards.size());
		Reward reward = rewards.get(0);
		assertEquals(Arrays.asList("receive 1 coin"), reward.getListTexts());
		assertEquals(Arrays.asList("You received: 1 coin!"), reward.getChatTexts());
		reward.getRewarder().accept(player);
		verify(economy).depositPlayer(player, 1);
	}

	@Test
	void shouldParseMoneyRewardPlural() throws Exception {
		mainConfig.load(Paths.get(getClass().getClassLoader().getResource("reward-parser/money-2.yml").toURI()).toFile());
		when(economy.currencyNamePlural()).thenReturn("coins");

		List<Reward> rewards = underTest.parseRewards("");

		assertEquals(1, rewards.size());
		Reward reward = rewards.get(0);
		assertEquals(Arrays.asList("receive 2 coins"), reward.getListTexts());
		assertEquals(Arrays.asList("You received: 2 coins!"), reward.getChatTexts());
		reward.getRewarder().accept(player);
		verify(economy).depositPlayer(player, 2);
	}

	@Test
	void shouldParseItemReward() throws Exception {
		mainConfig.load(Paths.get(getClass().getClassLoader().getResource("reward-parser/item.yml").toURI()).toFile());

		List<Reward> rewards = underTest.parseRewards("");

		assertEquals(1, rewards.size());
		Reward reward = rewards.get(0);
		assertEquals(Arrays.asList("receive 1 Diamond"), reward.getListTexts());
		assertEquals(Arrays.asList("You received an item reward: 1 Diamond"), reward.getChatTexts());

		// Note: this test is incomplete and cannot run the rewarder nor test for custom item names. Anything related
		// to item meta is only available on a server at runtime. Leveraging PowerMockito would be required.
	}

	@Test
	void shouldParseSingleCommandReward() throws Exception {
		World world = Mockito.mock(World.class);
		mainConfig.load(Paths.get(getClass().getClassLoader().getResource("reward-parser/command.yml").toURI()).toFile());
		when(player.getName()).thenReturn("Pyves");
		when(player.getLocation()).thenReturn(new Location(world, 1, 5, 8));
		when(player.getWorld()).thenReturn(world);
		when(world.getName()).thenReturn("Nether");

		List<Reward> rewards = underTest.parseRewards("");

		assertEquals(1, rewards.size());
		Reward reward = rewards.get(0);
		assertEquals(Arrays.asList("teleportation to somewhere special!"), reward.getListTexts());
		assertEquals(Arrays.asList("You received your reward: teleportation to somewhere special!"), reward.getChatTexts());
		reward.getRewarder().accept(player);
		verify(server).dispatchCommand(any(), eq("teleport Pyves"));
	}

	@Test
	void shouldParseMultipleCommandRewards() throws Exception {
		World world = Mockito.mock(World.class);
		mainConfig.load(Paths.get(getClass().getClassLoader().getResource("reward-parser/commands.yml").toURI()).toFile());
		when(player.getName()).thenReturn("Pyves");
		when(player.getLocation()).thenReturn(new Location(world, 1, 5, 8));
		when(player.getWorld()).thenReturn(world);
		when(world.getName()).thenReturn("Nether");

		List<Reward> rewards = underTest.parseRewards("");

		assertEquals(1, rewards.size());
		Reward reward = rewards.get(0);
		assertEquals(Arrays.asList("display 1", "display 2"), reward.getListTexts());
		assertEquals(Arrays.asList("You received your reward: display 1", "You received your reward: display 2"),
				reward.getChatTexts());
		reward.getRewarder().accept(player);
		verify(server).dispatchCommand(any(), eq("execute 1"));
		verify(server).dispatchCommand(any(), eq("execute 2"));
	}

	@Test
	void shouldParseExperienceReward() throws Exception {
		mainConfig.load(Paths.get(getClass().getClassLoader().getResource("reward-parser/experience.yml").toURI()).toFile());

		List<Reward> rewards = underTest.parseRewards("");

		assertEquals(1, rewards.size());
		Reward reward = rewards.get(0);
		assertEquals(Arrays.asList("receive 500 experience"), reward.getListTexts());
		assertEquals(Arrays.asList("You received: 500 experience!"), reward.getChatTexts());
		reward.getRewarder().accept(player);
		verify(player).giveExp(500);
	}

	@Test
	void shouldParseMaxHealthReward() throws Exception {
		mainConfig.load(Paths.get(getClass().getClassLoader().getResource("reward-parser/max-health.yml").toURI()).toFile());
		AttributeInstance healthAttribute = Mockito.mock(AttributeInstance.class);
		when(player.getAttribute(any())).thenReturn(healthAttribute);
		when(healthAttribute.getBaseValue()).thenReturn(1.0);

		List<Reward> rewards = underTest.parseRewards("");

		assertEquals(1, rewards.size());
		Reward reward = rewards.get(0);
		assertEquals(Arrays.asList("increase max health by 2"), reward.getListTexts());
		assertEquals(Arrays.asList("Your max health has increased by 2!"), reward.getChatTexts());
		reward.getRewarder().accept(player);
		verify(player).getAttribute(Attribute.GENERIC_MAX_HEALTH);
		verify(healthAttribute).setBaseValue(3.0);
	}

	@SuppressWarnings("deprecation")
	@Test
	void shouldParseMaxHealthRewardOnOlderMinecraftVersions() throws Exception {
		underTest = new RewardParser(mainConfig, langConfig, advancedAchievements, new MaterialHelper(Logger.getGlobal(), 8),
				8);
		mainConfig.load(Paths.get(getClass().getClassLoader().getResource("reward-parser/max-health.yml").toURI()).toFile());
		when(player.getMaxHealth()).thenReturn(2.0);

		List<Reward> rewards = underTest.parseRewards("");

		assertEquals(1, rewards.size());
		Reward reward = rewards.get(0);
		assertEquals(Arrays.asList("increase max health by 2"), reward.getListTexts());
		assertEquals(Arrays.asList("Your max health has increased by 2!"), reward.getChatTexts());
		reward.getRewarder().accept(player);
		verify(player).setMaxHealth(4.0);
	}

	@Test
	void shouldParseMaxOxygenReward() throws Exception {
		mainConfig.load(Paths.get(getClass().getClassLoader().getResource("reward-parser/max-oxygen.yml").toURI()).toFile());
		when(player.getMaximumAir()).thenReturn(5);

		List<Reward> rewards = underTest.parseRewards("");

		assertEquals(1, rewards.size());
		Reward reward = rewards.get(0);
		assertEquals(Arrays.asList("increase max oxygen by 10"), reward.getListTexts());
		assertEquals(Arrays.asList("Your max oxygen has increased by 10!"), reward.getChatTexts());
		reward.getRewarder().accept(player);
		verify(player).setMaximumAir(15);
	}

}
