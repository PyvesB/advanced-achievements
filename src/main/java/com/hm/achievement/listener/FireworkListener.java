package com.hm.achievement.listener;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import com.hm.achievement.AdvancedAchievements;

/**
 * Listener class to cancel damage from fireworks launched by the plugin.
 * 
 * @author Pyves
 *
 */
public class FireworkListener extends AbstractListener implements Listener {

	private final Set<String> fireworksLaunchedByPlugin;

	public FireworkListener(AdvancedAchievements plugin) {
		super(plugin);
		fireworksLaunchedByPlugin = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		Entity damager = event.getDamager();
		if (damager != null && fireworksLaunchedByPlugin.remove(damager.getUniqueId().toString())) {
			event.setCancelled(true);
		}
	}

	public void addFirework(Firework firework) {
		final String uuid = firework.getUniqueId().toString();
		fireworksLaunchedByPlugin.add(uuid);

		// Schedule for removal to avoid creating memory leaks if firework does not damage player.
		Bukkit.getServer().getScheduler().runTaskLaterAsynchronously(plugin, new Runnable() {

			@Override
			public void run() {
				fireworksLaunchedByPlugin.remove(uuid);
			}
		}, 100);
	}
}
