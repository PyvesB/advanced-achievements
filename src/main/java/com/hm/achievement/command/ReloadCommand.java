package com.hm.achievement.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.hm.achievement.AdvancedAchievements;

/**
 * Class in charge of handling the /aach reload command, which reloads the plugin's configuration files.
 * 
 * @author Pyves
 */
public class ReloadCommand extends AbstractCommand {

	public ReloadCommand(AdvancedAchievements plugin) {

		super(plugin);
	}

	@Override
	protected void executeCommand(CommandSender sender, String[] args) {

		if (!sender.hasPermission("achievement.reload")) {
			sender.sendMessage(plugin.getChatHeader()
					+ plugin.getPluginLang().getString("no-permissions", "You do not have the permission to do this."));
			return;
		}

		plugin.reloadConfig();
		plugin.configurationLoad(false);
		if (plugin.isSuccessfulLoad()) {
			if (sender instanceof Player)
				sender.sendMessage(plugin.getChatHeader() + plugin.getPluginLang()
						.getString("configuration-successfully-reloaded", "Configuration successfully reloaded."));
			plugin.getLogger().info("Configuration successfully reloaded.");
		} else {
			if (sender instanceof Player)
				sender.sendMessage(
						plugin.getChatHeader() + plugin.getPluginLang().getString("configuration-reload-failed",
								"Errors while reloading configuration. Please view logs for more details."));
			plugin.getLogger().severe("Errors while reloading configuration. Please view logs for more details.");
		}
	}

}
