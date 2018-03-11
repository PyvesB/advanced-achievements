package com.hm.achievement.command;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.hm.achievement.db.AbstractSQLDatabaseManager;
import com.hm.achievement.db.DatabaseCacheManager;
import com.hm.achievement.lang.Lang;
import com.hm.achievement.lang.command.CmdLang;
import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Class in charge of handling the /aach delete command, which deletes an achievement from a player.
 * 
 * @author Pyves
 */
@Singleton
public class DeleteCommand extends AbstractParsableCommand {

	private final DatabaseCacheManager databaseCacheManager;
	private final AbstractSQLDatabaseManager sqlDatabaseManager;

	private String langCheckAchievementFalse;
	private String langDeleteAchievements;

	@Inject
	public DeleteCommand(@Named("main") CommentedYamlConfiguration mainConfig,
			@Named("lang") CommentedYamlConfiguration langConfig, StringBuilder pluginHeader, ReloadCommand reloadCommand,
			DatabaseCacheManager databaseCacheManager, AbstractSQLDatabaseManager sqlDatabaseManager) {
		super(mainConfig, langConfig, pluginHeader, reloadCommand);
		this.databaseCacheManager = databaseCacheManager;
		this.sqlDatabaseManager = sqlDatabaseManager;
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		langCheckAchievementFalse = pluginHeader + Lang.get(CmdLang.CHECK_ACHIEVEMENTS_FALSE, langConfig);
		langDeleteAchievements = pluginHeader + Lang.get(CmdLang.DELETE_ACHIEVEMENTS, langConfig);
	}

	@Override
	void executeSpecificActions(CommandSender sender, String[] args, Player player) {
		String achievementName = parseAchievementName(args);

		// Check if achievement exists in database and display message accordingly; if received, delete it.
		if (!databaseCacheManager.hasPlayerAchievement(player.getUniqueId(), achievementName)) {
			sender.sendMessage(StringUtils.replaceEach(langCheckAchievementFalse, new String[] { "PLAYER", "ACH" },
					new String[] { args[args.length - 1], achievementName }));
		} else {
			String uuid = player.getUniqueId().toString();
			databaseCacheManager.getReceivedAchievementsCache().remove(uuid, achievementName);
			databaseCacheManager.getNotReceivedAchievementsCache().put(uuid, achievementName);
			databaseCacheManager.getTotalPlayerAchievementsCache().put(uuid,
					databaseCacheManager.getPlayerTotalAchievements(player.getUniqueId()) - 1);
			sqlDatabaseManager.deletePlayerAchievement(player.getUniqueId(), achievementName);

			sender.sendMessage(StringUtils.replaceEach(langDeleteAchievements, new String[] { "PLAYER", "ACH" },
					new String[] { args[args.length - 1], achievementName }));
		}
	}
}
