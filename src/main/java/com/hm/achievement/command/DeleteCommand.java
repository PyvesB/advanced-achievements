package com.hm.achievement.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.hm.achievement.AdvancedAchievements;

/**
 * Class in charge of handling the /aach delete command, which deletes an achievement from a player.
 * 
 * @author Pyves
 */
public class DeleteCommand extends AbstractParsableCommand {

	public DeleteCommand(AdvancedAchievements plugin) {

		super(plugin);
	}

	@Override
	protected void executeSpecificActions(CommandSender sender, String[] args, Player player) {

		String achievementName = parseAchievementName(args);

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
