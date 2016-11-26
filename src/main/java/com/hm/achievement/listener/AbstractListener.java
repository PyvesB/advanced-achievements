package com.hm.achievement.listener;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.particle.PacketSender;
import com.hm.achievement.particle.ReflectionUtils.PackageType;
import com.hm.achievement.utils.YamlManager;

/**
 * Abstract class in charge of factoring out common functionality for the listener classes.
 * 
 * @author Pyves
 */
public abstract class AbstractListener {

	protected final int version;
	protected final AdvancedAchievements plugin;

	private final Map<String, Long> cooldownMap;
	private final int cooldownTime;
	private final boolean cooldownActionBar;

	protected AbstractListener(AdvancedAchievements plugin) {

		this.plugin = plugin;
		// Simple and fast check to compare versions. Might need to be updated in the future depending on how the
		// Minecraft versions change in the future.
		version = Integer.parseInt(PackageType.getServerVersion().split("_")[1]);
		cooldownTime = plugin.getPluginConfig().getInt("StatisticCooldown", 10) * 1000;
		cooldownMap = new HashMap<>();
		cooldownActionBar = plugin.getPluginConfig().getBoolean("CooldownActionBar", true);

	}

	public Map<String, Long> getCooldownMap() {

		return cooldownMap;
	}

	/**
	 * Determines whether the listened event should be taken into account.
	 * 
	 * @param player
	 * @param category
	 * @return
	 */
	protected boolean shouldEventBeTakenIntoAccount(Player player, NormalAchievements category) {

		boolean permission = player.hasPermission(category.toPermName());
		boolean restrictedCreative = plugin.isRestrictCreative() && player.getGameMode() == GameMode.CREATIVE;
		boolean excludedWorld = plugin.isInExludedWorld(player);

		return permission && !restrictedCreative && !excludedWorld;
	}

	/**
	 * Determines whether the listened event should be taken into account. Ignore creative mode check.
	 * 
	 * @param player
	 * @param category
	 * @return
	 */
	protected boolean shouldEventBeTakenIntoAccountNoCreative(Player player, NormalAchievements category) {

		boolean permission = player.hasPermission(category.toPermName());
		boolean excludedWorld = plugin.isInExludedWorld(player);

		return permission && !excludedWorld;
	}

	/**
	 * Determines whether the listened event should be taken into account. Ignore permission check.
	 * 
	 * @param player
	 * @param category
	 * @return
	 */
	protected boolean shouldEventBeTakenIntoAccountNoPermission(Player player) {

		boolean restrictedCreative = plugin.isRestrictCreative() && player.getGameMode() == GameMode.CREATIVE;
		boolean excludedWorld = plugin.isInExludedWorld(player);

		return !restrictedCreative && !excludedWorld;
	}

	/**
	 * Determines whether a similar event was taken into account too recently and the player is still in the cooldown
	 * period.
	 * 
	 * @param player
	 * @return
	 */
	protected boolean isInCooldownPeriod(Player player) {

		String uuid = player.getUniqueId().toString();
		Long lastEventTime = cooldownMap.getOrDefault(uuid, 0L);
		long timeToWait = lastEventTime + cooldownTime - System.currentTimeMillis();
		if (timeToWait > 0) {
			if (cooldownActionBar) {
				String actionBarJsonMessage = "{\"text\":\"&o" + plugin.getPluginLang()
						.getString("statistic-cooldown",
								"Achievements cooldown: wait TIME seconds before this action counts again.")
						.replace("TIME", String.format("%.1f", (double) timeToWait / 1000)) + "\"}";
				try {
					PacketSender.sendChatPacket(player, actionBarJsonMessage, PacketSender.ACTION_BAR_BYTE);
				} catch (Exception e) {
					plugin.getLogger().warning("Errors while trying to display action bar message for cooldown.");
				}
			}
			return true;
		}
		cooldownMap.put(uuid, System.currentTimeMillis());
		return false;
	}

	/**
	 * Updates the statistic in the database for a NormalAchievement and awards an achievement if an available one is
	 * found.
	 * 
	 * @param player
	 * @param category
	 * @param incrementValue
	 */
	protected void updateStatisticAndAwardAchievementsIfAvailable(Player player, NormalAchievements category,
			int incrementValue) {

		int amount = plugin.getPoolsManager().getAndIncrementStatisticAmount(category, player, incrementValue);

		String configAchievement = category + "." + amount;

		awardAchievementIfAvailable(player, configAchievement);
	}

	/**
	 * Updates the statistic in the database for a MultipleAchievement and awards an achievement if an available one is
	 * found.
	 * 
	 * @param player
	 * @param category
	 * @param subcategory
	 * @param incrementValue
	 */
	protected void updateStatisticAndAwardAchievementsIfAvailable(Player player, MultipleAchievements category,
			String subcategory, int incrementValue) {

		int amount = plugin.getPoolsManager().getAndIncrementStatisticAmount(category, subcategory, player,
				incrementValue);

		String configAchievement = category + "." + subcategory + '.' + amount;

		awardAchievementIfAvailable(player, configAchievement);
	}

	/**
	 * Awards an achievement if the corresponding threshold was found in the configuration file.
	 * 
	 * @param player
	 * @param configAchievement
	 */
	protected void awardAchievementIfAvailable(Player player, String configAchievement) {

		YamlManager pluginConfig = plugin.getPluginConfig();
		if (pluginConfig.getString(configAchievement + ".Message", null) != null) {
			plugin.getAchievementDisplay().displayAchievement(player, configAchievement);
			plugin.getDb().registerAchievement(player, pluginConfig.getString(configAchievement + ".Name"),
					pluginConfig.getString(configAchievement + ".Message"));
			plugin.getReward().checkConfig(player, configAchievement);

		}
	}

}
