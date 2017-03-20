package com.hm.achievement.utils;

import java.io.IOException;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;

import com.hm.achievement.AdvancedAchievements;
import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Class that adds warnings to CommentedYamlConfiguration when the user has messed up the configuration.
 * 
 * @author Pyves
 *
 */
public class AchievementCommentedYamlConfiguration extends CommentedYamlConfiguration {

	private final AdvancedAchievements plugin;

	/**
	 * Creates a new AchievementCommentedYamlConfiguration object representing one of the plugin's configuration files.
	 * 
	 * @param fileName
	 *            Name of the configuration file situated in the resource folder of the plugin.
	 * @param plugin
	 *            Reference to AdvancedAchievements main class.
	 * @throws IOException
	 * @throws InvalidConfigurationException
	 */
	public AchievementCommentedYamlConfiguration(String fileName, AdvancedAchievements plugin)
			throws IOException, InvalidConfigurationException {

		super(fileName, plugin);
		this.plugin = plugin;
	}

	/**
	 * Gets the requested ConfigurationSection by path. Returns an empty ConfigurationSection if it does not exist.
	 * Warns user if he has deleted an achievement category but has not added it to the DisabledCategories list.
	 * 
	 * @param path
	 *            Path of the ConfigurationSection to get.
	 * @return Requested ConfigurationSection or empty one.
	 */
	@Override
	public ConfigurationSection getConfigurationSection(String path) {
		if (!this.contains(path) && !plugin.getDisabledCategorySet().contains(path)) {
			plugin.getLogger().warning("You have deleted a category (" + path
					+ ") from the configuration without adding it into DisabledCategories.");
			plugin.getLogger().warning(
					"This may lead to undefined behaviour, please add the category name to the DisabledCategories list.");
		}
		return super.getConfigurationSection(path);
	}
}
