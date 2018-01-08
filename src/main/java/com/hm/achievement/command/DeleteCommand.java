package com.hm.achievement.command;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.lang.Lang;
import com.hm.achievement.lang.command.CmdLang;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Class in charge of handling the /aach delete command, which deletes an achievement from a player.
 * 
 * @author Pyves
 */
public class DeleteCommand extends AbstractParsableCommand {

	private String langCheckAchievementFalse;
	private String langDeleteAchievements;

	public DeleteCommand(AdvancedAchievements plugin) {
		super(plugin);
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		langCheckAchievementFalse = Lang.getWithChatHeader(CmdLang.CHECK_ACHIEVEMENT_FALSE, plugin);
		langDeleteAchievements = Lang.getWithChatHeader(CmdLang.DELETE_ACHIEVEMENTS, plugin);
	}

	@Override
	protected void executeSpecificActions(CommandSender sender, String[] args, Player player) {
		String achievementName = parseAchievementName(args);

		// Check if achievement exists in database and display message accordingly; if received, delete it.
		if (!plugin.getCacheManager().hasPlayerAchievement(player.getUniqueId(), achievementName)) {
			sender.sendMessage(StringUtils.replaceEach(langCheckAchievementFalse, new String[] { "PLAYER", "ACH" },
					new String[] { args[args.length - 1], achievementName }));
		} else {
			String uuid = player.getUniqueId().toString();
			plugin.getCacheManager().getReceivedAchievementsCache().remove(uuid, achievementName);
			plugin.getCacheManager().getNotReceivedAchievementsCache().put(uuid, achievementName);
			plugin.getCacheManager().getTotalPlayerAchievementsCache().put(uuid,
					plugin.getCacheManager().getPlayerTotalAchievements(player.getUniqueId()) - 1);
			plugin.getDatabaseManager().deletePlayerAchievement(player.getUniqueId(), achievementName);

			sender.sendMessage(StringUtils.replaceEach(langDeleteAchievements, new String[] { "PLAYER", "ACH" },
					new String[] { args[args.length - 1], achievementName }));
		}
	}
}
