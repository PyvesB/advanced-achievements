package com.hm.achievement.listener;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.advancement.AchievementAdvancement;
import com.hm.achievement.advancement.AdvancementManager;
import com.hm.achievement.lang.Lang;
import com.hm.achievement.lang.ListenerLang;
import com.hm.achievement.utils.PlayerAdvancedAchievementEvent;
import com.hm.achievement.utils.RewardParser;
import com.hm.mcshared.particle.PacketSender;
import com.hm.mcshared.particle.ParticleEffect;
import net.milkbowl.vault.economy.Economy;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.*;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.advancement.Advancement;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

/**
 * Listener class to deal with achievement receptions: rewards, display and database operations.
 *
 * @author Pyves
 */
public class PlayerAdvancedAchievementListener extends AbstractListener {

	private static final Random RANDOM = new Random();

	private boolean configRewardCommandNotif;
	private String configFireworkStyle;
	private boolean configFirework;
	private boolean configSimplifiedReception;
	private boolean configTitleScreen;
	private boolean configNotifyOtherPlayers;
	private boolean configActionBarNotify;
	private boolean configHoverableReceiverChatText;
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

	public PlayerAdvancedAchievementListener(AdvancedAchievements plugin) {
		super(plugin);
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		configFireworkStyle = plugin.getPluginConfig().getString("FireworkStyle", "BALL_LARGE");
		configFirework = plugin.getPluginConfig().getBoolean("Firework", true);
		configSimplifiedReception = plugin.getPluginConfig().getBoolean("SimplifiedReception", false);
		configTitleScreen = plugin.getPluginConfig().getBoolean("TitleScreen", true);
		// Title screens introduced in Minecraft 1.8. Automatically relevant parameter for older versions.
		if (configTitleScreen && plugin.getServerVersion() < 8) {
			configTitleScreen = false;
		}
		configNotifyOtherPlayers = plugin.getPluginConfig().getBoolean("NotifyOtherPlayers", false);
		configActionBarNotify = plugin.getPluginConfig().getBoolean("ActionBarNotify", false);
		// Action bars introduced in Minecraft 1.8. Automatically relevant parameter for older versions.
		if (configActionBarNotify && plugin.getServerVersion() < 8) {
			configActionBarNotify = false;
		}
		// No longer available in default config, kept for compatibility with versions prior to 2.1; defines whether
		// a player is notified in case of a command reward.
		configRewardCommandNotif = plugin.getPluginConfig().getBoolean("RewardCommandNotif", true);
		configHoverableReceiverChatText = plugin.getPluginConfig().getBoolean("HoverableReceiverChatText", false);
		// Hoverable chat messages introduced in Minecraft 1.8. Automatically relevant parameter for older versions.
		if (configHoverableReceiverChatText && plugin.getServerVersion() < 8) {
			configHoverableReceiverChatText = false;
		}

		langCommandReward = Lang.get(ListenerLang.COMMAND_REWARD, plugin);
		langAchievementReceived = Lang.get(ListenerLang.ACHIEVEMENT_RECEIVED, plugin) + " " + ChatColor.WHITE;
		langItemRewardReceived = Lang.get(ListenerLang.ITEM_REWARD_RECEIVED, plugin) + " ";
		langMoneyRewardReceived = Lang.get(ListenerLang.MONEY_REWARD_RECEIVED, plugin);
		langExperienceRewardReceived = Lang.get(ListenerLang.EXPERIENCE_REWARD_RECEIVED, plugin);
		langIncreaseMaxHealthRewardReceived = Lang.get(ListenerLang.INCREASE_MAX_HEALTH_REWARD_RECEIVED, plugin);
		langIncreaseMaxOxygenRewardReceived = Lang.get(ListenerLang.INCREASE_MAX_OXYGEN_REWARD_RECEIVED, plugin);
		langAchievementNew = Lang.getWithChatHeader(ListenerLang.ACHIEVEMENT_NEW, plugin) + " " + ChatColor.WHITE;
		langCustomMessageCommandReward = Lang.get(ListenerLang.CUSTOM_COMMAND_REWARD, plugin);
		langAllAchievementsReceived = Lang.getWithChatHeader(ListenerLang.ALL_ACHIEVEMENTS_RECEIVED, plugin);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerAdvancedAchievementReception(PlayerAdvancedAchievementEvent event) {
		Player player = event.getPlayer();
		String uuid = player.getUniqueId().toString();
		// Check before updating caches; achievement could have already been received if MultiCommand is set to true in
		// the configuration.
		if (!plugin.getCacheManager().hasPlayerAchievement(player.getUniqueId(), event.getName())) {
			plugin.getCacheManager().getReceivedAchievementsCache().put(uuid, event.getName());
			plugin.getCacheManager().getNotReceivedAchievementsCache().remove(uuid, event.getName());
			plugin.getCacheManager().getTotalPlayerAchievementsCache().put(uuid,
					plugin.getCacheManager().getPlayerTotalAchievements(player.getUniqueId()) + 1);

			if (plugin.getServerVersion() >= 12) {
				Advancement advancement = Bukkit.getServer()
						.getAdvancement(new NamespacedKey(plugin, AdvancementManager.getKey(event.getName())));
				// Matching advancement might not exist if user has not called /aach generate.
				if (advancement != null) {
					player.getAdvancementProgress(advancement).awardCriteria(AchievementAdvancement.CRITERIA_NAME);
				}
			}
		}
		plugin.getDatabaseManager().registerAchievement(player.getUniqueId(), event.getName(), event.getMessage());

		List<String> rewardTexts = giveRewardsAndPrepareTexts(player, event.getCommandRewards(),
				event.getCommandMessage(), event.getItemReward(), event.getMoneyReward(), event.getExperienceReward(),
				event.getMaxHealthReward(), event.getMaxOxygenReward());
		displayAchievement(player, event.getName(), event.getDisplayName(), event.getMessage(), rewardTexts);

		if (plugin.getCacheManager().getPlayerTotalAchievements(player.getUniqueId()) == plugin
				.getAchievementsAndDisplayNames().size()) {
			handleAllAchievementsReceived(player);
		}
	}

	/**
	 * Gives relevant rewards and prepares the texts to be displayed to the receiver.
	 * 
	 * @param player
	 * @param commands
	 * @param commandMessage
	 * @param item
	 * @param money
	 * @param experience
	 * @param health
	 * @param oxygen
	 * @return all the reward texts to be displayed to the user
	 */
	private List<String> giveRewardsAndPrepareTexts(Player player, String[] commands, String commandMessage,
			ItemStack item, int money, int experience, int health, int oxygen) {
		List<String> rewardTexts = new ArrayList<>();
		if (commands != null && commands.length > 0) {
			rewardTexts.add(rewardCommands(commands, commandMessage));
		}
		if (item != null) {
			rewardTexts.add(rewardItem(player, item));
		}
		if (money > 0) {
			rewardTexts.add(rewardMoney(player, money));
		}
		if (experience > 0) {
			rewardTexts.add(rewardExperience(player, experience));
		}
		if (health > 0) {
			rewardTexts.add(rewardMaxHealth(player, health));
		}
		if (oxygen > 0) {
			rewardTexts.add(rewardMaxOxygen(player, oxygen));
		}
		return rewardTexts;
	}

	/**
	 * Executes player command rewards.
	 *
	 * @param commands
	 * @param message
	 * @return the reward text to display to the player
	 */
	private String rewardCommands(String[] commands, String message) {
		for (String command : commands) {
			plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
		}
		if (!configRewardCommandNotif || langCommandReward.length() == 0) {
			return "";
		}

		if (message != null) {
			return StringUtils.replace(langCustomMessageCommandReward, "MESSAGE", message);
		}

		return langCommandReward;
	}

	/**
	 * Gives an item reward to a player.
	 *
	 * @param player
	 * @param item
	 * @return the reward text to display to the player
	 */
	private String rewardItem(Player player, ItemStack item) {
		if (player.getInventory().firstEmpty() != -1) {
			player.getInventory().addItem(item);
		} else {
			player.getWorld().dropItem(player.getLocation(), item);
		}

		String name = item.getItemMeta().getDisplayName();
		if (name == null || name.isEmpty()) {
			name = plugin.getRewardParser().getItemName(item);
		}

		return langItemRewardReceived + name;
	}

	/**
	 * Gives a money reward to a player.
	 *
	 * @param player
	 * @param amount
	 * @return the reward text to display to the player
	 */
	private String rewardMoney(Player player, int amount) {
		Economy economy = plugin.getRewardParser().getEconomy();
		if (economy != null) {
			economy.depositPlayer(player, amount);

			String currencyName = plugin.getRewardParser().getCurrencyName(amount);
			return ChatColor.translateAlternateColorCodes('&',
					StringUtils.replaceOnce(langMoneyRewardReceived, "AMOUNT", amount + " " + currencyName));
		}
		plugin.getLogger().warning("You have specified a money reward but Vault was not linked successfully.");
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
		if (plugin.getServerVersion() >= 9) {
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
			plugin.getLogger().info(
					"Player " + player.getName() + " received the achievement: " + name + " (" + displayName + ")");
		} else {
			// Use the achievement key name (this name is used in the achievements table in the database).
			nameToShowUser = ChatColor.translateAlternateColorCodes('&', name);
			plugin.getLogger().info("Player " + player.getName() + " received the achievement: " + name);

		}
		String messageToShowUser = ChatColor.translateAlternateColorCodes('&', message);

		displayReceiverMessages(player, nameToShowUser, messageToShowUser, rewardTexts);

		// Notify other online players that the player has received an achievement.
		for (Player p : plugin.getServer().getOnlinePlayers()) {
			// Notify other players only if NotifyOtherPlayers is enabled and player has not used /aach toggle, or if
			// NotifyOtherPlayers is disabled and player has used /aach toggle.
			if (!p.getName().equals(player.getName())
					&& (configNotifyOtherPlayers ^ plugin.getToggleCommand().isPlayerToggled(p))) {
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
			String json = "{\"text\":\"" + langAchievementNew + nameToShowUser
					+ "\",\"hoverEvent\":{\"action\":\"show_text\",\"value\":[{\"text\":\""
					+ hover.substring(0, hover.length() - 1) + "\"}]}}";
			try {
				PacketSender.sendChatMessagePacket(player, json);
				return;
			} catch (Exception e) {
				plugin.getLogger().warning(
						"Errors while trying to display hoverable message for achievement reception. Displaying standard messages instead.");
			}
		}
		player.sendMessage(langAchievementNew + nameToShowUser);
		player.sendMessage(plugin.getChatHeader() + ChatColor.WHITE + messageToShowUser);
		rewardTexts.stream().filter(StringUtils::isNotBlank).forEach(
				t -> player.sendMessage(plugin.getChatHeader() + ChatColor.translateAlternateColorCodes('&', t)));
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
			String actionBarJsonMessage = "{\"text\":\"&o"
					+ StringUtils.replaceOnce(langAchievementReceived, "PLAYER", achievementReceiver.getName())
					+ nameToShowUser + "\"}";
			try {
				PacketSender.sendActionBarPacket(otherPlayer, actionBarJsonMessage);
			} catch (Exception e) {
				plugin.getLogger().warning("Errors while trying to display action bar message for notifications.");
			}
		} else {
			otherPlayer.sendMessage(plugin.getChatHeader()
					+ StringUtils.replaceOnce(langAchievementReceived, "PLAYER", achievementReceiver.getName())
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
			// Escape quotations in case quotations are used in config.yml.
			PacketSender.sendTitlePacket(player,
					"{\"text\":\"" + StringUtils.replace(nameToShowUser, "\"", "\\\"") + "\"}",
					"{\"text\":\"" + StringUtils.replace(messageToShowUser, "\"", "\\\"") + "\"}");
		} catch (Exception e) {
			plugin.getLogger().log(Level.SEVERE,
					"Errors while trying to display achievement screen title. Is your server up-to-date? ", e);
		}
	}

	/**
	 * Launches firework when receiving an achievement.
	 *
	 * @param player
	 */
	private void displayFirework(Player player) {
		Location location = player.getLocation();
		try {
			// Set firework to launch beneath user.
			location.setY(location.getY() - 1);

			Firework firework = player.getWorld().spawn(location, Firework.class);
			FireworkMeta fireworkMeta = firework.getFireworkMeta();
			Builder effectBuilder = FireworkEffect.builder().flicker(false).trail(false)
					.withColor(Color.WHITE.mixColors(Color.BLUE.mixColors(Color.NAVY))).withFade(Color.PURPLE);
			setFireworkType(effectBuilder);
			fireworkMeta.addEffects(effectBuilder.build());
			firework.setVelocity(player.getLocation().getDirection().multiply(0));
			firework.setFireworkMeta(fireworkMeta);

			// Firework launched by plugin: damage will later be cancelled out.
			plugin.getFireworkListener().addFirework(firework);
		} catch (Exception e) {
			// Particle effect workaround to handle various bugs in early Spigot 1.9 and 1.11 releases. We try to
			// simulate a firework.
			player.getWorld().playSound(location, Sound.ENTITY_FIREWORK_LAUNCH, 1, 0.6f);
			ParticleEffect.FIREWORKS_SPARK.display(0, 3, 0, 0.1f, 500, location, 1);
			player.getWorld().playSound(location, Sound.ENTITY_FIREWORK_BLAST, 1, 0.6f);
			player.getWorld().playSound(location, Sound.ENTITY_FIREWORK_TWINKLE, 1, 0.6f);
		}
	}

	/**
	 * Sets the type of the firwrok, which can either be predefined or random.
	 *
	 * @param effectBuilder
	 */
	private void setFireworkType(Builder effectBuilder) {
		if ("RANDOM".equalsIgnoreCase(configFireworkStyle)) {
			Type[] fireworkTypes = Type.values();
			effectBuilder.with(fireworkTypes[RANDOM.nextInt(fireworkTypes.length)]);
		} else {
			try {
				effectBuilder.with(Type.valueOf(configFireworkStyle.toUpperCase()));
			} catch (Exception e) {
				effectBuilder.with(Type.BALL_LARGE);
				plugin.getLogger().warning(
						"Error while loading FireworkStyle. Please use one of the following: BALL_LARGE, BALL, BURST, CREEPER or STAR.");
			}
		}
	}

	/**
	 * Displays a simplified particle effect and calm sound when receiving an achievement. Is used instead of
	 * displayFirework.
	 *
	 * @param player
	 */
	private void displaySimplifiedReception(Player player) {
		Location location = player.getLocation();
		// If old version, retrieving sound by name as it no longer exists in newer versions.
		Sound sound = plugin.getServerVersion() < 9 ? Sound.valueOf("LEVEL_UP") : Sound.ENTITY_PLAYER_LEVELUP;
		player.getWorld().playSound(location, sound, 1, 0.9f);
		ParticleEffect.FIREWORKS_SPARK.display(0, 3, 0, 0.1f, 500, location, 1);
	}

	/**
	 * Handles rewards and displaying messages when a player has received all achievements.
	 * 
	 * @param player
	 */
	private void handleAllAchievementsReceived(Player player) {
		RewardParser rewardParser = plugin.getRewardParser();
		List<String> rewardTexts = giveRewardsAndPrepareTexts(player,
				rewardParser.getCommandRewards("AllAchievementsReceivedRewards", player),
				rewardParser.getCustomCommandMessage("AllAchievementsReceivedRewards"),
				rewardParser.getItemReward("AllAchievementsReceivedRewards"),
				rewardParser.getRewardAmount("AllAchievementsReceivedRewards", "Money"),
				rewardParser.getRewardAmount("AllAchievementsReceivedRewards", "Experience"),
				rewardParser.getRewardAmount("AllAchievementsReceivedRewards", "IncreaseMaxHealth"),
				rewardParser.getRewardAmount("AllAchievementsReceivedRewards", "IncreaseMaxOxygen"));
		player.sendMessage(langAllAchievementsReceived);
		rewardTexts.stream().filter(StringUtils::isNotBlank).forEach(
				t -> player.sendMessage(plugin.getChatHeader() + ChatColor.translateAlternateColorCodes('&', t)));
	}
}
