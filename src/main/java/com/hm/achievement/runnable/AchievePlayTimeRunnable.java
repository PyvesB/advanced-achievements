package com.hm.achievement.runnable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;

import org.bukkit.entity.Player;

import com.hm.achievement.AdvancedAchievements;

public class AchievePlayTimeRunnable implements Runnable {

	private AdvancedAchievements plugin;
	private int[] achievementPlayTimes;
	private HashSet<?>[] playerAchievements;

	public AchievePlayTimeRunnable(AdvancedAchievements plugin) {

		this.plugin = plugin;
		achievementPlayTimes = new int[plugin.getConfig().getConfigurationSection("PlayedTime").getKeys(false).size()];
		int i = 0;
		for (String playedTime : plugin.getConfig().getConfigurationSection("PlayedTime").getKeys(false)) {
			achievementPlayTimes[i] = Integer.valueOf(playedTime);
		}

		playerAchievements = new HashSet<?>[achievementPlayTimes.length];
		for (i = 0; i < playerAchievements.length; ++i)
			playerAchievements[i] = new HashSet<Player>();

	}

	public void run() {

		registerTimes();
	}

	/**
	 * Update play times and store them into server's memory until player
	 * disconnects.
	 */
	@SuppressWarnings("unchecked")
	public void registerTimes() {

		for (Player player : plugin.getConnectionListener().getJoinTime().keySet()) {

			for (int i = 0; i < achievementPlayTimes.length; i++) {
				if (System.currentTimeMillis() - plugin.getConnectionListener().getJoinTime().get(player)
						+ plugin.getConnectionListener().getPlayTime().get(player) > achievementPlayTimes[i] * 3600000
						&& !playerAchievements[i].contains(player)) {
					if (!plugin.getDb().hasPlayerAchievement(player,
							plugin.getConfig().getString("PlayedTime." + achievementPlayTimes[i] + ".Name"))) {

						plugin.getAchievementDisplay().displayAchievement(player,
								"PlayedTime." + achievementPlayTimes[i]);
						SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
						plugin.getDb().registerAchievement(player,
								plugin.getConfig().getString("PlayedTime." + achievementPlayTimes[i] + ".Name"),
								plugin.getConfig().getString("PlayedTime." + achievementPlayTimes[i] + ".Message"),
								format.format(new Date()));
						plugin.getReward().checkConfig(player, "PlayedTime." + achievementPlayTimes[i]);

					}
					((HashSet<Player>) playerAchievements[i]).add(player);
				}
			}
		}
	}

	public HashSet<?>[] getPlayerAchievements() {

		return playerAchievements;
	}

}
