package com.hm.achievement.command.executable;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.hm.achievement.command.external.CommandUtils;

/**
 * Abstract class in charge of factoring out common functionality for commands with more than one argument (/aach give,
 * delete and check).
 *
 * @author Pyves
 */
public abstract class AbstractParsableCommand extends AbstractCommand {

	private String langPlayerOffline;
	private String langEntityNotPlayer;

	AbstractParsableCommand(YamlConfiguration mainConfig, YamlConfiguration langConfig, StringBuilder pluginHeader) {
		super(mainConfig, langConfig, pluginHeader);
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		langPlayerOffline = langConfig.getString("player-offline");
		langEntityNotPlayer = langConfig.getString("not-a-player");
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
		Entity[] entities = CommandUtils.getTargets(sender, searchedName);
		if (entities == null) {
			sender.sendMessage(pluginHeader + langEntityNotPlayer);
			return;
		}
		for (Entity entity : entities) {
			if (entity == null) {
				sender.sendMessage(pluginHeader + StringUtils.replaceOnce(langPlayerOffline, "PLAYER", searchedName));
				break;
			}
			if (entity instanceof Player) {
				onExecuteForPlayer(sender, args, (Player) entity);
			} else {
				sender.sendMessage(pluginHeader + StringUtils.replaceOnce(langEntityNotPlayer, "ENTITY", entity.getName()));
			}
		}
	}

	/**
	 * Extracts the name of the achievement from the command line arguments.
	 *
	 * @param args
	 * @return the achievement name
	 */
	String parseAchievementName(String[] args) {
		StringBuilder achievementName = new StringBuilder();
		// Rebuild name of achievement by concatenating elements in the string array. The name of the player is last.
		for (int i = 1; i < args.length - 1; i++) {
			achievementName.append(args[i]);
			if (i != args.length - 2) {
				achievementName.append(' ');
			}
		}
		return achievementName.toString();
	}
}
