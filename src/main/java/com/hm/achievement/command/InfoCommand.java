package com.hm.achievement.command;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.hm.achievement.AdvancedAchievements;

/**
 * Class in charge of displaying the plugin's extra information (/aach info).
 * 
 * @author Pyves
 */
public class InfoCommand extends AbstractCommand {

	public InfoCommand(AdvancedAchievements plugin) {

		super(plugin);
	}

	@Override
	protected void executeCommand(CommandSender sender, String[] args) {

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
		// Display whether Advanced Achievements is linked to Vault.
		if (plugin.setUpEconomy(false))
			sender.sendMessage(plugin.getChatHeader() + plugin.getColor()
					+ plugin.getPluginLang().getString("version-command-vault", "Vault integration:") + " "
					+ ChatColor.GRAY + "ON");
		else
			sender.sendMessage(plugin.getChatHeader() + plugin.getColor()
					+ plugin.getPluginLang().getString("version-command-vault", "Vault integration:") + " "
					+ ChatColor.GRAY + "OFF");
		// Display database type.
		if ("mysql".equalsIgnoreCase(plugin.getPluginConfig().getString("DatabaseType", "sqlite")))
			sender.sendMessage(plugin.getChatHeader() + plugin.getColor()
					+ plugin.getPluginLang().getString("version-command-database", "Database type:") + " "
					+ ChatColor.GRAY + "MySQL");
		else if ("postgresql".equalsIgnoreCase(plugin.getPluginConfig().getString("DatabaseType", "sqlite")))
			sender.sendMessage(plugin.getChatHeader() + plugin.getColor()
					+ plugin.getPluginLang().getString("version-command-database", "Database type:") + " "
					+ ChatColor.GRAY + "PostgreSQL");
		else
			sender.sendMessage(plugin.getChatHeader() + plugin.getColor()
					+ plugin.getPluginLang().getString("version-command-database", "Database type:") + " "
					+ ChatColor.GRAY + "SQLite");
	}
}
