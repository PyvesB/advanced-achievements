package com.hm.achievement;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.hm.achievement.language.Lang;

public class AchievementRewards {

	private AdvancedAchievements plugin;

	public AchievementRewards(AdvancedAchievements achievement) {

		this.plugin = achievement;
	}

	/**
	 * Check if achievement exists in configuration file.
	 */
	public Boolean checkAchievement(String ach) {

		String check = plugin.getConfig().getString(ach + ".Message", "null");
		if (check.equals("null")) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Give item reward to a player (specified in configuration file).
	 */
	public ItemStack giveItemReward(Player player, String ach, int amount) {

		ItemStack item;
		// Old config syntax.
		if (plugin.getConfig().getKeys(true).contains(ach + ".Reward.Item.Type"))
			item = new ItemStack(Material.getMaterial(plugin.getConfig().getString(ach + ".Reward.Item.Type", "stone")
					.toUpperCase()), amount);
		else
			// New config syntax.
			item = new ItemStack(Material.getMaterial(plugin.getConfig().getString(ach + ".Reward.Item", "stone")
					.toUpperCase()
					.substring(0, plugin.getConfig().getString(ach + ".Reward.Item", "stone").indexOf(" "))), amount);
		player.sendMessage(ChatColor.GRAY + "[" + ChatColor.DARK_PURPLE + plugin.getIcon() + ChatColor.GRAY + "] "
				+ Lang.ITEM_REWARD_RECEIVED);
		return item;
	}

	@SuppressWarnings("deprecation")
	public void rewardMoney(Player player, Integer amount) {

		if (plugin.setupEconomy()) {
			String price = Integer.toString(amount);
			double amtd = Double.valueOf(price.trim());
			// Deprecated method, was the only one existing prior to Vault 1.4.
			if (plugin.isRetroVault())
				plugin.getEconomy().depositPlayer(player.getName(), amtd);
			else
				plugin.getEconomy().depositPlayer(player, amtd);

			if (amount > 1)
				player.sendMessage(ChatColor.GRAY
						+ "["
						+ ChatColor.DARK_PURPLE
						+ plugin.getIcon()
						+ ChatColor.GRAY
						+ "] "
						+ ChatColor.translateAlternateColorCodes(
								'&',
								Lang.MONEY_REWARD_RECEIVED.toString().replace("AMOUNT",
										"&5" + amtd + " " + plugin.getEconomy().currencyNamePlural())));
			else
				player.sendMessage(ChatColor.GRAY
						+ "["
						+ ChatColor.DARK_PURPLE
						+ plugin.getIcon()
						+ ChatColor.GRAY
						+ "] "
						+ ChatColor.translateAlternateColorCodes(
								'&',
								Lang.MONEY_REWARD_RECEIVED.toString().replace("AMOUNT",
										"&5" + amtd + " " + plugin.getEconomy().currencyNameSingular())));
		}
	}

	public void checkConfig(Player player, String configAchievement) {

		// Supports both old and new plugin syntax.
		int money = Math.max(plugin.getConfig().getInt(configAchievement + ".Reward.Money", 0), plugin.getConfig()
				.getInt(configAchievement + ".Reward.Money.Amount", 0));

		int itemAmount = 0;
		// Old config syntax.
		if (plugin.getConfig().getKeys(true).contains(configAchievement + ".Reward.Item.Amount")) {
			itemAmount = plugin.getConfig().getInt(configAchievement + ".Reward.Item.Amount", 0);
		} else if (plugin.getConfig().getKeys(true).contains(configAchievement + ".Reward.Item")) { // New
																									// config
																									// syntax.
			int indexOfAmount = 0;
			indexOfAmount = plugin.getConfig().getString(configAchievement + ".Reward.Item", "").indexOf(" ");
			if (indexOfAmount != -1)
				itemAmount = Integer.valueOf(plugin.getConfig().getString(configAchievement + ".Reward.Item", "")
						.substring(indexOfAmount + 1));
		}

		String command = plugin.getConfig().getString(configAchievement + ".Reward.Command", "");

		if (money != 0) {
			rewardMoney(player, money);
		}
		if (itemAmount != 0) {
			ItemStack item = this.giveItemReward(player, configAchievement, itemAmount);
			PlayerInventory inv = player.getInventory();
			inv.addItem(item);
		}
		if (!command.equals("")) {

			command = command.replace("PLAYER", player.getName());
			plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
			if (!plugin.isRewardCommandNotif())
				return;
			player.sendMessage(ChatColor.GRAY + "[" + ChatColor.DARK_PURPLE + plugin.getIcon() + ChatColor.GRAY + "] "
					+ Lang.COMMAND_REWARD);

		}

	}

}
