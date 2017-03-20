package com.hm.achievement.command;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.hm.achievement.AdvancedAchievements;

/**
 * Class in charge of handling the /aach check command, which checks whether a player has received an achievement.
 * 
 * @author Pyves
 */
public class CheckCommand extends AbstractParsableCommand {

	public CheckCommand(AdvancedAchievements plugin) {
		super(plugin);
	}

	@Override
	protected void executeSpecificActions(CommandSender sender, String[] args, Player player) {
		String achievementName = parseAchievementName(args);

		// Check if achievement exists in database and display message accordingly.
		if (plugin.getPoolsManager().hasPlayerAchievement(player, achievementName)) {
			sender.sendMessage(plugin.getChatHeader() + StringUtils.replaceEach(
					plugin.getPluginLang().getString("check-achievement-true",
							"PLAYER has received the achievement ACH!"),
					new String[] { "PLAYER", "ACH" }, new String[] { args[args.length - 1], achievementName }));
		} else {
			sender.sendMessage(plugin.getChatHeader() + StringUtils.replaceEach(
					plugin.getPluginLang().getString("check-achievements-false",
							"PLAYER has not received the achievement ACH!"),
					new String[] { "PLAYER", "ACH" }, new String[] { args[args.length - 1], achievementName }));
		}
	}
}
