package com.hm.achievement.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;

import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.exception.PluginLoadError;
import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Class in charge of parsing the config.yml, lang.yml and gui.yml configuration files. It loads the files and populates
 * common data structures used in other parts of the plugin. Basic validation is performed on the achievements.
 *
 * @author Pyves
 */
@Singleton
public class ConfigurationParser {

	private final CommentedYamlConfiguration mainConfig;
	private final CommentedYamlConfiguration langConfig;
	private final CommentedYamlConfiguration guiConfig;
	private final FileUpdater fileUpdater;
	private final Map<String, String> achievementsAndDisplayNames;
	private final Map<String, List<Long>> sortedThresholds;
	private final Set<String> disabledCategories;
	private final StringBuilder pluginHeader;
	private final Logger logger;
	private final int serverVersion;

	@Inject
	public ConfigurationParser(@Named("main") CommentedYamlConfiguration mainConfig,
			@Named("lang") CommentedYamlConfiguration langConfig, @Named("gui") CommentedYamlConfiguration guiConfig,
			FileUpdater fileUpdater, Map<String, String> achievementsAndDisplayNames,
			Map<String, List<Long>> sortedThresholds, Set<String> disabledCategories, StringBuilder pluginHeader,
			Logger logger, int serverVersion) {
		this.mainConfig = mainConfig;
		this.langConfig = langConfig;
		this.guiConfig = guiConfig;
		this.fileUpdater = fileUpdater;
		this.achievementsAndDisplayNames = achievementsAndDisplayNames;
		this.sortedThresholds = sortedThresholds;
		this.disabledCategories = disabledCategories;
		this.pluginHeader = pluginHeader;
		this.logger = logger;
		this.serverVersion = serverVersion;
	}

	/**
	 * Loads the files and populates common data structures used in other parts of the plugin. Performs basic validation
	 * on the achievements.
	 *
	 * @throws PluginLoadError
	 */
	public void loadAndParseConfiguration() throws PluginLoadError {
		logger.info("Loading and backing up configuration files...");
		loadAndBackupConfiguration(mainConfig);
		loadAndBackupConfiguration(langConfig);
		loadAndBackupConfiguration(guiConfig);
		updateOldConfigurations();
		parseHeader();
		parseDisabledCategories();
		parseAchievements();
		logLoadingMessages();
	}

	/**
	 * Loads and backs up a configuration file.
	 *
	 * @param configuration
	 * @throws PluginLoadError
	 */
	private void loadAndBackupConfiguration(CommentedYamlConfiguration configuration) throws PluginLoadError {
		try {
			configuration.loadConfiguration();
		} catch (IOException | InvalidConfigurationException e) {
			throw new PluginLoadError("Failed to load " + configuration.getName()
					+ ". Verify its syntax on yaml-online-parser.appspot.com and use the following logs.", e);
		}

		try {
			configuration.backupConfiguration();
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Error while backing up " + configuration.getName() + ":", e);
		}
	}

	/**
	 * Calls the FileUpdater instance on the various configuration files.
	 */
	private void updateOldConfigurations() {
		fileUpdater.updateOldConfiguration(mainConfig);
		fileUpdater.updateOldLanguage(langConfig);
		fileUpdater.updateOldGUI(guiConfig);
	}

	/**
	 * Parses the plugin's header, used throughout the project.
	 */
	private void parseHeader() {
		pluginHeader.setLength(0);
		String icon = StringEscapeUtils.unescapeJava(mainConfig.getString("Icon", "\u2618"));
		if (StringUtils.isNotBlank(icon)) {
			String coloredIcon = ChatColor.getByChar(mainConfig.getString("Color", "5").charAt(0)) + icon;
			pluginHeader
					.append(ChatColor.translateAlternateColorCodes('&',
							StringUtils.replace(mainConfig.getString("ChatHeader", "&7[%ICON%&7]"), "%ICON%", coloredIcon)))
					.append(" ");
		}
		pluginHeader.trimToSize();
	}

	/**
	 * Extracts disabled categories from the configuration file.
	 */
	private void parseDisabledCategories() {
		disabledCategories.clear();
		disabledCategories.addAll(mainConfig.getList("DisabledCategories"));
		// Need PetMaster with a minimum version of 1.4 for PetMasterGive and PetMasterReceive categories.
		if ((!disabledCategories.contains(NormalAchievements.PETMASTERGIVE.toString())
				|| !disabledCategories.contains(NormalAchievements.PETMASTERRECEIVE.toString()))
				&& (!Bukkit.getPluginManager().isPluginEnabled("PetMaster") || Integer.parseInt(Character.toString(
						Bukkit.getPluginManager().getPlugin("PetMaster").getDescription().getVersion().charAt(2))) < 4)) {
			disabledCategories.add(NormalAchievements.PETMASTERGIVE.toString());
			disabledCategories.add(NormalAchievements.PETMASTERRECEIVE.toString());
			logger.warning("Overriding configuration: disabling PetMasterGive and PetMasterReceive categories.");
			logger.warning(
					"Ensure you have placed Pet Master with a minimum version of 1.4 in your plugins folder or add PetMasterGive and PetMasterReceive to the DisabledCategories list in config.yml.");
		}
		// Elytras introduced in Minecraft 1.9.
		if (!disabledCategories.contains(NormalAchievements.DISTANCEGLIDING.toString()) && serverVersion < 9) {
			disabledCategories.add(NormalAchievements.DISTANCEGLIDING.toString());
			logger.warning("Overriding configuration: disabling DistanceGliding category.");
			logger.warning(
					"Elytra are not available in your Minecraft version, please add DistanceGliding to the DisabledCategories list in config.yml.");
		}
		// Llamas introduced in Minecraft 1.11.
		if (!disabledCategories.contains(NormalAchievements.DISTANCELLAMA.toString()) && serverVersion < 11) {
			disabledCategories.add(NormalAchievements.DISTANCELLAMA.toString());
			logger.warning("Overriding configuration: disabling DistanceLlama category.");
			logger.warning(
					"Llamas not available in your Minecraft version, please add DistanceLlama to the DisabledCategories list in config.yml.");
		}
		// Breeding event introduced in Spigot 1319 (Minecraft 1.10.2).
		if (!disabledCategories.contains(MultipleAchievements.BREEDING.toString()) && serverVersion < 10) {
			disabledCategories.add(MultipleAchievements.BREEDING.toString());
			logger.warning("Overriding configuration: disabling Breeding category.");
			logger.warning(
					"The breeding event is not available in your server version, please add Breeding to the DisabledCategories list in config.yml.");
		}
	}

	/**
	 * Goes through all the achievements for non-disabled categories.
	 * 
	 * Populates relevant data structures and performs basic validation.
	 *
	 * @throws PluginLoadError If an achievement fails to parse due to misconfiguration.
	 */
	private void parseAchievements() throws PluginLoadError {
		achievementsAndDisplayNames.clear();
		sortedThresholds.clear();

		// Enumerate Commands achievements.
		if (!disabledCategories.contains("Commands")) {
			Set<String> commands = mainConfig.getShallowKeys("Commands");
			if (commands.isEmpty()) {
				disabledCategories.add("Commands");
			} else {
				for (String ach : commands) {
					parseAchievement("Commands." + ach);
				}
			}
		}

		// Enumerate the normal achievements.
		for (NormalAchievements category : NormalAchievements.values()) {
			String categoryName = category.toString();
			if (!disabledCategories.contains(categoryName)) {
				parseAchievements(categoryName);
			}
		}

		// Enumerate the achievements with multiple categories.
		for (MultipleAchievements category : MultipleAchievements.values()) {
			String categoryName = category.toString();
			if (!disabledCategories.contains(categoryName)) {
				for (String section : mainConfig.getShallowKeys(categoryName)) {
					parseAchievements(categoryName + '.' + section);
				}
			}
		}
	}

	/**
	 * Parses all achievements for a given category or category + subcategory. Populates the sortedThresholds map.
	 *
	 * @param path category or category.subcategory
	 * @throws PluginLoadError If an achievement fails to parse due to misconfiguration.
	 */
	private void parseAchievements(String path) throws PluginLoadError {
		Set<String> keys = mainConfig.getShallowKeys(path);

		// Disable category if no achievements exist
		// Don't add multi-achievement categories to disabled categories (path has a .)
		if (keys.isEmpty() && !path.contains(".")) {
			disabledCategories.add(path);
			return;
		}

		List<Long> thresholds = new ArrayList<>();
		for (String threshold : keys) {
			parseAchievement(path + "." + threshold);
			thresholds.add(Long.valueOf(threshold));
		}
		thresholds.sort(null);
		sortedThresholds.put(path, thresholds);
	}

	/**
	 * Performs validation for a single achievement and populates an entry in the achievementsAndDisplayNames map.
	 *
	 * @param path
	 * @throws PluginLoadError If the achievement fails to parse due to misconfiguration.
	 */
	private void parseAchievement(String path) throws PluginLoadError {
		String achName = mainConfig.getString(path + ".Name");
		if (achName == null) {
			throw new PluginLoadError("Achievement with path (" + path + ") is missing its Name parameter in config.yml.");
		} else if (achievementsAndDisplayNames.containsKey(achName)) {
			throw new PluginLoadError(
					"Duplicate achievement Name (" + achName + "). " + "Please ensure each Name is unique in config.yml.");
		} else {
			achievementsAndDisplayNames.put(achName, mainConfig.getString(path + ".DisplayName", ""));
		}
	}

	private void logLoadingMessages() {
		int disabledCategoryCount = disabledCategories.size();
		int categories = NormalAchievements.values().length + MultipleAchievements.values().length + 1
				- disabledCategoryCount;
		logger.info("Loaded " + achievementsAndDisplayNames.size() + " achievements in " + categories + " categories.");

		if (!disabledCategories.isEmpty()) {
			String noun = disabledCategoryCount == 1 ? "category" : "categories";
			logger.info(disabledCategoryCount + " disabled " + noun + ": " + disabledCategories.toString());
		}
	}

}
