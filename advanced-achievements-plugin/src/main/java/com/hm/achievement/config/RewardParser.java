package com.hm.achievement.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.text.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.domain.Reward;
import com.hm.achievement.utils.MaterialHelper;
import com.hm.achievement.utils.StringHelper;

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
	private final Server server;
	private final MaterialHelper materialHelper;
	private final int serverVersion;

	// Used for Vault plugin integration.
	private Economy economy;

	@Inject
	public RewardParser(@Named("main") YamlConfiguration mainConfig, @Named("lang") YamlConfiguration langConfig,
			AdvancedAchievements advancedAchievements, MaterialHelper materialHelper, int serverVersion) {
		this.mainConfig = mainConfig;
		this.langConfig = langConfig;
		this.materialHelper = materialHelper;
		this.serverVersion = serverVersion;
		this.server = advancedAchievements.getServer();
		// Try to retrieve an Economy instance from Vault.
		if (server.getPluginManager().isPluginEnabled("Vault")) {
			RegisteredServiceProvider<Economy> rsp = server.getServicesManager().getRegistration(Economy.class);
			if (rsp != null) {
				economy = rsp.getProvider();
			}
		}
	}

	public Economy getEconomy() {
		return economy;
	}

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
		List<ItemStack> itemStacks = new ArrayList<>();

		String itemPath = configSection.contains("Item") ? "Item" : "Items";
		for (String item : getOneOrManyConfigStrings(configSection, itemPath)) {
			if (!item.contains(" ")) {
				continue;
			}
			String[] parts = StringUtils.split(item);
			Optional<Material> rewardMaterial = materialHelper.matchMaterial(parts[0],
					"config.yml (" + (configSection.getCurrentPath() + ".Item") + ")");
			if (rewardMaterial.isPresent()) {
				int amount = NumberUtils.toInt(parts[1], 1);
				ItemStack itemStack = new ItemStack(rewardMaterial.get(), amount);
				String name = StringUtils.join(parts, " ", 2, parts.length);
				if (name.isEmpty()) {
					// Convert the item stack material to an item name in a readable format.
					name = WordUtils.capitalizeFully(itemStack.getType().toString().replace('_', ' '));
				} else if (itemStack.hasItemMeta()) {
					ItemMeta itemMeta = itemStack.getItemMeta();
					itemMeta.setDisplayName(name);
					itemStack.setItemMeta(itemMeta);
				}
				listTexts.add(StringUtils.replaceEach(langConfig.getString("list-reward-item"),
						new String[] { "AMOUNT", "ITEM" }, new String[] { Integer.toString(amount), name }));
				chatTexts.add(StringUtils.replaceEach(langConfig.getString("item-reward-received"),
						new String[] { "AMOUNT", "ITEM" }, new String[] { Integer.toString(amount), name }));
				itemStacks.add(itemStack);
			}
		}
		Consumer<Player> rewarder = player -> itemStacks.forEach(item -> {
			ItemStack playerItem = item.clone();
			ItemMeta itemMeta = playerItem.getItemMeta();
			if (itemMeta != null && itemMeta.hasDisplayName()) {
				itemMeta.setDisplayName(StringHelper.replacePlayerPlaceholders(itemMeta.getDisplayName(), player));
				playerItem.setItemMeta(itemMeta);
			}
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
		String displayPath = configSection.contains("Command") ? "Command.Display" : "Commands.Display";
		List<String> listTexts = getOneOrManyConfigStrings(configSection, displayPath);
		List<String> chatTexts = listTexts.stream()
				.map(message -> StringUtils.replace(langConfig.getString("custom-command-reward"), "MESSAGE", message))
				.collect(Collectors.toList());
		String executePath = configSection.contains("Command") ? "Command.Execute" : "Commands.Execute";
		Consumer<Player> rewarder = player -> getOneOrManyConfigStrings(configSection, executePath).stream()
				.map(command -> StringHelper.replacePlayerPlaceholders(command, player))
				.forEach(command -> server.dispatchCommand(server.getConsoleSender(), command));
		return new Reward(listTexts, chatTexts, rewarder);
	}

	private List<String> getOneOrManyConfigStrings(ConfigurationSection configSection, String path) {
		if (configSection.isList(path)) {
			// Real YAML list.
			return configSection.getStringList(path);
		}
		String configString = configSection.getString(path);
		if (configString != null) {
			// Either a list of strings separate by "; " (old configuration style), or a single string.
			return Arrays.asList(MULTIPLE_REWARDS_SPLITTER.split(StringUtils.normalizeSpace(configString)));
		}
		return Collections.emptyList();
	}

}
