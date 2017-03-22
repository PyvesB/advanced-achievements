package com.hm.achievement.command;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;

/**
 * Class in charge of handling the /aach reset command, which resets the statistics for a given player and achievement
 * category.
 * 
 * @author Pyves
 */
public class ResetCommand extends AbstractParsableCommand {

	public ResetCommand(AdvancedAchievements plugin) {
		super(plugin);
	}

	@Override
	protected void executeSpecificActions(CommandSender sender, String[] args, Player player) {
		for (NormalAchievements category : NormalAchievements.values()) {
			if (category.toString().equalsIgnoreCase(args[1])) {
				if (category == NormalAchievements.CONNECTIONS) {
					// Not handled by a database pool.
					plugin.getDb().clearConnection(player);
				} else {
					plugin.getPoolsManager().getHashMap(category).put(player.getUniqueId().toString(), 0L);
				}
				sender.sendMessage(plugin.getChatHeader() + args[1] + StringUtils.replaceOnce(
						plugin.getPluginLang().getString("reset-successful", " statistics were cleared for PLAYER."),
						"PLAYER", player.getName()));
				return;
			}
		}

		for (MultipleAchievements category : MultipleAchievements.values()) {
			if (category.toString().equalsIgnoreCase(args[1])) {
				for (String section : plugin.getPluginConfig().getConfigurationSection(category.toString())
						.getKeys(false)) {
					String subcategoryDBName;
					if (category == MultipleAchievements.PLAYERCOMMANDS) {
						subcategoryDBName = StringUtils.replace(section, " ", "");
					} else {
						subcategoryDBName = section;
					}
					plugin.getPoolsManager().getHashMap(category)
							.put(player.getUniqueId().toString() + subcategoryDBName, 0L);
				}
				sender.sendMessage(plugin.getChatHeader() + args[1] + StringUtils.replaceOnce(
						plugin.getPluginLang().getString("reset-successful", " statistics were cleared for PLAYER."),
						"PLAYER", player.getName()));
				return;
			}
		}
		sender.sendMessage(plugin.getChatHeader() + plugin.getPluginLang().getString("category-does-not-exist",
				"The specified category does not exist."));
	}
}
