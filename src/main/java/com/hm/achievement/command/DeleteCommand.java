package com.hm.achievement.command;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import com.hm.achievement.AdvancedAchievements;

public class DeleteCommand implements Listener {

	private AdvancedAchievements plugin;

	public DeleteCommand(AdvancedAchievements plugin) {

		this.plugin = plugin;
	}

	/**
	 * Delete an achievements from a player with an in game or console command.
	 */
	public void achievementDelete(CommandSender sender, String args[]) {

		// Parse command to separate achievement name and name of player.
		String achievementName = "";
		for (int i = 1; i < args.length - 1; i++) {
			if (i != args.length - 2)
				achievementName += args[i] + " ";
			else
				achievementName += args[i];
		}

		// Retrieve player instance with his name.
		Player player = null;
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
							.replaceAll("PLAYER", args[args.length - 1]));
			return;
		}

		if (!plugin.getDb().hasPlayerAchievement(player, achievementName))
			sender.sendMessage(plugin.getChatHeader() + plugin.getPluginLang()
					.getString("check-achievements-false", "PLAYER has not received the achievement ACH!")
					.replaceAll("PLAYER", args[args.length - 1]).replaceAll("ACH", achievementName));
		else {
			plugin.getDb().deletePlayerAchievement(player, achievementName);
			sender.sendMessage(plugin.getChatHeader() + plugin.getPluginLang()
					.getString("delete-achievements", "The achievement ACH was deleted from PLAYER.")
					.replaceAll("PLAYER", args[args.length - 1]).replaceAll("ACH", achievementName));
		}
	}
}
