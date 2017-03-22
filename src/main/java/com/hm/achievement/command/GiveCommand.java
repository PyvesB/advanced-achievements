package com.hm.achievement.command;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.PlayerAdvancedAchievementEvent.PlayerAdvancedAchievementEventBuilder;

/**
 * Class in charge of handling the /aach give command, which gives an achievement from the Commands category.
 * 
 * @author Pyves
 */
public class GiveCommand extends AbstractParsableCommand {

	private final boolean multiCommand;

	public GiveCommand(AdvancedAchievements plugin) {
		super(plugin);
		// Load configuration parameter.
		multiCommand = plugin.getPluginConfig().getBoolean("MultiCommand", true);
	}

	@Override
	protected void executeSpecificActions(CommandSender sender, String[] args, Player player) {
		String configAchievement = "Commands." + args[1];

		if (plugin.getPluginConfig().getString(configAchievement + ".Message", null) != null) {
			// Check whether player has already received achievement and cannot receive it again.
			String achievementName = plugin.getPluginConfig().getString(configAchievement + ".Name");
			if (!multiCommand && plugin.getPoolsManager().hasPlayerAchievement(player, achievementName)) {
				sender.sendMessage(
						StringUtils.replaceOnce(
								plugin.getChatHeader()
										+ plugin.getPluginLang().getString("achievement-already-received",
												"The player PLAYER has already received this achievement!"),
								"PLAYER", args[2]));
				return;
			}

			// Fire achievement event.
			PlayerAdvancedAchievementEventBuilder playerAdvancedAchievementEventBuilder = new PlayerAdvancedAchievementEventBuilder()
					.player(player).name(achievementName)
					.displayName(plugin.getPluginConfig().getString(configAchievement + ".DisplayName"))
					.message(plugin.getPluginConfig().getString(configAchievement + ".Message"))
					.commandRewards(plugin.getReward().getCommandRewards(configAchievement, player))
					.itemReward(plugin.getReward().getItemReward(configAchievement))
					.moneyReward(plugin.getReward().getMoneyAmount(configAchievement));

			Bukkit.getServer().getPluginManager().callEvent(playerAdvancedAchievementEventBuilder.build());

			sender.sendMessage(plugin.getChatHeader()
					+ plugin.getPluginLang().getString("achievement-given", "Achievement given!"));
		} else {
			// Achievement not found in the Commands category.
			sender.sendMessage(
					plugin.getChatHeader() + StringUtils.replaceOnce(
							plugin.getPluginLang().getString("achievement-not-found",
									"The specified achievement was not found in Commands category."),
							"PLAYER", args[2]));
		}
	}
}
