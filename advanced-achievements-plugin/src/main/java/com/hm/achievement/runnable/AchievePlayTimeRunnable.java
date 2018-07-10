package com.hm.achievement.runnable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.utils.RewardParser;
import com.hm.achievement.utils.StatisticIncreaseHandler;
import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Class used to monitor players' played times.
 * 
 * @author Pyves
 *
 */
@Singleton
public class AchievePlayTimeRunnable extends StatisticIncreaseHandler implements Runnable {

	private static final long MILLIS_PER_HOUR = TimeUnit.HOURS.toMillis(1);

	private Essentials essentials;
	private long previousRunMillis;

	private boolean configIgnoreAFKPlayedTime;

	@Inject
	public AchievePlayTimeRunnable(@Named("main") CommentedYamlConfiguration mainConfig, int serverVersion,
			Map<String, List<Long>> sortedThresholds, CacheManager cacheManager, RewardParser rewardParser) {
		super(mainConfig, serverVersion, sortedThresholds, cacheManager, rewardParser);

		if (Bukkit.getPluginManager().isPluginEnabled("Essentials")) {
			essentials = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
		}

		previousRunMillis = System.currentTimeMillis();
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		configIgnoreAFKPlayedTime = essentials == null ? false : mainConfig.getBoolean("IgnoreAFKPlayedTime", false);
	}

	@Override
	public void run() {
		Bukkit.getServer().getOnlinePlayers().stream().forEach(this::updateTime);

		previousRunMillis = System.currentTimeMillis();
	}

	/**
	 * Updates play time if all conditions are met and awards achievements if
	 * necessary.
	 * 
	 * @param player
	 */
	private void updateTime(Player player) {
		if (!shouldIncreaseBeTakenIntoAccount(player, NormalAchievements.PLAYEDTIME)) {
			return;
		}

		if (configIgnoreAFKPlayedTime) {
			User user = essentials.getUser(player);
			// If player is AFK, don't update played time.
			if (user != null && user.isAfk()) {
				return;
			}
		}

		int millisSinceLastRun = (int) (System.currentTimeMillis() - previousRunMillis);
		long totalMillis = cacheManager.getAndIncrementStatisticAmount(NormalAchievements.PLAYEDTIME,
				player.getUniqueId(), millisSinceLastRun);
		// Thresholds in the configuration are in hours.
		checkThresholdsAndAchievements(player, NormalAchievements.PLAYEDTIME.toString(), totalMillis / MILLIS_PER_HOUR);
	}
}
