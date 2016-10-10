package com.hm.achievement.runnable;

import java.util.Arrays;
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.hm.achievement.AdvancedAchievements;

/**
 * Class used to monitor players' played times.
 * 
 * @author Pyves
 *
 */
public class AchievePlayTimeRunnable implements Runnable {

	private AdvancedAchievements plugin;

	// List of achievements extracted from configuration.
	private int[] playtimeThresholds;

	// Arrays of HashSets corresponding to whether a player has obtained a specific PlayedTime achievement.
	// Each index in the array corresponds to one achievement, and has its associated player Set.
	// Used as pseudo-caching system to reduce load on database.
	private HashSet<?>[] playerAchievements;

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

		playtimeThresholds = new int[plugin.getPluginConfig().getConfigurationSection("PlayedTime").getKeys(false)
				.size()];
		int i = 0;
		for (String playedTime : plugin.getPluginConfig().getConfigurationSection("PlayedTime").getKeys(false)) {
			playtimeThresholds[i] = Integer.parseInt(playedTime);
			i++;
		}
		// Sort array; this will enable us to break loops as soon as we find an achievement not yet received by a
		// player.
		Arrays.sort(playtimeThresholds);

		playerAchievements = new HashSet<?>[playtimeThresholds.length];
		for (i = 0; i < playerAchievements.length; ++i)
			playerAchievements[i] = new HashSet<Player>();

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
	@SuppressWarnings("unchecked")
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
			for (int i = 0; i < playtimeThresholds.length; i++) {
				// Check whether player has met the threshold and whether we he has not yet received the achievement.
				if (System.currentTimeMillis() - plugin.getConnectionListener().getJoinTime().get(uuid)
						+ plugin.getConnectionListener().getPlayTime().get(uuid) > playtimeThresholds[i] * 3600000L) {
					if (!playerAchievements[i].contains(player)) {
						// The cache does not contain information about the reception of the achievement. Query
						// database.
						if (!plugin.getDb().hasPlayerAchievement(player,
								plugin.getPluginConfig().getString("PlayedTime." + playtimeThresholds[i] + ".Name"))) {
							plugin.getAchievementDisplay().displayAchievement(player,
									"PlayedTime." + playtimeThresholds[i]);
							plugin.getDb().registerAchievement(player,
									plugin.getPluginConfig().getString("PlayedTime." + playtimeThresholds[i] + ".Name"),
									plugin.getPluginConfig()
											.getString("PlayedTime." + playtimeThresholds[i] + ".Message"));
							plugin.getReward().checkConfig(player, "PlayedTime." + playtimeThresholds[i]);

						}
						// Player has now received this achievement.
						((HashSet<Player>) playerAchievements[i]).add(player);
					}
				} else {
					// Achievements with higher thresholds will not yet be received by the player. Break loop.
					break;
				}
			}
		}
	}

	public HashSet<?>[] getPlayerAchievements() {

		return playerAchievements;
	}

}
