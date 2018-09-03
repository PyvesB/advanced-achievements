package com.hm.achievement.command.executable;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.hm.achievement.lang.LangHelper;
import com.hm.achievement.lang.command.CmdLang;
import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Abstract class in charge of factoring out common functionality for commands with more than one argument (/aach give,
 * delete and check).
 *
 * @author Pyves
 */
public abstract class AbstractParsableCommand extends AbstractCommand {

	private String langPlayerOffline;

	AbstractParsableCommand(CommentedYamlConfiguration mainConfig, CommentedYamlConfiguration langConfig,
			StringBuilder pluginHeader) {
		super(mainConfig, langConfig, pluginHeader);
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		langPlayerOffline = LangHelper.get(CmdLang.PLAYER_OFFLINE, langConfig);
	}

	/**
	 * Executes actions specific to the class extending this abstract class.
	 *
	 * @param sender
	 * @param args
	 * @param player
	 */
	abstract void onExecuteForPlayer(CommandSender sender, String[] args, Player player);

	@Override
	void onExecute(CommandSender sender, String[] args) {
		String searchedName = args[args.length - 1];
		Player player = Bukkit.getOnlinePlayers().stream().filter(p -> p.getName().equalsIgnoreCase(searchedName))
				.findFirst().orElse(null);

		// If player not found or is offline.
		if (player == null) {
			sender.sendMessage(pluginHeader + StringUtils.replaceOnce(langPlayerOffline, "PLAYER", searchedName));
			return;
		}

		onExecuteForPlayer(sender, args, player);
	}
}
