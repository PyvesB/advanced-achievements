package com.hm.achievement;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.hm.achievement.utils.AchievementCommentedYamlConfiguration;

import net.milkbowl.vault.item.ItemInfo;
import net.milkbowl.vault.item.Items;

/**
 * Class in charge of handling the rewards for achievements.
 * 
 * @author Pyves
 */
public class AchievementRewards {

	private final AdvancedAchievements plugin;
	private final boolean rewardCommandNotif;

	public AchievementRewards(AdvancedAchievements achievement) {

		this.plugin = achievement;
		// No longer available in default config, kept for compatibility with versions prior to 2.1; defines whether
		// a player is notified in case of a command reward.
		rewardCommandNotif = plugin.getPluginConfig().getBoolean("RewardCommandNotif", true);
	}

	/**
	 * Constructs the listing of an achievement's rewards with strings coming from language file.
	 * 
	 * @param configAchievement
	 * @return type(s) of the achievement reward as an array of strings
	 */
	public List<String> getRewardListing(String configAchievement) {

		ArrayList<String> rewardType = new ArrayList<>();
		Set<String> keyNames = plugin.getPluginConfig().getKeys(true);

		AchievementCommentedYamlConfiguration pluginLang = plugin.getPluginLang();
		if (plugin.setUpEconomy(false) && keyNames.contains(configAchievement + ".Reward.Money")) {
			int amount = getMoneyAmount(configAchievement);
			rewardType.add(StringUtils.replaceOnce(pluginLang.getString("list-reward-money", "receive AMOUNT"),
					"AMOUNT",
					amount + " " + getCurrencyName(amount)));
		}
		if (keyNames.contains(configAchievement + ".Reward.Item")) {
			int amount = getItemAmount(configAchievement);
			String name = getItemName(getItemReward(configAchievement, amount));
			rewardType.add(StringUtils.replaceEach(pluginLang.getString("list-reward-item", "receive AMOUNT ITEM"),
					new String[] { "AMOUNT", "ITEM" }, new String[] { Integer.toString(amount), name }));
		}
		if (keyNames.contains(configAchievement + ".Reward.Command")) {
			rewardType.add(pluginLang.getString("list-reward-command", "other"));
		}
		return rewardType;
	}

	/**
	 * Main reward manager; parses the configuration and gives rewards accordingly.
	 * 
	 * @param player
	 * @param configAchievement
	 */
	public void checkConfig(Player player, String configAchievement) {

		int moneyAmount = getMoneyAmount(configAchievement);
		int itemAmount = getItemAmount(configAchievement);

		String commandReward = plugin.getPluginConfig().getString(configAchievement + ".Reward.Command", "");

		// Parsing of config finished; we now dispatch the rewards accordingly.
		if (moneyAmount > 0) {
			rewardMoney(player, moneyAmount);
		}

		if (itemAmount > 0) {
			ItemStack item = getItemReward(configAchievement, itemAmount);
			if (player.getInventory().firstEmpty() != -1 && item != null) {
				player.getInventory().addItem(item);
			} else if (item != null) {
				player.getWorld().dropItem(player.getLocation(), item);
			}
			player.sendMessage(plugin.getChatHeader()
					+ plugin.getPluginLang().getString("item-reward-received", "You received an item reward:") + " "
					+ getItemName(item));
		}

		if (commandReward.length() > 0) {
			commandReward = StringUtils.replace(commandReward, "PLAYER", player.getName());
			// Multiple reward commands can be set, separated by a semicolon and space. Extra parsing needed.
			String[] commands = commandReward.split("; ");
			for (String command : commands) {
				plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
			}

			String rewardMsg = plugin.getPluginLang().getString("command-reward", "Reward command carried out!");
			if (!rewardCommandNotif || rewardMsg.length() == 0) {
				return;
			}
			player.sendMessage(plugin.getChatHeader() + rewardMsg);
		}
	}

	/**
	 * Gives a money reward to a player.
	 * 
	 * @param player
	 * @param amount
	 */
	@SuppressWarnings("deprecation")
	private void rewardMoney(Player player, int amount) {

		if (plugin.setUpEconomy(true)) {
			try {
				plugin.getEconomy().depositPlayer(player, amount);
			} catch (NoSuchMethodError e) {
				// Deprecated method, but was the only one existing prior to Vault 1.4.
				plugin.getEconomy().depositPlayer(player.getName(), amount);
			}

			String currencyName = getCurrencyName(amount);

			player.sendMessage(plugin.getChatHeader() + ChatColor.translateAlternateColorCodes('&',
					StringUtils.replaceOnce(
							plugin.getPluginLang().getString("money-reward-received", "You received: AMOUNT!"),
							"AMOUNT", amount + " " + currencyName)));
		}
	}

	/**
	 * Returns an item reward for a given achievement (specified in configuration file).
	 * 
	 * @param player
	 * @param ach
	 * @param amount
	 * @return ItemStack object corresponding to the reward
	 */
	private ItemStack getItemReward(String ach, int amount) {

		ItemStack item = null;
		AchievementCommentedYamlConfiguration config = plugin.getPluginConfig();

		if (config.getKeys(true).contains(ach + ".Reward.Item.Type")) {
			// Old config syntax (type of item separated in a additional subcategory).
			Material rewardMaterial = Material
					.getMaterial(config.getString(ach + ".Reward.Item.Type", "stone").toUpperCase());
			if (rewardMaterial != null) {
				item = new ItemStack(rewardMaterial, amount);
			}
		} else {
			// New config syntax. Reward is of the form: "Item: coal 5"
			// The amount has already been parsed out and is provided by parameter amount.
			String materialNameAndQty = config.getString(ach + ".Reward.Item", "stone");
			int indexSpace = materialNameAndQty.indexOf(' ');

			String materialName;
			if (indexSpace > 0) {
				materialName = materialNameAndQty.toUpperCase().substring(0, indexSpace);
			} else {
				materialName = materialNameAndQty.toUpperCase();
			}

			Material rewardMaterial = Material.getMaterial(materialName);
			if (rewardMaterial != null) {
				item = new ItemStack(rewardMaterial, amount);
			}
		}
		if (item == null) {
			plugin.getLogger().warning("Invalid item reward for achievement \"" + config.getString(ach + ".Name")
					+ "\". Please specify a valid Material name.");
		}
		return item;
	}

	/**
	 * Extracts the money reward amount from the configuration.
	 * 
	 * @param configAchievement
	 * @return
	 */
	private int getMoneyAmount(String configAchievement) {

		// Supports both old and new plugin syntax (Amount used to be a separate sub-category).
		return Math.max(plugin.getPluginConfig().getInt(configAchievement + ".Reward.Money", 0),
				plugin.getPluginConfig().getInt(configAchievement + ".Reward.Money.Amount", 0));
	}

	/**
	 * Extracts the item reward amount from the configuration.
	 * 
	 * @param configAchievement
	 * @return
	 */
	private int getItemAmount(String configAchievement) {

		AchievementCommentedYamlConfiguration config = plugin.getPluginConfig();

		int itemAmount = 0;
		// Old config syntax.
		if (config.getKeys(true).contains(configAchievement + ".Reward.Item.Amount")) {
			itemAmount = config.getInt(configAchievement + ".Reward.Item.Amount", 0);
		} else if (config.getKeys(true).contains(configAchievement + ".Reward.Item")) {
			// New config syntax. Name of item and quantity are on the same line, separated by a space.
			String materialAndQty = config.getString(configAchievement + ".Reward.Item", "");
			int indexOfAmount = materialAndQty.indexOf(' ');
			if (indexOfAmount != -1) {
				itemAmount = Integer.parseInt(materialAndQty.substring(indexOfAmount + 1));
			}
		}
		return itemAmount;
	}

	/**
	 * Returns name of currency depending on amount.
	 * 
	 * @param amount
	 * @return
	 */
	private String getCurrencyName(int amount) {

		String currencyName;
		if (amount > 1) {
			currencyName = plugin.getEconomy().currencyNamePlural();
		} else {
			currencyName = plugin.getEconomy().currencyNameSingular();
		}
		return currencyName;
	}

	/**
	 * Returns the name of an item reward.
	 * 
	 * @param item
	 * @return
	 */
	private String getItemName(ItemStack item) {

		// Return Vault name of object if available.
		if (plugin.setUpEconomy(false)) {
			ItemInfo itemInfo = Items.itemByStack(item);
			if (itemInfo != null) {
				return itemInfo.getName();
			}
		}
		// Vault name of object not available.
		return StringUtils.replace(item.getType().toString(), "_", " ").toLowerCase();
	}
}
