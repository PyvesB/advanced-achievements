package com.hm.achievement.command;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.hm.achievement.lang.Lang;
import com.hm.achievement.lang.command.CmdLang;
import com.hm.achievement.lifecycle.Reloadable;
import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Abstract class in charge of factoring out common functionality for commands.
 *
 * @author Pyves
 */
public abstract class AbstractCommand implements Reloadable {

	final CommentedYamlConfiguration mainConfig;
	final CommentedYamlConfiguration langConfig;
	final StringBuilder pluginHeader;

	private String langNoPermissions;

	AbstractCommand(CommentedYamlConfiguration mainConfig, CommentedYamlConfiguration langConfig, StringBuilder pluginHeader,
			ReloadCommand reloadCommand) {
		this.mainConfig = mainConfig;
		this.langConfig = langConfig;
		this.pluginHeader = pluginHeader;
		reloadCommand.addObserver(this);
	}

	AbstractCommand(CommentedYamlConfiguration mainConfig, CommentedYamlConfiguration langConfig,
			StringBuilder pluginHeader) {
		this.mainConfig = mainConfig;
		this.langConfig = langConfig;
		this.pluginHeader = pluginHeader;
	}

	@Override
	public void extractConfigurationParameters() {
		langNoPermissions = pluginHeader.toString() + Lang.get(CmdLang.NO_PERMISSIONS, langConfig);
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
			sender.sendMessage(langNoPermissions);
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
	abstract void executeCommand(CommandSender sender, String[] args);

	String translateColorCodes(String translate) {
		return ChatColor.translateAlternateColorCodes('&', translate);
	}
}
