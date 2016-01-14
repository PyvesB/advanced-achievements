package com.hm.achievement.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.hm.achievement.AdvancedAchievements;

public class AchieveWorldTPListener implements Listener {

	private AdvancedAchievements plugin;

	public AchieveWorldTPListener(AdvancedAchievements plugin) {

		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {

		plugin.getAchieveDistanceRunnable().getPlayerLocations().put(event.getPlayer(),
				event.getPlayer().getLocation());

	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void playerTeleport(PlayerTeleportEvent event) {

		plugin.getAchieveDistanceRunnable().getPlayerLocations().put(event.getPlayer(), event.getTo());

	}

}
