package com.hm.achievement.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.hm.achievement.AdvancedAchievements;
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
		plugin.setSuccessfulLoad(true);

		CommentedYamlConfiguration config = plugin.loadAndBackupFile("config.yml");

		// Compare the DisabledCategories configuration list in the previous configuration file and the new one.
		Set<String> disabledCategorySet = plugin.extractDisabledCategories(config);
		if (!disabledCategorySet.equals(plugin.getDisabledCategorySet())) {
			if (sender instanceof Player) {
				sender.sendMessage(plugin.getChatHeader() + plugin.getPluginLang().getString("server-restart-reload",
						"DisabledCategories list was modified. Server must be fully reloaded or restarted for your changes to take effect."));
			}
			plugin.getLogger().warning(
					"DisabledCategories list was modified. Server must be fully reloaded or restarted for your changes to take effect.");
			plugin.getLogger().warning("Aborting plugin reload.");
			return;
		}

		plugin.setPluginConfig(config);
		plugin.setPluginLang(plugin.loadAndBackupFile(config.getString("LanguageFileName", "lang.yml")));
		plugin.setGui(plugin.loadAndBackupFile("gui.yml"));

		// Reload all observers.
		reloadableObservers.stream().forEach(Reloadable::extractConfigurationParameters);

		if (plugin.isSuccessfulLoad()) {
			if (sender instanceof Player) {
				sender.sendMessage(plugin.getChatHeader() + plugin.getPluginLang()
						.getString("configuration-successfully-reloaded", "Configuration successfully reloaded."));
			}
			plugin.getLogger().info("Configuration successfully reloaded.");
		} else {
			if (sender instanceof Player) {
				sender.sendMessage(
						plugin.getChatHeader() + plugin.getPluginLang().getString("configuration-reload-failed",
								"Errors while reloading configuration. Please view logs for more details."));
			}
			plugin.getLogger().severe("Errors while reloading configuration. Please view logs for more details.");
		}
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
