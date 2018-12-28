package com.hm.achievement.utils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.lifecycle.Reloadable;
import com.hm.achievement.utils.PlayerAdvancedAchievementEvent.PlayerAdvancedAchievementEventBuilder;
import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Abstract class in charge of factoring out common functionality for classes which track statistic increases (such as
 * listeners or runnables).
 * 
 * @author Pyves
 */
@Singleton
public class StatisticIncreaseHandler implements Reloadable {

	protected final CommentedYamlConfiguration mainConfig;
	protected final int serverVersion;
	protected final Map<String, List<Long>> sortedThresholds;
	protected final CacheManager cacheManager;
	protected final RewardParser rewardParser;

	private boolean configRestrictCreative;
	private boolean configRestrictSpectator;
	private boolean configRestrictAdventure;
	private Set<String> configExcludedWorlds;

	@Inject
	public StatisticIncreaseHandler(@Named("main") CommentedYamlConfiguration mainConfig, int serverVersion,
			Map<String, List<Long>> sortedThresholds, CacheManager cacheManager, RewardParser rewardParser) {
		this.mainConfig = mainConfig;
		this.serverVersion = serverVersion;
		this.sortedThresholds = sortedThresholds;
		this.cacheManager = cacheManager;
		this.rewardParser = rewardParser;
	}

	@Override
	public void extractConfigurationParameters() {
		configRestrictCreative = mainConfig.getBoolean("RestrictCreative", false);
		configRestrictSpectator = mainConfig.getBoolean("RestrictSpectator", true);
		configRestrictAdventure = mainConfig.getBoolean("RestrictAdventure", false);
		// Spectator mode introduced in Minecraft 1.8. Automatically relevant parameter for older versions.
		if (configRestrictSpectator && serverVersion < 8) {
			configRestrictSpectator = false;
		}
		configExcludedWorlds = new HashSet<>(mainConfig.getList("ExcludedWorlds"));
	}

	/**
	 * Compares the current value to the achievement thresholds. If a threshold is reached, awards the achievement if it
	 * wasn't previously received.
	 * 
	 * @param player
	 * @param categorySubcategory
	 * @param currentValue
	 */
	public void checkThresholdsAndAchievements(Player player, String categorySubcategory, long currentValue) {
		// Iterate through all the different thresholds.
		for (long threshold : sortedThresholds.get(categorySubcategory)) {
			// Check whether player has met the threshold.
			if (currentValue >= threshold) {
				String achievementPath = categorySubcategory + "." + threshold;
				String achievementName = mainConfig.getString(achievementPath + ".Name");
				// Check whether player has received the achievement.
				if (!cacheManager.hasPlayerAchievement(player.getUniqueId(), achievementName)) {
					String rewardPath = achievementPath + ".Reward";
					// Fire achievement event.
					PlayerAdvancedAchievementEventBuilder playerAdvancedAchievementEventBuilder = new PlayerAdvancedAchievementEventBuilder()
							.player(player).name(achievementName)
							.displayName(mainConfig.getString(achievementPath + ".DisplayName"))
							.message(mainConfig.getString(achievementPath + ".Message"))
							.commandRewards(rewardParser.getCommandRewards(rewardPath, player))
							.commandMessage(rewardParser.getCustomCommandMessages(rewardPath))
							.itemReward(rewardParser.getItemReward(rewardPath))
							.moneyReward(rewardParser.getRewardAmount(rewardPath, "Money"))
							.experienceReward(rewardParser.getRewardAmount(rewardPath, "Experience"))
							.maxHealthReward(rewardParser.getRewardAmount(rewardPath, "IncreaseMaxHealth"))
							.maxOxygenReward(rewardParser.getRewardAmount(rewardPath, "IncreaseMaxOxygen"));

					Bukkit.getPluginManager().callEvent(playerAdvancedAchievementEventBuilder.build());
				}
			} else {
				// Entries in List sorted in increasing order, all subsequent thresholds will fail the condition.
				return;
			}
		}
	}

	/**
	 * Determines whether the statistic increase should be taken into account.
	 * 
	 * @param player
	 * @param category
	 * @return true if the increase should be taken into account, false otherwise
	 */
	protected boolean shouldIncreaseBeTakenIntoAccount(Player player, NormalAchievements category) {
		boolean isNPC = player.hasMetadata("NPC");
		boolean permission = player.hasPermission(category.toPermName());
		boolean restrictedCreative = configRestrictCreative && player.getGameMode() == GameMode.CREATIVE;
		boolean restrictedSpectator = configRestrictSpectator && player.getGameMode() == GameMode.SPECTATOR;
		boolean restrictedAdventure = configRestrictAdventure && player.getGameMode() == GameMode.ADVENTURE;
		boolean excludedWorld = configExcludedWorlds.contains(player.getWorld().getName());

		return !isNPC && permission && !restrictedCreative && !restrictedSpectator && !restrictedAdventure && !excludedWorld;
	}

	/**
	 * Determines whether the statistic increase should be taken into account. Ignore permission check.
	 * 
	 * @param player
	 * @return true if the increase should be taken into account, false otherwise
	 */
	protected boolean shouldIncreaseBeTakenIntoAccountNoPermissions(Player player) {
		boolean isNPC = player.hasMetadata("NPC");
		boolean restrictedCreative = configRestrictCreative && player.getGameMode() == GameMode.CREATIVE;
		boolean restrictedSpectator = configRestrictSpectator && player.getGameMode() == GameMode.SPECTATOR;
		boolean restrictedAdventure = configRestrictAdventure && player.getGameMode() == GameMode.ADVENTURE;
		boolean excludedWorld = configExcludedWorlds.contains(player.getWorld().getName());

		return !isNPC && !restrictedCreative && !restrictedSpectator && !restrictedAdventure && !excludedWorld;
	}
}
