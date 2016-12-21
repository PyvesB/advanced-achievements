package com.hm.achievement.runnable;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

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

	public AchievePlayTimeRunnable(AdvancedAchievements plugin) {

		this.plugin = plugin;

		previousRunMillis = System.currentTimeMillis();
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
			playedTime = plugin.getDb().getPlaytimeAmount(player);
		}

		playedTime += System.currentTimeMillis() - previousRunMillis;

		plugin.getPoolsManager().getPlayedTimeHashMap().put(uuid, playedTime);

		// Iterate through all the different achievements.
		for (String achievementThreshold : plugin.getPluginConfig()
				.getConfigurationSection(NormalAchievements.PLAYEDTIME.toString()).getKeys(false)) {
			String achievementName = plugin.getPluginConfig()
					.getString(category + "." + achievementThreshold + ".Name");
			// Check whether player has met the threshold and whether we he has not yet received the achievement.
			if (playedTime > Integer.parseInt(achievementThreshold) * 3600000L
					&& !plugin.getPoolsManager().hasPlayerAchievement(player, achievementName)) {
				plugin.getAchievementDisplay().displayAchievement(player, category + "." + achievementThreshold);
				plugin.getDb().registerAchievement(player, achievementName,
						plugin.getPluginConfig().getString(category + "." + achievementThreshold + ".Message"));
				plugin.getPoolsManager().getReceivedAchievementsCache().put(uuid, achievementName);
				plugin.getPoolsManager().getNotReceivedAchievementsCache().remove(uuid, achievementName);
				plugin.getReward().checkConfig(player, category + "." + achievementThreshold);
			}
		}
	}
}
