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
import org.apache.commons.lang3.math.NumberUtils;
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

	private static final Pattern MULTIPLE_REWARDS_SPLITTER = Pattern.compile(";\\s*");

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
	 * @param player
	 * @return type(s) of the achievement reward as an array of strings
	 */
	public List<String> getRewardListing(String path, Player player) {
		List<String> rewardTypes = new ArrayList<>();
		Set<String> keyNames = mainConfig.getKeys(true);

		if (economy != null && keyNames.contains(path + ".Money")) {
			int amount = getRewardAmount(path, "Money");
			rewardTypes.add(StringUtils.replaceOnce(langListRewardMoney, "AMOUNT", amount + " " + getCurrencyName(amount)));
		}

		if (keyNames.contains(path + ".Item")) {
			ItemStack[] items = getItemRewards(path, player);
			for (ItemStack item : items) {
				ItemMeta itemMeta = item.getItemMeta();
				String name = itemMeta.hasDisplayName() ? itemMeta.getDisplayName() : getItemName(item);
				rewardTypes.add(StringUtils.replaceEach(langListRewardItem, new String[] { "AMOUNT", "ITEM" },
						new String[] { Integer.toString(item.getAmount()), name }));
			}
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
		return mainConfig.getInt(path + "." + type);
	}

	/**
	 * Returns an item reward for a given achievement (specified in configuration file). Reward is of the form: "Item:
	 * coal 5 Christmas Coal"
	 *
	 * @param path achievement configuration path
	 * @param player
	 * @return ItemStack object corresponding to the reward
	 */
	public ItemStack[] getItemRewards(String path, Player player) {
		String itemString = StringUtils.normalizeSpace(mainConfig.getString(path + ".Item", ""));
		if (!itemString.contains(" ")) {
			return null;
		}

		String[] itemStrings = MULTIPLE_REWARDS_SPLITTER.split(itemString);
		ItemStack[] itemData = new ItemStack[itemStrings.length];
		for (int i = 0; i < itemStrings.length; i++) {
			String[] parts = StringUtils.split(itemStrings[i]);
			Optional<Material> rewardMaterial = materialHelper.matchMaterial(parts[0],
					"config.yml (" + (path + ".Item") + ")");
			if (rewardMaterial.isPresent()) {
				ItemStack item = new ItemStack(rewardMaterial.get(), NumberUtils.toInt(parts[1], 1));
				ItemMeta meta = item.getItemMeta();
				String name = replacePlayerPlaceholders(StringUtils.join(parts, " ", 2, parts.length), player);
				if (!name.isEmpty()) {
					meta.setDisplayName(name);
				}
				item.setItemMeta(meta);
				itemData[i] = item;
			} else {
				return null;
			}
		}
		return itemData;
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
		// Multiple reward commands can be set, separated by a semicolon and space. Extra parsing needed.
		return MULTIPLE_REWARDS_SPLITTER.split(replacePlayerPlaceholders(commandReward, player));
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
	 * Replaces supported placeholders with player-specific information.
	 * 
	 * @param str
	 * @param player
	 * 
	 * @return the input string with all placeholders resolved
	 */
	private String replacePlayerPlaceholders(String str, Player player) {
		return StringUtils.replaceEach(str,
				new String[] { "PLAYER_WORLD", "PLAYER_X", "PLAYER_Y", "PLAYER_Z", "PLAYER" },
				new String[] { player.getWorld().getName(), Integer.toString(player.getLocation().getBlockX()),
						Integer.toString(player.getLocation().getBlockY()),
						Integer.toString(player.getLocation().getBlockZ()), player.getName() });
	}
}
