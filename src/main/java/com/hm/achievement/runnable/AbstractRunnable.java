package com.hm.achievement.runnable;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.utils.PlayerAdvancedAchievementEvent.PlayerAdvancedAchievementEventBuilder;
import com.hm.achievement.utils.Reloadable;
import com.hm.mcshared.particle.ReflectionUtils.PackageType;

/**
 * Abstract class in charge of factoring out common functionality for runnables.
 * 
 * @author Pyves
 */
public class AbstractRunnable implements Reloadable {

	protected final AdvancedAchievements plugin;
	protected final int version;

	private boolean configRestrictCreative;
	private boolean configRestrictSpectator;
	private boolean configRestrictAdventure;
	private Set<String> configExcludedWorlds;

	public AbstractRunnable(AdvancedAchievements plugin) {
		this.plugin = plugin;
		// Simple parsing of game version. Might need to be updated in the future depending on how the Minecraft
		// versions change in the future.
		version = Integer.parseInt(PackageType.getServerVersion().split("_")[1]);
	}

	@Override
	public void extractConfigurationParameters() {
		configRestrictCreative = plugin.getPluginConfig().getBoolean("RestrictCreative", false);
		configRestrictSpectator = plugin.getPluginConfig().getBoolean("RestrictSpectator", true);
		configRestrictAdventure = plugin.getPluginConfig().getBoolean("RestrictAdventure", false);
		// Spectator mode introduced in Minecraft 1.8. Automatically relevant parameter for older versions.
		if (configRestrictSpectator && version < 8) {
			configRestrictSpectator = false;
		}
		configExcludedWorlds = new HashSet<>(plugin.getPluginConfig().getList("ExcludedWorlds"));
	}

	/**
	 * Determines whether the listened event should be taken into account. Ignore permission check.
	 * 
	 * @param player
	 * @param category
	 * @return
	 */
	protected boolean shouldRunBeTakenIntoAccount(Player player) {
		boolean isNPC = player.hasMetadata("NPC");
		boolean restrictedCreative = configRestrictCreative && player.getGameMode() == GameMode.CREATIVE;
		boolean restrictedSpectator = configRestrictSpectator && player.getGameMode() == GameMode.SPECTATOR;
		boolean restrictedAdventure = configRestrictAdventure && player.getGameMode() == GameMode.ADVENTURE;
		boolean excludedWorld = configExcludedWorlds.contains(player.getWorld().getName());

		return !isNPC && !restrictedCreative && !restrictedSpectator && !restrictedAdventure && !excludedWorld;
	}

	/**
	 * Compares the current value to the achievement thresholds. If a threshold is reached, awards the achievement if it
	 * wasn't previously received.
	 * 
	 * @param player
	 * @param thresholds
	 * @param currentValue
	 */
	protected void checkThresholdsAndAchievements(Player player, Map<Long, String> thresholds, long currentValue) {
		// Iterate through all the different thresholds.
		for (Entry<Long, String> achievementThreshold : thresholds.entrySet()) {
			// Check whether player has met the threshold.
			if (currentValue > achievementThreshold.getKey()) {
				String configAchievement = achievementThreshold.getValue();
				String achievementName = plugin.getPluginConfig().getString(configAchievement + ".Name");
				// Check whether player has received the achievement.
				if (!plugin.getCacheManager().hasPlayerAchievement(player.getUniqueId(), achievementName)) {
					// Fire achievement event.
					PlayerAdvancedAchievementEventBuilder playerAdvancedAchievementEventBuilder = new PlayerAdvancedAchievementEventBuilder()
							.player(player).name(achievementName)
							.displayName(plugin.getPluginConfig().getString(configAchievement + ".DisplayName"))
							.message(plugin.getPluginConfig().getString(configAchievement + ".Message"))
							.commandRewards(plugin.getRewardParser().getCommandRewards(configAchievement, player))
							.itemReward(plugin.getRewardParser().getItemReward(configAchievement))
							.moneyReward(plugin.getRewardParser().getRewardAmount(configAchievement, "Money"))
							.experienceReward(plugin.getRewardParser().getRewardAmount(configAchievement, "Experience"))
							.maxHealthReward(
									plugin.getRewardParser().getRewardAmount(configAchievement, "IncreaseMaxHealth"));

					Bukkit.getServer().getPluginManager().callEvent(playerAdvancedAchievementEventBuilder.build());
				}
			} else {
				// Entries in TreeMap sorted in increasing order, all subsequent thresholds will fail the condition.
				return;
			}
		}
	}
}
