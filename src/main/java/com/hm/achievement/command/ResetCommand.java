package com.hm.achievement.command;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.db.CachedStatistic;
import com.hm.achievement.lang.Lang;
import com.hm.achievement.lang.command.CmdLang;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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

		langResetSuccessful = Lang.get(CmdLang.RESET_SUCCESSFUL, plugin);
		langCategoryDoesNotExist = Lang.getWithChatHeader(CmdLang.CATEGORY_DOES_NOT_EXIST, plugin);
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
			for (String subcategory : plugin.getPluginConfig().getConfigurationSection(category.toString())
					.getKeys(false)) {
				String categoryPath = category.toString() + "." + StringUtils.deleteWhitespace(subcategory);
				if (categoryPath.equalsIgnoreCase(args[1])) {
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
					sender.sendMessage(plugin.getChatHeader() + args[1]
							+ StringUtils.replaceOnce(langResetSuccessful, "PLAYER", player.getName()));
					return;
				}
			}
		}

		sender.sendMessage(StringUtils.replaceOnce(langCategoryDoesNotExist, "CAT", args[1]));
	}
}
