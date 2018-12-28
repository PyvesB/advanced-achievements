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

		configIgnoreAFKPlayedTime = essentials != null && mainConfig.getBoolean("IgnoreAFKPlayedTime", false);
	}

	@Override
	public void run() {
		long currentTime = System.currentTimeMillis();
		Bukkit.getOnlinePlayers().stream().forEach(p -> updateTime(p, currentTime));
		previousRunMillis = currentTime;
	}

	/**
	 * Updates play time if all conditions are met and awards achievements if necessary.
	 * 
	 * @param player
	 * @param currentTime
	 */
	private void updateTime(Player player, long currentTime) {
		if (!shouldIncreaseBeTakenIntoAccount(player, NormalAchievements.PLAYEDTIME)) {
			return;
		}

		if (configIgnoreAFKPlayedTime) {
			// If player is AFK, don't update played time.
			if (essentials.getUser(player).isAfk()) {
				return;
			}
		}

		int millisSinceLastRun = (int) (currentTime - previousRunMillis);
		long totalMillis = cacheManager.getAndIncrementStatisticAmount(NormalAchievements.PLAYEDTIME, player.getUniqueId(),
				millisSinceLastRun);
		// Thresholds in the configuration are in hours.
		checkThresholdsAndAchievements(player, NormalAchievements.PLAYEDTIME.toString(), totalMillis / MILLIS_PER_HOUR);
	}
}
