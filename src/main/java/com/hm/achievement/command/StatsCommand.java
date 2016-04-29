package com.hm.achievement.command;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.hm.achievement.AdvancedAchievements;

public class StatsCommand {

	private AdvancedAchievements plugin;
	private int totalAchievements;

	public StatsCommand(AdvancedAchievements plugin) {

		this.plugin = plugin;
		// Calculate the total number of achievements in the config file.
		for (String type : AdvancedAchievements.NORMAL_ACHIEVEMENTS)
			totalAchievements += plugin.getPluginConfig().getConfigurationSection(type).getKeys(false).size();
		for (String type : AdvancedAchievements.MULTIPLE_ACHIEVEMENTS)
			for (String item : plugin.getPluginConfig().getConfigurationSection(type).getKeys(false))
				totalAchievements += plugin.getPluginConfig().getConfigurationSection(type + '.' + item).getKeys(false)
						.size();
	}

	/**
	 * Get statistics of the player by displaying number of achievements
	 * received and total number of achievements.
	 */
	public void getStats(Player player) {

		int achievements = plugin.getDb().getPlayerAchievementsAmount(player);

		// Display number of achievements received and total achievements.
		player.sendMessage(plugin.getChatHeader()
				+ plugin.getPluginLang().getString("number-achievements", "Achievements received:") + " "
				+ plugin.getColor() + achievements + ChatColor.GRAY + "/" + plugin.getColor() + totalAchievements);

		// Display progress bar.
		if (totalAchievements > 0) {
			String barDisplay = "";
			for (int i = 1; i <= 146 - plugin.getIcon().length(); i++) {
				if (i < ((146 - plugin.getIcon().length()) * achievements) / totalAchievements)
					barDisplay = barDisplay + plugin.getColor() + "|";
				else {
					barDisplay = barDisplay + "&8|";
				}
			}
			player.sendMessage(plugin.getChatHeader() + "[" + ChatColor.translateAlternateColorCodes('&', barDisplay)
					+ ChatColor.GRAY + "]");
		}

	}
}
