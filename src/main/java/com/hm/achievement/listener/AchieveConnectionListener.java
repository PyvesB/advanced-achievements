package com.hm.achievement.listener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.utils.Cleanable;
import com.hm.achievement.utils.PlayerAdvancedAchievementEvent.PlayerAdvancedAchievementEventBuilder;

/**
 * Listener class to deal with Connections achievements, as well as update checker.
 * 
 * @author Pyves
 *
 */
public class AchieveConnectionListener extends AbstractListener implements Cleanable {

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

	// Contains UUIDs of players for which a AchieveConnectionRunnable ran successfully without returning.
	private final Set<String> playersAchieveConnectionRan;

	public AchieveConnectionListener(AdvancedAchievements plugin) {
		super(plugin);

		playersAchieveConnectionRan = new HashSet<>();
	}

	@Override
	public void cleanPlayerData(UUID uuid) {
		playersAchieveConnectionRan.remove(uuid.toString());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent event) {
		scheduleTask(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onWorldChanged(PlayerChangedWorldEvent event) {
		if (playersAchieveConnectionRan.contains(event.getPlayer().getUniqueId().toString())) {
			return;
		}

		scheduleTask(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onGameModeChange(PlayerGameModeChangeEvent event) {
		if (playersAchieveConnectionRan.contains(event.getPlayer().getUniqueId().toString())) {
			return;
		}

		scheduleTask(event.getPlayer());
	}

	/**
	 * Schedules a delayed task to deal with Connection achievements if player is not in restricted creative or blocked
	 * world.
	 * 
	 * @param player
	 */
	private void scheduleTask(final Player player) {
		// Schedule delayed task to check if player should receive a Connections achievement. This is done to avoid
		// immediately awarding the achievement.
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(
				Bukkit.getPluginManager().getPlugin(plugin.getDescription().getName()), new Runnable() {

					@Override
					public void run() {
						// Check if player has disconnected by the time this runnable was scheduled and other
						// achievement conditions.
						if (!player.isOnline()
								|| !shouldEventBeTakenIntoAccount(player, NormalAchievements.CONNECTIONS)) {
							return;
						}

						// Check whether another runnable has already done the work (even though this method is intended
						// to run once per player per connection instance, it might happen with some server settings).
						if (playersAchieveConnectionRan.contains(player.getUniqueId().toString())) {
							return;
						}

						String dateString = DATE_FORMAT.format(new Date());
						if (!dateString
								.equals(plugin.getDatabaseManager().getPlayerConnectionDate(player.getUniqueId()))) {
							int connections = plugin.getDatabaseManager().updateAndGetConnection(player.getUniqueId(),
									dateString);
							String configAchievement = NormalAchievements.CONNECTIONS + "." + connections;
							if (plugin.getPluginConfig().getString(configAchievement + ".Message", null) != null) {
								// Fire achievement event.
								PlayerAdvancedAchievementEventBuilder playerAdvancedAchievementEventBuilder = new PlayerAdvancedAchievementEventBuilder()
										.player(player)
										.name(plugin.getPluginConfig().getString(configAchievement + ".Name"))
										.displayName(
												plugin.getPluginConfig().getString(configAchievement + ".DisplayName"))
										.message(plugin.getPluginConfig().getString(configAchievement + ".Message"))
										.commandRewards(
												plugin.getRewardParser().getCommandRewards(configAchievement, player))
										.itemReward(plugin.getRewardParser().getItemReward(configAchievement))
										.moneyReward(plugin.getRewardParser().getMoneyAmount(configAchievement));

								Bukkit.getServer().getPluginManager()
										.callEvent(playerAdvancedAchievementEventBuilder.build());
							}
						}

						// Ran successfully to completion: no need to re-run while player is connected.
						playersAchieveConnectionRan.add(player.getUniqueId().toString());
					}
				}, 100);
	}
}
