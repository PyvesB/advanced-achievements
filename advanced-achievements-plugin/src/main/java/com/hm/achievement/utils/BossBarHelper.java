package com.hm.achievement.utils;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class BossBarHelper {

	/**
	 * Temporarily displays a boss bar indicating achievement progress when receiving an achievement. Extract to a
	 * separate class to avoid loading errors when registering listeners.
	 *
	 * @param player
	 * @param progress
	 * @param color
	 * @param message
	 * @param plugin
	 */
	public static void displayBossBar(Plugin plugin, Player player, double progress, String color, String message) {
		BossBar bossBar = Bukkit.getServer().createBossBar(message, BarColor.valueOf(color), BarStyle.SOLID);
		bossBar.setProgress(progress);
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> bossBar.addPlayer(player), 110);
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> bossBar.removePlayer(player), 240);
	}

	private BossBarHelper() {
		// Not called.
	}

}
