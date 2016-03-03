package com.hm.achievement.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.hm.achievement.AdvancedAchievements;

public class AchieveTeleportListener implements Listener {

	private AdvancedAchievements plugin;

	public AchieveTeleportListener(AdvancedAchievements plugin) {

		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void playerTeleport(PlayerTeleportEvent event) {

		// Update location of player if he teleports somewhere else.
		plugin.getAchieveDistanceRunnable().getPlayerLocations().put(event.getPlayer(),
				event.getTo());

	}

}
