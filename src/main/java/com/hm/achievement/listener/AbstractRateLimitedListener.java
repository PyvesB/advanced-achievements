package com.hm.achievement.listener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.utils.Cleanable;
import com.hm.mcshared.particle.PacketSender;

/**
 * Abstract class in charge of factoring out common functionality for the listener classes with cooldown maps.
 * 
 * @author Pyves
 */
public class AbstractRateLimitedListener extends AbstractListener implements Cleanable {

	protected final Map<String, Long> cooldownMap;

	private int configStatisticCooldown;
	private boolean configCooldownActionBar;
	private String langStatisticCooldown;

	protected AbstractRateLimitedListener(AdvancedAchievements plugin) {
		super(plugin);

		cooldownMap = new HashMap<>();
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		configStatisticCooldown = plugin.getPluginConfig().getInt("StatisticCooldown", 10) * 1000;
		configCooldownActionBar = plugin.getPluginConfig().getBoolean("CooldownActionBar", true);
		// Action bars introduced in Minecraft 1.8. Automatically relevant parameter for older versions.
		if (configCooldownActionBar && version < 8) {
			configCooldownActionBar = false;
		}

		langStatisticCooldown = plugin.getPluginLang().getString("statistic-cooldown",
				"Achievements cooldown, wait TIME seconds before this action counts again.");
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
	protected boolean isInCooldownPeriod(Player player, boolean delay, NormalAchievements category) {
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
	protected boolean isInCooldownPeriod(Player player, String prefixInMap, boolean delay,
			NormalAchievements category) {
		List<Long> categoryThresholds = plugin.getSortedThresholds().get(category.toString());
		long hardestAchievementThreshold = categoryThresholds.get(categoryThresholds.size() - 1);
		long currentPlayerStatistic = plugin.getCacheManager().getAndIncrementStatisticAmount(category,
				player.getUniqueId(), 0);
		// Ignore cooldown if player has received all achievements in the category.
		if (currentPlayerStatistic >= hardestAchievementThreshold) {
			return false;
		}

		String uuid = player.getUniqueId().toString();
		Long lastEventTime = cooldownMap.get(prefixInMap + uuid);
		if (lastEventTime == null) {
			lastEventTime = 0L;
		}
		long timeToWait = lastEventTime + configStatisticCooldown - System.currentTimeMillis();
		if (timeToWait > 0) {
			if (configCooldownActionBar) {
				final String actionBarJsonMessage = "{\"text\":\"&o" + StringUtils.replaceOnce(langStatisticCooldown,
						"TIME", String.format("%.1f", (double) timeToWait / 1000)) + "\"}";
				if (delay) {
					// Display message with a delay to avoid it being overwritten by another message (typically disc
					// name).
					Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(
							Bukkit.getPluginManager().getPlugin(plugin.getDescription().getName()),
							() -> displayActionBarMessage(player, actionBarJsonMessage), 20);
				} else {
					displayActionBarMessage(player, actionBarJsonMessage);
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
	 * @param actionBarJsonMessage
	 */
	private void displayActionBarMessage(final Player player, final String actionBarJsonMessage) {
		try {
			PacketSender.sendActionBarPacket(player, actionBarJsonMessage);
		} catch (Exception e) {
			plugin.getLogger().warning("Errors while trying to display action bar message for cooldown.");
		}
	}
}
