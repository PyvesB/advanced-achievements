package com.hm.achievement.command;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.utils.StatisticIncreaseHandler;

/**
 * Class in charge of increase the progression of an achievement by command.
 * 
 * @author Phoetrix
 */
public class AddCommand extends AbstractCommand {

	private String langPlayerOffline;
	private String langErrorValue;
	private String langAchievementIncrease;
	private String langUnknownCategory;

	private class StatisticIncrease extends StatisticIncreaseHandler {
		protected StatisticIncrease(AdvancedAchievements plugin) {
			super(plugin);
		}

		protected void updateStatisticAndAwardAchievementsIfAvailable(Player player, NormalAchievements category,
				int incrementValue) {
			long amount = plugin.getCacheManager().getAndIncrementStatisticAmount(category, player.getUniqueId(),
					incrementValue);
			checkThresholdsAndAchievements(player, category.toString(), amount);
		}

		protected void updateStatisticAndAwardAchievementsIfAvailable(Player player, MultipleAchievements category,
				String subcategory, int incrementValue) {
			long amount = plugin.getCacheManager().getAndIncrementStatisticAmount(category, subcategory,
					player.getUniqueId(), incrementValue);
			checkThresholdsAndAchievements(player, category + "." + subcategory, amount);
		}
	}

	static StatisticIncrease statistic;

	public AddCommand(AdvancedAchievements plugin) {
		super(plugin);
		statistic = new StatisticIncrease(plugin);

	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();
		langPlayerOffline = plugin.getChatHeader()
				+ plugin.getPluginLang().getString("player-offline", "The player PLAYER is offline!");
		langErrorValue = plugin.getChatHeader()
				+ plugin.getPluginLang().getString("error-value", "The value VALUE must to be an integer!");
		langAchievementIncrease = plugin.getChatHeader() + plugin.getPluginLang().getString("achievement-increase",
				"Achievement ACH increase by AMOUNT for PLAYER!");
		langUnknownCategory = plugin.getChatHeader()
				+ plugin.getPluginLang().getString("achievement-unknown", "Achievement ACH is unknown!");
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void executeCommand(CommandSender sender, String[] args) {
		int value = 0;
		Player player = Bukkit.getPlayer(args[3]);

		if (player == null) {
			sender.sendMessage(StringUtils.replaceOnce(langPlayerOffline, "PLAYER", args[3]));
			return;
		}

		try {
			value = Integer.parseInt(args[1]);
		} catch (NumberFormatException nfe) {
			sender.sendMessage(StringUtils.replaceOnce(langErrorValue, "VALUE", args[1]));
			return;
		}

		for (NormalAchievements category : NormalAchievements.values()) {
			String categoryName = category.toString();

			if (args[2].equalsIgnoreCase(categoryName) && !categoryName.equalsIgnoreCase("connections")) {
				statistic.updateStatisticAndAwardAchievementsIfAvailable(player, category, value);
				sender.sendMessage(StringUtils.replaceEach(langAchievementIncrease,
						new String[] { "ACH", "AMOUNT", "PLAYER" }, new String[] { args[2], args[1], args[3] }));
				return;
			}
		}
		for (MultipleAchievements category : MultipleAchievements.values()) {
			String categoryName = category.toString();

			for (String subcategory : plugin.getPluginConfig().getConfigurationSection(categoryName).getKeys(false)) {
				String categoryPath = categoryName + "." + subcategory;

				if (args[2].equalsIgnoreCase(categoryPath)) {
					statistic.updateStatisticAndAwardAchievementsIfAvailable(player, category, subcategory, value);
					sender.sendMessage(StringUtils.replaceEach(langAchievementIncrease,
							new String[] { "ACH", "AMOUNT", "PLAYER" }, new String[] { args[2], args[1], args[3] }));
					return;
				}
			}
		}

		sender.sendMessage(StringUtils.replaceOnce(langUnknownCategory, "ACH", args[2]));
	}
}
