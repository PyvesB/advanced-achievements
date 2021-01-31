package com.hm.achievement.listener.statistics;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.config.AchievementMap;
import com.hm.achievement.db.AbstractDatabaseManager;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.lifecycle.Cleanable;
import com.hm.achievement.utils.PlayerAdvancedAchievementEvent;

/**
 * Listener class to deal with Connections achievements. This class uses delays processing of tasks to avoid spamming a
 * barely connected player.
 * 
 * @author Pyves
 *
 */
@Singleton
public class ConnectionsListener extends AbstractListener implements Cleanable {

	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	private final Set<UUID> playersConnectionProcessed = new HashSet<>();
	private final AdvancedAchievements advancedAchievements;
	private final AbstractDatabaseManager databaseManager;

	@Inject
	public ConnectionsListener(@Named("main") YamlConfiguration mainConfig, int serverVersion, AchievementMap achievementMap,
			CacheManager cacheManager, AdvancedAchievements advancedAchievements, AbstractDatabaseManager databaseManager) {
		super(NormalAchievements.CONNECTIONS, mainConfig, serverVersion, achievementMap, cacheManager);
		this.advancedAchievements = advancedAchievements;
		this.databaseManager = databaseManager;
	}

	@Override
	public void cleanPlayerData(UUID uuid) {
		playersConnectionProcessed.remove(uuid);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent event) {
		scheduleAwardConnection(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onWorldChanged(PlayerChangedWorldEvent event) {
		scheduleAwardConnection(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onGameModeChange(PlayerGameModeChangeEvent event) {
		scheduleAwardConnection(event.getPlayer());
	}

	/**
	 * Schedules a delayed task to deal with Connection achievements.
	 * 
	 * @param player
	 */
	private void scheduleAwardConnection(Player player) {
		if (!playersConnectionProcessed.contains(player.getUniqueId())) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(advancedAchievements, () -> {
				// In addition to the usual reception conditions, check that the player is still connected and that
				// another runnable hasn't already done the work (even though this method is intended to run once per
				// player per connection instance, it might happen with some server settings).
				if (shouldIncreaseBeTakenIntoAccount(player, category) && player.isOnline()
						&& !playersConnectionProcessed.contains(player.getUniqueId())) {
					handleConnectionAchievements(player);
					// Ran successfully to completion: no need to re-run while player is connected.
					playersConnectionProcessed.add(player.getUniqueId());
				}
			}, 100);
		}
	}

	/**
	 * Updates Connection statistics and awards an achievement if need-be.
	 * 
	 * @param player
	 */
	private void handleConnectionAchievements(Player player) {
		String dateString = LocalDate.now().format(DATE_TIME_FORMATTER);
		if (!dateString.equals(databaseManager.getPlayerConnectionDate(player.getUniqueId()))) {
			long connections = databaseManager.updateAndGetConnection(player.getUniqueId(), dateString);
			achievementMap.getForCategory(NormalAchievements.CONNECTIONS).stream()
					.filter(achievement -> achievement.getThreshold() == connections)
					.filter(achievement -> player.hasPermission("achievement." + achievement.getName()))
					.forEach(achievement -> Bukkit.getPluginManager()
							.callEvent(new PlayerAdvancedAchievementEvent(player, achievement)));
		}
	}
}
