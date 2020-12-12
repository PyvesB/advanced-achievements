package com.hm.achievement.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public class ReloadableYamlConfiguration extends YamlConfiguration {

	@Override
	public void load(File file) throws FileNotFoundException, IOException, InvalidConfigurationException {
		// The Javadocs state that "All the values contained within this configuration will be removed". This is
		// incorrect, we need to manually clear the map containing configuration. See SPIGOT-6274.
		map.clear();
		super.load(file);
	}
}
