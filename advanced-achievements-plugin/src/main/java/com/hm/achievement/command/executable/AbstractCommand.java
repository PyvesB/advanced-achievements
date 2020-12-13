package com.hm.achievement.command.executable;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import com.hm.achievement.lifecycle.Reloadable;

/**
 * Abstract class in charge of factoring out common functionality for commands.
 *
 * @author Pyves
 */
public abstract class AbstractCommand implements Reloadable {

	final YamlConfiguration mainConfig;
	final YamlConfiguration langConfig;
	final StringBuilder pluginHeader;

	private String langNoPermissions;

	AbstractCommand(YamlConfiguration mainConfig, YamlConfiguration langConfig, StringBuilder pluginHeader) {
		this.mainConfig = mainConfig;
		this.langConfig = langConfig;
		this.pluginHeader = pluginHeader;
	}

	@Override
	public void extractConfigurationParameters() {
		langNoPermissions = pluginHeader.toString() + langConfig.getString("no-permissions");
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
