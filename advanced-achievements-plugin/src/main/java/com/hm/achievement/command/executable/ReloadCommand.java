package com.hm.achievement.command.executable;

import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.exception.PluginLoadError;
import com.hm.achievement.lang.LangHelper;
import com.hm.achievement.lang.command.CmdLang;
import com.hm.achievement.lifecycle.PluginLoader;
import com.hm.achievement.lifecycle.Reloadable;
import com.hm.mcshared.file.CommentedYamlConfiguration;

import dagger.Lazy;

/**
 * Class in charge of handling the /aach reload command, which reloads the plugin's configuration files.
 *
 * @author Pyves
 */
@Singleton
@CommandSpec(name = "reload", permission = "reload", minArgs = 1, maxArgs = 1)
public class ReloadCommand extends AbstractCommand {

	private final CommentedYamlConfiguration guiConfig;
	private final AdvancedAchievements advancedAchievements;
	private final Logger logger;
	private final Lazy<PluginLoader> pluginLoader;
	private final Lazy<Set<Reloadable>> reloadables;

	private String langConfigReloadFailed;
	private String langConfigSuccessfullyReloaded;

	@Inject
	public ReloadCommand(@Named("main") CommentedYamlConfiguration mainConfig,
			@Named("lang") CommentedYamlConfiguration langConfig, @Named("gui") CommentedYamlConfiguration guiConfig,
			StringBuilder pluginHeader, AdvancedAchievements advancedAchievements, Logger logger,
			Lazy<PluginLoader> pluginLoader, Lazy<Set<Reloadable>> reloadables) {
		super(mainConfig, langConfig, pluginHeader);
		this.guiConfig = guiConfig;
		this.advancedAchievements = advancedAchievements;
		this.logger = logger;
		this.pluginLoader = pluginLoader;
		this.reloadables = reloadables;
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		langConfigReloadFailed = pluginHeader + LangHelper.get(CmdLang.CONFIGURATION_RELOAD_FAILED, langConfig);
		langConfigSuccessfullyReloaded = pluginHeader
				+ LangHelper.get(CmdLang.CONFIGURATION_SUCCESSFULLY_RELOADED, langConfig);
	}

	/**
	 * Notifies all Reloadables.
	 * 
	 * @throws PluginLoadError
	 */
	public void notifyObservers() throws PluginLoadError {
		for (Reloadable reloadable : reloadables.get()) {
			reloadable.extractConfigurationParameters();
		}
	}

	@Override
	void onExecute(CommandSender sender, String[] args) {
		advancedAchievements.reloadConfig();

		try {
			mainConfig.loadConfiguration();
			langConfig.loadConfiguration();
			guiConfig.loadConfiguration();
			pluginLoader.get().loadAdvancedAchievements(false);
		} catch (PluginLoadError | IOException | InvalidConfigurationException e) {
			if (sender instanceof Player) {
				sender.sendMessage(langConfigReloadFailed);
			}
			logger.log(Level.SEVERE, "A non recoverable error was encountered while reloading the plugin, disabling it:", e);
			Bukkit.getPluginManager().disablePlugin(advancedAchievements);
			return;
		}

		if (sender instanceof Player) {
			sender.sendMessage(langConfigSuccessfullyReloaded);
		}
		logger.info("Configuration successfully reloaded.");
	}
}
