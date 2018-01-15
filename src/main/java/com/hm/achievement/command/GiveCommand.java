package com.hm.achievement.command;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.lang.Lang;
import com.hm.achievement.lang.command.CmdLang;
import com.hm.achievement.utils.PlayerAdvancedAchievementEvent.PlayerAdvancedAchievementEventBuilder;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Class in charge of handling the /aach give command, which gives an achievement from the Commands category.
 * 
 * @author Pyves
 */
public class GiveCommand extends AbstractParsableCommand {

	private boolean configMultiCommand;
	private String langAchievementAlreadyReceived;
	private String langAchievementGiven;
	private String langAchievementNotFound;

	public GiveCommand(AdvancedAchievements plugin) {
		super(plugin);
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		configMultiCommand = plugin.getPluginConfig().getBoolean("MultiCommand", true);

		langAchievementAlreadyReceived = Lang.getWithChatHeader(CmdLang.ACHIEVEMENT_ALREADY_RECEIVED, plugin);
		langAchievementGiven = Lang.getWithChatHeader(CmdLang.ACHIEVEMENT_GIVEN, plugin);
		langAchievementNotFound = Lang.getWithChatHeader(CmdLang.ACHIEVEMENT_NOT_FOUND, plugin);
	}

	@Override
	protected void executeSpecificActions(CommandSender sender, String[] args, Player player) {
		String achievementPath = "Commands." + args[1];

		if (plugin.getPluginConfig().getString(achievementPath + ".Message", null) != null) {
			// Check whether player has already received achievement and cannot receive it again.
			String achievementName = plugin.getPluginConfig().getString(achievementPath + ".Name");
			if (!configMultiCommand
					&& plugin.getCacheManager().hasPlayerAchievement(player.getUniqueId(), achievementName)) {
				sender.sendMessage(StringUtils.replaceOnce(langAchievementAlreadyReceived, "PLAYER", args[2]));
				return;
			}

			String rewardPath = achievementPath + ".Reward";
			// Fire achievement event.
			PlayerAdvancedAchievementEventBuilder playerAdvancedAchievementEventBuilder = new PlayerAdvancedAchievementEventBuilder()
					.player(player).name(achievementName)
					.displayName(plugin.getPluginConfig().getString(achievementPath + ".DisplayName"))
					.message(plugin.getPluginConfig().getString(achievementPath + ".Message"))
					.commandRewards(plugin.getRewardParser().getCommandRewards(rewardPath, player))
					.commandMessage(plugin.getRewardParser().getCustomCommandMessage(rewardPath))
					.itemReward(plugin.getRewardParser().getItemReward(rewardPath))
					.moneyReward(plugin.getRewardParser().getRewardAmount(rewardPath, "Money"))
					.experienceReward(plugin.getRewardParser().getRewardAmount(rewardPath, "Experience"))
					.maxHealthReward(plugin.getRewardParser().getRewardAmount(rewardPath, "IncreaseMaxHealth"))
					.maxOxygenReward(plugin.getRewardParser().getRewardAmount(rewardPath, "IncreaseMaxOxygen"));

			Bukkit.getServer().getPluginManager().callEvent(playerAdvancedAchievementEventBuilder.build());

			sender.sendMessage(langAchievementGiven);
		} else {
			sender.sendMessage(StringUtils.replaceOnce(langAchievementNotFound, "PLAYER", args[2]));
		}
	}
}
