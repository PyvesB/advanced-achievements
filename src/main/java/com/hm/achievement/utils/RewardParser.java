package com.hm.achievement.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;

import com.hm.achievement.AdvancedAchievements;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.item.ItemInfo;
import net.milkbowl.vault.item.Items;

/**
 * Class in charge of handling the rewards for achievements.
 * 
 * @author Pyves
 */
public class RewardParser implements Reloadable {

	private final AdvancedAchievements plugin;

	private String langListRewardMoney;
	private String langListRewardItem;
	private String langListRewardCommand;
	private String langListRewardExperience;
	// Used for Vault plugin integration.
	private Economy economy;

	public RewardParser(AdvancedAchievements achievement) {
		this.plugin = achievement;
	}

	@Override
	public void extractConfigurationParameters() {
		langListRewardMoney = plugin.getPluginLang().getString("list-reward-money", "receive AMOUNT");
		langListRewardItem = plugin.getPluginLang().getString("list-reward-item", "receive AMOUNT ITEM");
		langListRewardCommand = plugin.getPluginLang().getString("list-reward-command", "other");
		langListRewardExperience = plugin.getPluginLang().getString("list-reward-experience",
				"receive AMOUNT experience");
	}

	public Economy getEconomy() {
		return economy;
	}

	/**
	 * Tries to hook up with Vault, and log if this is called on plugin initialisation.
	 * 
	 * @param log
	 * @return true if Vault available, false otherwise
	 */
	public boolean isEconomySet(boolean log) {
		if (economy != null) {
			return true;
		}
		try {
			RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServer().getServicesManager()
					.getRegistration(net.milkbowl.vault.economy.Economy.class);
			if (economyProvider != null) {
				economy = economyProvider.getProvider();
			}
			return economy != null;
		} catch (NoClassDefFoundError e) {
			if (log) {
				plugin.getLogger().warning("Attempt to hook up with Vault failed. Money rewardParser ignored.");
			}
			return false;
		}
	}

	/**
	 * Constructs the listing of an achievement's rewards with strings coming from language file.
	 * 
	 * @param configAchievement
	 * @return type(s) of the achievement reward as an array of strings
	 */
	public List<String> getRewardListing(String configAchievement) {
		List<String> rewardTypes = new ArrayList<>();
		Set<String> keyNames = plugin.getPluginConfig().getKeys(true);

		if (isEconomySet(false) && keyNames.contains(configAchievement + ".Reward.Money")) {
			int amount = getRewardAmount(configAchievement, "Money");
			rewardTypes.add(
					StringUtils.replaceOnce(langListRewardMoney, "AMOUNT", amount + " " + getCurrencyName(amount)));
		}

		if (keyNames.contains(configAchievement + ".Reward.Item")) {
			int amount = getItemAmount(configAchievement);
			String name = getItemName(getItemReward(configAchievement));
			rewardTypes.add(StringUtils.replaceEach(langListRewardItem, new String[] { "AMOUNT", "ITEM" },
					new String[] { Integer.toString(amount), name }));
		}

		if (keyNames.contains(configAchievement + ".Reward.Command")) {
			rewardTypes.add(langListRewardCommand);
		}

		if (keyNames.contains(configAchievement + ".Reward.Experience")) {
			int amount = getRewardAmount(configAchievement, "Experience");
			rewardTypes.add(StringUtils.replaceOnce(langListRewardExperience, "AMOUNT", Integer.toString(amount)));
		}
		return rewardTypes;
	}

	/**
	 * Returns name of currency depending on amount.
	 * 
	 * @param amount
	 * @return
	 */
	public String getCurrencyName(int amount) {
		String currencyName;
		if (amount > 1) {
			currencyName = economy.currencyNamePlural();
		} else {
			currencyName = economy.currencyNameSingular();
		}
		return currencyName;
	}

	/**
	 * Returns the name of an item reward.
	 * 
	 * @param item
	 * @return
	 */
	public String getItemName(ItemStack item) {
		// Return Vault name of object if available.
		if (isEconomySet(false)) {
			ItemInfo itemInfo = Items.itemByStack(item);
			if (itemInfo != null) {
				return itemInfo.getName();
			}
		}
		// Vault name of object not available.
		return StringUtils.replace(item.getType().toString(), "_", " ").toLowerCase();
	}

	/**
	 * Extracts the money or experience reward amount from the configuration.
	 * 
	 * @param configAchievement
	 * @return
	 */
	public int getRewardAmount(String configAchievement, String type) {
		// Supports both old and new plugin syntax (Amount used to be a separate sub-category).
		return Math.max(plugin.getPluginConfig().getInt(configAchievement + ".Reward." + type, 0),
				plugin.getPluginConfig().getInt(configAchievement + ".Reward." + type + ".Amount", 0));
	}

	/**
	 * Returns an item reward for a given achievement (specified in configuration file).
	 * 
	 * @param player
	 * @param configAchievement
	 * @param amount
	 * @return ItemStack object corresponding to the reward
	 */
	public ItemStack getItemReward(String configAchievement) {
		int amount = getItemAmount(configAchievement);
		if (amount <= 0) {
			return null;
		}

		ItemStack item = null;
		AchievementCommentedYamlConfiguration config = plugin.getPluginConfig();
		if (config.getKeys(true).contains(configAchievement + ".Reward.Item.Type")) {
			// Old config syntax (type of item separated in a additional subcategory).
			Material rewardMaterial = Material
					.getMaterial(config.getString(configAchievement + ".Reward.Item.Type", "stone").toUpperCase());
			if (rewardMaterial != null) {
				item = new ItemStack(rewardMaterial, amount);
			}
		} else {
			// New config syntax. Reward is of the form: "Item: coal 5"
			// The amount has already been parsed out and is provided by parameter amount.
			String materialNameAndQty = config.getString(configAchievement + ".Reward.Item", "stone");
			int spaceIndex = materialNameAndQty.indexOf(' ');

			String materialName;
			if (spaceIndex > 0) {
				materialName = materialNameAndQty.toUpperCase().substring(0, spaceIndex);
			} else {
				materialName = materialNameAndQty.toUpperCase();
			}

			Material rewardMaterial = Material.getMaterial(materialName);
			if (rewardMaterial != null) {
				item = new ItemStack(rewardMaterial, amount);
			}
		}
		if (item == null) {
			plugin.getLogger().warning("Invalid item reward for achievement \""
					+ config.getString(configAchievement + ".Name") + "\". Please specify a valid Material name.");
		}
		return item;
	}

	/**
	 * Extracts the list of commands to be executed as rewards.
	 * 
	 * @param configAchievement
	 * @param player
	 * @return
	 */
	public String[] getCommandRewards(String configAchievement, Player player) {
		String commandReward = plugin.getPluginConfig().getString(configAchievement + ".Reward.Command", null);
		if (commandReward == null) {
			return new String[0];
		}
		commandReward = StringUtils.replace(commandReward, "PLAYER", player.getName());
		// Multiple reward commands can be set, separated by a semicolon and space. Extra parsing needed.
		return commandReward.split("; ");
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
		if (config.getKeys(true).contains(configAchievement + ".Reward.Item.Amount")) {
			// Old config syntax.
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
}
