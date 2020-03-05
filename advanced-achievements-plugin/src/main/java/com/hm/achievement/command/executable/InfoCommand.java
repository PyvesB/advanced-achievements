package com.hm.achievement.command.executable;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.text.StringEscapeUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.lang.LangHelper;
import com.hm.achievement.lang.command.InfoLang;
import com.hm.achievement.utils.RewardParser;
import com.hm.mcshared.file.CommentedYamlConfiguration;

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
	public InfoCommand(@Named("main") CommentedYamlConfiguration mainConfig,
			@Named("lang") CommentedYamlConfiguration langConfig, StringBuilder pluginHeader,
			AdvancedAchievements advancedAchievements, RewardParser rewardParser) {
		super(mainConfig, langConfig, pluginHeader);
		this.advancedAchievements = advancedAchievements;
		this.rewardParser = rewardParser;
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		ChatColor configColor = ChatColor.getByChar(mainConfig.getString("Color", "5"));
		String configIcon = StringEscapeUtils.unescapeJava(mainConfig.getString("Icon", "\u2618"));
		configDatabaseType = mainConfig.getString("DatabaseType", "sqlite");

		header = configColor + "------------ " + configIcon + translateColorCodes(" &lAdvanced Achievements ") + configColor
				+ configIcon + configColor + " ------------";

		langVersionCommandDescription = pluginHeader.toString() + configColor
				+ LangHelper.get(InfoLang.DESCRIPTION, langConfig)
				+ " " + ChatColor.GRAY + LangHelper.get(InfoLang.DESCRIPTION_DETAILS, langConfig);

		langVersionCommandVersion = pluginHeader.toString() + configColor + LangHelper.get(InfoLang.VERSION, langConfig)
				+ " " + ChatColor.GRAY + advancedAchievements.getDescription().getVersion();

		langVersionCommandAuthor = pluginHeader.toString() + configColor + LangHelper.get(InfoLang.AUTHOR, langConfig)
				+ " " + ChatColor.GRAY + advancedAchievements.getDescription().getAuthors().get(0);

		langVersionCommandWebsite = pluginHeader.toString() + configColor + LangHelper.get(InfoLang.WEBSITE, langConfig)
				+ " " + ChatColor.GRAY + advancedAchievements.getDescription().getWebsite();

		// Display whether Advanced Achievements is linked to Vault.
		String vaultState = rewardParser.getEconomy() != null ? "&a\u2714" : "&4\u2718";
		langVersionCommandVault = pluginHeader.toString() + configColor + LangHelper.get(InfoLang.VAULT, langConfig)
				+ " " + ChatColor.GRAY + translateColorCodes(StringEscapeUtils.unescapeJava(vaultState));

		// Display whether Advanced Achievements is linked to Pet Master.
		String petMasterState = Bukkit.getPluginManager().isPluginEnabled("PetMaster") ? "&a\u2714" : "&4\u2718";
		langVersionCommandPetmaster = pluginHeader.toString() + configColor + LangHelper.get(InfoLang.PETMASTER, langConfig)
				+ " " + ChatColor.GRAY + translateColorCodes(StringEscapeUtils.unescapeJava(petMasterState));

		// Display whether Advanced Achievements is linked to BungeeTabListPlus.
		String btlpState = Bukkit.getPluginManager().isPluginEnabled("BungeeTabListPlus") ? "&a\u2714" : "&4\u2718";
		langVersionCommandBtlp = pluginHeader.toString() + configColor + LangHelper.get(InfoLang.BTLP, langConfig)
				+ " " + ChatColor.GRAY + translateColorCodes(StringEscapeUtils.unescapeJava(btlpState));

		// Display whether Advanced Achievements is linked to Essentials.
		boolean essentialsUsed = Bukkit.getPluginManager().isPluginEnabled("Essentials")
				&& mainConfig.getBoolean("IgnoreAFKPlayedTime");
		String essentialsState = essentialsUsed ? "&a\u2714" : "&4\u2718";
		langVersionCommandEssentials = pluginHeader.toString() + configColor
				+ LangHelper.get(InfoLang.ESSENTIALS, langConfig)
				+ " " + ChatColor.GRAY + translateColorCodes(StringEscapeUtils.unescapeJava(essentialsState));

		// Display whether Advanced Achievements is linked to PlaceholderAPI.
		String placeholderAPIState = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI") ? "&a\u2714" : "&4\u2718";
		langVersionCommandPlaceholderAPI = pluginHeader.toString() + configColor
				+ LangHelper.get(InfoLang.PLACEHOLDERAPI, langConfig) + " " + ChatColor.GRAY
				+ translateColorCodes(StringEscapeUtils.unescapeJava(placeholderAPIState));

		// Display database type.
		String databaseType = getDatabaseType();
		langVersionCommandDatabase = pluginHeader.toString() + configColor + LangHelper.get(InfoLang.DATABASE, langConfig)
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
