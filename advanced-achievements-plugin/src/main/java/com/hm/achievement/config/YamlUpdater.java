package com.hm.achievement.config;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import com.hm.achievement.AdvancedAchievements;

public class YamlUpdater {

	private final AdvancedAchievements plugin;

	@Inject
	public YamlUpdater(AdvancedAchievements plugin) {
		this.plugin = plugin;
	}

	/**
	 * Updates user configurations by appending any YAML sections that are present in the default files shipped with the
	 * plugin. Comments, if any, are also included. If file updates are performed, the config object is reloaded.
	 * 
	 * @param defaultConfigName
	 * @param userConfigName
	 * @param userConfig
	 * @throws InvalidConfigurationException
	 * @throws IOException
	 */
	public void update(String defaultConfigName, String userConfigName, YamlConfiguration userConfig)
			throws InvalidConfigurationException, IOException {
		try (BufferedReader defaultConfigReader = new BufferedReader(
				new InputStreamReader(plugin.getResource(defaultConfigName), UTF_8))) {
			List<String> defaultLines = defaultConfigReader.lines().collect(Collectors.toList());
			YamlConfiguration defaultConfig = new YamlConfiguration();
			defaultConfig.loadFromString(StringUtils.join(defaultLines, System.lineSeparator()));

			List<String> sectionsToAppend = defaultConfig.getKeys(false).stream()
					.filter(key -> !userConfig.getKeys(false).contains(key))
					.flatMap(missingKey -> extractSectionForMissingKey(defaultLines, missingKey))
					.collect(Collectors.toList());

			if (!sectionsToAppend.isEmpty()) {
				Path userConfigPath = Paths.get(plugin.getDataFolder().getPath(), userConfigName);
				Files.write(userConfigPath, sectionsToAppend, StandardOpenOption.APPEND);
				userConfig.load(userConfigPath.toFile());
			}
		}
	}

	private Stream<String> extractSectionForMissingKey(List<String> defaultLines, String key) {
		for (int i = 0; i < defaultLines.size(); ++i) {
			if (defaultLines.get(i).startsWith(key)) {
				int start = i;
				// Include all comments lines above the missing key, if any.
				while (defaultLines.get(start - 1).startsWith("#")) {
					--start;
				}
				int end = i + 1;
				// Include all lines belonging to the same YAML section, i.e. starting with spaces.
				while (defaultLines.get(end).startsWith(" ")) {
					++end;
				}
				return Stream.concat(Stream.of(""), defaultLines.subList(start, end).stream());
			}
		}
		return Stream.of();
	}

}
