package com.hm.achievement.command;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.hm.achievement.db.DatabaseCacheManager;
import com.hm.achievement.lang.Lang;
import com.hm.achievement.lang.command.CmdLang;
import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Class in charge of handling the /aach check command, which checks whether a player has received an achievement.
 * 
 * @author Pyves
 */
@Singleton
public class CheckCommand extends AbstractParsableCommand {

	private final DatabaseCacheManager databaseCacheManager;

	private String langCheckAchievementTrue;
	private String langCheckAchievementFalse;

	@Inject
	public CheckCommand(@Named("main") CommentedYamlConfiguration mainConfig,
			@Named("lang") CommentedYamlConfiguration langConfig, StringBuilder pluginHeader, ReloadCommand reloadCommand,
			DatabaseCacheManager databaseCacheManager) {
		super(mainConfig, langConfig, pluginHeader, reloadCommand);
		this.databaseCacheManager = databaseCacheManager;
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		langCheckAchievementTrue = pluginHeader + Lang.get(CmdLang.CHECK_ACHIEVEMENT_TRUE, langConfig);
		langCheckAchievementFalse = pluginHeader + Lang.get(CmdLang.CHECK_ACHIEVEMENTS_FALSE, langConfig);
	}

	@Override
	void executeSpecificActions(CommandSender sender, String[] args, Player player) {
		String achievementName = parseAchievementName(args);

		// Check if achievement exists in database and display message accordingly.
		if (databaseCacheManager.hasPlayerAchievement(player.getUniqueId(), achievementName)) {
			sender.sendMessage(StringUtils.replaceEach(langCheckAchievementTrue, new String[] { "PLAYER", "ACH" },
					new String[] { args[args.length - 1], achievementName }));
		} else {
			sender.sendMessage(StringUtils.replaceEach(langCheckAchievementFalse, new String[] { "PLAYER", "ACH" },
					new String[] { args[args.length - 1], achievementName }));
		}
	}
}
