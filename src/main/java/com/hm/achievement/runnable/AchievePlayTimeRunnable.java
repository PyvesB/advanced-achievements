package com.hm.achievement.runnable;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.command.ReloadCommand;
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

	private Essentials essentials;
	private long previousRunMillis;

	private boolean configIgnoreAFKPlayedTime;

	@Inject
	public AchievePlayTimeRunnable(@Named("main") CommentedYamlConfiguration mainConfig, int serverVersion,
			Map<String, List<Long>> sortedThresholds, CacheManager cacheManager, RewardParser rewardParser,
			ReloadCommand reloadCommand) {
		super(mainConfig, serverVersion, sortedThresholds, cacheManager, rewardParser, reloadCommand);

		if (Bukkit.getPluginManager().isPluginEnabled("Essentials")) {
			essentials = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
		}

		previousRunMillis = System.currentTimeMillis();
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		if (essentials != null) {
			configIgnoreAFKPlayedTime = mainConfig.getBoolean("IgnoreAFKPlayedTime", false);
		} else {
			configIgnoreAFKPlayedTime = false;
		}
	}

	@Override
	public void run() {
		Bukkit.getServer().getOnlinePlayers().stream().forEach(this::updateTime);

		previousRunMillis = System.currentTimeMillis();
	}

	/**
	 * Updates play time if all conditions are met and awards achievements if necessary.
	 * 
	 * @param player
	 */
	private void updateTime(Player player) {
		// If player is in restricted game mode or is in a blocked world, don't update played time.
		if (!shouldIncreaseBeTakenIntoAccountNoPermissions(player)) {
			return;
		}

		if (configIgnoreAFKPlayedTime) {
			User user = essentials.getUser(player);
			// If player is AFK, don't update played time.
			if (user != null && user.isAfk()) {
				return;
			}
		}

		// Do not register any times if player does not have permission.
		if (!player.hasPermission(NormalAchievements.PLAYEDTIME.toPermName())) {
			return;
		}

		long playedTime = cacheManager.getAndIncrementStatisticAmount(NormalAchievements.PLAYEDTIME, player.getUniqueId(),
				(int) (System.currentTimeMillis() - previousRunMillis));
		checkThresholdsAndAchievements(player, NormalAchievements.PLAYEDTIME.toString(), playedTime);
	}
}
