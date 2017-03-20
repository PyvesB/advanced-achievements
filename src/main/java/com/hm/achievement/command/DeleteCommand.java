package com.hm.achievement.command;

import org.apache.commons.lang.StringUtils;
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
		if (!plugin.getPoolsManager().hasPlayerAchievement(player, achievementName)) {
			sender.sendMessage(plugin.getChatHeader() + StringUtils.replaceEach(
					plugin.getPluginLang().getString("check-achievements-false",
							"PLAYER has not received the achievement ACH!"),
					new String[] { "PLAYER", "ACH" }, new String[] { args[args.length - 1], achievementName }));
		} else {
			plugin.getDb().deletePlayerAchievement(player, achievementName);
			String uuid = player.getUniqueId().toString();
			plugin.getPoolsManager().getReceivedAchievementsCache().remove(uuid, achievementName);
			plugin.getPoolsManager().getNotReceivedAchievementsCache().put(uuid, achievementName);
			sender.sendMessage(plugin.getChatHeader() + StringUtils.replaceEach(
					plugin.getPluginLang().getString("delete-achievements",
							"The achievement ACH was deleted from PLAYER."),
					new String[] { "PLAYER", "ACH" }, new String[] { args[args.length - 1], achievementName }));
		}
	}
}
