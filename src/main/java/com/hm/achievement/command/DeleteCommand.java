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

	private String langCheckAchievementFalse;
	private String langDeleteAchievements;

	public DeleteCommand(AdvancedAchievements plugin) {
		super(plugin);
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		langCheckAchievementFalse = plugin.getChatHeader() + plugin.getPluginLang()
				.getString("check-achievements-false", "PLAYER has not received the achievement ACH!");
		langDeleteAchievements = plugin.getChatHeader() + plugin.getPluginLang().getString("delete-achievements",
				"The achievement ACH was deleted from PLAYER.");
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
