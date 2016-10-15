package com.hm.achievement.runnable;

import java.util.Set;

import org.bukkit.Bukkit;
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

	// Multimaps corresponding to the players who have received played time achievements.
	// Each key in the multimap corresponds to one achievement threshold, and has its associated player Set.
	// Used as pseudo-caching system to reduce load on database as times are monitored on a regular basis.
	HashMultimap<Integer, String> achievementsCache;

	public AchievePlayTimeRunnable(AdvancedAchievements plugin) {

		this.plugin = plugin;

		extractAchievementsFromConfig();
	}

	/**
	 * Load list of achievements from configuration.
	 * 
	 * @param plugin
	 */
	public void extractAchievementsFromConfig() {

		Set<String> configKeys = plugin.getConfig().getConfigurationSection("PlayedTime").getKeys(false);

		achievementsCache = HashMultimap.create(configKeys.size(), 1);

		// Populate the multimap with the different threshold keys. This is used to iterate through the multimap when
		// players start connecting to the server.
		for (String time : configKeys)
			achievementsCache.put(Integer.valueOf(time), null);
	}

	@Override
	public void run() {

		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
			registerTimes(player);
		}
	}

	/**
	 * Update play times and store them into server's memory until player disconnects.
	 * 
	 * @param player
	 */
	public void registerTimes(Player player) {

		// Do not register any times if player does not have permission.
		if (!player.hasPermission("achievement.count.playedtime"))
			return;

		String uuid = player.getUniqueId().toString();

		// Extra check in case server was reloaded and players did not reconnect;
		// in that case the values are no longer in HashTables and must be rewritten.
		if (!plugin.getConnectionListener().getJoinTime().containsKey(uuid)) {
			plugin.getConnectionListener().getJoinTime().put(uuid, System.currentTimeMillis());
			plugin.getConnectionListener().getPlayTime().put(uuid, plugin.getDb().updateAndGetPlaytime(uuid, 0L));
		} else {
			// Iterate through all the different achievements.
			for (Integer achievementThreshold : achievementsCache.keySet()) {
				// Check whether player has met the threshold and whether we he has not yet received the achievement.
				if (System.currentTimeMillis() - plugin.getConnectionListener().getJoinTime().get(uuid)
						+ plugin.getConnectionListener().getPlayTime().get(uuid) > achievementThreshold * 3600000L) {
					if (!achievementsCache.get(achievementThreshold).contains(uuid)) {
						// The cache does not contain information about the reception of the achievement. Query
						// database.
						if (!plugin.getDb().hasPlayerAchievement(player,
								plugin.getPluginConfig().getString("PlayedTime." + achievementThreshold + ".Name"))) {
							plugin.getAchievementDisplay().displayAchievement(player,
									"PlayedTime." + achievementThreshold);
							plugin.getDb().registerAchievement(player,
									plugin.getPluginConfig().getString("PlayedTime." + achievementThreshold + ".Name"),
									plugin.getPluginConfig()
											.getString("PlayedTime." + achievementThreshold + ".Message"));
							plugin.getReward().checkConfig(player, "PlayedTime." + achievementThreshold);

						}
						// Player has received this achievement.
						achievementsCache.put(achievementThreshold, uuid);
					}
				}
			}
		}
	}

	public HashMultimap<Integer, String> getAchievementsCache() {

		return achievementsCache;
	}

}
