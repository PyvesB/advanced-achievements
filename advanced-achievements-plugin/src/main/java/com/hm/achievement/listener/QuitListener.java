package com.hm.achievement.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import com.hm.achievement.lifecycle.Cleanable;

/**
 * Listener class to deal with Distance and PlayedTime achievements.
 * 
 * @author Pyves
 *
 */
@Singleton
public class QuitListener implements Listener {

	private final List<Cleanable> cleanableObservers = new ArrayList<>();

	@Inject
	public QuitListener() {
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerQuit(PlayerQuitEvent event) {
		UUID uuid = event.getPlayer().getUniqueId();

		// Delay cleaning up to avoid invalidating data immediately: players frequently disconnect and reconnect just
		// after. This also avoids players taking advantage of the reset of cooldowns.
		Bukkit.getServer().getScheduler()
				.scheduleSyncDelayedTask(Bukkit.getPluginManager().getPlugin("AdvancedAchievements"), () -> {
					if (Bukkit.getPlayer(uuid) != null) {
						// Player reconnected.
						return;
					}

					// Notify all observers.
					cleanableObservers.stream().forEach(cleanable -> cleanable.cleanPlayerData(uuid));
				}, 200);
	}

	/**
	 * Adds a new Cleanable object that will be notified when a player has disconnected from the server.
	 * 
	 * @param cleanable
	 */
	public void addObserver(Cleanable cleanable) {
		cleanableObservers.add(cleanable);
	}
}
