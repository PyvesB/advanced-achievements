package com.hm.achievement.command.executable;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.lang.LangHelper;
import com.hm.achievement.lang.command.CmdLang;
import com.hm.achievement.utils.StatisticIncreaseHandler;
import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Class in charge of increase a statistic of an achievement by command.
 *
 * @author Phoetrix
 */
@Singleton
@CommandSpec(name = "add", permission = "add", minArgs = 4, maxArgs = 4)
public class AddCommand extends AbstractParsableCommand {

	private final CacheManager cacheManager;
	private final StatisticIncreaseHandler statisticIncreaseHandler;

	private String langErrorValue;
	private String langStatisticIncreased;
	private String langCategoryDoesNotExist;

	@Inject
	public AddCommand(@Named("main") CommentedYamlConfiguration mainConfig,
			@Named("lang") CommentedYamlConfiguration langConfig, StringBuilder pluginHeader, CacheManager cacheManager,
			StatisticIncreaseHandler statisticIncreaseHandler) {
		super(mainConfig, langConfig, pluginHeader);
		this.cacheManager = cacheManager;
		this.statisticIncreaseHandler = statisticIncreaseHandler;
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		langErrorValue = pluginHeader + LangHelper.get(CmdLang.ERROR_VALUE, langConfig);
		langStatisticIncreased = pluginHeader + LangHelper.get(CmdLang.STATISTIC_INCREASED, langConfig);
		langCategoryDoesNotExist = pluginHeader + LangHelper.get(CmdLang.CATEGORY_DOES_NOT_EXIST, langConfig);
	}

	@Override
	void onExecuteForPlayer(CommandSender sender, String[] args, Player player) {
		int value;

		if (NumberUtils.isDigits(args[1])) {
			value = Integer.parseInt(args[1]);
		} else {
			sender.sendMessage(StringUtils.replaceOnce(langErrorValue, "VALUE", args[1]));
			return;
		}

		for (NormalAchievements category : NormalAchievements.values()) {
			String categoryName = category.toString();

			if (args[2].equalsIgnoreCase(categoryName) && category != NormalAchievements.CONNECTIONS) {
				long amount = cacheManager.getAndIncrementStatisticAmount(category, player.getUniqueId(), value);
				statisticIncreaseHandler.checkThresholdsAndAchievements(player, category.toString(), amount);
				sender.sendMessage(StringUtils.replaceEach(langStatisticIncreased,
						new String[] { "ACH", "AMOUNT", "PLAYER" }, new String[] { args[2], args[1], args[3] }));
				return;
			}
		}
		for (MultipleAchievements category : MultipleAchievements.values()) {
			String categoryName = category.toString();

			for (String subcategory : mainConfig.getShallowKeys(categoryName)) {
				String categoryPath = categoryName + "." + StringUtils.deleteWhitespace(subcategory);

				if (args[2].equalsIgnoreCase(categoryPath)) {
					long amount = cacheManager.getAndIncrementStatisticAmount(category, subcategory, player.getUniqueId(),
							value);
					statisticIncreaseHandler.checkThresholdsAndAchievements(player, category + "." + subcategory, amount);
					sender.sendMessage(StringUtils.replaceEach(langStatisticIncreased,
							new String[] { "ACH", "AMOUNT", "PLAYER" }, new String[] { args[2], args[1], args[3] }));
					return;
				}
			}
		}

		sender.sendMessage(StringUtils.replaceOnce(langCategoryDoesNotExist, "CAT", args[2]));
	}
}
