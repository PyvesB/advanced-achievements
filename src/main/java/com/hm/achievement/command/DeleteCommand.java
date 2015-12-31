package com.hm.achievement.command;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.language.Lang;

public class DeleteCommand implements Listener {

	private AdvancedAchievements plugin;

	public DeleteCommand(AdvancedAchievements plugin) {

		this.plugin = plugin;
	}

	/**
	 * Check if a player has received an achievement with an in game or console
	 * command.
	 */
	public void achievementDelete(CommandSender sender, String args[]) {

		String achievementName = "";
		for (int i = 1; i < args.length - 1; i++) {
			if (i != args.length - 2)
				achievementName += args[i] + " ";
			else
				achievementName += args[i];
		}
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
					+ Lang.PLAYER_OFFLINE.toString().replaceAll("PLAYER", args[args.length - 1]));
			return;
		}

		if (! plugin.getDb().hasPlayerAchievement(player, achievementName))
			sender.sendMessage(plugin.getChatHeader() + Lang.CHECK_ACHIEVEMENT_FALSE.toString()
					.replaceAll("PLAYER", args[args.length - 1]).replaceAll("ACH", achievementName));
		else{
			plugin.getDb().deletePlayerAchievement(player, achievementName);
			sender.sendMessage(plugin.getChatHeader() + Lang.DELETE_ACHIEVEMENT.toString()
					.replaceAll("PLAYER", args[args.length - 1]).replaceAll("ACH", achievementName));
		}
	}
}

