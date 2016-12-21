package com.hm.achievement.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.hm.achievement.AdvancedAchievements;

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
						plugin.getChatHeader() + plugin.getPluginLang()
								.getString("achievement-already-received",
										"The player PLAYER has already received this achievement!")
								.replace("PLAYER", args[2]));
				return;
			}

			// Display, register and give rewards of achievement.
			plugin.getAchievementDisplay().displayAchievement(player, configAchievement);
			plugin.getDb().registerAchievement(player, achievementName,
					plugin.getPluginConfig().getString(configAchievement + ".Message"));
			String uuid = player.getUniqueId().toString();
			plugin.getPoolsManager().getReceivedAchievementsCache().put(uuid, achievementName);
			plugin.getPoolsManager().getNotReceivedAchievementsCache().remove(uuid, achievementName);
			plugin.getReward().checkConfig(player, configAchievement);

			sender.sendMessage(plugin.getChatHeader()
					+ plugin.getPluginLang().getString("achievement-given", "Achievement given!"));
		} else {
			// Achievement not found in the Commands category.
			sender.sendMessage(plugin.getChatHeader() + plugin.getPluginLang()
					.getString("achievement-not-found", "The specified achievement was not found in Commands category.")
					.replace("PLAYER", args[2]));
		}
	}
}
