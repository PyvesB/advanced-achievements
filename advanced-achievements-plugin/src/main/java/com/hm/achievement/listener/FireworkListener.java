package com.hm.achievement.listener;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
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

	private final Set<UUID> fireworksLaunchedByPlugin = Collections.newSetFromMap(new ConcurrentHashMap<UUID, Boolean>());
	private final AdvancedAchievements advancedAchievements;

	@Inject
	public FireworkListener(AdvancedAchievements advancedAchievements) {
		this.advancedAchievements = advancedAchievements;
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		Entity damager = event.getDamager();
		if (damager != null && fireworksLaunchedByPlugin.contains(damager.getUniqueId())) {
			event.setCancelled(true);
		}
	}

	public void addFirework(Firework firework) {
		fireworksLaunchedByPlugin.add(firework.getUniqueId());

		// Schedule for removal to avoid creating memory leaks.
		Bukkit.getScheduler().runTaskLaterAsynchronously(advancedAchievements,
				() -> fireworksLaunchedByPlugin.remove(firework.getUniqueId()), 100);
	}
}
