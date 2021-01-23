package com.hm.achievement.command.executable;

import java.util.Collections;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.hm.achievement.config.AchievementMap;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.utils.StringHelper;

/**
 * Class in charge of handling the /aach reset command, which resets the statistics for a given player and achievement
 * category.
 * 
 * @author Pyves
 */
@Singleton
@CommandSpec(name = "reset", permission = "reset", minArgs = 3, maxArgs = 3)
public class ResetCommand extends AbstractParsableCommand {

	public static final String WILDCARD = "*";

	private final CacheManager cacheManager;
	private final AchievementMap achievementMap;

	private String langResetSuccessful;
	private String langResetAllSuccessful;
	private String langCategoryDoesNotExist;

	@Inject
	public ResetCommand(@Named("main") YamlConfiguration mainConfig, @Named("lang") YamlConfiguration langConfig,
			StringBuilder pluginHeader, CacheManager cacheManager, AchievementMap achievementMap) {
		super(mainConfig, langConfig, pluginHeader);
		this.cacheManager = cacheManager;
		this.achievementMap = achievementMap;
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		langResetSuccessful = pluginHeader + langConfig.getString("reset-successful");
		langResetAllSuccessful = pluginHeader + langConfig.getString("reset-all-successful");
		langCategoryDoesNotExist = pluginHeader + langConfig.getString("category-does-not-exist");
	}

	@Override
	void onExecuteForPlayer(CommandSender sender, String[] args, Player player) {
		String categoryWithSubcategory = args[1];
		Set<String> categorySubcategories = achievementMap.getCategorySubcategories();
		if (WILDCARD.equals(categoryWithSubcategory)) {
			cacheManager.resetPlayerStatistics(player.getUniqueId(), categorySubcategories);
			sender.sendMessage(StringUtils.replace(langResetAllSuccessful, "PLAYER", player.getName()));
		} else if (categorySubcategories.contains(categoryWithSubcategory)) {
			cacheManager.resetPlayerStatistics(player.getUniqueId(), Collections.singletonList(categoryWithSubcategory));
			sender.sendMessage(StringUtils.replaceEach(langResetSuccessful, new String[] { "CAT", "PLAYER" },
					new String[] { categoryWithSubcategory, player.getName() }));
		} else {
			sender.sendMessage(StringUtils.replaceEach(langCategoryDoesNotExist, new String[] { "CAT", "CLOSEST_MATCH" },
					new String[] { categoryWithSubcategory,
							StringHelper.getClosestMatch(categoryWithSubcategory, categorySubcategories) }));
		}
	}
}
