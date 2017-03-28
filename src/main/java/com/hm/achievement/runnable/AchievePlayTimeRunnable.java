package com.hm.achievement.runnable;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.utils.PlayerAdvancedAchievementEvent.PlayerAdvancedAchievementEventBuilder;

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
	private void registerTimes(Player player) {
		// If player is in restricted creative mode or is in a blocked world, don't update played time.
		if (player.hasMetadata("NPC") || plugin.isRestrictCreative() && player.getGameMode() == GameMode.CREATIVE
				|| plugin.isRestrictSpectator() && player.getGameMode() == GameMode.SPECTATOR
				|| plugin.isInExludedWorld(player)) {
			return;
		}

		NormalAchievements category = NormalAchievements.PLAYEDTIME;

		// Do not register any times if player does not have permission.
		if (!player.hasPermission(category.toPermName())) {
			return;
		}

		long playedTime = plugin.getPoolsManager().getAndIncrementStatisticAmount(NormalAchievements.PLAYEDTIME,
				player.getUniqueId(), (int) (System.currentTimeMillis() - previousRunMillis));

		// Iterate through all the different achievements.
		for (String achievementThreshold : plugin.getPluginConfig()
				.getConfigurationSection(NormalAchievements.PLAYEDTIME.toString()).getKeys(false)) {
			String achievementName = plugin.getPluginConfig()
					.getString(category + "." + achievementThreshold + ".Name");

			// Check whether player has met the threshold and whether we he has not yet received the achievement.
			if (playedTime > Long.parseLong(achievementThreshold) * 3600000L
					&& !plugin.getPoolsManager().hasPlayerAchievement(player.getUniqueId(), achievementName)) {
				String configAchievement = category.toString() + achievementThreshold;
				// Fire achievement event.
				PlayerAdvancedAchievementEventBuilder playerAdvancedAchievementEventBuilder = new PlayerAdvancedAchievementEventBuilder()
						.player(player).name(plugin.getPluginConfig().getString(achievementName))
						.displayName(plugin.getPluginConfig().getString(configAchievement + ".DisplayName"))
						.message(plugin.getPluginConfig().getString(configAchievement + ".Message"))
						.commandRewards(plugin.getRewardParser().getCommandRewards(configAchievement, player))
						.itemReward(plugin.getRewardParser().getItemReward(configAchievement))
						.moneyReward(plugin.getRewardParser().getMoneyAmount(configAchievement));

				Bukkit.getServer().getPluginManager().callEvent(playerAdvancedAchievementEventBuilder.build());
			}
		}
	}
}
