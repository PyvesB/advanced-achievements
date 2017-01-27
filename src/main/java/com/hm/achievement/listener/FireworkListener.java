package com.hm.achievement.listener;

import java.util.HashSet;
import java.util.Set;

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
		fireworksLaunchedByPlugin = new HashSet<>();
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {

		Entity damager = event.getDamager();
		if (damager != null && fireworksLaunchedByPlugin.remove(damager.getUniqueId().toString())) {
			event.setCancelled(true);
		}
	}

	public void addFirework(Firework firework) {

		fireworksLaunchedByPlugin.add(firework.getUniqueId().toString());
	}
}
