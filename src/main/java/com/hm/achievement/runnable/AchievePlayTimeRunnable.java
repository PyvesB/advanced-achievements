package com.hm.achievement.runnable;

import java.util.Map;
import java.util.TreeMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.NormalAchievements;

/**
 * Class used to monitor players' played times.
 * 
 * @author Pyves
 *
 */
public class AchievePlayTimeRunnable extends AbstractRunnable implements Runnable {

	private Essentials essentials;
	private long previousRunMillis;
	// Keys in the map are thresholds in milliseconds, values are the paths to the achievements.
	private final Map<Long, String> parsedThresholds;

	private boolean configIgnoreAFKPlayedTime;

	public AchievePlayTimeRunnable(AdvancedAchievements plugin) {
		super(plugin);

		if (Bukkit.getPluginManager().isPluginEnabled("Essentials")) {
			essentials = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
		}

		previousRunMillis = System.currentTimeMillis();
		parsedThresholds = new TreeMap<>();
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		if (essentials != null) {
			configIgnoreAFKPlayedTime = plugin.getPluginConfig().getBoolean("IgnoreAFKPlayedTime", false);
		} else {
			configIgnoreAFKPlayedTime = false;
		}

		parsedThresholds.clear();
		for (String achievementThreshold : plugin.getPluginConfig()
				.getConfigurationSection(NormalAchievements.PLAYEDTIME.toString()).getKeys(false)) {
			parsedThresholds.put(Long.parseLong(achievementThreshold) * 3600000L,
					NormalAchievements.PLAYEDTIME + "." + achievementThreshold);
		}
	}

	@Override
	public void run() {
		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
			updateTime(player);
		}

		previousRunMillis = System.currentTimeMillis();
	}

	/**
	 * Updates play time if all conditions are met and awards achievements if necessary.
	 * 
	 * @param player
	 */
	private void updateTime(Player player) {
		// If player is in restricted game mode or is in a blocked world, don't update played time.
		if (!shouldRunBeTakenIntoAccount(player)) {
			return;
		}

		if (configIgnoreAFKPlayedTime) {
			User user = essentials.getUser(player);
			// If player is AFK, don't update played time.
			if (user != null && user.isAfk()) {
				return;
			}
		}

		NormalAchievements category = NormalAchievements.PLAYEDTIME;

		// Do not register any times if player does not have permission.
		if (!player.hasPermission(category.toPermName())) {
			return;
		}

		long playedTime = plugin.getCacheManager().getAndIncrementStatisticAmount(NormalAchievements.PLAYEDTIME,
				player.getUniqueId(), (int) (System.currentTimeMillis() - previousRunMillis));

		checkThresholdsAndAchievements(player, parsedThresholds, playedTime);
	}
}
