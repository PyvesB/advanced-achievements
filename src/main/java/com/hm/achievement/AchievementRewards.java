package com.hm.achievement;

import net.milkbowl.vault.item.Items;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.hm.achievement.language.Lang;

public class AchievementRewards {

	private AdvancedAchievements plugin;
	private boolean retroVault;
	private boolean rewardCommandNotif;

	public AchievementRewards(AdvancedAchievements achievement) {

		this.plugin = achievement;
		retroVault = plugin.getConfig().getBoolean("RetroVault", false);
		// No longer available in default config, kept for compatibility with
		// versions prior to 2.1.
		rewardCommandNotif = plugin.getConfig().getBoolean("RewardCommandNotif", true);
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

		if (plugin.setUpEconomy())
			try {
				player.sendMessage(plugin.getChatHeader() + Lang.ITEM_REWARD_RECEIVED + " "
						+ Items.itemByStack(item).getName());
				return item;
			} catch (Exception ex) {
				// Do nothing, another message will be displayed just bellow.
			}
		player.sendMessage(plugin.getChatHeader() + Lang.ITEM_REWARD_RECEIVED + " "
				+ item.getType().toString().replace("_", " ").toLowerCase());
		return item;
	}

	@SuppressWarnings("deprecation")
	public void rewardMoney(Player player, Integer amount) {

		if (plugin.setUpEconomy()) {
			String price = Integer.toString(amount);
			double amtd = Double.valueOf(price.trim());
			// Deprecated method, was the only one existing prior to Vault 1.4.
			if (retroVault)
				plugin.getEconomy().depositPlayer(player.getName(), amtd);
			else
				plugin.getEconomy().depositPlayer(player, amtd);

			if (amount > 1)
				player.sendMessage(plugin.getChatHeader()
						+ ChatColor.translateAlternateColorCodes(
								'&',
								Lang.MONEY_REWARD_RECEIVED.toString().replace("AMOUNT",
										"&5" + amtd + " " + plugin.getEconomy().currencyNamePlural())));
			else
				player.sendMessage(plugin.getChatHeader()
						+ ChatColor.translateAlternateColorCodes(
								'&',
								Lang.MONEY_REWARD_RECEIVED.toString().replace("AMOUNT",
										"&5" + amtd + " " + plugin.getEconomy().currencyNameSingular())));
		}
	}

	/**
	 * Return the type of an achievement reward.
	 */
	public String getRewardType(String configAchievement) {

		String rewardType = "";
		if (plugin.getConfig().getKeys(true).contains(configAchievement + ".Reward.Money"))
			rewardType = Lang.LIST_REWARD_MONEY.toString();
		if (plugin.getConfig().getKeys(true).contains(configAchievement + ".Reward.Item"))
			if (rewardType != "")
				rewardType += ", " + Lang.LIST_REWARD_ITEM;
			else
				rewardType = Lang.LIST_REWARD_ITEM.toString();
		if (plugin.getConfig().getKeys(true).contains(configAchievement + ".Reward.command"))
			if (rewardType != "")
				rewardType += ", " + Lang.LIST_REWARD_COMMAND;
			else
				rewardType = Lang.LIST_REWARD_COMMAND.toString();

		return rewardType;
	}

	/**
	 * Deal with rewards.
	 */
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
			if (player.getInventory().firstEmpty() != -1)
				player.getInventory().addItem(item);
			else
				player.getWorld().dropItem(player.getLocation(), item);
		}
		if (!command.equals("")) {

			command = command.replace("PLAYER", player.getName());
			plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
			if (! rewardCommandNotif || Lang.COMMAND_REWARD.toString().equals(""))
				return;
			player.sendMessage(plugin.getChatHeader() + Lang.COMMAND_REWARD);

		}

	}

}
