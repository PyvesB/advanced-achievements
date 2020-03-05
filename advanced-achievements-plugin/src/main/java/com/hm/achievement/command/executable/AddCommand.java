package com.hm.achievement.command.executable;

import java.util.Set;
import java.util.concurrent.TimeUnit;

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
import com.hm.achievement.utils.StringHelper;
import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Class in charge of increase a statistic of an achievement by command.
 *
 * @author Phoetrix
 */
@Singleton
@CommandSpec(name = "add", permission = "add", minArgs = 4, maxArgs = 4)
public class AddCommand extends AbstractParsableCommand {

	private static final long MILLIS_PER_HOUR = TimeUnit.HOURS.toMillis(1);

	private final CacheManager cacheManager;
	private final StatisticIncreaseHandler statisticIncreaseHandler;

	private String langErrorValue;
	private String langStatisticIncreased;
	private String langCategoryDoesNotExist;
	private final Set<String> enabledCategoriesWithSubcategories;

	@Inject
	public AddCommand(@Named("main") CommentedYamlConfiguration mainConfig,
			@Named("lang") CommentedYamlConfiguration langConfig, StringBuilder pluginHeader, CacheManager cacheManager,
			StatisticIncreaseHandler statisticIncreaseHandler, Set<String> enabledCategoriesWithSubcategories) {
		super(mainConfig, langConfig, pluginHeader);
		this.cacheManager = cacheManager;
		this.statisticIncreaseHandler = statisticIncreaseHandler;
		this.enabledCategoriesWithSubcategories = enabledCategoriesWithSubcategories;
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
		if (!NumberUtils.isCreatable(args[1])) {
			sender.sendMessage(StringUtils.replaceOnce(langErrorValue, "VALUE", args[1]));
			return;
		}

		int valueToAdd = Integer.parseInt(args[1]);
		if (enabledCategoriesWithSubcategories.contains(args[2])) {
			if (args[2].contains(".")) {
				MultipleAchievements category = MultipleAchievements.getByName(StringUtils.substringBefore(args[2], "."));
				long amount = cacheManager.getAndIncrementStatisticAmount(category, StringUtils.substringAfter(args[2], "."),
						player.getUniqueId(), valueToAdd);
				statisticIncreaseHandler.checkThresholdsAndAchievements(player, args[2], amount);
				sender.sendMessage(StringUtils.replaceEach(langStatisticIncreased,
						new String[] { "ACH", "AMOUNT", "PLAYER" }, new String[] { args[2], args[1], args[3] }));
			} else if (!NormalAchievements.CONNECTIONS.toString().equals(args[2])) {
				NormalAchievements category = NormalAchievements.getByName(args[2]);
				long amount;
				if (category == NormalAchievements.PLAYEDTIME) {
					// Thresholds in the configuration are in hours, underlying statistics are millis.
					valueToAdd = (int) (valueToAdd * MILLIS_PER_HOUR);
					amount = cacheManager.getAndIncrementStatisticAmount(category, player.getUniqueId(), valueToAdd)
							/ MILLIS_PER_HOUR;
				} else {
					amount = cacheManager.getAndIncrementStatisticAmount(category, player.getUniqueId(), valueToAdd);
				}
				statisticIncreaseHandler.checkThresholdsAndAchievements(player, category.toString(), amount);
				sender.sendMessage(StringUtils.replaceEach(langStatisticIncreased,
						new String[] { "ACH", "AMOUNT", "PLAYER" }, new String[] { args[2], args[1], args[3] }));
			}
		} else {
			sender.sendMessage(StringUtils.replaceEach(langCategoryDoesNotExist, new String[] { "CAT", "CLOSEST_MATCH" },
					new String[] { args[2], StringHelper.getClosestMatch(args[2], enabledCategoriesWithSubcategories) }));
		}
	}
}
