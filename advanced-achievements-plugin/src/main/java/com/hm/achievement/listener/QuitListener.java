package com.hm.achievement.listener;

import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.exception.PluginLoadError;
import com.hm.achievement.lifecycle.Cleanable;
import com.hm.achievement.lifecycle.Reloadable;
import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Listener class to deal with Distance and PlayedTime achievements.
 * 
 * @author Pyves
 *
 */
@Singleton
public class QuitListener implements Listener, Reloadable {

	private final CommentedYamlConfiguration mainConfig;
	private final AdvancedAchievements advancedAchievements;
	private final Set<Cleanable> cleanables;

	private long cleanDelay;

	@Inject
	public QuitListener(@Named("main") CommentedYamlConfiguration mainConfig, AdvancedAchievements advancedAchievements,
			Set<Cleanable> cleanables) {
		this.mainConfig = mainConfig;
		this.advancedAchievements = advancedAchievements;
		this.cleanables = cleanables;
	}

	@Override
	public void extractConfigurationParameters() throws PluginLoadError {
		cleanDelay = mainConfig.getBoolean("BungeeMode") ? 50L : 10000L;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerQuit(PlayerQuitEvent event) {
		UUID uuid = event.getPlayer().getUniqueId();

		// Delay cleaning up to avoid invalidating data immediately: players frequently disconnect and reconnect just
		// after. This also avoids players taking advantage of the reset of cooldowns.
		Bukkit.getScheduler().scheduleSyncDelayedTask(advancedAchievements, () -> {
			if (Bukkit.getPlayer(uuid) != null) {
				// Player reconnected.
				return;
			}

			// Notify all observers.
			cleanables.forEach(cleanable -> cleanable.cleanPlayerData(uuid));
		}, cleanDelay);
	}
}
