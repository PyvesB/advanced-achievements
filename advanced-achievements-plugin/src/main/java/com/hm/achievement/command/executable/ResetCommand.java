package com.hm.achievement.command.executable;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.db.AbstractDatabaseManager;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.db.CachedStatistic;
import com.hm.achievement.lang.LangHelper;
import com.hm.achievement.lang.command.CmdLang;
import com.hm.achievement.utils.StringHelper;
import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Class in charge of handling the /aach reset command, which resets the statistics for a given player and achievement
 * category.
 * 
 * @author Pyves
 */
@Singleton
@CommandSpec(name = "reset", permission = "reset", minArgs = 3, maxArgs = 3)
public class ResetCommand extends AbstractParsableCommand {

	private final CacheManager cacheManager;
	private final AbstractDatabaseManager databaseManager;
	private final Set<String> enabledCategoriesWithSubcategories;

	private String langResetSuccessful;
	private String langCategoryDoesNotExist;

	@Inject
	public ResetCommand(@Named("main") CommentedYamlConfiguration mainConfig,
			@Named("lang") CommentedYamlConfiguration langConfig, StringBuilder pluginHeader, CacheManager cacheManager,
			AbstractDatabaseManager databaseManager, Set<String> enabledCategoriesWithSubcategories) {
		super(mainConfig, langConfig, pluginHeader);
		this.cacheManager = cacheManager;
		this.databaseManager = databaseManager;
		this.enabledCategoriesWithSubcategories = enabledCategoriesWithSubcategories;
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		langResetSuccessful = LangHelper.get(CmdLang.RESET_SUCCESSFUL, langConfig);
		langCategoryDoesNotExist = pluginHeader + LangHelper.get(CmdLang.CATEGORY_DOES_NOT_EXIST, langConfig);
	}

	@Override
	void onExecuteForPlayer(CommandSender sender, String[] args, Player player) {
		String uuid = player.getUniqueId().toString();

		if (enabledCategoriesWithSubcategories.contains(args[1])) {
			if (args[1].contains(".")) {
				MultipleAchievements category = MultipleAchievements.getByName(StringUtils.substringBefore(args[1], "."));
				String subcategory = StringUtils.substringAfter(args[1], ".");
				CachedStatistic statistic = cacheManager.getHashMap(category)
						.get(cacheManager.getMultipleCategoryCacheKey(player.getUniqueId(), subcategory));
				if (statistic == null) {
					cacheManager.getHashMap(category).put(cacheManager.getMultipleCategoryCacheKey(player.getUniqueId(),
							subcategory), new CachedStatistic(0L, false));
				} else {
					statistic.setValue(0L);
				}
				sender.sendMessage(pluginHeader + args[1] +
						StringUtils.replaceOnce(langResetSuccessful, "PLAYER", player.getName()));
			} else {
				NormalAchievements category = NormalAchievements.getByName(args[1]);
				if (category == NormalAchievements.CONNECTIONS) {
					// Not handled by a database cache.
					databaseManager.clearConnection(player.getUniqueId());
				} else {
					CachedStatistic statistic = cacheManager.getHashMap(category).get(uuid);
					if (statistic == null) {
						cacheManager.getHashMap(category).put(uuid, new CachedStatistic(0L, false));
					} else {
						statistic.setValue(0L);
					}
				}
				sender.sendMessage(pluginHeader + args[1] +
						StringUtils.replaceOnce(langResetSuccessful, "PLAYER", player.getName()));
			}
		} else {
			sender.sendMessage(StringUtils.replaceEach(langCategoryDoesNotExist, new String[] { "CAT", "CLOSEST_MATCH" },
					new String[] { args[1], StringHelper.getClosestMatch(args[1], enabledCategoriesWithSubcategories) }));
		}
	}
}
