package com.hm.achievement.command;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.hm.achievement.AdvancedAchievements;

/**
 * Abstract class in charge of factoring out common functionality for commands with more than one argument (/aach give,
 * delete and check).
 * 
 * @author Pyves
 */
public abstract class AbstractParsableCommand extends AbstractCommand {

	private String langPlayerOffline;

	protected AbstractParsableCommand(AdvancedAchievements plugin) {
		super(plugin);
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		langPlayerOffline = plugin.getPluginLang().getString("player-offline", "The player PLAYER is offline!");
	}

	/**
	 * Executes actions specific to the class extending this abstract class.
	 * 
	 * @param sender
	 * @param args
	 * @param player
	 */
	protected abstract void executeSpecificActions(CommandSender sender, String[] args, Player player);

	@Override
	protected void executeCommand(CommandSender sender, String[] args) {
		String searchedName = args[args.length - 1];
		Player player = Bukkit.getOnlinePlayers().stream()
				.filter(p -> p.getName().equalsIgnoreCase(searchedName))
				.findFirst().orElse(null);

		// If player not found or is offline.
		if (player == null) {
			sender.sendMessage(plugin.getChatHeader()
					+ StringUtils.replaceOnce(langPlayerOffline, "PLAYER", searchedName));
			return;
		}

		executeSpecificActions(sender, args, player);
	}

	/**
	 * Extracts the name of the achievement from the command line arguments.
	 * 
	 * @param args
	 * @return the achievement name
	 */
	protected String parseAchievementName(String[] args) {
		StringBuilder achievementName = new StringBuilder();
		// Rebuild name of achievement by concatenating elements in the string array. The name of the player is last.
		for (int i = 1; i < args.length - 1; i++) {
			achievementName.append(args[i]);
			if (i != args.length - 2) {
				achievementName.append(' ');
			}
		}
		return StringUtils.replace(achievementName.toString(), "\u2423", " ");
	}
}
