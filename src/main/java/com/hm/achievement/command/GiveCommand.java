package com.hm.achievement.command;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.hm.achievement.db.CacheManager;
import com.hm.achievement.lang.Lang;
import com.hm.achievement.lang.command.CmdLang;
import com.hm.achievement.utils.PlayerAdvancedAchievementEvent.PlayerAdvancedAchievementEventBuilder;
import com.hm.achievement.utils.RewardParser;
import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Class in charge of handling the /aach give command, which gives an achievement from the Commands category.
 * 
 * @author Pyves
 */
@Singleton
public class GiveCommand extends AbstractParsableCommand {

	private final CacheManager cacheManager;
	private final RewardParser rewardParser;

	private boolean configMultiCommand;
	private String langAchievementAlreadyReceived;
	private String langAchievementGiven;
	private String langAchievementNotFound;

	@Inject
	public GiveCommand(@Named("main") CommentedYamlConfiguration mainConfig,
			@Named("lang") CommentedYamlConfiguration langConfig, StringBuilder pluginHeader, ReloadCommand reloadCommand,
			CacheManager cacheManager, RewardParser rewardParser) {
		super(mainConfig, langConfig, pluginHeader, reloadCommand);
		this.cacheManager = cacheManager;
		this.rewardParser = rewardParser;
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		configMultiCommand = mainConfig.getBoolean("MultiCommand", true);

		langAchievementAlreadyReceived = pluginHeader + Lang.get(CmdLang.ACHIEVEMENT_ALREADY_RECEIVED, langConfig);
		langAchievementGiven = pluginHeader + Lang.get(CmdLang.ACHIEVEMENT_GIVEN, langConfig);
		langAchievementNotFound = pluginHeader + Lang.get(CmdLang.ACHIEVEMENT_NOT_FOUND, langConfig);
	}

	@Override
	void executeSpecificActions(CommandSender sender, String[] args, Player player) {
		String achievementPath = "Commands." + args[1];

		if (mainConfig.getString(achievementPath + ".Message", null) != null) {
			// Check whether player has already received achievement and cannot receive it again.
			String achievementName = mainConfig.getString(achievementPath + ".Name");
			if (!configMultiCommand && cacheManager.hasPlayerAchievement(player.getUniqueId(), achievementName)) {
				sender.sendMessage(StringUtils.replaceOnce(langAchievementAlreadyReceived, "PLAYER", args[2]));
				return;
			}

			String rewardPath = achievementPath + ".Reward";
			// Fire achievement event.
			PlayerAdvancedAchievementEventBuilder playerAdvancedAchievementEventBuilder = new PlayerAdvancedAchievementEventBuilder()
					.player(player).name(achievementName).displayName(mainConfig.getString(achievementPath + ".DisplayName"))
					.message(mainConfig.getString(achievementPath + ".Message"))
					.commandRewards(rewardParser.getCommandRewards(rewardPath, player))
					.commandMessage(rewardParser.getCustomCommandMessage(rewardPath))
					.itemReward(rewardParser.getItemReward(rewardPath))
					.moneyReward(rewardParser.getRewardAmount(rewardPath, "Money"))
					.experienceReward(rewardParser.getRewardAmount(rewardPath, "Experience"))
					.maxHealthReward(rewardParser.getRewardAmount(rewardPath, "IncreaseMaxHealth"))
					.maxOxygenReward(rewardParser.getRewardAmount(rewardPath, "IncreaseMaxOxygen"));

			Bukkit.getServer().getPluginManager().callEvent(playerAdvancedAchievementEventBuilder.build());

			sender.sendMessage(langAchievementGiven);
		} else {
			sender.sendMessage(StringUtils.replaceOnce(langAchievementNotFound, "PLAYER", args[2]));
		}
	}
}
