package com.hm.achievement.command.executable;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.text.StringEscapeUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.config.RewardParser;

/**
 * Class in charge of displaying the plugin's extra information (/aach info).
 *
 * @author Pyves
 */
@Singleton
@CommandSpec(name = "info", permission = "", minArgs = 1, maxArgs = 1)
public class InfoCommand extends AbstractCommand {

	private final AdvancedAchievements advancedAchievements;
	private final RewardParser rewardParser;

	private String configDatabaseType;
	private String header;

	private String langVersionCommandDescription;
	private String langVersionCommandAuthor;
	private String langVersionCommandVersion;
	private String langVersionCommandWebsite;
	private String langVersionCommandVault;
	private String langVersionCommandPetmaster;
	private String langVersionCommandBtlp;
	private String langVersionCommandEssentials;
	private String langVersionCommandPlaceholderAPI;
	private String langVersionCommandDatabase;

	@Inject
	public InfoCommand(@Named("main") YamlConfiguration mainConfig, @Named("lang") YamlConfiguration langConfig,
			StringBuilder pluginHeader, AdvancedAchievements advancedAchievements, RewardParser rewardParser) {
		super(mainConfig, langConfig, pluginHeader);
		this.advancedAchievements = advancedAchievements;
		this.rewardParser = rewardParser;
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		ChatColor configColor = ChatColor.getByChar(mainConfig.getString("Color"));
		String configIcon = StringEscapeUtils.unescapeJava(mainConfig.getString("Icon"));
		configDatabaseType = mainConfig.getString("DatabaseType");

		header = configColor + "------------ " + configIcon + translateColorCodes(" &lAdvanced Achievements ") + configColor
				+ configIcon + configColor + " ------------";

		langVersionCommandDescription = pluginHeader.toString() + configColor
				+ langConfig.getString("version-command-description") + " " + ChatColor.GRAY
				+ langConfig.getString("version-command-description-details");

		langVersionCommandVersion = pluginHeader.toString() + configColor + langConfig.getString("version-command-version")
				+ " " + ChatColor.GRAY + advancedAchievements.getDescription().getVersion();

		langVersionCommandAuthor = pluginHeader.toString() + configColor + langConfig.getString("version-command-author")
				+ " " + ChatColor.GRAY + advancedAchievements.getDescription().getAuthors().get(0);

		langVersionCommandWebsite = pluginHeader.toString() + configColor + langConfig.getString("version-command-website")
				+ " " + ChatColor.GRAY + advancedAchievements.getDescription().getWebsite();

		// Display whether Advanced Achievements is linked to Vault.
		String vaultState = rewardParser.getEconomy() != null ? "&a\u2714" : "&4\u2718";
		langVersionCommandVault = pluginHeader.toString() + configColor + langConfig.getString("version-command-vault")
				+ " " + ChatColor.GRAY + translateColorCodes(StringEscapeUtils.unescapeJava(vaultState));

		// Display whether Advanced Achievements is linked to Pet Master.
		String petMasterState = Bukkit.getPluginManager().isPluginEnabled("PetMaster") ? "&a\u2714" : "&4\u2718";
		langVersionCommandPetmaster = pluginHeader.toString() + configColor
				+ langConfig.getString("version-command-petmaster") + " " + ChatColor.GRAY
				+ translateColorCodes(StringEscapeUtils.unescapeJava(petMasterState));

		// Display whether Advanced Achievements is linked to BungeeTabListPlus.
		String btlpState = Bukkit.getPluginManager().isPluginEnabled("BungeeTabListPlus") ? "&a\u2714" : "&4\u2718";
		langVersionCommandBtlp = pluginHeader.toString() + configColor + langConfig.getString("version-command-btlp")
				+ " " + ChatColor.GRAY + translateColorCodes(StringEscapeUtils.unescapeJava(btlpState));

		// Display whether Advanced Achievements is linked to Essentials.
		boolean essentialsUsed = Bukkit.getPluginManager().isPluginEnabled("Essentials")
				&& mainConfig.getBoolean("IgnoreAFKPlayedTime");
		String essentialsState = essentialsUsed ? "&a\u2714" : "&4\u2718";
		langVersionCommandEssentials = pluginHeader.toString() + configColor
				+ langConfig.getString("version-command-essentials") + " " + ChatColor.GRAY
				+ translateColorCodes(StringEscapeUtils.unescapeJava(essentialsState));

		// Display whether Advanced Achievements is linked to PlaceholderAPI.
		String placeholderAPIState = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI") ? "&a\u2714" : "&4\u2718";
		langVersionCommandPlaceholderAPI = pluginHeader.toString() + configColor
				+ langConfig.getString("version-command-placeholderapi") + " " + ChatColor.GRAY
				+ translateColorCodes(StringEscapeUtils.unescapeJava(placeholderAPIState));

		// Display database type.
		String databaseType = getDatabaseType();
		langVersionCommandDatabase = pluginHeader.toString() + configColor + langConfig.getString("version-command-database")
				+ " " + ChatColor.GRAY + databaseType;
	}

	private String getDatabaseType() {
		if ("mysql".equalsIgnoreCase(configDatabaseType)) {
			return "MySQL";
		} else if ("postgresql".equalsIgnoreCase(configDatabaseType)) {
			return "PostgreSQL";
		} else if ("h2".equalsIgnoreCase(configDatabaseType)) {
			return "H2";
		} else {
			return "SQLite";
		}
	}

	@Override
	void onExecute(CommandSender sender, String[] args) {
		sender.sendMessage(header);
		sender.sendMessage(langVersionCommandDescription);
		sender.sendMessage(langVersionCommandVersion);
		sender.sendMessage(langVersionCommandAuthor);
		sender.sendMessage(langVersionCommandWebsite);
		if (sender.hasPermission("achievement.*")) {
			sender.sendMessage(langVersionCommandVault);
			sender.sendMessage(langVersionCommandPetmaster);
			sender.sendMessage(langVersionCommandBtlp);
			sender.sendMessage(langVersionCommandEssentials);
			sender.sendMessage(langVersionCommandPlaceholderAPI);
			sender.sendMessage(langVersionCommandDatabase);
		}
	}
}
