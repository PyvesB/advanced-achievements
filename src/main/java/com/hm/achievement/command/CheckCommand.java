package com.hm.achievement.command;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.hm.achievement.AdvancedAchievements;

/**
 * Class in charge of handling the /aach check command, which checks whether a player has received an achievement.
 * 
 * @author Pyves
 */
public class CheckCommand extends AbstractParsableCommand {

	private String langCheckAchievementTrue;
	private String langCheckAchievementFalse;

	public CheckCommand(AdvancedAchievements plugin) {
		super(plugin);
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		langCheckAchievementTrue = plugin.getChatHeader() + plugin.getPluginLang().getString("check-achievement-true",
				"PLAYER has received the achievement ACH!");
		langCheckAchievementFalse = plugin.getChatHeader() + plugin.getPluginLang()
				.getString("check-achievements-false", "PLAYER has not received the achievement ACH!");
	}

	@Override
	protected void executeSpecificActions(CommandSender sender, String[] args, Player player) {
		String achievementName = parseAchievementName(args);

		// Check if achievement exists in database and display message accordingly.
		if (plugin.getCacheManager().hasPlayerAchievement(player.getUniqueId(), achievementName)) {
			sender.sendMessage(StringUtils.replaceEach(langCheckAchievementTrue, new String[] { "PLAYER", "ACH" },
					new String[] { args[args.length - 1], achievementName }));
		} else {
			sender.sendMessage(StringUtils.replaceEach(langCheckAchievementFalse, new String[] { "PLAYER", "ACH" },
					new String[] { args[args.length - 1], achievementName }));
		}
	}
}
