package com.hm.achievement;

import com.hm.achievement.utils.YamlManager;
import net.milkbowl.vault.item.Items;

import java.util.ArrayList;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Class in charge of distributing the rewards when receiving an achievement.
 * 
 * @author Pyves
 */
public class AchievementRewards {

	private AdvancedAchievements plugin;
	private boolean rewardCommandNotif;

	public AchievementRewards(AdvancedAchievements achievement) {

		this.plugin = achievement;
		// No longer available in default config, kept for compatibility with versions prior to 2.1; defines whether
		// a player is notified in case of a command reward.
		rewardCommandNotif = plugin.getPluginConfig().getBoolean("RewardCommandNotif", true);
	}

	/**
	 * Give item reward to a player (specified in configuration file).
	 * 
	 * @param player
	 * @param ach
	 * @param amount
	 * @return ItemStack object corresponding to the reward
	 */
	public ItemStack getItemReward(Player player, String ach, int amount) {

		ItemStack item;
		YamlManager config = plugin.getPluginConfig();

		try {
			// Old config syntax (type of item separated in a additional subcategory).
			if (config.getKeys(true).contains(ach + ".Reward.Item.Type")) {
				item = new ItemStack(
						Material.getMaterial(config.getString(ach + ".Reward.Item.Type", "stone").toUpperCase()),
						amount);
			} else {
				// New config syntax. Reward is of the form:
				// Item: coal 5
				// The amount has already been parsed out and is provided by parameter amount.
				String materialNameAndQty = config.getString(ach + ".Reward.Item", "stone");
				int indexSpace = materialNameAndQty.indexOf(" ");

				String materialName;
				if (indexSpace > 0)
					materialName = materialNameAndQty.toUpperCase().substring(0, indexSpace);
				else
					materialName = materialNameAndQty.toUpperCase();

				item = new ItemStack(Material.getMaterial(materialName), amount);
			}
		} catch (NullPointerException e) {
			plugin.getLogger().warning("Invalid item reward for achievement \"" + config.getString(ach + ".Name")
					+ "\". Please specify a valid Material name.");
			return null;
		}

		// Display Vault name of object if available.
		if (plugin.setUpEconomy(false)) {
			try {
				player.sendMessage(plugin.getChatHeader()
						+ plugin.getPluginLang().getString("item-reward-received", "You received an item reward:") + " "
						+ Items.itemByStack(item).getName());
				return item;
			} catch (Exception e) {
				// Do nothing, another message will be displayed just below.
			}
		}

		// Vault name of object not available.
		player.sendMessage(plugin.getChatHeader()
				+ plugin.getPluginLang().getString("item-reward-received", "You received an item reward:") + " "
				+ item.getType().toString().replace("_", " ").toLowerCase());
		return item;
	}

	/**
	 * Give money reward to a player (specified in configuration file).
	 * 
	 * @param player
	 * @param amount
	 */
	@SuppressWarnings("deprecation")
	public void rewardMoney(Player player, int amount) {

		if (plugin.setUpEconomy(true)) {

			try {
				plugin.getEconomy().depositPlayer(player, amount);
			} catch (NoSuchMethodError e) {
				// Deprecated method, but was the only one existing prior to Vault 1.4.
				plugin.getEconomy().depositPlayer(player.getName(), amount);
			}

			// If player has set different currency names depending on amount,
			// adapt message accordingly.
			if (amount > 1)
				player.sendMessage(plugin.getChatHeader() + ChatColor.translateAlternateColorCodes('&',
						plugin.getPluginLang().getString("money-reward-received", "You received: AMOUNT !")
								.replace("AMOUNT", amount + " " + plugin.getEconomy().currencyNamePlural())));
			else
				player.sendMessage(plugin.getChatHeader() + ChatColor.translateAlternateColorCodes('&',
						plugin.getPluginLang().getString("money-reward-received", "You received: AMOUNT !")
								.replace("AMOUNT", amount + " " + plugin.getEconomy().currencyNameSingular())));
		}
	}

	/**
	 * Return the type(s) of an achievement reward with strings coming from language file.
	 * 
	 * @param configAchievement
	 * @return type(s) of the achievement reward as an array of strings
	 */
	public ArrayList<String> getRewardType(String configAchievement) {

		ArrayList<String> rewardType = new ArrayList<String>();
		Set<String> keyNames = plugin.getPluginConfig().getKeys(true);

		if (keyNames.contains(configAchievement + ".Reward.Money"))
			rewardType.add(plugin.getPluginLang().getString("list-reward-money", "money"));
		if (keyNames.contains(configAchievement + ".Reward.Item"))
			rewardType.add(plugin.getPluginLang().getString("list-reward-item", "item"));
		if (keyNames.contains(configAchievement + ".Reward.Command"))
			rewardType.add(plugin.getPluginLang().getString("list-reward-command", "other"));

		return rewardType;
	}

	/**
	 * Main reward manager (deals with configuration).
	 * 
	 * @param player
	 * @param configAchievement
	 */
	public void checkConfig(Player player, String configAchievement) {

		YamlManager config = plugin.getPluginConfig();

		// Supports both old and new plugin syntax (Amount was once a separate sub-category).
		int money = Math.max(config.getInt(configAchievement + ".Reward.Money", 0),
				config.getInt(configAchievement + ".Reward.Money.Amount", 0));

		int itemAmount = 0;

		// Old config syntax.
		if (config.getKeys(true).contains(configAchievement + ".Reward.Item.Amount")) {
			itemAmount = config.getInt(configAchievement + ".Reward.Item.Amount", 0);
		} else if (config.getKeys(true).contains(configAchievement + ".Reward.Item")) {
			// New config syntax. Name of item and quantity are on the same line, separated by a space.
			String materialAndQty = config.getString(configAchievement + ".Reward.Item", "");
			int indexOfAmount = 0;
			indexOfAmount = materialAndQty.indexOf(" ");
			if (indexOfAmount != -1)
				itemAmount = Integer.parseInt(materialAndQty.substring(indexOfAmount + 1));
		}

		String commandReward = config.getString(configAchievement + ".Reward.Command", "");

		// Parsing of config finished; we now dispatch the rewards accordingly.
		if (money > 0)
			rewardMoney(player, money);

		if (itemAmount > 0) {
			ItemStack item = this.getItemReward(player, configAchievement, itemAmount);
			if (player.getInventory().firstEmpty() != -1 && item != null)
				player.getInventory().addItem(item);
			else if (item != null)
				player.getWorld().dropItem(player.getLocation(), item);
		}

		if (commandReward.length() > 0) {
			commandReward = commandReward.replace("PLAYER", player.getName());
			// Multiple reward command can be set, separated by a semicolon and space. Extra parsing needed.
			String[] commands = commandReward.split("; ");
			for (String command : commands)
				plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);

			String rewardMsg = plugin.getPluginLang().getString("command-reward", "Reward command carried out!");
			if (!rewardCommandNotif || rewardMsg.length() == 0)
				return;

			player.sendMessage(plugin.getChatHeader() + rewardMsg);
		}
	}
}
