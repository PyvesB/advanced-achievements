package com.hm.achievement.command;

import org.apache.commons.lang3.StringEscapeUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.hm.achievement.AdvancedAchievements;

/**
 * Class in charge of displaying the plugin's extra information (/aach info).
 * 
 * @author Pyves
 */
public class InfoCommand extends AbstractCommand {

	private String langVersionCommandDescription;
	private String langVersionCommandAuthor;
	private String langVersionCommandVersion;
	private String langVersionCommandWebsite;
	private String langVersionCommandVault;
	private String langVersionCommandPetmaster;
	private String langVersionCommandBtlp;
	private String langVersionCommandEssentials;
	private String langVersionCommandPlaceholderAPI;
	private String configDatabaseType;
	private String langVersionCommandDatabase;
	private String header;

	public InfoCommand(AdvancedAchievements plugin) {
		super(plugin);
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		configDatabaseType = plugin.getPluginConfig().getString("DatabaseType", "sqlite");

		header = configColor + "------------ " + configIcon
				+ ChatColor.translateAlternateColorCodes('&', " &lAdvanced Achievements ") + configColor + configIcon
				+ configColor + " ------------";

		langVersionCommandDescription = plugin.getChatHeader() + configColor
				+ plugin.getPluginLang().getString("version-command-description", "Description:") + " " + ChatColor.GRAY
				+ plugin.getPluginLang().getString("version-command-description-details",
						"Advanced Achievements enables unique and challenging achievements. Try to collect as many as you can, earn rewards, climb the rankings and receive RP books!");

		langVersionCommandVersion = plugin.getChatHeader() + configColor
				+ plugin.getPluginLang().getString("version-command-version", "Version:") + " " + ChatColor.GRAY
				+ plugin.getDescription().getVersion();

		langVersionCommandAuthor = plugin.getChatHeader() + configColor
				+ plugin.getPluginLang().getString("version-command-author", "Author:") + " " + ChatColor.GRAY
				+ plugin.getDescription().getAuthors().get(0);

		langVersionCommandWebsite = plugin.getChatHeader() + configColor
				+ plugin.getPluginLang().getString("version-command-website", "Website:") + " " + ChatColor.GRAY
				+ plugin.getDescription().getWebsite();

		// Display whether Advanced Achievements is linked to Vault.
		String vaultState = plugin.getRewardParser().getEconomy() != null ? "&a\u2714" : "&4\u2718";
		langVersionCommandVault = plugin.getChatHeader() + configColor
				+ plugin.getPluginLang().getString("version-command-vault", "Vault integration:") + " " + ChatColor.GRAY
				+ ChatColor.translateAlternateColorCodes('&', StringEscapeUtils.unescapeJava(vaultState));

		// Display whether Advanced Achievements is linked to Pet Master.
		String petMasterState = plugin.getPetMasterGiveReceiveListener() != null ? "&a\u2714" : "&4\u2718";
		langVersionCommandPetmaster = plugin.getChatHeader() + configColor
				+ plugin.getPluginLang().getString("version-command-petmaster", "Pet Master integration:") + " "
				+ ChatColor.GRAY
				+ ChatColor.translateAlternateColorCodes('&', StringEscapeUtils.unescapeJava(petMasterState));

		// Display whether Advanced Achievements is linked to BungeeTabListPlus.
		String btlpState = Bukkit.getPluginManager().isPluginEnabled("BungeeTabListPlus") ? "&a\u2714" : "&4\u2718";
		langVersionCommandBtlp = plugin.getChatHeader() + configColor
				+ plugin.getPluginLang().getString("version-command-btlp", "BungeeTabListPlus integration:") + " "
				+ ChatColor.GRAY
				+ ChatColor.translateAlternateColorCodes('&', StringEscapeUtils.unescapeJava(btlpState));

		// Display whether Advanced Achievements is linked to Essentials.
		boolean essentialsUsed = Bukkit.getPluginManager().isPluginEnabled("Essentials")
				&& plugin.getPluginConfig().getBoolean("IgnoreAFKPlayedTime", false);
		String essentialsState = essentialsUsed ? "&a\u2714" : "&4\u2718";
		langVersionCommandEssentials = plugin.getChatHeader() + configColor
				+ plugin.getPluginLang().getString("version-command-essentials", "Essentials integration:") + " "
				+ ChatColor.GRAY
				+ ChatColor.translateAlternateColorCodes('&', StringEscapeUtils.unescapeJava(essentialsState));

		// Display whether Advanced Achievements is linked to PlaceholderAPI.
		String placeholderAPIState = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI") ? "&a\u2714"
				: "&4\u2718";
		langVersionCommandPlaceholderAPI = plugin.getChatHeader() + configColor
				+ plugin.getPluginLang().getString("version-command-placeholderapi", "PlaceholderAPI integration:")
				+ " " + ChatColor.GRAY
				+ ChatColor.translateAlternateColorCodes('&', StringEscapeUtils.unescapeJava(placeholderAPIState));

		// Display database type.
		String databaseType;
		if ("mysql".equalsIgnoreCase(configDatabaseType)) {
			databaseType = "MySQL";
		} else if ("postgresql".equalsIgnoreCase(configDatabaseType)) {
			databaseType = "PostgreSQL";
		} else {
			databaseType = "SQLite";
		}
		langVersionCommandDatabase = plugin.getChatHeader() + configColor
				+ plugin.getPluginLang().getString("version-command-database", "Database type:") + " " + ChatColor.GRAY
				+ databaseType;
	}

	@Override
	protected void executeCommand(CommandSender sender, String[] args) {
		sender.sendMessage(header);
		sender.sendMessage(langVersionCommandDescription);
		sender.sendMessage(langVersionCommandVersion);
		sender.sendMessage(langVersionCommandAuthor);
		sender.sendMessage(langVersionCommandWebsite);
		sender.sendMessage(langVersionCommandVault);
		sender.sendMessage(langVersionCommandPetmaster);
		sender.sendMessage(langVersionCommandBtlp);
		sender.sendMessage(langVersionCommandEssentials);
		sender.sendMessage(langVersionCommandPlaceholderAPI);
		sender.sendMessage(langVersionCommandDatabase);
	}
}
