package com.hm.achievement.command;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.lang.Lang;
import com.hm.achievement.lang.command.CmdLang;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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

		langCheckAchievementTrue = Lang.getWithChatHeader(CmdLang.CHECK_ACHIEVEMENT_TRUE, plugin);
		langCheckAchievementFalse = Lang.getWithChatHeader(CmdLang.CHECK_ACHIEVEMENT_FALSE, plugin);
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
