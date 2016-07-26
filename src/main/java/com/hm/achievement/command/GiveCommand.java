package com.hm.achievement.command;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import com.hm.achievement.AdvancedAchievements;

/**
 * Class in charge of handling the /aach give command, which gives an achievement from the reward category.
 * 
 * @author Pyves
 */
public class GiveCommand implements Listener {

	private AdvancedAchievements plugin;
	private Boolean multiCommand;

	public GiveCommand(AdvancedAchievements plugin) {

		this.plugin = plugin;
		// Load configuration parameter.
		multiCommand = plugin.getPluginConfig().getBoolean("MultiCommand", true);
	}

	/**
	 * Give an achievement with an in game or console command.
	 * 
	 * @param sender
	 * @param args
	 */
	public void achievementGive(CommandSender sender, String args[]) {

		String configAchievement = "Commands." + args[1];

		Player player = null;
		// Retrieve player instance with his name.
		for (Player currentPlayer : Bukkit.getOnlinePlayers()) {
			if (currentPlayer.getName().equalsIgnoreCase(args[2])) {
				player = currentPlayer;
				break;
			}
		}

		// If player not found or is offline.
		if (player == null) {
			sender.sendMessage(plugin.getChatHeader() + plugin.getPluginLang()
					.getString("player-offline", "The player PLAYER is offline!").replace("PLAYER", args[2]));
			return;
		}

		if (plugin.getPluginConfig().getString(configAchievement + ".Message", null) != null) {
			// Check whether player has already received achievement and cannot receive it again.
			if (!multiCommand && plugin.getDb().hasPlayerAchievement(player,
					plugin.getPluginConfig().getString(configAchievement + ".Name"))) {
				sender.sendMessage(
						plugin.getChatHeader() + plugin.getPluginLang()
								.getString("achievement-already-received",
										"The player PLAYER has already received this achievement!")
								.replace("PLAYER", args[2]));
				return;
			}

			// Display, register and give rewards of achievement.
			plugin.getAchievementDisplay().displayAchievement(player, configAchievement);
			plugin.getDb().registerAchievement(player, plugin.getPluginConfig().getString(configAchievement + ".Name"),
					plugin.getPluginConfig().getString(configAchievement + ".Message"));
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
