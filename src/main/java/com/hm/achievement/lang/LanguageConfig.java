package com.hm.achievement.lang;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.exception.PluginLoadError;
import com.hm.mcshared.file.CommentedYamlConfiguration;
import org.bukkit.configuration.InvalidConfigurationException;

import java.io.IOException;

public class LanguageConfig extends CommentedYamlConfiguration {

	private LanguageConfig(AdvancedAchievements plugin) throws IOException, InvalidConfigurationException {
		super(plugin.getPluginConfig().getString("LanguageFileName", "lang.yml"), plugin);
	}

	public String getMsg(Msg msg) {
		return getString(msg.getPath(), msg.getDefaultMessage());
	}

	public static LanguageConfig load(AdvancedAchievements plugin) throws PluginLoadError {
		try {
			return new LanguageConfig(plugin);
		} catch (IOException | InvalidConfigurationException e) {
			throw new PluginLoadError("Error while loading locale. " +
					"Verify its syntax on yaml-online-parser.appspot.com and use the following logs", e);
		}
	}
}
