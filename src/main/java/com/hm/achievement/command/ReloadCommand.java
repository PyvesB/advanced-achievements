package com.hm.achievement.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import com.hm.achievement.lang.LanguageConfig;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.exception.PluginLoadError;
import com.hm.achievement.utils.Reloadable;
import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Class in charge of handling the /aach reload command, which reloads the plugin's configuration files.
 * 
 * @author Pyves
 */
public class ReloadCommand extends AbstractCommand {

	private final List<Reloadable> reloadableObservers;

	public ReloadCommand(AdvancedAchievements plugin) {
		super(plugin);

		reloadableObservers = new ArrayList<>();
	}

	@Override
	protected void executeCommand(CommandSender sender, String[] args) {
		plugin.reloadConfig();

		try {
			CommentedYamlConfiguration config = plugin.loadAndBackupFile("config.yml");

			// Compare the DisabledCategories configuration list in the previous configuration file and the new one.
			Set<String> disabledCategorySet = plugin.extractDisabledCategories(config);
			if (!disabledCategorySet.equals(plugin.getDisabledCategorySet())) {
				if (sender instanceof Player) {
					sender.sendMessage(plugin.getChatHeader() + plugin.getPluginLang().getString(
							"server-restart-reload",
							"DisabledCategories list was modified. Server must be fully reloaded or restarted for your changes to take effect."));
				}
				plugin.getLogger().warning(
						"DisabledCategories list was modified. Server must be fully reloaded or restarted for your changes to take effect.");
				plugin.getLogger().warning("Aborting plugin reload.");
				return;
			}

			plugin.setPluginConfig(config);
			plugin.setPluginLang(LanguageConfig.load(plugin));
			plugin.setGui(plugin.loadAndBackupFile("gui.yml"));
		} catch (PluginLoadError e) {
			if (sender instanceof Player) {
				sender.sendMessage(
						plugin.getChatHeader() + plugin.getPluginLang().getString("configuration-reload-failed",
								"Errors while reloading configuration. Please view logs for more details."));
			}
			plugin.getLogger().log(Level.SEVERE,
					"A non recoverable error was encountered while reloading the plugin, disabling it.", e);
			Bukkit.getServer().getPluginManager().disablePlugin(plugin);
			return;
		}

		// Reload all observers.
		reloadableObservers.stream().forEach(Reloadable::extractConfigurationParameters);

		if (sender instanceof Player) {
			sender.sendMessage(plugin.getChatHeader() + plugin.getPluginLang()
					.getString("configuration-successfully-reloaded", "Configuration successfully reloaded."));
		}
		plugin.getLogger().info("Configuration successfully reloaded.");

	}

	/**
	 * Adds a new Reloadable object that will be notified when the plugin is reloaded.
	 * 
	 * @param reloadable
	 */
	public void registerReloadable(Reloadable reloadable) {
		reloadableObservers.add(reloadable);
	}
}
