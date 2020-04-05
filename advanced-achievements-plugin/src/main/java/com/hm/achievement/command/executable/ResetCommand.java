package com.hm.achievement.command.executable;

import java.util.Collections;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.hm.achievement.db.CacheManager;
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

	public static final String WILDCARD = "*";

	private final CacheManager cacheManager;
	private final Set<String> enabledCategoriesWithSubcategories;

	private String langResetSuccessful;
	private String langResetAllSuccessful;
	private String langCategoryDoesNotExist;

	@Inject
	public ResetCommand(@Named("main") CommentedYamlConfiguration mainConfig,
			@Named("lang") CommentedYamlConfiguration langConfig, StringBuilder pluginHeader, CacheManager cacheManager,
			Set<String> enabledCategoriesWithSubcategories) {
		super(mainConfig, langConfig, pluginHeader);
		this.cacheManager = cacheManager;
		this.enabledCategoriesWithSubcategories = enabledCategoriesWithSubcategories;
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		langResetSuccessful = pluginHeader + LangHelper.get(CmdLang.RESET_SUCCESSFUL, langConfig);
		langResetAllSuccessful = pluginHeader + LangHelper.get(CmdLang.RESET_ALL_SUCCESSFUL, langConfig);
		langCategoryDoesNotExist = pluginHeader + LangHelper.get(CmdLang.CATEGORY_DOES_NOT_EXIST, langConfig);
	}

	@Override
	void onExecuteForPlayer(CommandSender sender, String[] args, Player player) {
		String categoryWithSubcategory = args[1];
		if (WILDCARD.equals(categoryWithSubcategory)) {
			cacheManager.resetPlayerStatistics(player.getUniqueId(), enabledCategoriesWithSubcategories);
			sender.sendMessage(StringUtils.replace(langResetAllSuccessful, "PLAYER", player.getName()));
		} else if (enabledCategoriesWithSubcategories.contains(categoryWithSubcategory)) {
			cacheManager.resetPlayerStatistics(player.getUniqueId(), Collections.singletonList(categoryWithSubcategory));
			sender.sendMessage(StringUtils.replaceEach(langResetSuccessful, new String[] { "CAT", "PLAYER" },
					new String[] { categoryWithSubcategory, player.getName() }));
		} else {
			sender.sendMessage(StringUtils.replaceEach(langCategoryDoesNotExist, new String[] { "CAT", "CLOSEST_MATCH" },
					new String[] { categoryWithSubcategory,
							StringHelper.getClosestMatch(categoryWithSubcategory, enabledCategoriesWithSubcategories) }));
		}
	}
}
