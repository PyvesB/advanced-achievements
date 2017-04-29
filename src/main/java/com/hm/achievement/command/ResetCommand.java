package com.hm.achievement.command;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.db.CachedStatistic;

/**
 * Class in charge of handling the /aach reset command, which resets the statistics for a given player and achievement
 * category.
 * 
 * @author Pyves
 */
public class ResetCommand extends AbstractParsableCommand {

	private String langResetSuccessful;
	private String langCategoryDoesNotExist;

	public ResetCommand(AdvancedAchievements plugin) {
		super(plugin);
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		langResetSuccessful = plugin.getPluginLang().getString("reset-successful",
				" statistics were cleared for PLAYER.");
		langCategoryDoesNotExist = plugin.getChatHeader()
				+ plugin.getPluginLang().getString("category-does-not-exist", "The specified category does not exist.");
	}

	@Override
	protected void executeSpecificActions(CommandSender sender, String[] args, Player player) {
		String uuid = player.getUniqueId().toString();
		for (NormalAchievements category : NormalAchievements.values()) {
			if (category.toString().equalsIgnoreCase(args[1])) {
				if (category == NormalAchievements.CONNECTIONS) {
					// Not handled by a database cache.
					plugin.getDatabaseManager().clearConnection(player.getUniqueId());
				} else {
					CachedStatistic statistic = plugin.getCacheManager().getHashMap(category).get(uuid);
					if (statistic == null) {
						plugin.getCacheManager().getHashMap(category).put(uuid, new CachedStatistic(0L, false));
					} else {
						statistic.setValue(0L);
					}
				}
				sender.sendMessage(plugin.getChatHeader() + args[1]
						+ StringUtils.replaceOnce(langResetSuccessful, "PLAYER", player.getName()));
				return;
			}
		}

		for (MultipleAchievements category : MultipleAchievements.values()) {
			if (category.toString().equalsIgnoreCase(args[1])) {
				for (String subcategory : plugin.getPluginConfig().getConfigurationSection(category.toString())
						.getKeys(false)) {
					CachedStatistic statistic = plugin.getCacheManager().getHashMap(category)
							.get(plugin.getCacheManager().getMultipleCategoryCacheKey(category, player.getUniqueId(),
									subcategory));
					if (statistic == null) {
						plugin.getCacheManager().getHashMap(category).put(plugin.getCacheManager()
								.getMultipleCategoryCacheKey(category, player.getUniqueId(), subcategory),
								new CachedStatistic(0L, false));
					} else {
						statistic.setValue(0L);
					}
				}
				sender.sendMessage(plugin.getChatHeader() + args[1]
						+ StringUtils.replaceOnce(langResetSuccessful, "PLAYER", player.getName()));
				return;
			}
		}
		sender.sendMessage(langCategoryDoesNotExist);
	}
}
