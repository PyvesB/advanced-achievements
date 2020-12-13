package com.hm.achievement.command.executable;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.hm.achievement.db.CacheManager;

/**
 * Class in charge of handling the /aach check command, which checks whether a player has received an achievement.
 * 
 * @author Pyves
 */
@Singleton
@CommandSpec(name = "check", permission = "check", minArgs = 3, maxArgs = Integer.MAX_VALUE)
public class CheckCommand extends AbstractParsableCommand {

	private final CacheManager cacheManager;

	private String langCheckAchievementTrue;
	private String langCheckAchievementFalse;

	@Inject
	public CheckCommand(@Named("main") YamlConfiguration mainConfig, @Named("lang") YamlConfiguration langConfig,
			StringBuilder pluginHeader, CacheManager cacheManager) {
		super(mainConfig, langConfig, pluginHeader);
		this.cacheManager = cacheManager;
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		langCheckAchievementTrue = pluginHeader + langConfig.getString("check-achievement-true");
		langCheckAchievementFalse = pluginHeader + langConfig.getString("check-achievements-false");
	}

	@Override
	void onExecuteForPlayer(CommandSender sender, String[] args, Player player) {
		String achievementName = parseAchievementName(args);

		// Check if achievement exists in database and display message accordingly.
		if (cacheManager.hasPlayerAchievement(player.getUniqueId(), achievementName)) {
			sender.sendMessage(StringUtils.replaceEach(langCheckAchievementTrue, new String[] { "PLAYER", "ACH" },
					new String[] { args[args.length - 1], achievementName }));
		} else {
			sender.sendMessage(StringUtils.replaceEach(langCheckAchievementFalse, new String[] { "PLAYER", "ACH" },
					new String[] { args[args.length - 1], achievementName }));
		}
	}
}
