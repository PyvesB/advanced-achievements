package com.hm.achievement.command;

import org.apache.commons.lang.StringEscapeUtils;
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
		sender.sendMessage(plugin.getColor() + "------------ " + plugin.getColor() + plugin.getIcon()
				+ ChatColor.translateAlternateColorCodes('&', " &lAdvanced Achievements ") + plugin.getColor()
				+ plugin.getIcon() + plugin.getColor() + " ------------");
		sender.sendMessage(plugin.getChatHeader() + plugin.getColor()
				+ plugin.getPluginLang().getString("version-command-description", "Description:") + " " + ChatColor.GRAY
				+ plugin.getPluginLang().getString("version-command-description-details",
						"Advanced Achievements enables unique and challenging achievements. Try to collect as many as you can, earn rewards, climb the rankings and receive RP books!"));
		sender.sendMessage(plugin.getChatHeader() + plugin.getColor()
				+ plugin.getPluginLang().getString("version-command-version", "Version:") + " " + ChatColor.GRAY
				+ plugin.getDescription().getVersion());
		sender.sendMessage(plugin.getChatHeader() + plugin.getColor()
				+ plugin.getPluginLang().getString("version-command-author", "Author:") + " " + ChatColor.GRAY
				+ plugin.getDescription().getAuthors().get(0));
		sender.sendMessage(plugin.getChatHeader() + plugin.getColor()
				+ plugin.getPluginLang().getString("version-command-website", "Website:") + " " + ChatColor.GRAY
				+ plugin.getDescription().getWebsite());

		// Display whether Advanced Achievements is linked to Vault.
		String vaultState;
		if (plugin.getRewardParser().isEconomySet(false)) {
			vaultState = "&a\u2714";
		} else {
			vaultState = "&4\u2718";
		}
		sender.sendMessage(plugin.getChatHeader() + plugin.getColor()
				+ plugin.getPluginLang().getString("version-command-vault", "Vault integration:") + " " + ChatColor.GRAY
				+ ChatColor.translateAlternateColorCodes('&', StringEscapeUtils.unescapeJava(vaultState)));

		// Display whether Advanced Achievements is linked to Pet Master.
		String petMasterState;
		if (plugin.getPetMasterGiveReceiveListener() != null) {
			petMasterState = "&a\u2714";
		} else {
			petMasterState = "&4\u2718";
		}
		sender.sendMessage(plugin.getChatHeader() + plugin.getColor()
				+ plugin.getPluginLang().getString("version-command-petmaster", "Pet Master integration:") + " "
				+ ChatColor.GRAY
				+ ChatColor.translateAlternateColorCodes('&', StringEscapeUtils.unescapeJava(petMasterState)));

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
