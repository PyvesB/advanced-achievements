package com.hm.achievement.listener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.bukkit.entity.Player;

import com.hm.achievement.AdvancedAchievements;

public class AchievePlayTimeRunnable implements Runnable {

	private AdvancedAchievements plugin;
	private ArrayList<Integer> achievementPlayTimes;

	public AchievePlayTimeRunnable(AdvancedAchievements plugin) {

		this.plugin = plugin;
		achievementPlayTimes = new ArrayList<Integer>();
		for (String playedTime : plugin.getConfig().getConfigurationSection("PlayedTime").getKeys(false))
			achievementPlayTimes.add(Integer.valueOf(playedTime));

	}

	public void run() {

		registerTimes();
	}

	public void registerTimes() {

		for (Player player : AchieveConnectionListener.getJoinTime().keySet()) {

			if (!AchieveConnectionListener.getJoinTime().containsKey(player)) {
				AchieveConnectionListener.getJoinTime().put(player, System.currentTimeMillis());
				AchieveConnectionListener.getPlayTime().put(player, plugin.getDb().registerPlaytime(player, (long) 0));
			} else {
				for (int achievementPlayTime : achievementPlayTimes) {
					if (System.currentTimeMillis() - AchieveConnectionListener.getJoinTime().get(player)
							+ AchieveConnectionListener.getPlayTime().get(player) > achievementPlayTime * 3600000
							&& !plugin.getDb().hasAchievement(player,
									plugin.getConfig().getString("PlayedTime." + achievementPlayTime + ".Name"))) {

						String name = plugin.getConfig().getString("PlayedTime." + achievementPlayTime + ".Name");
						String msg = plugin.getConfig().getString("PlayedTime." + achievementPlayTime + ".Message");
						plugin.getAchievementDisplay().displayAchievement(player, name, msg);
						Date now = new Date();
						SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
						plugin.getDb().registerAchievement(player,
								plugin.getConfig().getString("PlayedTime." + achievementPlayTime + ".Name"),
								plugin.getConfig().getString("PlayedTime." + achievementPlayTime + ".Message"),
								format.format(now));
						plugin.getReward().checkConfig(player, "PlayedTime." + achievementPlayTime);
					}
				}
			}
		}
	}

}
