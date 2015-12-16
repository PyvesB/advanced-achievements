package com.hm.achievement.command;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.language.Lang;

public class InfoCommand {

	private AdvancedAchievements plugin;

	public InfoCommand(AdvancedAchievements plugin) {

		this.plugin = plugin;
	}

	/**
	 * Display information about the plugin.
	 */
	public void getInfo(CommandSender sender) {

		sender.sendMessage(plugin.getChatHeader() + plugin.getColor() + Lang.VERSION_COMMAND_NAME + " "
				+ ChatColor.GRAY + plugin.getDescription().getName());
		sender.sendMessage(plugin.getChatHeader() + plugin.getColor() + Lang.VERSION_COMMAND_VERSION + " "
				+ ChatColor.GRAY + plugin.getDescription().getVersion());
		sender.sendMessage(plugin.getChatHeader() + plugin.getColor() + Lang.VERSION_COMMAND_WEBSITE + " "
				+ ChatColor.GRAY + plugin.getDescription().getWebsite());
		sender.sendMessage(plugin.getChatHeader() + plugin.getColor() + Lang.VERSION_COMMAND_AUTHOR + " "
				+ ChatColor.GRAY + plugin.getDescription().getAuthors().get(0));
		sender.sendMessage(plugin.getChatHeader() + plugin.getColor() + Lang.VERSION_COMMAND_DESCRIPTION + " "
				+ ChatColor.GRAY + Lang.VERSION_COMMAND_DESCRIPTION_DETAILS);
		if (plugin.setUpEconomy())
			sender.sendMessage(plugin.getChatHeader() + plugin.getColor() + Lang.VERSION_COMMAND_VAULT + " "
					+ ChatColor.GRAY + "YES");
		else
			sender.sendMessage(plugin.getChatHeader() + plugin.getColor() + Lang.VERSION_COMMAND_VAULT + " "
					+ ChatColor.GRAY + "NO");
		if (plugin.getConfig().getString("DatabaseType", "sqlite").equalsIgnoreCase("mysql"))
			sender.sendMessage(plugin.getChatHeader() + plugin.getColor() + Lang.VERSION_COMMAND_DATABASE + " "
					+ ChatColor.GRAY + "MySQL");
		else
			sender.sendMessage(plugin.getChatHeader() + plugin.getColor() + Lang.VERSION_COMMAND_DATABASE + " "
					+ ChatColor.GRAY + "SQLite");

	}
}
