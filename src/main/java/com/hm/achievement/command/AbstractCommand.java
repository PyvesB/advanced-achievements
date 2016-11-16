package com.hm.achievement.command;

import org.bukkit.command.CommandSender;

import com.hm.achievement.AdvancedAchievements;

/**
 * Abstract class in charge of factoring out common functionality for commands.
 * 
 * @author Pyves
 */
public abstract class AbstractCommand {

	protected AdvancedAchievements plugin;

	protected AbstractCommand(AdvancedAchievements plugin) {

		this.plugin = plugin;
	}

	/**
	 * Executes the command issued by the sender if he has the relevant permissions. If permission null, skip check.
	 * 
	 * @param sender
	 * @param args
	 * @param permission
	 */
	public void executeCommand(CommandSender sender, String[] args, String permission) {

		if (permission != null && !sender.hasPermission("achievement." + permission)) {
			sender.sendMessage(plugin.getChatHeader()
					+ plugin.getPluginLang().getString("no-permissions", "You do not have the permission to do this."));
			return;
		}

		executeCommand(sender, args);
	}

	/**
	 * Executes the command issued by the sender.
	 * 
	 * @param sender
	 * @param args
	 */
	protected abstract void executeCommand(CommandSender sender, String[] args);

}
