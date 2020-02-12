package com.hm.achievement.listener;

import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.lifecycle.Cleanable;

/**
 * Listener class to deal with Distance and PlayedTime achievements.
 * 
 * @author Pyves
 *
 */
@Singleton
public class QuitListener implements Listener {

	private final AdvancedAchievements advancedAchievements;
	private final Set<Cleanable> cleanables;

	@Inject
	public QuitListener(AdvancedAchievements advancedAchievements, Set<Cleanable> cleanables) {
		this.advancedAchievements = advancedAchievements;
		this.cleanables = cleanables;
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
			cleanables.stream().forEach(cleanable -> cleanable.cleanPlayerData(uuid));
		}, 200);
	}
}
