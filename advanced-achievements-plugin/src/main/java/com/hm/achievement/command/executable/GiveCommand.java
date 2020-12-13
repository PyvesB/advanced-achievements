package com.hm.achievement.command.executable;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.hm.achievement.category.CommandAchievements;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.utils.PlayerAdvancedAchievementEvent.PlayerAdvancedAchievementEventBuilder;
import com.hm.achievement.utils.RewardParser;
import com.hm.achievement.utils.StringHelper;

/**
 * Class in charge of handling the /aach give command, which gives an achievement from the Commands category.
 * 
 * @author Pyves
 */
@Singleton
@CommandSpec(name = "give", permission = "give", minArgs = 3, maxArgs = 3)
public class GiveCommand extends AbstractParsableCommand {

	private final CacheManager cacheManager;
	private final RewardParser rewardParser;

	private boolean configMultiCommand;
	private String langAchievementAlreadyReceived;
	private String langAchievementGiven;
	private String langAchievementNotFound;
	private String langAchievementNoPermission;

	@Inject
	public GiveCommand(@Named("main") YamlConfiguration mainConfig, @Named("lang") YamlConfiguration langConfig,
			StringBuilder pluginHeader, CacheManager cacheManager, RewardParser rewardParser) {
		super(mainConfig, langConfig, pluginHeader);
		this.cacheManager = cacheManager;
		this.rewardParser = rewardParser;
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		configMultiCommand = mainConfig.getBoolean("MultiCommand");

		langAchievementAlreadyReceived = pluginHeader + langConfig.getString("achievement-already-received");
		langAchievementGiven = pluginHeader + langConfig.getString("achievement-given");
		langAchievementNotFound = pluginHeader + langConfig.getString("achievement-not-found");
		langAchievementNoPermission = pluginHeader + langConfig.getString("achievement-no-permission");
	}

	@Override
	void onExecuteForPlayer(CommandSender sender, String[] args, Player player) {
		String achievementPath = CommandAchievements.COMMANDS + "." + args[1];

		if (mainConfig.contains(achievementPath)) {
			// Check whether player has already received achievement and cannot receive it again.
			String achievementName = mainConfig.getString(achievementPath + ".Name");
			if (!configMultiCommand && cacheManager.hasPlayerAchievement(player.getUniqueId(), achievementName)) {
				sender.sendMessage(StringUtils.replaceOnce(langAchievementAlreadyReceived, "PLAYER", args[2]));
				return;
			} else if (!player.hasPermission("achievement." + achievementName)) {
				sender.sendMessage(StringUtils.replaceOnce(langAchievementNoPermission, "PLAYER", args[2]));
				return;
			}

			String rewardPath = achievementPath + ".Reward";
			// Fire achievement event.
			PlayerAdvancedAchievementEventBuilder playerAdvancedAchievementEventBuilder = new PlayerAdvancedAchievementEventBuilder()
					.player(player)
					.name(achievementName)
					.displayName(mainConfig.getString(achievementPath + ".DisplayName"))
					.message(mainConfig.getString(achievementPath + ".Message"))
					.type(mainConfig.getString(achievementPath + ".Type"))
					.commandRewards(rewardParser.getCommandRewards(rewardPath, player))
					.commandMessage(rewardParser.getCustomCommandMessages(rewardPath))
					.itemRewards(rewardParser.getItemRewards(rewardPath, player))
					.moneyReward(rewardParser.getRewardAmount(rewardPath, "Money"))
					.experienceReward(rewardParser.getRewardAmount(rewardPath, "Experience"))
					.maxHealthReward(rewardParser.getRewardAmount(rewardPath, "IncreaseMaxHealth"))
					.maxOxygenReward(rewardParser.getRewardAmount(rewardPath, "IncreaseMaxOxygen"));

			Bukkit.getPluginManager().callEvent(playerAdvancedAchievementEventBuilder.build());

			sender.sendMessage(langAchievementGiven);
		} else {
			sender.sendMessage(StringUtils.replaceOnce(langAchievementNotFound, "CLOSEST_MATCH",
					StringHelper.getClosestMatch(args[1],
							mainConfig.getConfigurationSection(CommandAchievements.COMMANDS.toString()).getKeys(false))));
		}
	}
}
