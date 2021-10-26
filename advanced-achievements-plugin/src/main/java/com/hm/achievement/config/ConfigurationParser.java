package com.hm.achievement.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.Category;
import com.hm.achievement.category.CommandAchievements;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.domain.Achievement;
import com.hm.achievement.domain.Achievement.AchievementBuilder;
import com.hm.achievement.exception.PluginLoadError;
import com.hm.achievement.utils.StringHelper;

/**
 * Class in charge of parsing the config.yml, lang.yml and gui.yml configuration files. It loads the files and populates
 * common data structures used in other parts of the plugin. Basic validation is performed on the achievements.
 *
 * @author Pyves
 */
public class ConfigurationParser {

	private final YamlConfiguration mainConfig;
	private final YamlConfiguration langConfig;
	private final YamlConfiguration guiConfig;
	private final AchievementMap achievementMap;
	private final Set<Category> disabledCategories;
	private final StringBuilder pluginHeader;
	private final Logger logger;
	private final int serverVersion;
	private final YamlUpdater yamlUpdater;
	private final AdvancedAchievements plugin;
	private final RewardParser rewardParser;

	@Inject
	public ConfigurationParser(@Named("main") YamlConfiguration mainConfig, @Named("lang") YamlConfiguration langConfig,
			@Named("gui") YamlConfiguration guiConfig, AchievementMap achievementMap, Set<Category> disabledCategories,
			StringBuilder pluginHeader, Logger logger, int serverVersion, YamlUpdater yamlUpdater,
			AdvancedAchievements plugin, RewardParser rewardParser) {
		this.mainConfig = mainConfig;
		this.langConfig = langConfig;
		this.guiConfig = guiConfig;
		this.achievementMap = achievementMap;
		this.disabledCategories = disabledCategories;
		this.pluginHeader = pluginHeader;
		this.logger = logger;
		this.serverVersion = serverVersion;
		this.yamlUpdater = yamlUpdater;
		this.plugin = plugin;
		this.rewardParser = rewardParser;
	}

	/**
	 * Loads the files and populates common data structures used in other parts of the plugin. Performs basic validation
	 * on the achievements.
	 *
	 * @throws PluginLoadError
	 */
	public void loadAndParseConfiguration() throws PluginLoadError {
		logger.info("Backing up and loading configuration files...");
		backupAndLoadConfiguration("config.yml", "config.yml", mainConfig);
		backupAndLoadConfiguration("lang.yml", mainConfig.getString("LanguageFileName"), langConfig);
		backupAndLoadConfiguration("gui.yml", "gui.yml", guiConfig);
		parseHeader();
		parseDisabledCategories();
		parseAchievements();
		logLoadingMessages();
	}

	/**
	 * Reterive keys associated with the configuration section at the given path
	 *
	 * @param path
	 * @return A set containing the keys
	 */
	private Set<String> getSectionKeys(String path) {
		return this.mainConfig.isConfigurationSection(path)
				? this.mainConfig.getConfigurationSection(path).getKeys(false)
				: Collections.emptySet();
	}

	/**
	 * Loads and backs up a configuration file.
	 *
	 * @param latestConfigName
	 * @param userConfigName
	 * @param userConfig
	 *
	 * @throws PluginLoadError
	 */
	private void backupAndLoadConfiguration(String latestConfigName, String userConfigName, YamlConfiguration userConfig)
			throws PluginLoadError {
		File configFile = new File(plugin.getDataFolder(), userConfigName);
		try {
			File backupFile = new File(plugin.getDataFolder(), userConfigName + ".bak");
			// Overwrite previous backup only if a newer version of the file exists.
			if (configFile.lastModified() > backupFile.lastModified()) {
				Files.copy(configFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Failed to back up " + userConfigName + ":", e);
		}

		try {
			if (!configFile.exists()) {
				configFile.getParentFile().mkdir();
				try (InputStream defaultConfig = plugin.getResource(userConfigName)) {
					Files.copy(defaultConfig, configFile.toPath());
				}
			}
			userConfig.load(configFile);
			yamlUpdater.update(latestConfigName, userConfigName, userConfig);
		} catch (IOException | InvalidConfigurationException e) {
			throw new PluginLoadError("Failed to load " + userConfigName
					+ ". Verify its syntax on yaml-online-parser.appspot.com and use the following logs.", e);
		}
	}

	/**
	 * Parses the plugin's header, used throughout the project.
	 */
	private void parseHeader() {
		pluginHeader.setLength(0);
		String icon = StringEscapeUtils.unescapeJava(mainConfig.getString("Icon"));
		if (StringUtils.isNotBlank(icon)) {
			String coloredIcon = ChatColor.getByChar(mainConfig.getString("Color")) + icon;
			pluginHeader
					.append(ChatColor.translateAlternateColorCodes('&',
							StringUtils.replace(mainConfig.getString("ChatHeader"), "%ICON%", coloredIcon)))
					.append(" ");
		}
		pluginHeader.trimToSize();
	}

	/**
	 * Extracts disabled categories from the configuration file.
	 * 
	 * @throws PluginLoadError
	 */
	private void parseDisabledCategories() throws PluginLoadError {
		extractDisabledCategoriesFromConfig();
		// Need PetMaster for PetMasterGive and PetMasterReceive categories.
		if ((!disabledCategories.contains(NormalAchievements.PETMASTERGIVE)
				|| !disabledCategories.contains(NormalAchievements.PETMASTERRECEIVE))
				&& !Bukkit.getPluginManager().isPluginEnabled("PetMaster")) {
			disabledCategories.add(NormalAchievements.PETMASTERGIVE);
			disabledCategories.add(NormalAchievements.PETMASTERRECEIVE);
			logger.warning("Overriding configuration: disabling PetMasterGive and PetMasterReceive categories.");
			logger.warning(
					"Ensure you have placed Pet Master in your plugins folder or add PetMasterGive and PetMasterReceive to the DisabledCategories list in config.yml.");
		}
		// Need Jobs for JobsReborn category.
		if (!disabledCategories.contains(MultipleAchievements.JOBSREBORN)
				&& !Bukkit.getPluginManager().isPluginEnabled("Jobs")) {
			disabledCategories.add(MultipleAchievements.JOBSREBORN);
			logger.warning("Overriding configuration: disabling JobsReborn category.");
			logger.warning(
					"Ensure you have placed JobsReborn in your plugins folder or add JobsReborn to the DisabledCategories list in config.yml.");
		}
		// Raids introduced in 1.14.
		if (!disabledCategories.contains(NormalAchievements.RAIDSWON) && serverVersion < 14) {
			disabledCategories.add(NormalAchievements.RAIDSWON);
			logger.warning("Overriding configuration: disabling RaidsWon category.");
			logger.warning(
					"Raids are not available in your server version, please add RaidsWon to the DisabledCategories list in config.yml.");
		}
	}

	/**
	 * Performs validation for the DisabledCategories list and maps the values to Category instances.
	 * 
	 * @throws PluginLoadError
	 */
	private void extractDisabledCategoriesFromConfig() throws PluginLoadError {
		disabledCategories.clear();
		for (String disabledCategory : mainConfig.getStringList("DisabledCategories")) {
			Category category = CommandAchievements.COMMANDS.toString().equals(disabledCategory)
					? CommandAchievements.COMMANDS
					: null;
			if (category == null) {
				category = NormalAchievements.getByName(disabledCategory);
			}
			if (category == null) {
				category = MultipleAchievements.getByName(disabledCategory);
			}
			if (category == null) {
				List<String> allCategories = new ArrayList<>();
				Arrays.stream(NormalAchievements.values()).forEach(n -> allCategories.add(n.toString()));
				Arrays.stream(MultipleAchievements.values()).forEach(m -> allCategories.add(m.toString()));
				allCategories.add(CommandAchievements.COMMANDS.toString());
				throw new PluginLoadError("Category " + disabledCategory + " specified in DisabledCategories is misspelt. "
						+ "Did you mean " + StringHelper.getClosestMatch(disabledCategory, allCategories) + "?");
			}
			disabledCategories.add(category);
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
		achievementMap.clearAll();

		// Enumerate Commands achievements.
		if (!disabledCategories.contains(CommandAchievements.COMMANDS)) {
			Set<String> commands = getSectionKeys(CommandAchievements.COMMANDS.toString());
			if (commands.isEmpty()) {
				disabledCategories.add(CommandAchievements.COMMANDS);
			} else {
				for (String command : commands) {
					parseAchievement(CommandAchievements.COMMANDS, command, -1L);
				}
			}
		}

		// Enumerate the normal achievements.
		for (NormalAchievements category : NormalAchievements.values()) {
			if (!disabledCategories.contains(category)) {
				if (getSectionKeys(category.toString()).isEmpty()) {
					disabledCategories.add(category);
					continue;
				}
				for (long threshold : getSortedThresholds(category.toString())) {
					parseAchievement(category, "", threshold);
				}
			}
		}

		// Enumerate the achievements with multiple categories.
		for (MultipleAchievements category : MultipleAchievements.values()) {
			if (!disabledCategories.contains(category)) {
				Set<String> subcategories = getSectionKeys(category.toString());
				if (subcategories.isEmpty()) {
					disabledCategories.add(category);
					continue;
				}

				for (String subcategory : subcategories) {
					for (long threshold : getSortedThresholds(category + "." + subcategory)) {
						parseAchievement(category, subcategory, threshold);
					}
				}
			}
		}
	}

	private List<Long> getSortedThresholds(String path) {
		return mainConfig.getConfigurationSection(path).getKeys(false).stream()
				.map(Long::parseLong)
				.sorted()
				.collect(Collectors.toList());
	}

	/**
	 * Performs validation for a single achievement and populates an entry in the namesToDisplayNames map.
	 * 
	 * @param category
	 * @param subcategory
	 * @param threshold
	 *
	 * @throws PluginLoadError If the achievement fails to parse due to misconfiguration.
	 */
	private void parseAchievement(Category category, String subcategory, long threshold) throws PluginLoadError {
		String path;
		if (category instanceof CommandAchievements) {
			path = category + "." + subcategory;
		} else if (category instanceof NormalAchievements) {
			path = category + "." + threshold;
		} else {
			path = category + "." + subcategory + "." + threshold;
		}
		ConfigurationSection section = mainConfig.getConfigurationSection(path);
		if (!section.contains("Name")) {
			throw new PluginLoadError("Achievement with path (" + path + ") is missing its Name parameter in config.yml.");
		} else if (achievementMap.getForName(section.getString("Name")) != null) {
			throw new PluginLoadError("Duplicate achievement Name (" + section.getString("Name") + "). "
					+ "Please ensure each Name is unique in config.yml.");
		} else if (!section.contains("Message")) {
			throw new PluginLoadError(
					"Achievement with path (" + path + ") is missing its Message parameter in config.yml.");
		}
		String rewardPath = mainConfig.isConfigurationSection(path + ".Reward") ? path + ".Reward" : path + ".Rewards";

		Achievement achievement = new AchievementBuilder()
				.name(section.getString("Name"))
				.displayName(StringUtils.defaultString(section.getString("DisplayName"), section.getString("Name")))
				.message(section.getString("Message"))
				.goal(StringUtils.defaultString(section.getString("Goal"), section.getString("Message")))
				.type(section.getString("Type"))
				.threshold(threshold)
				.category(category)
				.subcategory(subcategory)
				.rewards(rewardParser.parseRewards(rewardPath))
				.build();
		achievementMap.put(achievement);
	}

	private void logLoadingMessages() {
		int disabledCategoryCount = disabledCategories.size();
		int categories = NormalAchievements.values().length + MultipleAchievements.values().length + 1
				- disabledCategoryCount;
		logger.info("Loaded " + achievementMap.getAll().size() + " achievements in " + categories + " categories.");

		if (!disabledCategories.isEmpty()) {
			String noun = disabledCategoryCount == 1 ? "category" : "categories";
			logger.info(disabledCategoryCount + " disabled " + noun + ": " + disabledCategories.toString());
		}
	}

}
