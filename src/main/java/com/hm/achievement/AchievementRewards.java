package com.hm.achievement;

import net.milkbowl.vault.item.Items;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class AchievementRewards {

	private AdvancedAchievements plugin;
	private boolean rewardCommandNotif;

	public AchievementRewards(AdvancedAchievements achievement) {

		this.plugin = achievement;
		// No longer available in default config, kept for compatibility with
		// versions prior to 2.1.
		rewardCommandNotif = plugin.getPluginConfig().getBoolean("RewardCommandNotif", true);
	}

	/**
	 * Check if achievement exists in configuration file.
	 */
	public Boolean checkAchievement(String ach) {

		String check = plugin.getPluginConfig().getString(ach + ".Message", "null");
		if (check.equals("null")) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Get item reward to a player (specified in configuration file).
	 */
	public ItemStack getItemReward(Player player, String ach, int amount) {

		ItemStack item;
		// Old config syntax.
		if (plugin.getPluginConfig().getKeys(true).contains(ach + ".Reward.Item.Type"))
			item = new ItemStack(
					Material.getMaterial(
							plugin.getPluginConfig().getString(ach + ".Reward.Item.Type", "stone").toUpperCase()),
					amount);
		// New config syntax.
		else
			item = new ItemStack(
					Material.getMaterial(
							plugin.getPluginConfig()
									.getString(ach + ".Reward.Item", "stone").toUpperCase().substring(0, plugin
											.getPluginConfig().getString(ach + ".Reward.Item", "stone").indexOf(" "))),
					amount);

		// Display Vault name of object if available.
		if (plugin.setUpEconomy())
			try {
				player.sendMessage(plugin.getChatHeader()
						+ plugin.getPluginLang().getString("item-reward-received", "You received an item reward:") + " "
						+ Items.itemByStack(item).getName());
				return item;
			} catch (Exception ex) {
				// Do nothing, another message will be displayed just bellow.
			}
		player.sendMessage(plugin.getChatHeader()
				+ plugin.getPluginLang().getString("item-reward-received", "You received an item reward:") + " "
				+ item.getType().toString().replace("_", " ").toLowerCase());
		return item;
	}

	/**
	 * Give money reward to a player (specified in configuration file).
	 */
	@SuppressWarnings("deprecation")
	public void rewardMoney(Player player, int amount) {

		if (plugin.setUpEconomy()) {
			String price = Integer.toString(amount);
			double amtd = Double.valueOf(price.trim());

			try {
				plugin.getEconomy().depositPlayer(player, amtd);
			} catch (NoSuchMethodError e) {
				// Deprecated method, the following one was the only one
				// existing prior to Vault 1.4.
				plugin.getEconomy().depositPlayer(player.getName(), amtd);
			}

			// If player has set different currency names depending on name,
			// adapt message accordingly.
			if (amount > 1)
				player.sendMessage(plugin.getChatHeader() + ChatColor.translateAlternateColorCodes('&',
						plugin.getPluginLang().getString("money-reward-received", "You received: AMOUNT !")
								.replace("AMOUNT", amtd + " " + plugin.getEconomy().currencyNamePlural())));
			else
				player.sendMessage(plugin.getChatHeader() + ChatColor.translateAlternateColorCodes('&',
						plugin.getPluginLang().getString("money-reward-received", "You received: AMOUNT !")
								.replace("AMOUNT", amtd + " " + plugin.getEconomy().currencyNameSingular())));
		}
	}

	/**
	 * Return the type of an achievement reward with strings coming from
	 * language file.
	 */
	public String getRewardType(String configAchievement) {

		String rewardType = "";
		if (plugin.getPluginConfig().getKeys(true).contains(configAchievement + ".Reward.Money"))
			rewardType = plugin.getPluginLang().getString("list-reward-money", "money");
		if (plugin.getPluginConfig().getKeys(true).contains(configAchievement + ".Reward.Item"))
			if (rewardType.length() != 0)
				rewardType += ", " + plugin.getPluginLang().getString("list-reward-item", "item");
			else
				rewardType = plugin.getPluginLang().getString("list-reward-item", "item");
		if (plugin.getPluginConfig().getKeys(true).contains(configAchievement + ".Reward.command"))
			if (rewardType.length() != 0)
				rewardType += ", " + plugin.getPluginLang().getString("list-reward-command", "other");
			else
				rewardType = plugin.getPluginLang().getString("list-reward-command", "other");

		return rewardType;
	}

	/**
	 * Main reward manager (deals with configuration).
	 */
	public void checkConfig(Player player, String configAchievement) {

		// Supports both old and new plugin syntax.
		int money = Math.max(plugin.getPluginConfig().getInt(configAchievement + ".Reward.Money", 0),
				plugin.getPluginConfig().getInt(configAchievement + ".Reward.Money.Amount", 0));

		int itemAmount = 0;
		// Old config syntax.
		if (plugin.getPluginConfig().getKeys(true).contains(configAchievement + ".Reward.Item.Amount")) {
			itemAmount = plugin.getPluginConfig().getInt(configAchievement + ".Reward.Item.Amount", 0);
		} else if (plugin.getPluginConfig().getKeys(true).contains(configAchievement + ".Reward.Item")) { // New
			// config
			// syntax.
			int indexOfAmount = 0;
			indexOfAmount = plugin.getPluginConfig().getString(configAchievement + ".Reward.Item", "").indexOf(" ");
			if (indexOfAmount != -1)
				itemAmount = Integer.valueOf(plugin.getPluginConfig().getString(configAchievement + ".Reward.Item", "")
						.substring(indexOfAmount + 1));
		}

		String commandReward = plugin.getPluginConfig().getString(configAchievement + ".Reward.Command", "");

		if (money != 0) {
			rewardMoney(player, money);
		}
		if (itemAmount != 0) {
			ItemStack item = this.getItemReward(player, configAchievement, itemAmount);
			if (player.getInventory().firstEmpty() != -1)
				player.getInventory().addItem(item);
			else
				player.getWorld().dropItem(player.getLocation(), item);
		}
		if (commandReward.length() != 0) {

			commandReward = commandReward.replace("PLAYER", player.getName());
			// Multiple reward command can be set, separated by a semicolon and
			// space.
			String[] commands = commandReward.split("; ");
			for (String command : commands)
				plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
			if (!rewardCommandNotif
					|| plugin.getPluginLang().getString("command-reward", "Reward command carried out!").length() == 0)
				return;
			player.sendMessage(plugin.getChatHeader()
					+ plugin.getPluginLang().getString("command-reward", "Reward command carried out!"));

		}

	}

}
