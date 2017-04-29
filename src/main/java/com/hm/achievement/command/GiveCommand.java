package com.hm.achievement.command;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.utils.PlayerAdvancedAchievementEvent.PlayerAdvancedAchievementEventBuilder;

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

		langAchievementAlreadyReceived = plugin.getChatHeader() + plugin.getPluginLang()
				.getString("achievement-already-received", "The player PLAYER has already received this achievement!");
		langAchievementGiven = plugin.getChatHeader()
				+ plugin.getPluginLang().getString("achievement-given", "Achievement given!");
		langAchievementNotFound = plugin.getChatHeader() + plugin.getPluginLang().getString("achievement-not-found",
				"The specified achievement was not found in Commands category.");
	}

	@Override
	protected void executeSpecificActions(CommandSender sender, String[] args, Player player) {
		String configAchievement = "Commands." + args[1];

		if (plugin.getPluginConfig().getString(configAchievement + ".Message", null) != null) {
			// Check whether player has already received achievement and cannot receive it again.
			String achievementName = plugin.getPluginConfig().getString(configAchievement + ".Name");
			if (!configMultiCommand
					&& plugin.getCacheManager().hasPlayerAchievement(player.getUniqueId(), achievementName)) {
				sender.sendMessage(StringUtils.replaceOnce(langAchievementAlreadyReceived, "PLAYER", args[2]));
				return;
			}

			// Fire achievement event.
			PlayerAdvancedAchievementEventBuilder playerAdvancedAchievementEventBuilder = new PlayerAdvancedAchievementEventBuilder()
					.player(player).name(achievementName)
					.displayName(plugin.getPluginConfig().getString(configAchievement + ".DisplayName"))
					.message(plugin.getPluginConfig().getString(configAchievement + ".Message"))
					.commandRewards(plugin.getRewardParser().getCommandRewards(configAchievement, player))
					.itemReward(plugin.getRewardParser().getItemReward(configAchievement))
					.moneyReward(plugin.getRewardParser().getMoneyAmount(configAchievement));

			Bukkit.getServer().getPluginManager().callEvent(playerAdvancedAchievementEventBuilder.build());

			sender.sendMessage(langAchievementGiven);
		} else {
			sender.sendMessage(StringUtils.replaceOnce(langAchievementNotFound, "PLAYER", args[2]));
		}
	}
}
