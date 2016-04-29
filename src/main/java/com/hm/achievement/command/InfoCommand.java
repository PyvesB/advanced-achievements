package com.hm.achievement.command;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.hm.achievement.AdvancedAchievements;

public class InfoCommand {

	private AdvancedAchievements plugin;

	public InfoCommand(AdvancedAchievements plugin) {

		this.plugin = plugin;
	}

	/**
	 * Display various information about the plugin to the command sender.
	 */
	public void getInfo(CommandSender sender) {

		sender.sendMessage(plugin.getChatHeader() + plugin.getColor()
				+ plugin.getPluginLang().getString("version-command-name", "Name:") + " " + ChatColor.GRAY
				+ plugin.getDescription().getName());
		sender.sendMessage(plugin.getChatHeader() + plugin.getColor()
				+ plugin.getPluginLang().getString("version-command-version", "Version:") + " " + ChatColor.GRAY
				+ plugin.getDescription().getVersion());
		sender.sendMessage(plugin.getChatHeader() + plugin.getColor()
				+ plugin.getPluginLang().getString("version-command-website", "Website:") + " " + ChatColor.GRAY
				+ plugin.getDescription().getWebsite());
		sender.sendMessage(plugin.getChatHeader() + plugin.getColor()
				+ plugin.getPluginLang().getString("version-command-author", "Author:") + " " + ChatColor.GRAY
				+ plugin.getDescription().getAuthors().get(0));
		sender.sendMessage(plugin.getChatHeader() + plugin.getColor()
				+ plugin.getPluginLang().getString("version-command-description", "Description:") + " " + ChatColor.GRAY
				+ plugin.getPluginLang().getString("version-command-description-details",
						"Advanced Achievements enables unique and challenging achievements. Try to collect as many as you can, earn rewards, climb the rankings and receive RP books!"));
		if (plugin.setUpEconomy())
			sender.sendMessage(plugin.getChatHeader() + plugin.getColor()
					+ plugin.getPluginLang().getString("version-command-vault", "Vault integration:") + " "
					+ ChatColor.GRAY + "YES");
		else
			sender.sendMessage(plugin.getChatHeader() + plugin.getColor()
					+ plugin.getPluginLang().getString("version-command-vault", "Vault integration:") + " "
					+ ChatColor.GRAY + "NO");
		if (plugin.getPluginConfig().getString("DatabaseType", "sqlite").equalsIgnoreCase("mysql"))
			sender.sendMessage(plugin.getChatHeader() + plugin.getColor()
					+ plugin.getPluginLang().getString("version-command-database", "Database type:") + " "
					+ ChatColor.GRAY + "MySQL");
		else
			sender.sendMessage(plugin.getChatHeader() + plugin.getColor()
					+ plugin.getPluginLang().getString("version-command-database", "Database type:") + " "
					+ ChatColor.GRAY + "SQLite");

	}
}
