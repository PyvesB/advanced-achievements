package com.hm.achievement.listener.statistics;

import java.util.HashMap;
import java.util.Map;
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
import com.hm.achievement.db.data.ConnectionInformation;
import com.hm.achievement.lifecycle.Cleanable;

/**
 * Listener class to deal with Connections achievements. This class uses delays processing of tasks to avoid spamming a
 * barely connected player.
 * 
 * @author Pyves
 *
 */
@Singleton
public class ConnectionsListener extends AbstractListener implements Cleanable {

	private final Map<UUID, String> playerConnectionDates = new HashMap<>();
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
	public void cleanPlayerData() {
		playerConnectionDates.keySet().removeIf(player -> !Bukkit.getOfflinePlayer(player).isOnline());
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
		Bukkit.getScheduler().scheduleSyncDelayedTask(advancedAchievements, () -> {
			String cachedDate = playerConnectionDates.get(player.getUniqueId());
			String today = ConnectionInformation.today();
			if (!today.equals(cachedDate) && shouldIncreaseBeTakenIntoAccount(player, category) && player.isOnline()) {
				playerConnectionDates.put(player.getUniqueId(), today);
				ConnectionInformation connectionInformation = databaseManager.getConnectionInformation(player.getUniqueId());
				if (!today.equals(connectionInformation.getDate())) {
					databaseManager.updateConnectionInformation(player.getUniqueId(), connectionInformation.getCount() + 1);
					checkThresholdsAndAchievements(player, category, connectionInformation.getCount() + 1);
				}
			}
		}, 100);
	}
}
