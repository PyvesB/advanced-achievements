package com.hm.achievement.command;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import com.hm.achievement.AdvancedAchievements;

/**
 * Class in charge of handling the /aach check command, which checks whether a player has received an achievement.
 * 
 * @author Pyves
 */
public class CheckCommand implements Listener {

	private AdvancedAchievements plugin;

	public CheckCommand(AdvancedAchievements plugin) {

		this.plugin = plugin;
	}

	/**
	 * Check if a player has received an achievement with an in game or console command.
	 * 
	 * @param sender
	 * @param args
	 */
	public void achievementCheck(CommandSender sender, String args[]) {

		String achievementName = "";
		// Rebuild name of achievement by concatenating elements in the string array. The name of the player is last.
		for (int i = 1; i < args.length - 1; i++) {
			if (i != args.length - 2)
				achievementName += args[i] + " ";
			else
				achievementName += args[i];
		}

		Player player = null;
		// Retrieve player instance with his name.
		for (Player currentPlayer : Bukkit.getOnlinePlayers()) {
			if (currentPlayer.getName().equalsIgnoreCase(args[args.length - 1])) {
				player = currentPlayer;
				break;
			}
		}

		// If player not found or is offline.
		if (player == null) {
			sender.sendMessage(plugin.getChatHeader()
					+ plugin.getPluginLang().getString("player-offline", "The player PLAYER is offline!")
							.replace("PLAYER", args[args.length - 1]));
			return;
		}

		// Check if achievement exists in database and display message accordingly.
		if (plugin.getDb().hasPlayerAchievement(player, achievementName))
			sender.sendMessage(plugin.getChatHeader() + plugin.getPluginLang()
					.getString("check-achievement-true", "PLAYER has received the achievement ACH!")
					.replace("PLAYER", args[args.length - 1]).replace("ACH", achievementName));
		else
			sender.sendMessage(plugin.getChatHeader() + plugin.getPluginLang()
					.getString("check-achievements-false", "PLAYER has not received the achievement ACH!")
					.replace("PLAYER", args[args.length - 1]).replace("ACH", achievementName));
	}
}
