package com.hm.achievement.runnable;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.utils.AchievementCommentedYamlConfiguration;
import com.hm.achievement.utils.PlayerAdvancedAchievementEvent.PlayerAdvancedAchievementEventBuilder;

/**
 * Runnable task to check for Connection achievements after a player has connected.
 * 
 * @author Pyves
 *
 */
public class AchieveConnectionRunnable extends AbstractRunnable implements Runnable {

	private final Player player;

	public AchieveConnectionRunnable(Player player, AdvancedAchievements plugin) {
		super(plugin);
		super.extractConfigurationParameters();
		this.player = player;
	}

	@Override
	public void run() {
		// Check if player is still online.
		if (!player.isOnline()) {
			return;
		}

		// Check again in case player has changed world or game mode by the time this runnable was scheduled.
		if (!shouldRunBeTakenIntoAccount(player)) {
			return;
		}

		String uuid = player.getUniqueId().toString();

		// Check whether another runnable has already done the work (even though this method is intended to run once
		// per player per connection instance, this might happen in some server configurations).
		if (plugin.getConnectionListener().getPlayersAchieveConnectionRan().contains(uuid)) {
			return;
		}

		NormalAchievements category = NormalAchievements.CONNECTIONS;

		if (!player.hasPermission(category.toPermName())) {
			return;
		}

		Date now = new Date();
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");

		if (!format.format(now).equals(plugin.getDatabaseManager().getPlayerConnectionDate(player.getUniqueId()))) {
			int connections = plugin.getDatabaseManager().updateAndGetConnection(player.getUniqueId(),
					format.format(now));
			String configAchievement = category + "." + connections;
			AchievementCommentedYamlConfiguration pluginConfig = plugin.getPluginConfig();
			if (pluginConfig.getString(configAchievement + ".Message", null) != null) {
				// Fire achievement event.
				PlayerAdvancedAchievementEventBuilder playerAdvancedAchievementEventBuilder = new PlayerAdvancedAchievementEventBuilder()
						.player(player).name(plugin.getPluginConfig().getString(configAchievement + ".Name"))
						.displayName(plugin.getPluginConfig().getString(configAchievement + ".DisplayName"))
						.message(plugin.getPluginConfig().getString(configAchievement + ".Message"))
						.commandRewards(plugin.getRewardParser().getCommandRewards(configAchievement, player))
						.itemReward(plugin.getRewardParser().getItemReward(configAchievement))
						.moneyReward(plugin.getRewardParser().getMoneyAmount(configAchievement));

				Bukkit.getServer().getPluginManager().callEvent(playerAdvancedAchievementEventBuilder.build());
			}
		}

		// Ran successfully to completion: no need to re-run while player is connected.
		plugin.getConnectionListener().getPlayersAchieveConnectionRan().add(uuid);
	}
}
