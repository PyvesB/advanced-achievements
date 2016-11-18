package com.hm.achievement.runnable;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import com.google.common.collect.HashMultimap;
import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.NormalAchievements;

/**
 * Class used to monitor players' played times.
 * 
 * @author Pyves
 *
 */
public class AchievePlayTimeRunnable implements Runnable {

	private final AdvancedAchievements plugin;

	private long previousRunMillis;

	// Multimaps corresponding to the players who have received played time achievements.
	// Each key in the multimap corresponds to one achievement threshold, and has its associated player Set.
	// Used as pseudo-caching system to reduce load on database as times are monitored on a regular basis.
	private HashMultimap<Integer, String> achievementsCache;

	public AchievePlayTimeRunnable(AdvancedAchievements plugin) {

		this.plugin = plugin;

		extractAchievementsFromConfig();

		previousRunMillis = System.currentTimeMillis();
	}

	/**
	 * Loads list of achievements from configuration.
	 * 
	 */
	public void extractAchievementsFromConfig() {

		Set<String> configKeys = plugin.getConfig().getConfigurationSection(NormalAchievements.PLAYEDTIME.toString())
				.getKeys(false);

		achievementsCache = HashMultimap.create(configKeys.size(), 1);

		// Populate the multimap with the different threshold keys and null values. This is used to easily iterate
		// through the thresholds without referring to the config file again.
		for (String time : configKeys)
			achievementsCache.put(Integer.valueOf(time), null);
	}

	@Override
	public void run() {

		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
			registerTimes(player);
		}

		previousRunMillis = System.currentTimeMillis();
	}

	/**
	 * Updates play times and stores them into server's memory until player disconnects.
	 * 
	 * @param player
	 */
	public void registerTimes(Player player) {

		// If player is in restricted creative mode or is in a blocked world, don't update played time.
		if (plugin.isRestrictCreative() && player.getGameMode() == GameMode.CREATIVE
				|| plugin.isInExludedWorld(player)) {
			return;
		}

		NormalAchievements category = NormalAchievements.PLAYEDTIME;

		// Do not register any times if player does not have permission.
		if (!player.hasPermission(category.toPermName()))
			return;

		String uuid = player.getUniqueId().toString();

		Long playedTime = plugin.getPoolsManager().getPlayedTimeHashMap().get(uuid);
		if (playedTime == null) {
			playedTime = plugin.getDb().getPlaytime(player);
		}

		playedTime += System.currentTimeMillis() - previousRunMillis;

		plugin.getPoolsManager().getPlayedTimeHashMap().put(uuid, playedTime);

		// Iterate through all the different achievements.
		for (Integer achievementThreshold : achievementsCache.keySet()) {
			// Check whether player has met the threshold and whether we he has not yet received the achievement.
			if (playedTime > achievementThreshold * 3600000L
					&& !achievementsCache.get(achievementThreshold).contains(uuid)) {
				// The cache does not contain information about the reception of the achievement. Query
				// database.
				if (!plugin.getDb().hasPlayerAchievement(player,
						plugin.getPluginConfig().getString(category + "." + achievementThreshold + ".Name"))) {
					plugin.getAchievementDisplay().displayAchievement(player, category + "." + achievementThreshold);
					plugin.getDb().registerAchievement(player,
							plugin.getPluginConfig().getString(category + "." + achievementThreshold + ".Name"),
							plugin.getPluginConfig().getString(category + "." + achievementThreshold + ".Message"));
					plugin.getReward().checkConfig(player, category + "." + achievementThreshold);

				}
				// Player has received this achievement.
				achievementsCache.put(achievementThreshold, uuid);
			}
		}
	}

	public HashMultimap<Integer, String> getAchievementsCache() {

		return achievementsCache;
	}
}
