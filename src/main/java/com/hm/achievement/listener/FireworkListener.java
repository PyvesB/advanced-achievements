package com.hm.achievement.listener;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

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
@Singleton
public class FireworkListener implements Listener {

	private final Set<String> fireworksLaunchedByPlugin = Collections
			.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
	private final AdvancedAchievements advancedAchievements;

	@Inject
	public FireworkListener(AdvancedAchievements advancedAchievements) {
		this.advancedAchievements = advancedAchievements;
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		Entity damager = event.getDamager();
		if (damager != null && fireworksLaunchedByPlugin.contains(damager.getUniqueId().toString())) {
			event.setCancelled(true);
		}
	}

	public void addFirework(Firework firework) {
		String uuid = firework.getUniqueId().toString();
		fireworksLaunchedByPlugin.add(uuid);

		// Schedule for removal to avoid creating memory leaks.
		Bukkit.getServer().getScheduler().runTaskLaterAsynchronously(advancedAchievements,
				() -> fireworksLaunchedByPlugin.remove(uuid), 100);
	}
}
