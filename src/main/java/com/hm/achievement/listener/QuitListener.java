package com.hm.achievement.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerQuitEvent;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.utils.Cleanable;

/**
 * Listener class to deal with Distance and PlayedTime achievements.
 * 
 * @author Pyves
 *
 */
public class QuitListener extends AbstractListener {

	private final List<Cleanable> cleanableObservers;

	public QuitListener(AdvancedAchievements plugin) {
		super(plugin);

		cleanableObservers = new ArrayList<>();
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerQuit(final PlayerQuitEvent event) {

		final UUID uuid = event.getPlayer().getUniqueId();

		// Delay cleaning up to avoid invalidating data immediately: players frequently disconnect and reconnect just
		// after. This also avoids players taking advantage of the reset of cooldowns.
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(
				Bukkit.getPluginManager().getPlugin(plugin.getDescription().getName()), new Runnable() {

					@Override
					public void run() {
						if (Bukkit.getPlayer(uuid) != null) {
							// Player reconnected.
							return;
						}

						for (Cleanable cleanable : cleanableObservers) {
							// Clean all observers.
							cleanable.cleanPlayerData(uuid);
						}
					}
				}, 200);
	}

	public void registerCleanable(Cleanable cleanable) {
		cleanableObservers.add(cleanable);
	}
}
