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

import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.command.ReloadCommand;
import com.hm.achievement.db.DatabaseCacheManager;
import com.hm.achievement.lang.Lang;
import com.hm.achievement.lang.ListenerLang;
import com.hm.achievement.lifecycle.Cleanable;
import com.hm.achievement.listener.QuitListener;
import com.hm.achievement.utils.RewardParser;
import com.hm.mcshared.file.CommentedYamlConfiguration;
import com.hm.mcshared.particle.FancyMessageSender;

/**
 * Abstract class in charge of factoring out common functionality for the listener classes with cooldown maps.
 *
 * @author Pyves
 */
public class AbstractRateLimitedListener extends AbstractListener implements Cleanable {

	final Map<String, Long> cooldownMap = new HashMap<>();

	private final CommentedYamlConfiguration langConfig;
	private final Logger logger;

	private Map<String, Integer> configStatisticCooldown;
	private boolean configCooldownActionBar;

	private String langStatisticCooldown;

	AbstractRateLimitedListener(CommentedYamlConfiguration mainConfig, int serverVersion,
			Map<String, List<Long>> sortedThresholds, DatabaseCacheManager databaseCacheManager, RewardParser rewardParser,
			ReloadCommand reloadCommand, CommentedYamlConfiguration langConfig, Logger logger, QuitListener quitListener) {
		super(mainConfig, serverVersion, sortedThresholds, databaseCacheManager, rewardParser, reloadCommand);
		this.langConfig = langConfig;
		this.logger = logger;
		quitListener.addObserver(this);
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

		langStatisticCooldown = Lang.get(ListenerLang.STATISTIC_COOLDOWN, langConfig);
	}

	@Override
	public void cleanPlayerData(UUID uuid) {
		cooldownMap.remove(uuid.toString());
	}

	/**
	 * Determines whether a similar event was taken into account too recently and the player is still in the cooldown
	 * period.
	 *
	 * @param player
	 * @param delay
	 * @param category
	 * @return true if the player is still in cooldown, false otherwise
	 */
	boolean isInCooldownPeriod(Player player, boolean delay, NormalAchievements category) {
		return isInCooldownPeriod(player, "", delay, category);
	}

	/**
	 * Determines whether a similar event was taken into account too recently and the player is still in the cooldown
	 * period. Stores elements in the map with prefixes to enable several distinct entries for the same player.
	 *
	 * @param player
	 * @param prefixInMap
	 * @param delay
	 * @param category
	 * @return true if the player is still in cooldown, false otherwise
	 */
	boolean isInCooldownPeriod(Player player, String prefixInMap, boolean delay, NormalAchievements category) {
		List<Long> categoryThresholds = sortedThresholds.get(category.toString());
		long hardestAchievementThreshold = categoryThresholds.get(categoryThresholds.size() - 1);
		long currentPlayerStatistic = databaseCacheManager.getAndIncrementStatisticAmount(category, player.getUniqueId(), 0);
		// Ignore cooldown if player has received all achievements in the category.
		if (currentPlayerStatistic >= hardestAchievementThreshold) {
			return false;
		}

		String uuid = player.getUniqueId().toString();
		Long lastEventTime = cooldownMap.get(prefixInMap + uuid);
		if (lastEventTime == null) {
			lastEventTime = 0L;
		}
		long timeToWait = lastEventTime + configStatisticCooldown.get(category.toString()) - System.currentTimeMillis();
		if (timeToWait > 0) {
			if (configCooldownActionBar) {
				String message = "&o" + StringUtils.replaceOnce(langStatisticCooldown, "TIME",
						String.format("%.1f", (double) timeToWait / 1000));
				if (delay) {
					// Display message with a delay to avoid it being overwritten by another message (typically disc
					// name).
					Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(
							Bukkit.getPluginManager().getPlugin("AdvancedAchievements"),
							() -> displayActionBarMessage(player, message), 20);
				} else {
					displayActionBarMessage(player, message);
				}
			}
			return true;
		}
		cooldownMap.put(prefixInMap + uuid, System.currentTimeMillis());
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
