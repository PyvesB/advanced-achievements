package com.hm.achievement.command.executable;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.hm.achievement.lang.LangHelper;
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

	AbstractCommand(CommentedYamlConfiguration mainConfig, CommentedYamlConfiguration langConfig,
			StringBuilder pluginHeader) {
		this.mainConfig = mainConfig;
		this.langConfig = langConfig;
		this.pluginHeader = pluginHeader;
	}

	@Override
	public void extractConfigurationParameters() {
		langNoPermissions = pluginHeader.toString() + LangHelper.get(CmdLang.NO_PERMISSIONS, langConfig);
	}

	/**
	 * Executes the command issued by the sender if he has the relevant permissions. If permission null, skip check.
	 *
	 * @param sender
	 * @param args
	 */
	public void execute(CommandSender sender, String[] args) {
		String permission = getClass().getAnnotation(CommandSpec.class).permission();
		if (!permission.isEmpty() && !sender.hasPermission("achievement." + permission)) {
			sender.sendMessage(langNoPermissions);
			return;
		}

		onExecute(sender, args);
	}

	/**
	 * Executes behaviour specific to the implementing command.
	 *
	 * @param sender
	 * @param args
	 */
	abstract void onExecute(CommandSender sender, String[] args);

	String translateColorCodes(String translate) {
		return ChatColor.translateAlternateColorCodes('&', translate);
	}
}
