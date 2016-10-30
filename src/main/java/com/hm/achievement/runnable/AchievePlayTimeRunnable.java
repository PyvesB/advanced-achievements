package com.hm.achievement.runnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import com.google.common.collect.HashMultimap;
import com.hm.achievement.AdvancedAchievements;

/**
 * Class used to monitor players' played times.
 * 
 * @author Pyves
 *
 */
public class AchievePlayTimeRunnable implements Runnable {

	private AdvancedAchievements plugin;

	private Map<String, Long> playTime;

	private long previousRunMillis;

	// Multimaps corresponding to the players who have received played time achievements.
	// Each key in the multimap corresponds to one achievement threshold, and has its associated player Set.
	// Used as pseudo-caching system to reduce load on database as times are monitored on a regular basis.
	HashMultimap<Integer, String> achievementsCache;

	public AchievePlayTimeRunnable(AdvancedAchievements plugin) {

		this.plugin = plugin;

		if (plugin.isAsyncPooledRequestsSender())
			playTime = new ConcurrentHashMap<>();
		else
			playTime = new HashMap<>();

		extractAchievementsFromConfig();
		
		previousRunMillis = System.currentTimeMillis();
	}

	/**
	 * Load list of achievements from configuration.
	 * 
	 * @param plugin
	 */
	public void extractAchievementsFromConfig() {

		Set<String> configKeys = plugin.getConfig().getConfigurationSection("PlayedTime").getKeys(false);

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
	 * Update play times and store them into server's memory until player disconnects.
	 * 
	 * @param player
	 */
	public void registerTimes(Player player) {

		// If player is in restricted creative mode or is in a blocked world, don't update played time.
		if (plugin.isRestrictCreative() && player.getGameMode() == GameMode.CREATIVE || plugin.isInExludedWorld(player))
			return;

		// Do not register any times if player does not have permission.
		if (!player.hasPermission("achievement.count.playedtime"))
			return;

		String uuid = player.getUniqueId().toString();

		Long playedTime = playTime.get(uuid);
		if (!playTime.containsKey(uuid)) {
			playedTime = plugin.getDb().getPlaytime(player);
		}

		playedTime += System.currentTimeMillis() - previousRunMillis;

		playTime.put(uuid, playedTime);

		// Iterate through all the different achievements.
		for (Integer achievementThreshold : achievementsCache.keySet()) {
			// Check whether player has met the threshold and whether we he has not yet received the achievement.
			if (playedTime > achievementThreshold * 3600000L
					&& !achievementsCache.get(achievementThreshold).contains(uuid)) {
				// The cache does not contain information about the reception of the achievement. Query
				// database.
				if (!plugin.getDb().hasPlayerAchievement(player,
						plugin.getPluginConfig().getString("PlayedTime." + achievementThreshold + ".Name"))) {
					plugin.getAchievementDisplay().displayAchievement(player, "PlayedTime." + achievementThreshold);
					plugin.getDb().registerAchievement(player,
							plugin.getPluginConfig().getString("PlayedTime." + achievementThreshold + ".Name"),
							plugin.getPluginConfig().getString("PlayedTime." + achievementThreshold + ".Message"));
					plugin.getReward().checkConfig(player, "PlayedTime." + achievementThreshold);

				}
				// Player has received this achievement.
				achievementsCache.put(achievementThreshold, uuid);
			}
		}
	}

	public HashMultimap<Integer, String> getAchievementsCache() {

		return achievementsCache;
	}

	/**
	 * Retrieve map containing the time played by each player.
	 * 
	 * @return playTime map
	 */
	public Map<String, Long> getPlayTime() {

		return playTime;
	}

}
