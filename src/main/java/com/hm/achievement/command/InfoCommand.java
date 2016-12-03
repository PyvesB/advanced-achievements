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
		String vaultState;
		if (plugin.setUpEconomy(false)) {
			vaultState = "ON";
		} else {
			vaultState = "OFF";
		}
		sender.sendMessage(plugin.getChatHeader() + plugin.getColor()
				+ plugin.getPluginLang().getString("version-command-vault", "Vault integration:") + " " + ChatColor.GRAY
				+ vaultState);

		// Display whether Advanced Achievements is linked to Pet Master.
		String petMasterState;
		if (plugin.getPetMasterGiveReceiveListener() != null) {
			petMasterState = "ON";
		} else {
			petMasterState = "OFF";
		}
		sender.sendMessage(plugin.getChatHeader() + plugin.getColor()
				+ plugin.getPluginLang().getString("version-command-petmaster", "Pet Master integration:") + " "
				+ ChatColor.GRAY + petMasterState);

		// Display database type.
		String databaseType;
		if ("mysql".equalsIgnoreCase(plugin.getPluginConfig().getString("DatabaseType", "sqlite"))) {
			databaseType = "MySQL";
		} else if ("postgresql".equalsIgnoreCase(plugin.getPluginConfig().getString("DatabaseType", "sqlite"))) {
			databaseType = "PostgreSQL";
		} else {
			databaseType = "SQLite";
		}
		sender.sendMessage(plugin.getChatHeader() + plugin.getColor()
				+ plugin.getPluginLang().getString("version-command-database", "Database type:") + " " + ChatColor.GRAY
				+ databaseType);
	}
}
