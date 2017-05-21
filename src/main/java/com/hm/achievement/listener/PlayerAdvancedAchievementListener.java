package com.hm.achievement.listener;

import java.util.Random;
import java.util.logging.Level;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.advancement.AchievementAdvancement;
import com.hm.achievement.advancement.AdvancementManager;
import com.hm.achievement.utils.PlayerAdvancedAchievementEvent;
import com.hm.mcshared.particle.PacketSender;
import com.hm.mcshared.particle.ParticleEffect;

/**
 * Listener class to deal with achievement receptions: rewards, display and database operations.
 * 
 * @author Pyves
 *
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
	private String langCommandReward;
	private String langAchievementReceived;
	private String langItemRewardReceived;
	private String langMoneyRewardReceived;
	private String langExperienceRewardReceived;
	private String langAchievementNew;

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
		if (configTitleScreen && version < 8) {
			configTitleScreen = false;
		}
		configNotifyOtherPlayers = plugin.getPluginConfig().getBoolean("NotifyOtherPlayers", false);
		configActionBarNotify = plugin.getPluginConfig().getBoolean("ActionBarNotify", false);
		// Action bars introduced in Minecraft 1.8. Automatically relevant parameter for older versions.
		if (configActionBarNotify && version < 8) {
			configActionBarNotify = false;
		}
		// No longer available in default config, kept for compatibility with versions prior to 2.1; defines whether
		// a player is notified in case of a command reward.
		configRewardCommandNotif = plugin.getPluginConfig().getBoolean("RewardCommandNotif", true);

		langCommandReward = plugin.getPluginLang().getString("command-reward", "Reward command carried out!");
		langAchievementReceived = plugin.getPluginLang().getString("achievement-received",
				"PLAYER received the achievement:") + " " + ChatColor.WHITE;
		langItemRewardReceived = plugin.getChatHeader()
				+ plugin.getPluginLang().getString("item-reward-received", "You received an item reward:") + " ";
		langMoneyRewardReceived = plugin.getChatHeader()
				+ plugin.getPluginLang().getString("money-reward-received", "You received: AMOUNT!");
		langExperienceRewardReceived = plugin.getChatHeader()
				+ plugin.getPluginLang().getString("experience-reward-received", "You received: AMOUNT experience!");
		langAchievementNew = plugin.getChatHeader()
				+ plugin.getPluginLang().getString("achievement-new", "New Achievement:") + " " + ChatColor.WHITE;
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

			if (version >= 12) {
				Advancement advancement = Bukkit.getServer()
						.getAdvancement(new NamespacedKey(plugin, AdvancementManager.getKey(event.getName())));
				// Matching advancement might not exist if user has not called /aach generate.
				if (advancement != null) {
					player.getAdvancementProgress(advancement).awardCriteria(AchievementAdvancement.CRITERIA_NAME);
				}
			}
		}
		plugin.getDatabaseManager().registerAchievement(player.getUniqueId(), event.getName(), event.getMessage());

		displayAchievement(player, event.getName(), event.getDisplayName(), event.getMessage());

		if (event.getCommandRewards() != null && event.getCommandRewards().length > 0) {
			rewardCommands(player, event.getCommandRewards());
		}
		if (event.getItemReward() != null) {
			rewardItem(player, event.getItemReward());
		}
		if (event.getMoneyReward() > 0) {
			rewardMoney(player, event.getMoneyReward());
		}
		if (event.getExperienceReward() > 0) {
			rewardExperience(player, event.getExperienceReward());
		}
	}

	/**
	 * Executes player command rewards.
	 * 
	 * @param player
	 * @param commands
	 */
	private void rewardCommands(Player player, String[] commands) {
		for (String command : commands) {
			plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
		}
		if (!configRewardCommandNotif || langCommandReward.length() == 0) {
			return;
		}
		player.sendMessage(plugin.getChatHeader() + langCommandReward);
	}

	/**
	 * Gives an item reward to a player.
	 * 
	 * @param player
	 * @param item
	 */
	private void rewardItem(Player player, ItemStack item) {
		if (player.getInventory().firstEmpty() != -1) {
			player.getInventory().addItem(item);
		} else {
			player.getWorld().dropItem(player.getLocation(), item);
		}
		player.sendMessage(langItemRewardReceived + plugin.getRewardParser().getItemName(item));
	}

	/**
	 * Gives a money reward to a player.
	 * 
	 * @param player
	 * @param amount
	 */
	@SuppressWarnings("deprecation")
	private void rewardMoney(Player player, int amount) {
		if (plugin.getRewardParser().isEconomySet(true)) {
			try {
				plugin.getRewardParser().getEconomy().depositPlayer(player, amount);
			} catch (NoSuchMethodError e) {
				// Deprecated method, but was the only one existing prior to Vault 1.4.
				plugin.getRewardParser().getEconomy().depositPlayer(player.getName(), amount);
			}

			String currencyName = plugin.getRewardParser().getCurrencyName(amount);
			player.sendMessage(ChatColor.translateAlternateColorCodes('&',
					StringUtils.replaceOnce(langMoneyRewardReceived, "AMOUNT", amount + " " + currencyName)));
		}
	}

	/**
	 * Gives an experience reward to a player.
	 * 
	 * @param player
	 * @param amount
	 */
	private void rewardExperience(Player player, int amount) {
		player.giveExp(amount);
		player.sendMessage(ChatColor.translateAlternateColorCodes('&',
				StringUtils.replaceOnce(langExperienceRewardReceived, "AMOUNT", Integer.toString(amount))));
	}

	/**
	 * Displays chat messages, screen title and launches a firework when a player receives an achievement.
	 * 
	 * @param player
	 * @param configAchievement
	 */
	private void displayAchievement(Player player, String name, String displayName, String description) {
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

		player.sendMessage(langAchievementNew + nameToShowUser);

		// Notify other online players that the player has received an achievement.
		for (Player p : plugin.getServer().getOnlinePlayers()) {
			// Notify other players only if NotifyOtherPlayers is enabled and player has not used /aach toggle, or if
			// NotifyOtherPlayers is disabled and player has used /aach toggle.
			if ((configNotifyOtherPlayers ^ plugin.getToggleCommand().isPlayerToggled(p))
					&& !p.getName().equals(player.getName())) {
				displayNotification(player, nameToShowUser, p);
			}
		}
		String msg = ChatColor.translateAlternateColorCodes('&', description);
		player.sendMessage(plugin.getChatHeader() + ChatColor.WHITE + msg);

		if (configFirework) {
			displayFirework(player);
		} else if (configSimplifiedReception) {
			displaySimplifiedReception(player);
		}

		if (configTitleScreen) {
			displayTitle(player, nameToShowUser, msg);
		}
	}

	/**
	 * Displays an action bar message or chat notification to another player.
	 * 
	 * @param achievementReceiver
	 * @param configAchievement
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
	 * @param msg
	 */
	private void displayTitle(Player player, String nameToShowUser, String msg) {
		try {
			// Escape quotations in case quotations are used in config.yml.
			PacketSender.sendTitlePacket(player,
					"{\"text\":\"" + StringUtils.replace(nameToShowUser, "\"", "\\\"") + "\"}",
					"{\"text\":\"" + StringUtils.replace(msg, "\"", "\\\"") + "\"}");
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
		Sound sound;
		if (version < 9) {
			// Old enum for versions prior to Minecraft 1.9. Retrieving it by name as it does no longer exist in
			// newer versions.
			sound = Sound.valueOf("LEVEL_UP");
		} else {
			// Play sound with enum for newer versions.
			sound = Sound.ENTITY_PLAYER_LEVELUP;
		}
		player.getWorld().playSound(location, sound, 1, 0.9f);
		ParticleEffect.FIREWORKS_SPARK.display(0, 3, 0, 0.1f, 500, location, 1);
	}
}
