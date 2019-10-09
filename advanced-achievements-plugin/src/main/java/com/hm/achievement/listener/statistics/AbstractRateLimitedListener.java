package com.hm.achievement.listener.statistics;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.Category;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.lang.LangHelper;
import com.hm.achievement.lang.ListenerLang;
import com.hm.achievement.lifecycle.Cleanable;
import com.hm.achievement.utils.RewardParser;
import com.hm.mcshared.file.CommentedYamlConfiguration;
import com.hm.mcshared.particle.FancyMessageSender;

/**
 * Abstract class in charge of factoring out common functionality for the listener classes with cooldown maps.
 *
 * @author Pyves
 */
public class AbstractRateLimitedListener extends AbstractListener implements Cleanable {


	private final Map<Integer, Map<UUID, Long>> slotsToPlayersLastActionTimes = new HashMap<>();
	private final AdvancedAchievements advancedAchievements;
	private final CommentedYamlConfiguration langConfig;
	private final Logger logger;

	private Map<String, Integer> configStatisticCooldown;
	private boolean configCooldownActionBar;

	private String langStatisticCooldown;

	AbstractRateLimitedListener(Category category, CommentedYamlConfiguration mainConfig, int serverVersion,
			Map<String, List<Long>> sortedThresholds, CacheManager cacheManager, RewardParser rewardParser,
			AdvancedAchievements advancedAchievements, CommentedYamlConfiguration langConfig, Logger logger) {
		super(category, mainConfig, serverVersion, sortedThresholds, cacheManager, rewardParser);
		this.advancedAchievements = advancedAchievements;
		this.langConfig = langConfig;
		this.logger = logger;
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		configStatisticCooldown = new HashMap<>();
		for (String cooldownCategory : Arrays.asList("LavaBuckets", "WaterBuckets", "Milk", "Beds", "Brewing",
				"MusicDiscs")) {
			if (mainConfig.isInt("StatisticCooldown")) {
				// Old configuration style for plugin versions up to version 5.4.
				configStatisticCooldown.put(cooldownCategory, mainConfig.getInt("StatisticCooldown", 10) * 1000);
			} else {
				configStatisticCooldown.put(cooldownCategory,
						mainConfig.getInt("StatisticCooldown." + cooldownCategory, 10) * 1000);
			}
		}
		configCooldownActionBar = mainConfig.getBoolean("CooldownActionBar", true);
		// Action bars introduced in Minecraft 1.8. Automatically relevant parameter for older versions.
		if (configCooldownActionBar && serverVersion < 8) {
			configCooldownActionBar = false;
		}

		langStatisticCooldown = LangHelper.get(ListenerLang.STATISTIC_COOLDOWN, langConfig);
	}

	@Override
	public void cleanPlayerData(UUID uuid) {
		slotsToPlayersLastActionTimes.values().forEach(m -> m.remove(uuid));
	}

	void updateStatisticAndAwardAchievementsIfAvailable(Player player, int incrementValue, int slotNumber) {
		if (!isInCooldownPeriod(player, slotNumber)) {
			super.updateStatisticAndAwardAchievementsIfAvailable(player, incrementValue);
		}
	}

	@Override
	void updateStatisticAndAwardAchievementsIfAvailable(Player player, int incrementValue) {
		if (!isInCooldownPeriod(player, 0)) {
			super.updateStatisticAndAwardAchievementsIfAvailable(player, incrementValue);
		}
	}

	/**
	 * Determines whether a similar event was taken into account too recently and the player is still in the cooldown
	 * period.
	 *
	 * @param player
	 * @param slotNumber
	 * @return true if the player is still in cooldown, false otherwise
	 */
	private boolean isInCooldownPeriod(Player player, int slotNumber) {
		List<Long> categoryThresholds = sortedThresholds.get(category.toString());
		long hardestAchievementThreshold = categoryThresholds.get(categoryThresholds.size() - 1);
		long currentPlayerStatistic = cacheManager.getAndIncrementStatisticAmount((NormalAchievements) category,
				player.getUniqueId(), 0);
		// Ignore cooldown if player has received all achievements in the category.
		if (currentPlayerStatistic >= hardestAchievementThreshold) {
			return false;
		}

		Map<UUID, Long> playersLastActionTimes = slotsToPlayersLastActionTimes.computeIfAbsent(slotNumber, HashMap::new);
		Long lastEventTime = playersLastActionTimes.get(player.getUniqueId());
		if (lastEventTime == null) {
			lastEventTime = 0L;
		}
		long timeToWait = lastEventTime + configStatisticCooldown.get(category.toString()) - System.currentTimeMillis();
		if (timeToWait > 0) {
			if (configCooldownActionBar) {
				String message = "&o" + StringUtils.replaceOnce(langStatisticCooldown, "TIME",
						String.format("%.1f", (double) timeToWait / 1000));
				if (category == NormalAchievements.MUSICDISCS) {
					// Display message with a delay to avoid it being overwritten by disc name message.s
					Bukkit.getScheduler().scheduleSyncDelayedTask(advancedAchievements,
							() -> displayActionBarMessage(player, message), 20);
				} else {
					displayActionBarMessage(player, message);
				}
			}
			return true;
		}
		playersLastActionTimes.put(player.getUniqueId(), System.currentTimeMillis());
		return false;
	}

	/**
	 * Displays the cooldown action bar message.
	 *
	 * @param player
	 * @param message
	 */
	private void displayActionBarMessage(Player player, String message) {
		try {
			FancyMessageSender.sendActionBarMessage(player, message);
		} catch (Exception e) {
			logger.warning("Failed to display action bar message for cooldown.");
		}
	}
}
