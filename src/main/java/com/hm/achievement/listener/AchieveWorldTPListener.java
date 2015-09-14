package com.hm.achievement.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.hm.achievement.runnable.AchieveDistanceRunnable;

public class AchieveWorldTPListener implements Listener {

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void worldJoin(PlayerChangedWorldEvent event) {

		AchieveDistanceRunnable.getAchievementLocations().put(event.getPlayer(), event.getPlayer().getLocation());

	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void playerTeleport(PlayerTeleportEvent event) {

		AchieveDistanceRunnable.getAchievementLocations().put(event.getPlayer(), event.getTo());

	}

}
