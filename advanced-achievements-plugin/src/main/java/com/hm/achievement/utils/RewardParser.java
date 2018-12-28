package com.hm.achievement.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;

import com.hm.achievement.lang.LangHelper;
import com.hm.achievement.lang.RewardLang;
import com.hm.achievement.lifecycle.Reloadable;
import com.hm.mcshared.file.CommentedYamlConfiguration;

import net.milkbowl.vault.economy.Economy;

/**
 * Class in charge of handling the rewards for achievements.
 *
 * @author Pyves
 */
@Singleton
public class RewardParser implements Reloadable {

	private static final Pattern MULTIPLE_REWARD_COMMANDS_SPLITTER = Pattern.compile(";\\s*");

	private final CommentedYamlConfiguration mainConfig;
	private final CommentedYamlConfiguration langConfig;
	private final MaterialHelper materialHelper;

	private String langListRewardMoney;
	private String langListRewardItem;
	private String langListRewardCommand;
	private String langListRewardExperience;
	private String langListRewardIncreaseMaxHealth;
	private String langListRewardIncreaseMaxOxygen;
	// Used for Vault plugin integration.
	private Economy economy;

	@Inject
	public RewardParser(@Named("main") CommentedYamlConfiguration mainConfig,
			@Named("lang") CommentedYamlConfiguration langConfig, MaterialHelper materialHelper) {
		this.mainConfig = mainConfig;
		this.langConfig = langConfig;
		this.materialHelper = materialHelper;
		// Try to retrieve an Economy instance from Vault.
		if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
			RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
			if (rsp != null) {
				economy = rsp.getProvider();
			}
		}
	}

	@Override
	public void extractConfigurationParameters() {
		langListRewardMoney = LangHelper.get(RewardLang.MONEY, langConfig);
		langListRewardItem = LangHelper.get(RewardLang.ITEM, langConfig);
		langListRewardCommand = LangHelper.get(RewardLang.COMMAND, langConfig);
		langListRewardExperience = LangHelper.get(RewardLang.EXPERIENCE, langConfig);
		langListRewardIncreaseMaxHealth = LangHelper.get(RewardLang.INCREASE_MAX_HEALTH, langConfig);
		langListRewardIncreaseMaxOxygen = LangHelper.get(RewardLang.INCREASE_MAX_OXYGEN, langConfig);
	}

	public Economy getEconomy() {
		return economy;
	}

	/**
	 * Constructs the listing of an achievement's rewards with strings coming from language file.
	 *
	 * @param path achievement path
	 * @return type(s) of the achievement reward as an array of strings
	 */
	public List<String> getRewardListing(String path) {
		List<String> rewardTypes = new ArrayList<>();
		Set<String> keyNames = mainConfig.getKeys(true);

		if (economy != null && keyNames.contains(path + ".Money")) {
			int amount = getRewardAmount(path, "Money");
			rewardTypes.add(StringUtils.replaceOnce(langListRewardMoney, "AMOUNT", amount + " " + getCurrencyName(amount)));
		}

		if (keyNames.contains(path + ".Item")) {
			int amount = getItemAmount(path);
			String name = getItemName(path);
			if (name.isEmpty()) {
				name = getItemName(getItemReward(path));
			}
			rewardTypes.add(StringUtils.replaceEach(langListRewardItem, new String[] { "AMOUNT", "ITEM" },
					new String[] { Integer.toString(amount), name }));
		}

		if (keyNames.contains(path + ".Experience")) {
			int amount = getRewardAmount(path, "Experience");
			rewardTypes.add(StringUtils.replaceOnce(langListRewardExperience, "AMOUNT", Integer.toString(amount)));
		}

		if (keyNames.contains(path + ".IncreaseMaxHealth")) {
			int amount = getRewardAmount(path, "IncreaseMaxHealth");
			rewardTypes.add(StringUtils.replaceOnce(langListRewardIncreaseMaxHealth, "AMOUNT", Integer.toString(amount)));
		}

		if (keyNames.contains(path + ".IncreaseMaxOxygen")) {
			int amount = getRewardAmount(path, "IncreaseMaxOxygen");
			rewardTypes.add(StringUtils.replaceOnce(langListRewardIncreaseMaxOxygen, "AMOUNT", Integer.toString(amount)));
		}

		if (keyNames.contains(path + ".Command")) {
			List<String> messages = getCustomCommandMessages(path);
			if (messages.isEmpty()) {
				rewardTypes.add(langListRewardCommand);
			} else {
				rewardTypes.addAll(messages);
			}
		}
		return rewardTypes;
	}

	/**
	 * Returns name of currency depending on amount.
	 *
	 * @param amount achievement configuration path
	 * @return the name of the currency
	 */
	public String getCurrencyName(int amount) {
		return amount > 1 ? economy.currencyNamePlural() : economy.currencyNameSingular();
	}

	/**
	 * Returns the name of an item reward, in a readable format.
	 *
	 * @param item the item reward
	 * @return the item name
	 */
	public String getItemName(ItemStack item) {
		return WordUtils.capitalizeFully(item.getType().toString().replace('_', ' '));
	}

	/**
	 * Extracts the money, experience, increased max health or increased max oxygen rewards amount from the
	 * configuration.
	 *
	 * @param path achievement configuration path
	 * @param type reward type
	 * @return the reward amount
	 */
	public int getRewardAmount(String path, String type) {
		// Supports both old and new plugin syntax (Amount used to be a separate sub-category).
		return Math.max(mainConfig.getInt(path + "." + type, 0), mainConfig.getInt(path + "." + type + ".Amount", 0));
	}

	/**
	 * Returns an item reward for a given achievement (specified in configuration file).
	 *
	 * @param path achievement configuration path
	 * @return ItemStack object corresponding to the reward
	 */
	public ItemStack getItemReward(String path) {
		int amount = getItemAmount(path);
		if (amount <= 0) {
			return null;
		}

		String typePath = path + ".Item.Type";
		if (mainConfig.getKeys(true).contains(typePath)) {
			// Old config syntax (type of item separated in a additional subcategory).
			Optional<Material> rewardMaterial = materialHelper.matchMaterial(mainConfig.getString(typePath),
					"config.yml (" + typePath + ")");
			if (rewardMaterial.isPresent()) {
				return new ItemStack(rewardMaterial.get(), amount);
			}
		} else {
			// New config syntax. Reward is of the form: "Item: coal 5 Christmas Coal"
			// The amount has already been parsed out and is provided by parameter amount.
			String itemPath = path + ".Item";
			String materialNameAndQty = mainConfig.getString(itemPath, "");
			String materialName = StringUtils.substringBefore(materialNameAndQty, " ");

			Optional<Material> rewardMaterial = materialHelper.matchMaterial(materialName, "config.yml (" + typePath + ")");
			if (rewardMaterial.isPresent()) {
				ItemStack item = new ItemStack(rewardMaterial.get(), amount);

				String name = getItemName(path);
				if (!name.isEmpty()) {
					ItemMeta meta = item.getItemMeta();
					meta.setDisplayName(name);
					item.setItemMeta(meta);
				}
				return item;
			}
		}
		return null;
	}

	/**
	 * Extracts the list of commands to be executed as rewards.
	 *
	 * @param path achievement configuration path
	 * @param player the player to parse commands for
	 * @return the array containing the commands to be performed as a reward
	 */
	public String[] getCommandRewards(String path, Player player) {
		String searchFrom = path + ".Command";
		if (mainConfig.isConfigurationSection(path + ".Command")) {
			searchFrom += ".Execute";
		}

		String commandReward = mainConfig.getString(searchFrom, null);
		if (commandReward == null) {
			return new String[0];
		}
		commandReward = StringUtils.replaceEach(commandReward,
				new String[] { "PLAYER_WORLD", "PLAYER_X", "PLAYER_Y", "PLAYER_Z", "PLAYER" },
				new String[] { player.getWorld().getName(), Integer.toString(player.getLocation().getBlockX()),
						Integer.toString(player.getLocation().getBlockY()),
						Integer.toString(player.getLocation().getBlockZ()), player.getName() });
		// Multiple reward commands can be set, separated by a semicolon and space. Extra parsing needed.
		return MULTIPLE_REWARD_COMMANDS_SPLITTER.split(commandReward);
	}

	/**
	 * Extracts custom command message from config. Might be null.
	 *
	 * @param path achievement configuration path
	 * @return the custom command message (null if not present)
	 * @author tassu
	 */
	public List<String> getCustomCommandMessages(String path) {
		if (!mainConfig.contains(path + ".Command.Display")) {
			return Collections.emptyList();
		}

		if (mainConfig.isList(path + ".Command.Display")) {
			return mainConfig.getStringList(path + ".Command.Display");
		}

		return Collections.singletonList(mainConfig.getString(path + ".Command.Display"));
	}

	/**
	 * Extracts the item reward amount from the configuration.
	 *
	 * @param path achievement configuration path
	 * @return the amount for an item reward
	 */
	private int getItemAmount(String path) {
		int itemAmount = 0;
		if (mainConfig.getKeys(true).contains(path + ".Item.Amount")) {
			// Old config syntax.
			itemAmount = mainConfig.getInt(path + ".Item.Amount", 0);
		} else if (mainConfig.getKeys(true).contains(path + ".Item")) {
			// New config syntax. Name of item and quantity are on the same line, separated by a space.
			String materialAndQty = StringUtils.normalizeSpace(mainConfig.getString(path + ".Item", ""));
			String intString = StringUtils.substringBefore(StringUtils.substringAfter(materialAndQty, " "), " ");
			itemAmount = Integer.parseInt(intString);
		}
		return itemAmount;
	}

	/**
	 * Extracts the item reward custom name from the configuration. Not supported for old config syntax.
	 *
	 * @param path achievement configuration path
	 * @return the custom name for an item reward
	 */
	private String getItemName(String path) {
		String configString = mainConfig.getString(path + ".Item", "");
		String[] splittedString = StringUtils.split(configString);
		return StringUtils.join(splittedString, " ", 2, splittedString.length).trim();
	}
}
