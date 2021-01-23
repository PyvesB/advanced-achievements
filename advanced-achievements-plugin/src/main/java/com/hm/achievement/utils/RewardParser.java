package com.hm.achievement.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.text.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;

import com.hm.achievement.domain.Reward;

import net.milkbowl.vault.economy.Economy;

/**
 * Class in charge of handling the rewards for achievements.
 *
 * @author Pyves
 */
@Singleton
public class RewardParser {

	private static final Pattern MULTIPLE_REWARDS_SPLITTER = Pattern.compile(";\\s*");

	private final YamlConfiguration mainConfig;
	private final YamlConfiguration langConfig;
	private final MaterialHelper materialHelper;
	private final int serverVersion;

	// Used for Vault plugin integration.
	private Economy economy;

	@Inject
	public RewardParser(@Named("main") YamlConfiguration mainConfig, @Named("lang") YamlConfiguration langConfig,
			MaterialHelper materialHelper, int serverVersion) {
		this.mainConfig = mainConfig;
		this.langConfig = langConfig;
		this.materialHelper = materialHelper;
		this.serverVersion = serverVersion;
		// Try to retrieve an Economy instance from Vault.
		if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
			RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
			if (rsp != null) {
				economy = rsp.getProvider();
			}
		}
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
	public List<Reward> parseRewards(String path) {
		ConfigurationSection configSection = mainConfig.getConfigurationSection(path + ".Reward");
		if (configSection == null) {
			configSection = mainConfig.getConfigurationSection(path + ".Rewards");
		}
		List<Reward> rewards = new ArrayList<>();
		if (configSection != null) {
			if (economy != null && configSection.contains("Money")) {
				rewards.add(parseMoneyReward(configSection));
			}
			if (configSection.contains("Item") || configSection.contains("Items")) {
				rewards.add(parseItemReward(configSection));
			}
			if (configSection.contains("Experience")) {
				rewards.add(parseExperienceReward(configSection));
			}
			if (configSection.contains("IncreaseMaxHealth")) {
				rewards.add(parseIncreaseMaxHealthReward(configSection));
			}
			if (configSection.contains("IncreaseMaxOxygen")) {
				rewards.add(parseIncreaseMaxOxygenReward(configSection));
			}
			if (configSection.contains("Command") || configSection.contains("Commands")) {
				rewards.add(parseCommandReward(configSection));
			}
		}
		return rewards;
	}

	private Reward parseMoneyReward(ConfigurationSection configSection) {
		int amount = configSection.getInt("Money");
		String currencyName = amount > 1 ? economy.currencyNamePlural() : economy.currencyNameSingular();
		String listText = StringUtils.replaceOnce(langConfig.getString("list-reward-money"), "AMOUNT",
				amount + " " + currencyName);
		String chatText = ChatColor.translateAlternateColorCodes('&',
				StringUtils.replaceOnce(langConfig.getString("money-reward-received"), "AMOUNT",
						amount + " " + currencyName));
		Consumer<Player> rewarder = player -> economy.depositPlayer(player, amount);
		return new Reward(Collections.singletonList(listText), Collections.singletonList(chatText), rewarder);
	}

	private Reward parseItemReward(ConfigurationSection configSection) {
		List<String> listTexts = new ArrayList<>();
		List<String> chatTexts = new ArrayList<>();
		List<ItemStack> items = new ArrayList<>();

		String itemString = configSection.getString("Item", configSection.getString("Items", ""));
		String[] itemStrings = MULTIPLE_REWARDS_SPLITTER.split(StringUtils.normalizeSpace(itemString));
		for (int i = 0; i < itemStrings.length; i++) {
			if (!itemStrings[i].contains(" ")) {
				continue;
			}
			String[] parts = StringUtils.split(itemStrings[i]);
			Optional<Material> rewardMaterial = materialHelper.matchMaterial(parts[0],
					"config.yml (" + (configSection.getCurrentPath() + ".Item") + ")");
			if (rewardMaterial.isPresent()) {
				int amount = NumberUtils.toInt(parts[1], 1);
				ItemStack item = new ItemStack(rewardMaterial.get(), amount);
				ItemMeta meta = item.getItemMeta();
				String name = StringUtils.join(parts, " ", 2, parts.length);
				if (name.isEmpty()) {
					name = getItemName(item);
				} else {
					meta.setDisplayName(name);
				}
				listTexts.add(StringUtils.replaceEach(langConfig.getString("list-reward-item"),
						new String[] { "AMOUNT", "ITEM" }, new String[] { Integer.toString(amount), name }));
				chatTexts.add(StringUtils.replaceEach(langConfig.getString("item-reward-received") + " ",
						new String[] { "AMOUNT", "ITEM" }, new String[] { Integer.toString(amount), name }));
				item.setItemMeta(meta);
				items.add(item);
			}
		}
		Consumer<Player> rewarder = player -> items.forEach(item -> {
			ItemStack playerItem = item.clone();
			ItemMeta itemMeta = playerItem.getItemMeta();
			if (itemMeta.hasDisplayName()) {
				itemMeta.setDisplayName(StringHelper.replacePlayerPlaceholders(itemMeta.getDisplayName(), player));
			}
			playerItem.setItemMeta(itemMeta);
			if (player.getInventory().firstEmpty() != -1) {
				player.getInventory().addItem(playerItem);
			} else {
				player.getWorld().dropItem(player.getLocation(), playerItem);
			}
		});
		return new Reward(listTexts, chatTexts, rewarder);
	}

	private Reward parseExperienceReward(ConfigurationSection configSection) {
		int amount = configSection.getInt("Experience");
		String listText = StringUtils.replaceOnce(langConfig.getString("list-reward-experience"), "AMOUNT",
				Integer.toString(amount));
		String chatText = ChatColor.translateAlternateColorCodes('&',
				StringUtils.replaceOnce(langConfig.getString("experience-reward-received"), "AMOUNT",
						Integer.toString(amount)));
		Consumer<Player> rewarder = player -> player.giveExp(amount);
		return new Reward(Collections.singletonList(listText), Collections.singletonList(chatText), rewarder);
	}

	private Reward parseIncreaseMaxHealthReward(ConfigurationSection configSection) {
		int amount = configSection.getInt("IncreaseMaxHealth");
		String listText = StringUtils.replaceOnce(langConfig.getString("list-reward-increase-max-health"), "AMOUNT",
				Integer.toString(amount));
		String chatText = ChatColor.translateAlternateColorCodes('&',
				StringUtils.replaceOnce(langConfig.getString("increase-max-health-reward-received"), "AMOUNT",
						Integer.toString(amount)));
		@SuppressWarnings("deprecation")
		Consumer<Player> rewarder = player -> {
			if (serverVersion >= 9) {
				AttributeInstance playerAttribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
				playerAttribute.setBaseValue(playerAttribute.getBaseValue() + amount);
			} else {
				player.setMaxHealth(player.getMaxHealth() + amount);
			}
		};
		return new Reward(Collections.singletonList(listText), Collections.singletonList(chatText), rewarder);
	}

	private Reward parseIncreaseMaxOxygenReward(ConfigurationSection configSection) {
		int amount = configSection.getInt("IncreaseMaxOxygen");
		String listText = StringUtils.replaceOnce(langConfig.getString("list-reward-increase-max-oxygen"), "AMOUNT",
				Integer.toString(amount));
		String chatText = ChatColor.translateAlternateColorCodes('&',
				StringUtils.replaceOnce(langConfig.getString("increase-max-oxygen-reward-received"), "AMOUNT",
						Integer.toString(amount)));
		Consumer<Player> rewarder = player -> player.setMaximumAir(player.getMaximumAir() + amount);
		return new Reward(Collections.singletonList(listText), Collections.singletonList(chatText), rewarder);
	}

	private Reward parseCommandReward(ConfigurationSection configSection) {
		List<String> listTexts = getCustomCommandMessages(configSection);
		List<String> chatTexts = new ArrayList<>();
		if (listTexts.isEmpty()) {
			listTexts.add(langConfig.getString("list-reward-command"));
			if (!langConfig.getString("command-reward").isEmpty()) {
				chatTexts.add(langConfig.getString("command-reward"));
			}
		}
		listTexts.stream()
				.map(message -> StringUtils.replace(langConfig.getString("custom-command-reward"), "MESSAGE", message))
				.forEach(chatTexts::add);

		Consumer<Player> rewarder = player -> getCommandRewards(configSection, player)
				.forEach(command -> Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command));

		return new Reward(listTexts, chatTexts, rewarder);
	}

	/**
	 * Returns the name of an item reward, in a readable format.
	 *
	 * @param item the item reward
	 * @return the item name
	 */
	private String getItemName(ItemStack item) {
		return WordUtils.capitalizeFully(item.getType().toString().replace('_', ' '));
	}

	/**
	 * Extracts the list of commands to be executed as rewards.
	 *
	 * @param configSection achievement configuration path
	 * @param player the player to parse commands for
	 * @return the array containing the commands to be performed as a reward
	 */
	private List<String> getCommandRewards(ConfigurationSection configSection, Player player) {
		String searchFrom = configSection.contains("Command") ? "Command" : "Commands";
		if (configSection.isConfigurationSection(searchFrom)) {
			searchFrom += ".Execute";
		}

		String commandReward = configSection.getString(searchFrom);
		if (commandReward == null) {
			return Collections.emptyList();
		}
		// Multiple reward commands can be set, separated by a semicolon and space. Extra parsing needed.
		return Arrays.asList(MULTIPLE_REWARDS_SPLITTER.split(StringHelper.replacePlayerPlaceholders(commandReward, player)));
	}

	/**
	 * Extracts custom command message from config. Might be null.
	 *
	 * @param configSection achievement configuration path
	 * @return the custom command message (null if not present)
	 * @author tassu
	 */
	private List<String> getCustomCommandMessages(ConfigurationSection configSection) {
		if (configSection.isList("Command.Display")) {
			return configSection.getStringList("Command.Display");
		}

		List<String> messages = new ArrayList<>();
		if (configSection.contains("Command.Display")) {
			messages.add(configSection.getString("Command.Display"));
		}
		return messages;
	}

}
