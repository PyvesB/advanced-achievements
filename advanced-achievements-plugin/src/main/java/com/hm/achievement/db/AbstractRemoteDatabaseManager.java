package com.hm.achievement.db;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.logging.Logger;

import javax.inject.Named;

import com.hm.achievement.exception.PluginLoadError;
import com.hm.mcshared.file.CommentedYamlConfiguration;
import com.zaxxer.hikari.HikariConfig;

/**
 * Class used to handle a remote (in the sense not managed by the plugin) database.
 * 
 * @author Pyves
 *
 */
public class AbstractRemoteDatabaseManager extends AbstractDatabaseManager {

	volatile String jdbcUrl;
	volatile String username;
	volatile String password;
	volatile String additionalConnectionOptions;

	private final String databaseType;

	public AbstractRemoteDatabaseManager(@Named("main") CommentedYamlConfiguration mainConfig, Logger logger,
			@Named("ntd") Map<String, String> namesToDisplayNames, DatabaseUpdater databaseUpdater, String dataSourceClassName,
			String databaseType) {
		super(mainConfig, logger, namesToDisplayNames, databaseUpdater, dataSourceClassName);
		this.databaseType = databaseType;
	}

	@Override
	void performPreliminaryTasks() throws PluginLoadError, ClassNotFoundException {
		if (dataSourceClassName != null) Class.forName(dataSourceClassName);

		jdbcUrl = getJdbcUrl();
		try {
			username = URLEncoder.encode(getDatabaseConfig("DatabaseUser", "User", "root"), UTF_8.name());
			password = URLEncoder.encode(getDatabaseConfig("DatabasePassword", "Password", "root"), UTF_8.name());
		} catch (UnsupportedEncodingException e) {
			throw new PluginLoadError("Failed to retieve database credentials. Please verify your settings in config.yml.");
		}
		additionalConnectionOptions = mainConfig.getString("AdditionalConnectionOptions", "");
	}

	private String getJdbcUrl() {
		String jdbcUrl = getDatabaseConfig("DatabaseAddress", "Database", "");
		// Attempt to deal with common address mistakes where prefixes such as jdbc: or jdbc:mysql:// are omitted.
		if (!jdbcUrl.startsWith("jdbc:")) {
			if (jdbcUrl.startsWith(databaseType + "://")) {
				return "jdbc:" + jdbcUrl;
			} else {
				return "jdbc:" + databaseType + "://" + jdbcUrl;
			}
		}
		return jdbcUrl;
	}

	private String getDatabaseConfig(String newName, String oldName, String defaultValue) {
		return mainConfig.getString(newName, mainConfig.getString(databaseType.toUpperCase() + "." + oldName, defaultValue));
	}

	@Override
	HikariConfig getConfig() {
		HikariConfig config = new HikariConfig();
		if (dataSourceClassName != null) config.setDataSourceClassName(dataSourceClassName);
		config.setJdbcUrl(jdbcUrl);
		config.setUsername(username);
		config.setPassword(password);
		return config;
	}
}
