package com.hm.achievement.listener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.advancement.Advancement;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.advancement.AchievementAdvancement;
import com.hm.achievement.advancement.AdvancementManager;
import com.hm.achievement.command.executable.ToggleCommand;
import com.hm.achievement.db.AbstractDatabaseManager;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.lang.LangHelper;
import com.hm.achievement.lang.ListenerLang;
import com.hm.achievement.lifecycle.Reloadable;
import com.hm.achievement.utils.PlayerAdvancedAchievementEvent;
import com.hm.achievement.utils.RewardParser;
import com.hm.achievement.utils.SoundPlayer;
import com.hm.mcshared.file.CommentedYamlConfiguration;
import com.hm.mcshared.particle.FancyMessageSender;
import com.hm.mcshared.particle.ParticleEffect;

import net.milkbowl.vault.economy.Economy;

/**
 * Listener class to deal with achievement receptions: rewards, display and database operations.
 *
 * @author Pyves
 */
@Singleton
public class PlayerAdvancedAchievementListener implements Listener, Reloadable {

	private static final Random RANDOM = new Random();

	private final CommentedYamlConfiguration mainConfig;
	private final CommentedYamlConfiguration langConfig;
	private final int serverVersion;
	private final Logger logger;
	private final StringBuilder pluginHeader;
	private final CacheManager cacheManager;
	private final AdvancedAchievements advancedAchievements;
	private final RewardParser rewardParser;
	private final Map<String, String> namesToDisplayNames;
	private final AbstractDatabaseManager databaseManager;
	private final ToggleCommand toggleCommand;
	private final FireworkListener fireworkListener;
	private final SoundPlayer soundPlayer;

	private String configFireworkStyle;
	private boolean configFirework;
	private boolean configSimplifiedReception;
	private boolean configTitleScreen;
	private boolean configNotifyOtherPlayers;
	private boolean configActionBarNotify;
	private boolean configHoverableReceiverChatText;
	private boolean configReceiverChatMessages;

	private String langCommandReward;
	private String langAchievementReceived;
	private String langItemRewardReceived;
	private String langMoneyRewardReceived;
	private String langExperienceRewardReceived;
	private String langIncreaseMaxHealthRewardReceived;
	private String langIncreaseMaxOxygenRewardReceived;
	private String langAchievementNew;
	private String langCustomMessageCommandReward;
	private String langAllAchievementsReceived;

	@Inject
	public PlayerAdvancedAchievementListener(@Named("main") CommentedYamlConfiguration mainConfig,
			@Named("lang") CommentedYamlConfiguration langConfig, int serverVersion, Logger logger,
			StringBuilder pluginHeader, CacheManager cacheManager, AdvancedAchievements advancedAchievements,
			RewardParser rewardParser, @Named("ntd") Map<String, String> namesToDisplayNames,
			AbstractDatabaseManager databaseManager, ToggleCommand toggleCommand, FireworkListener fireworkListener,
			SoundPlayer soundPlayer) {
		this.mainConfig = mainConfig;
		this.langConfig = langConfig;
		this.serverVersion = serverVersion;
		this.logger = logger;
		this.pluginHeader = pluginHeader;
		this.cacheManager = cacheManager;
		this.advancedAchievements = advancedAchievements;
		this.rewardParser = rewardParser;
		this.namesToDisplayNames = namesToDisplayNames;
		this.databaseManager = databaseManager;
		this.toggleCommand = toggleCommand;
		this.fireworkListener = fireworkListener;
		this.soundPlayer = soundPlayer;
	}

	@Override
	public void extractConfigurationParameters() {
		configFireworkStyle = mainConfig.getString("FireworkStyle", "BALL_LARGE").toUpperCase();
		if (!"RANDOM".equals(configFireworkStyle) && !EnumUtils.isValidEnum(Type.class, configFireworkStyle)) {
			configFireworkStyle = Type.BALL_LARGE.name();
			logger.warning("Failed to load FireworkStyle, using ball_large instead. Please use one of the following: "
					+ "ball_large, ball, burst, creeper, star or random.");
		}
		configFirework = mainConfig.getBoolean("Firework", true);
		configSimplifiedReception = mainConfig.getBoolean("SimplifiedReception");
		configTitleScreen = mainConfig.getBoolean("TitleScreen", true);
		// Title screens introduced in Minecraft 1.8. Automatically relevant parameter for older versions.
		if (configTitleScreen && serverVersion < 8) {
			configTitleScreen = false;
		}
		configNotifyOtherPlayers = mainConfig.getBoolean("NotifyOtherPlayers");
		configActionBarNotify = mainConfig.getBoolean("ActionBarNotify");
		// Action bars introduced in Minecraft 1.8. Automatically relevant parameter for older versions.
		if (configActionBarNotify && serverVersion < 8) {
			configActionBarNotify = false;
		}
		configHoverableReceiverChatText = mainConfig.getBoolean("HoverableReceiverChatText");
		// Hoverable chat messages introduced in Minecraft 1.8. Automatically relevant parameter for older versions.
		if (configHoverableReceiverChatText && serverVersion < 8) {
			configHoverableReceiverChatText = false;
		}
		configReceiverChatMessages = mainConfig.getBoolean("ReceiverChatMessages", true);

		langCommandReward = LangHelper.get(ListenerLang.COMMAND_REWARD, langConfig);
		langAchievementReceived = LangHelper.get(ListenerLang.ACHIEVEMENT_RECEIVED, langConfig) + " " + ChatColor.WHITE;
		langItemRewardReceived = LangHelper.get(ListenerLang.ITEM_REWARD_RECEIVED, langConfig) + " ";
		langMoneyRewardReceived = LangHelper.get(ListenerLang.MONEY_REWARD_RECEIVED, langConfig);
		langExperienceRewardReceived = LangHelper.get(ListenerLang.EXPERIENCE_REWARD_RECEIVED, langConfig);
		langIncreaseMaxHealthRewardReceived = LangHelper.get(ListenerLang.INCREASE_MAX_HEALTH_REWARD_RECEIVED, langConfig);
		langIncreaseMaxOxygenRewardReceived = LangHelper.get(ListenerLang.INCREASE_MAX_OXYGEN_REWARD_RECEIVED, langConfig);
		langAchievementNew = pluginHeader + LangHelper.get(ListenerLang.ACHIEVEMENT_NEW, langConfig) + " " + ChatColor.WHITE;
		langCustomMessageCommandReward = LangHelper.get(ListenerLang.CUSTOM_COMMAND_REWARD, langConfig);
		langAllAchievementsReceived = pluginHeader + LangHelper.get(ListenerLang.ALL_ACHIEVEMENTS_RECEIVED, langConfig);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerAdvancedAchievementReception(PlayerAdvancedAchievementEvent event) {
		Player player = event.getPlayer();
		// Achievement could have already been received if MultiCommand is set to true in the configuration.
		if (!cacheManager.hasPlayerAchievement(player.getUniqueId(), event.getName())) {
			cacheManager.registerNewlyReceivedAchievement(player.getUniqueId(), event.getName());

			if (serverVersion >= 12) {
				Advancement advancement = Bukkit.getAdvancement(new NamespacedKey(advancedAchievements,
						AdvancementManager.getKey(event.getName())));
				// Matching advancement might not exist if user has not called /aach generate.
				if (advancement != null) {
					player.getAdvancementProgress(advancement).awardCriteria(AchievementAdvancement.CRITERIA_NAME);
				}
			}
		}
		databaseManager.registerAchievement(player.getUniqueId(), event.getName(), event.getMessage());

		List<String> rewardTexts = giveRewardsAndPrepareTexts(player, event.getCommandRewards(), event.getCommandMessages(),
				event.getItemRewards(), event.getMoneyReward(), event.getExperienceReward(), event.getMaxHealthReward(),
				event.getMaxOxygenReward());
		displayAchievement(player, event.getName(), event.getDisplayName(), event.getMessage(), rewardTexts);

		if (cacheManager.getPlayerTotalAchievements(player.getUniqueId()) == namesToDisplayNames.size()) {
			handleAllAchievementsReceived(player);
		}
	}

	/**
	 * Gives relevant rewards and prepares the texts to be displayed to the receiver.
	 * 
	 * @param player
	 * @param commands
	 * @param commandMessages
	 * @param items
	 * @param money
	 * @param experience
	 * @param health
	 * @param oxygen
	 * @return all the reward texts to be displayed to the user
	 */
	private List<String> giveRewardsAndPrepareTexts(Player player, String[] commands, List<String> commandMessages,
			ItemStack[] items, int money, int experience, int health, int oxygen) {
		List<String> rewardTexts = new ArrayList<>();
		if (commands != null && commands.length > 0) {
			rewardTexts.addAll(rewardCommands(commands, commandMessages));
		}
		if (items != null) {
			rewardTexts.addAll(rewardItems(player, items));
		}
		if (money != 0) {
			rewardTexts.add(rewardMoney(player, money));
		}
		if (experience != 0) {
			rewardTexts.add(rewardExperience(player, experience));
		}
		if (health != 0) {
			rewardTexts.add(rewardMaxHealth(player, health));
		}
		if (oxygen != 0) {
			rewardTexts.add(rewardMaxOxygen(player, oxygen));
		}
		return rewardTexts;
	}

	/**
	 * Executes player command rewards.
	 *
	 * @param commands
	 * @param messages
	 * @return the reward text to display to the player
	 */
	private List<String> rewardCommands(String[] commands, List<String> messages) {
		for (String command : commands) {
			advancedAchievements.getServer().dispatchCommand(advancedAchievements.getServer().getConsoleSender(), command);
		}
		if (langCommandReward.isEmpty()) {
			return Collections.emptyList();
		}

		if (messages.isEmpty()) {
			return Collections.singletonList(langCommandReward);
		}
		return messages.stream()
				.map(message -> StringUtils.replace(langCustomMessageCommandReward, "MESSAGE", message))
				.collect(Collectors.toList());
	}

	/**
	 * Gives an item reward to a player.
	 *
	 * @param player
	 * @param items
	 * @return the reward text to display to the player
	 */
	private List<String> rewardItems(Player player, ItemStack[] items) {
		List<String> itemNames = new ArrayList<>();
		for (ItemStack item : items) {
			if (player.getInventory().firstEmpty() != -1) {
				player.getInventory().addItem(item);
			} else {
				player.getWorld().dropItem(player.getLocation(), item);
			}
			ItemMeta itemMeta = item.getItemMeta();
			String name = itemMeta.hasDisplayName() ? itemMeta.getDisplayName() : rewardParser.getItemName(item);
			itemNames.add(StringUtils.replaceEach(langItemRewardReceived, new String[] { "AMOUNT", "ITEM" },
					new String[] { Integer.toString(item.getAmount()), name }));
		}
		return itemNames;
	}

	/**
	 * Gives a money reward to a player.
	 *
	 * @param player
	 * @param amount
	 * @return the reward text to display to the player
	 */
	private String rewardMoney(Player player, int amount) {
		Economy economy = rewardParser.getEconomy();
		if (economy != null) {
			economy.depositPlayer(player, amount);

			String currencyName = rewardParser.getCurrencyName(amount);
			return ChatColor.translateAlternateColorCodes('&',
					StringUtils.replaceOnce(langMoneyRewardReceived, "AMOUNT", amount + " " + currencyName));
		}
		logger.warning("You have specified a money reward but Vault was not linked successfully.");
		return "";
	}

	/**
	 * Gives an experience reward to a player.
	 *
	 * @param player
	 * @param amount
	 * @return the reward text to display to the player
	 */
	private String rewardExperience(Player player, int amount) {
		player.giveExp(amount);
		return ChatColor.translateAlternateColorCodes('&',
				StringUtils.replaceOnce(langExperienceRewardReceived, "AMOUNT", Integer.toString(amount)));
	}

	/**
	 * Gives an increased max health reward to a player.
	 *
	 * @param player
	 * @param amount
	 * @return the reward text to display to the player
	 */
	@SuppressWarnings("deprecation")
	private String rewardMaxHealth(Player player, int amount) {
		if (serverVersion >= 9) {
			AttributeInstance playerAttribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
			playerAttribute.setBaseValue(playerAttribute.getBaseValue() + amount);
		} else {
			player.setMaxHealth(player.getMaxHealth() + amount);
		}
		return ChatColor.translateAlternateColorCodes('&',
				StringUtils.replaceOnce(langIncreaseMaxHealthRewardReceived, "AMOUNT", Integer.toString(amount)));
	}

	/**
	 * Gives an increased max oxygen reward to a player.
	 *
	 * @param player
	 * @param amount
	 * @return the reward text to display to the player
	 */
	private String rewardMaxOxygen(Player player, int amount) {
		player.setMaximumAir(player.getMaximumAir() + amount);
		return ChatColor.translateAlternateColorCodes('&',
				StringUtils.replaceOnce(langIncreaseMaxOxygenRewardReceived, "AMOUNT", Integer.toString(amount)));
	}

	/**
	 * Displays chat messages, screen title and launches a firework when a player receives an achievement.
	 *
	 * @param player
	 * @param name
	 * @param displayName
	 * @param message
	 * @param rewardTexts
	 */
	private void displayAchievement(Player player, String name, String displayName, String message,
			List<String> rewardTexts) {
		String nameToShowUser;
		if (StringUtils.isNotBlank(displayName)) {
			// Display name is defined; use it.
			nameToShowUser = ChatColor.translateAlternateColorCodes('&', displayName);
			logger.info("Player " + player.getName() + " received the achievement: " + name + " (" + displayName + ")");
		} else {
			// Use the achievement key name (this name is used in the achievements table in the database).
			nameToShowUser = ChatColor.translateAlternateColorCodes('&', name);
			logger.info("Player " + player.getName() + " received the achievement: " + name);

		}
		String messageToShowUser = ChatColor.translateAlternateColorCodes('&', message);

		if (configReceiverChatMessages) {
			displayReceiverMessages(player, nameToShowUser, messageToShowUser, rewardTexts);
		}

		// Notify other online players that the player has received an achievement.
		for (Player p : advancedAchievements.getServer().getOnlinePlayers()) {
			// Notify other players only if NotifyOtherPlayers is enabled and player has not used /aach toggle, or if
			// NotifyOtherPlayers is disabled and player has used /aach toggle.
			if (!p.getName().equals(player.getName()) && (configNotifyOtherPlayers ^ toggleCommand.isPlayerToggled(p))) {
				displayNotification(player, nameToShowUser, p);
			}
		}

		if (configFirework) {
			displayFirework(player);
		} else if (configSimplifiedReception) {
			displaySimplifiedReception(player);
		}

		if (configTitleScreen) {
			displayTitle(player, nameToShowUser, messageToShowUser);
		}
	}

	/**
	 * Displays texts related to the achievement in the receiver's chat. This method can display a single hoverable
	 * message or several messages one after the other.
	 *
	 * @param player
	 * @param nameToShowUser
	 * @param messageToShowUser
	 * @param rewardTexts
	 */
	private void displayReceiverMessages(Player player, String nameToShowUser, String messageToShowUser,
			List<String> rewardTexts) {
		if (configHoverableReceiverChatText) {
			StringBuilder hover = new StringBuilder(messageToShowUser + "\n");
			rewardTexts.stream().filter(StringUtils::isNotBlank)
					.forEach(t -> hover.append(ChatColor.translateAlternateColorCodes('&', t)).append("\n"));
			try {
				FancyMessageSender.sendHoverableMessage(player, langAchievementNew + nameToShowUser,
						hover.substring(0, hover.length() - 1), "white");
				return;
			} catch (Exception e) {
				logger.warning(
						"Failed to display hoverable message for achievement reception. Displaying standard messages instead.");
			}
		}
		player.sendMessage(langAchievementNew + nameToShowUser);
		player.sendMessage(pluginHeader.toString() + ChatColor.WHITE + messageToShowUser);
		rewardTexts.stream().filter(StringUtils::isNotBlank)
				.forEach(t -> player.sendMessage(pluginHeader + ChatColor.translateAlternateColorCodes('&', t)));
	}

	/**
	 * Displays an action bar message or chat notification to another player.
	 *
	 * @param achievementReceiver
	 * @param nameToShowUser
	 * @param otherPlayer
	 */
	private void displayNotification(Player achievementReceiver, String nameToShowUser, Player otherPlayer) {
		if (configActionBarNotify) {
			try {
				FancyMessageSender.sendActionBarMessage(otherPlayer,
						"&o" + StringUtils.replaceOnce(langAchievementReceived, "PLAYER", achievementReceiver.getName())
								+ nameToShowUser);
			} catch (Exception e) {
				logger.warning("Failed to display action bar message for achievement reception notification.");
			}
		} else {
			otherPlayer.sendMessage(
					pluginHeader + StringUtils.replaceOnce(langAchievementReceived, "PLAYER", achievementReceiver.getName())
							+ nameToShowUser);
		}
	}

	/**
	 * Displays title when receiving an achievement.
	 *
	 * @param player
	 * @param nameToShowUser
	 * @param messageToShowUser
	 */
	private void displayTitle(Player player, String nameToShowUser, String messageToShowUser) {
		try {
			FancyMessageSender.sendTitle(player, nameToShowUser, messageToShowUser);
		} catch (Exception e) {
			logger.warning("Failed to display achievement screen title.");
		}
	}

	/**
	 * Launches firework when receiving an achievement.
	 *
	 * @param player
	 */
	private void displayFirework(Player player) {
		// Set firework to launch beneath player.
		Location location = player.getLocation().subtract(0, 1, 0);
		try {
			Firework firework = player.getWorld().spawn(location, Firework.class);
			FireworkMeta fireworkMeta = firework.getFireworkMeta();
			FireworkEffect fireworkEffect = FireworkEffect.builder()
					.flicker(false)
					.trail(false)
					.withColor(Color.WHITE.mixColors(Color.BLUE.mixColors(Color.NAVY)))
					.withFade(Color.PURPLE)
					.with(getFireworkType())
					.build();
			fireworkMeta.addEffects(fireworkEffect);
			firework.setFireworkMeta(fireworkMeta);
			firework.setVelocity(location.getDirection().multiply(0));

			// Firework launched by plugin: damage will later be cancelled out.
			fireworkListener.addFirework(firework);
		} catch (Exception e) {
			// Particle effect workaround to handle various bugs in early Spigot 1.9 and 1.11 releases. We try to
			// simulate a firework.
			soundPlayer.play(player, "ENTITY_FIREWORK_ROCKET_LAUNCH", "ENTITY_FIREWORK_ROCKET_LAUNCH",
					"ENTITY_FIREWORK_LAUNCH");
			if (serverVersion >= 13) {
				player.spawnParticle(Particle.FIREWORKS_SPARK, player.getLocation(), 500, 0, 3, 0, 0.1f);
			} else {
				ParticleEffect.FIREWORKS_SPARK.display(0, 3, 0, 0.1f, 500, player.getLocation(), 1);
			}
			soundPlayer.play(player, "ENTITY_FIREWORK_ROCKET_BLAST", "ENTITY_FIREWORK_ROCKET_BLAST",
					"ENTITY_FIREWORK_BLAST");
			soundPlayer.play(player, "ENTITY_FIREWORK_ROCKET_BLAST", "ENTITY_FIREWORK_ROCKET_BLAST",
					"ENTITY_FIREWORK_TWINKLE");
		}
	}

	/**
	 * Gets the type of the firework, which can either be predefined or random.
	 *
	 * @return the firework type.
	 */
	private Type getFireworkType() {
		if ("RANDOM".equals(configFireworkStyle)) {
			Type[] fireworkTypes = Type.values();
			return fireworkTypes[RANDOM.nextInt(fireworkTypes.length)];
		} else {
			return Type.valueOf(configFireworkStyle);
		}
	}

	/**
	 * Displays a simplified particle effect and calm sound when receiving an achievement. Is used instead of
	 * displayFirework.
	 *
	 * @param player
	 */
	private void displaySimplifiedReception(Player player) {
		soundPlayer.play(player, "ENTITY_PLAYER_LEVELUP", "ENTITY_PLAYER_LEVELUP", "LEVEL_UP");
		if (serverVersion >= 13) {
			player.spawnParticle(Particle.FIREWORKS_SPARK, player.getLocation(), 500, 0, 3, 0, 0.1f);
		} else {
			ParticleEffect.FIREWORKS_SPARK.display(0, 3, 0, 0.1f, 500, player.getLocation(), 1);
		}
	}

	/**
	 * Handles rewards and displaying messages when a player has received all achievements.
	 * 
	 * @param player
	 */
	private void handleAllAchievementsReceived(Player player) {
		List<String> rewardTexts = giveRewardsAndPrepareTexts(player,
				rewardParser.getCommandRewards("AllAchievementsReceivedRewards", player),
				rewardParser.getCustomCommandMessages("AllAchievementsReceivedRewards"),
				rewardParser.getItemRewards("AllAchievementsReceivedRewards", player),
				rewardParser.getRewardAmount("AllAchievementsReceivedRewards", "Money"),
				rewardParser.getRewardAmount("AllAchievementsReceivedRewards", "Experience"),
				rewardParser.getRewardAmount("AllAchievementsReceivedRewards", "IncreaseMaxHealth"),
				rewardParser.getRewardAmount("AllAchievementsReceivedRewards", "IncreaseMaxOxygen"));
		player.sendMessage(langAllAchievementsReceived);
		rewardTexts.stream().filter(StringUtils::isNotBlank)
				.forEach(t -> player.sendMessage(pluginHeader + ChatColor.translateAlternateColorCodes('&', t)));
	}
}
