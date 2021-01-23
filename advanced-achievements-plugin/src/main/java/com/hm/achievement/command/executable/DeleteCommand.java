package com.hm.achievement.command.executable;

import java.util.Collections;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.hm.achievement.config.AchievementMap;
import com.hm.achievement.db.AbstractDatabaseManager;
import com.hm.achievement.db.CacheManager;

/**
 * Class in charge of handling the /aach delete command, which deletes an achievement from a player.
 * 
 * @author Pyves
 */
@Singleton
@CommandSpec(name = "delete", permission = "delete", minArgs = 3, maxArgs = Integer.MAX_VALUE)
public class DeleteCommand extends AbstractParsableCommand {

	public static final String WILDCARD = "*";

	private final CacheManager cacheManager;
	private final AbstractDatabaseManager databaseManager;
	private final AchievementMap achievementMap;

	private String langCheckAchievementFalse;
	private String langDeleteAchievements;
	private String langAllDeleteAchievements;

	@Inject
	public DeleteCommand(@Named("main") YamlConfiguration mainConfig, @Named("lang") YamlConfiguration langConfig,
			StringBuilder pluginHeader, CacheManager cacheManager, AbstractDatabaseManager databaseManager,
			AchievementMap achievementMap) {
		super(mainConfig, langConfig, pluginHeader);
		this.cacheManager = cacheManager;
		this.databaseManager = databaseManager;
		this.achievementMap = achievementMap;
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		langCheckAchievementFalse = pluginHeader + langConfig.getString("check-achievements-false");
		langDeleteAchievements = pluginHeader + langConfig.getString("delete-achievements");
		langAllDeleteAchievements = pluginHeader + langConfig.getString("delete-all-achievements");
	}

	@Override
	void onExecuteForPlayer(CommandSender sender, String[] args, Player player) {
		String achievementName = parseAchievementName(args);

		if (WILDCARD.equals(achievementName)) {
			cacheManager.removePreviouslyReceivedAchievements(player.getUniqueId(), achievementMap.getAllNames());
			databaseManager.deleteAllPlayerAchievements(player.getUniqueId());
			sender.sendMessage(StringUtils.replace(langAllDeleteAchievements, "PLAYER", args[args.length - 1]));
		} else if (cacheManager.hasPlayerAchievement(player.getUniqueId(), achievementName)) {
			cacheManager.removePreviouslyReceivedAchievements(player.getUniqueId(),
					Collections.singletonList(achievementName));
			databaseManager.deletePlayerAchievement(player.getUniqueId(), achievementName);
			sender.sendMessage(StringUtils.replaceEach(langDeleteAchievements, new String[] { "PLAYER", "ACH" },
					new String[] { args[args.length - 1], achievementName }));
		} else {
			sender.sendMessage(StringUtils.replaceEach(langCheckAchievementFalse, new String[] { "PLAYER", "ACH" },
					new String[] { args[args.length - 1], achievementName }));
		}
	}
}
