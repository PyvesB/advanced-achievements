package com.hm.achievement.command;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.db.AbstractSQLDatabaseManager;
import com.hm.achievement.db.CachedStatistic;
import com.hm.achievement.db.DatabaseCacheManager;
import com.hm.achievement.lang.Lang;
import com.hm.achievement.lang.command.CmdLang;
import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Class in charge of handling the /aach reset command, which resets the statistics for a given player and achievement
 * category.
 * 
 * @author Pyves
 */
@Singleton
public class ResetCommand extends AbstractParsableCommand {

	private final DatabaseCacheManager databaseCacheManager;
	private final AbstractSQLDatabaseManager sqlDatabaseManager;

	private String langResetSuccessful;
	private String langCategoryDoesNotExist;

	@Inject
	public ResetCommand(@Named("main") CommentedYamlConfiguration mainConfig,
			@Named("lang") CommentedYamlConfiguration langConfig, StringBuilder pluginHeader, ReloadCommand reloadCommand,
			DatabaseCacheManager databaseCacheManager, AbstractSQLDatabaseManager sqlDatabaseManager) {
		super(mainConfig, langConfig, pluginHeader, reloadCommand);
		this.databaseCacheManager = databaseCacheManager;
		this.sqlDatabaseManager = sqlDatabaseManager;
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		langResetSuccessful = Lang.get(CmdLang.RESET_SUCCESSFUL, langConfig);
		langCategoryDoesNotExist = pluginHeader + Lang.get(CmdLang.CATEGORY_DOES_NOT_EXIST, langConfig);
	}

	@Override
	void executeSpecificActions(CommandSender sender, String[] args, Player player) {
		String uuid = player.getUniqueId().toString();
		for (NormalAchievements category : NormalAchievements.values()) {
			if (category.toString().equalsIgnoreCase(args[1])) {
				if (category == NormalAchievements.CONNECTIONS) {
					// Not handled by a database cache.
					sqlDatabaseManager.clearConnection(player.getUniqueId());
				} else {
					CachedStatistic statistic = databaseCacheManager.getHashMap(category).get(uuid);
					if (statistic == null) {
						databaseCacheManager.getHashMap(category).put(uuid, new CachedStatistic(0L, false));
					} else {
						statistic.setValue(0L);
					}
				}
				sender.sendMessage(
						pluginHeader + args[1] + StringUtils.replaceOnce(langResetSuccessful, "PLAYER", player.getName()));
				return;
			}
		}

		for (MultipleAchievements category : MultipleAchievements.values()) {
			for (String subcategory : mainConfig.getShallowKeys(category.toString())) {
				String categoryPath = category.toString() + "." + StringUtils.deleteWhitespace(subcategory);
				if (categoryPath.equalsIgnoreCase(args[1])) {
					CachedStatistic statistic = databaseCacheManager.getHashMap(category).get(
							databaseCacheManager.getMultipleCategoryCacheKey(category, player.getUniqueId(), subcategory));
					if (statistic == null) {
						databaseCacheManager.getHashMap(category).put(databaseCacheManager.getMultipleCategoryCacheKey(
								category, player.getUniqueId(), subcategory), new CachedStatistic(0L, false));
					} else {
						statistic.setValue(0L);
					}
					sender.sendMessage(pluginHeader + args[1]
							+ StringUtils.replaceOnce(langResetSuccessful, "PLAYER", player.getName()));
					return;
				}
			}
		}

		sender.sendMessage(StringUtils.replaceOnce(langCategoryDoesNotExist, "CAT", args[1]));
	}
}
