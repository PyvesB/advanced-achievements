package com.hm.achievement.runnable;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.utils.PlayerAdvancedAchievementEvent.PlayerAdvancedAchievementEventBuilder;

/**
 * Class used to monitor players' played times.
 * 
 * @author Pyves
 *
 */
public class AchievePlayTimeRunnable extends AbstractRunnable implements Runnable {

	private Essentials essentials;
	private long previousRunMillis;
	
	private boolean configIgnoreAFKPlayedTime;

	public AchievePlayTimeRunnable(AdvancedAchievements plugin) {
		super(plugin);

		if (Bukkit.getPluginManager().isPluginEnabled("Essentials")) {
			essentials = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
		}
		
		previousRunMillis = System.currentTimeMillis();
	}
	
	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		if (essentials != null) {
			configIgnoreAFKPlayedTime = plugin.getPluginConfig().getBoolean("IgnoreAFKPlayedTime", false);
		} else {
			configIgnoreAFKPlayedTime = false;
		}
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
	private void registerTimes(Player player) {
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

		// Iterate through all the different achievements.
		for (String achievementThreshold : plugin.getPluginConfig()
				.getConfigurationSection(NormalAchievements.PLAYEDTIME.toString()).getKeys(false)) {
			String achievementName = plugin.getPluginConfig()
					.getString(category + "." + achievementThreshold + ".Name");

			// Check whether player has met the threshold and whether we he has not yet received the achievement.
			if (playedTime > Long.parseLong(achievementThreshold) * 3600000L
					&& !plugin.getCacheManager().hasPlayerAchievement(player.getUniqueId(), achievementName)) {
				String configAchievement = category.toString() + "." + achievementThreshold;
				// Fire achievement event.
				PlayerAdvancedAchievementEventBuilder playerAdvancedAchievementEventBuilder = new PlayerAdvancedAchievementEventBuilder()
						.player(player).name(achievementName)
						.displayName(plugin.getPluginConfig().getString(configAchievement + ".DisplayName"))
						.message(plugin.getPluginConfig().getString(configAchievement + ".Message"))
						.commandRewards(plugin.getRewardParser().getCommandRewards(configAchievement, player))
						.itemReward(plugin.getRewardParser().getItemReward(configAchievement))
						.moneyReward(plugin.getRewardParser().getRewardAmount(configAchievement, "Money"))
						.experienceReward(plugin.getRewardParser().getRewardAmount(configAchievement, "Experience"))
						.maxHealthReward(
								plugin.getRewardParser().getRewardAmount(configAchievement, "IncreaseMaxHealth"));

				Bukkit.getServer().getPluginManager().callEvent(playerAdvancedAchievementEventBuilder.build());
			}
		}
	}
}
