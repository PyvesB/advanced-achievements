package com.hm.achievement.listener;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;

import com.hm.achievement.AdvancedAchievements;
import com.hm.mcshared.particle.PacketSender;

/**
 * Abstract class in charge of factoring out common functionality for the listener classes with cooldown maps.
 * 
 * @author Pyves
 */
public class AbstractRateLimitedListener extends AbstractListener {

	private final Map<String, Long> cooldownMap;

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

	/**
	 * Determines whether a similar event was taken into account too recently and the player is still in the cooldown
	 * period.
	 * 
	 * @param player
	 * @return
	 */
	protected boolean isInCooldownPeriod(Player player) {
		return isInCooldownPeriod(player, "");
	}

	/**
	 * Determines whether a similar event was taken into account too recently and the player is still in the cooldown
	 * period. Stores elements in the map with prefixes to enable several distinct entries for the same player.
	 * 
	 * @param player
	 * @return
	 */
	protected boolean isInCooldownPeriod(Player player, String prefixInMap) {
		String uuid = player.getUniqueId().toString();
		Long lastEventTime = cooldownMap.get(prefixInMap + uuid);
		if (lastEventTime == null) {
			lastEventTime = 0L;
		}
		long timeToWait = lastEventTime + configStatisticCooldown - System.currentTimeMillis();
		if (timeToWait > 0) {
			if (configCooldownActionBar) {
				String actionBarJsonMessage = "{\"text\":\"&o" + StringUtils.replaceOnce(langStatisticCooldown, "TIME",
						String.format("%.1f", (double) timeToWait / 1000)) + "\"}";
				try {
					PacketSender.sendActionBarPacket(player, actionBarJsonMessage);
				} catch (Exception e) {
					plugin.getLogger().warning("Errors while trying to display action bar message for cooldown.");
				}
			}
			return true;
		}
		cooldownMap.put(prefixInMap + uuid, System.currentTimeMillis());
		return false;
	}

	/**
	 * Removes a given player UUID from the cooldown map.
	 * 
	 * @param playerUUID
	 */
	protected void removePlayerFromCooldownMap(String playerUUID) {
		cooldownMap.remove(playerUUID);
	}
}
