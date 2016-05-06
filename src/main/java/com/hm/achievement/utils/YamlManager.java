package com.hm.achievement.utils;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.hm.achievement.AdvancedAchievements;

public class YamlManager {

	private int comments;
	private FileManager manager;

	private File file;
	private FileConfiguration config;

	public YamlManager(Reader reader, File configFile, int comments, AdvancedAchievements plugin)
			throws IOException, InvalidConfigurationException {
		this.comments = comments;
		this.manager = new FileManager(plugin);
		this.file = configFile;
		this.config = new YamlConfiguration();
			config.load(reader);
	}

	public String getString(String path) {

		return this.config.getString(path);
	}

	public String getString(String path, String def) {

		return this.config.getString(path, def);
	}

	public int getInt(String path) {

		return this.config.getInt(path);
	}

	public int getInt(String path, int def) {

		return this.config.getInt(path, def);
	}

	public boolean getBoolean(String path) {

		return this.config.getBoolean(path);
	}

	public boolean getBoolean(String path, boolean def) {

		return this.config.getBoolean(path, def);
	}

	public ConfigurationSection getConfigurationSection(String path) {

		return this.config.getConfigurationSection(path);
	}

	public boolean isConfigurationSection(String path) {

		return this.config.isConfigurationSection(path);
	}

	public List<?> getList(String path) {

		return this.config.getList(path);
	}

	public List<?> getList(String path, List<?> def) {

		return this.config.getList(path, def);
	}

	public boolean contains(String path) {

		return this.config.contains(path);
	}

	public void set(String path, Object value) {

		this.config.set(path, value);
	}

	public void set(String path, Object value, String comment) {

		if (!this.config.contains(path)) {
			this.config.set(manager.getPluginName() + "_COMMENT_" + comments, comment);
			comments++;
		}
		this.config.set(path, value);
	}

	public void set(String path, Object value, String[] comment) {

		for (String comm : comment) {
			if (!this.config.contains(path)) {
				// Insert comment as new value in the file; will be converted
				// back to a comment later.
				this.config.set(manager.getPluginName() + "_COMMENT_" + comments, comm);
				comments++;
			}
		}
		this.config.set(path, value);
	}

	public void reloadConfig() throws IOException {

		this.config = YamlConfiguration.loadConfiguration(manager.getConfigContent(file));
	}

	public void saveConfig() throws IOException {

		String config = this.config.saveToString();
		manager.saveConfig(config, this.file);
	}

	public Set<String> getKeys(boolean deep) {

		return this.config.getKeys(deep);
	}
}
