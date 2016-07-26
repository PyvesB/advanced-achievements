package com.hm.achievement.command;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import com.hm.achievement.AdvancedAchievements;

/**
 * Class in charge of handling the /aach delete command, which deletes an achievement from a player.
 * 
 * @author Pyves
 */
public class DeleteCommand implements Listener {

	private AdvancedAchievements plugin;

	public DeleteCommand(AdvancedAchievements plugin) {

		this.plugin = plugin;
	}

	/**
	 * Delete an achievement from a player with an in game or console command.
	 * 
	 * @param sender
	 * @param args
	 */
	public void achievementDelete(CommandSender sender, String args[]) {

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

		// Check if achievement exists in database and display message accordingly; if received, delete it.
		if (!plugin.getDb().hasPlayerAchievement(player, achievementName)) {
			sender.sendMessage(plugin.getChatHeader() + plugin.getPluginLang()
					.getString("check-achievements-false", "PLAYER has not received the achievement ACH!")
					.replace("PLAYER", args[args.length - 1]).replace("ACH", achievementName));
		} else {
			plugin.getDb().deletePlayerAchievement(player, achievementName);
			sender.sendMessage(plugin.getChatHeader() + plugin.getPluginLang()
					.getString("delete-achievements", "The achievement ACH was deleted from PLAYER.")
					.replace("PLAYER", args[args.length - 1]).replace("ACH", achievementName));
		}
	}
}
