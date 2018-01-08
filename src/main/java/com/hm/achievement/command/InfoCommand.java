package com.hm.achievement.command;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.lang.InfoLang;
import com.hm.achievement.lang.Lang;
import org.apache.commons.lang3.StringEscapeUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

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
				+ Lang.get(InfoLang.DESCRIPTION, plugin) + " " + ChatColor.GRAY
				+ Lang.get(InfoLang.DESCRIPTION_DETAILS, plugin);

		langVersionCommandVersion = plugin.getChatHeader() + configColor
				+ Lang.get(InfoLang.VERSION, plugin) + " " + ChatColor.GRAY
				+ plugin.getDescription().getVersion();

		langVersionCommandAuthor = plugin.getChatHeader() + configColor
				+ Lang.get(InfoLang.AUTHOR, plugin) + " " + ChatColor.GRAY
				+ plugin.getDescription().getAuthors().get(0);

		langVersionCommandWebsite = plugin.getChatHeader() + configColor
				+ Lang.get(InfoLang.WEBSITE, plugin) + " " + ChatColor.GRAY
				+ plugin.getDescription().getWebsite();

		// Display whether Advanced Achievements is linked to Vault.
		String vaultState = plugin.getRewardParser().getEconomy() != null ? "&a\u2714" : "&4\u2718";
		langVersionCommandVault = plugin.getChatHeader() + configColor
				+ Lang.get(InfoLang.VAULT, plugin) + " " + ChatColor.GRAY
				+ ChatColor.translateAlternateColorCodes('&', StringEscapeUtils.unescapeJava(vaultState));

		// Display whether Advanced Achievements is linked to Pet Master.
		String petMasterState = plugin.getPetMasterGiveReceiveListener() != null ? "&a\u2714" : "&4\u2718";
		langVersionCommandPetmaster = plugin.getChatHeader() + configColor
				+ Lang.get(InfoLang.PETMASTER, plugin) + " " + ChatColor.GRAY
				+ ChatColor.translateAlternateColorCodes('&', StringEscapeUtils.unescapeJava(petMasterState));

		// Display whether Advanced Achievements is linked to BungeeTabListPlus.
		String btlpState = Bukkit.getPluginManager().isPluginEnabled("BungeeTabListPlus") ? "&a\u2714" : "&4\u2718";
		langVersionCommandBtlp = plugin.getChatHeader() + configColor
				+ Lang.get(InfoLang.BTLP, plugin) + " " + ChatColor.GRAY
				+ ChatColor.translateAlternateColorCodes('&', StringEscapeUtils.unescapeJava(btlpState));

		// Display whether Advanced Achievements is linked to Essentials.
		boolean essentialsUsed = Bukkit.getPluginManager().isPluginEnabled("Essentials")
				&& plugin.getPluginConfig().getBoolean("IgnoreAFKPlayedTime", false);
		String essentialsState = essentialsUsed ? "&a\u2714" : "&4\u2718";
		langVersionCommandEssentials = plugin.getChatHeader() + configColor
				+ Lang.get(InfoLang.ESSENTIALS, plugin) + " " + ChatColor.GRAY
				+ ChatColor.translateAlternateColorCodes('&', StringEscapeUtils.unescapeJava(essentialsState));

		// Display whether Advanced Achievements is linked to PlaceholderAPI.
		String placeholderAPIState = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI") ? "&a\u2714" : "&4\u2718";
		langVersionCommandPlaceholderAPI = plugin.getChatHeader() + configColor
				+ Lang.get(InfoLang.PLACEHOLDERAPI, plugin) + " " + ChatColor.GRAY
				+ ChatColor.translateAlternateColorCodes('&', StringEscapeUtils.unescapeJava(placeholderAPIState));

		// Display database type.
		String databaseType = getDatabaseType();
		langVersionCommandDatabase = plugin.getChatHeader() + configColor
				+ Lang.get(InfoLang.DATABASE, plugin) + " " + ChatColor.GRAY
				+ databaseType;
	}

	private String getDatabaseType() {
		if ("mysql".equalsIgnoreCase(configDatabaseType)) {
			return "MySQL";
		} else if ("postgresql".equalsIgnoreCase(configDatabaseType)) {
			return "PostgreSQL";
		} else {
			return "SQLite";
		}
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
