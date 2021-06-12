package com.hm.achievement.listener;

import java.util.HashMap;
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
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.meta.FireworkMeta;

import com.darkblade12.particleeffect.ParticleEffect;
import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.advancement.AchievementAdvancement;
import com.hm.achievement.advancement.AdvancementManager;
import com.hm.achievement.command.executable.ToggleCommand;
import com.hm.achievement.config.AchievementMap;
import com.hm.achievement.config.RewardParser;
import com.hm.achievement.db.AbstractDatabaseManager;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.domain.Achievement;
import com.hm.achievement.domain.Reward;
import com.hm.achievement.lifecycle.Reloadable;
import com.hm.achievement.utils.ColorHelper;
import com.hm.achievement.utils.FancyMessageSender;
import com.hm.achievement.utils.PlayerAdvancedAchievementEvent;
import com.hm.achievement.utils.SoundPlayer;
import com.hm.achievement.utils.StringHelper;

/**
 * Listener class to deal with achievement receptions: rewards, display and database operations.
 *
 * @author Pyves
 */
@Singleton
public class PlayerAdvancedAchievementListener implements Listener, Reloadable {

	private static final Random RANDOM = new Random();

	private static final Map<ChatColor, ChatColor> FIREWORK_COLOR_MIX = new HashMap<>();
	static {
		FIREWORK_COLOR_MIX.put(ChatColor.AQUA, ChatColor.DARK_AQUA);
		FIREWORK_COLOR_MIX.put(ChatColor.BLACK, ChatColor.GRAY);
		FIREWORK_COLOR_MIX.put(ChatColor.BLUE, ChatColor.DARK_BLUE);
		FIREWORK_COLOR_MIX.put(ChatColor.GRAY, ChatColor.DARK_GRAY);
		FIREWORK_COLOR_MIX.put(ChatColor.DARK_AQUA, ChatColor.AQUA);
		FIREWORK_COLOR_MIX.put(ChatColor.DARK_BLUE, ChatColor.BLUE);
		FIREWORK_COLOR_MIX.put(ChatColor.DARK_GRAY, ChatColor.GRAY);
		FIREWORK_COLOR_MIX.put(ChatColor.DARK_GREEN, ChatColor.GREEN);
		FIREWORK_COLOR_MIX.put(ChatColor.DARK_PURPLE, ChatColor.LIGHT_PURPLE);
		FIREWORK_COLOR_MIX.put(ChatColor.DARK_RED, ChatColor.RED);
		FIREWORK_COLOR_MIX.put(ChatColor.GOLD, ChatColor.YELLOW);
		FIREWORK_COLOR_MIX.put(ChatColor.GREEN, ChatColor.DARK_GREEN);
		FIREWORK_COLOR_MIX.put(ChatColor.LIGHT_PURPLE, ChatColor.DARK_PURPLE);
		FIREWORK_COLOR_MIX.put(ChatColor.RED, ChatColor.DARK_RED);
		FIREWORK_COLOR_MIX.put(ChatColor.WHITE, ChatColor.GRAY);
		FIREWORK_COLOR_MIX.put(ChatColor.YELLOW, ChatColor.GOLD);
	}

	private final YamlConfiguration mainConfig;
	private final YamlConfiguration langConfig;
	private final int serverVersion;
	private final Logger logger;
	private final StringBuilder pluginHeader;
	private final CacheManager cacheManager;
	private final AdvancedAchievements advancedAchievements;
	private final RewardParser rewardParser;
	private final AchievementMap achievementMap;
	private final AbstractDatabaseManager databaseManager;
	private final ToggleCommand toggleCommand;
	private final FireworkListener fireworkListener;
	private final SoundPlayer soundPlayer;

	private String configFireworkStyle;
	private boolean configFirework;
	private Color configColor;
	private Color mixColor;
	private boolean configSimplifiedReception;
	private boolean configTitleScreen;
	private boolean configNotifyOtherPlayers;
	private boolean configActionBarNotify;
	private boolean configHoverableReceiverChatText;
	private boolean configReceiverChatMessages;

	private String langAchievementReceived;
	private String langAchievementNew;
	private String langAllAchievementsReceived;

	@Inject
	public PlayerAdvancedAchievementListener(@Named("main") YamlConfiguration mainConfig,
			@Named("lang") YamlConfiguration langConfig, int serverVersion, Logger logger, StringBuilder pluginHeader,
			CacheManager cacheManager, AdvancedAchievements advancedAchievements, RewardParser rewardParser,
			AchievementMap achievementMap, AbstractDatabaseManager databaseManager, ToggleCommand toggleCommand,
			FireworkListener fireworkListener, SoundPlayer soundPlayer) {
		this.mainConfig = mainConfig;
		this.langConfig = langConfig;
		this.serverVersion = serverVersion;
		this.logger = logger;
		this.pluginHeader = pluginHeader;
		this.cacheManager = cacheManager;
		this.advancedAchievements = advancedAchievements;
		this.rewardParser = rewardParser;
		this.achievementMap = achievementMap;
		this.databaseManager = databaseManager;
		this.toggleCommand = toggleCommand;
		this.fireworkListener = fireworkListener;
		this.soundPlayer = soundPlayer;
	}

	@Override
	public void extractConfigurationParameters() {
		configFireworkStyle = mainConfig.getString("FireworkStyle").toUpperCase();
		if (!"RANDOM".equals(configFireworkStyle) && !EnumUtils.isValidEnum(Type.class, configFireworkStyle)) {
			configFireworkStyle = Type.BALL_LARGE.name();
			logger.warning("Failed to load FireworkStyle, using ball_large instead. Please use one of the following: "
					+ "ball_large, ball, burst, creeper, star or random.");
		}
		configFirework = mainConfig.getBoolean("Firework");
		configSimplifiedReception = mainConfig.getBoolean("SimplifiedReception");
		configTitleScreen = mainConfig.getBoolean("TitleScreen");
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
		configReceiverChatMessages = mainConfig.getBoolean("ReceiverChatMessages");
		ChatColor chatColor = ChatColor.getByChar(mainConfig.getString("Color"));
		configColor = ColorHelper.convertChatColorToColor(chatColor);
		mixColor = Color.WHITE.mixColors(ColorHelper.convertChatColorToColor(FIREWORK_COLOR_MIX.get(chatColor)));

		langAchievementReceived = langConfig.getString("achievement-received") + " " + ChatColor.WHITE;
		langAchievementNew = pluginHeader + langConfig.getString("achievement-new") + " " + ChatColor.WHITE;
		langAllAchievementsReceived = pluginHeader + langConfig.getString("all-achievements-received");
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerAdvancedAchievementReception(PlayerAdvancedAchievementEvent event) {
		Achievement achievement = event.getAchievement();
		Player player = event.getPlayer();
		// Achievement could have already been received if MultiCommand is set to true in the configuration.
		if (!cacheManager.hasPlayerAchievement(player.getUniqueId(), achievement.getName())) {
			cacheManager.registerNewlyReceivedAchievement(player.getUniqueId(), achievement.getName());

			if (serverVersion >= 12) {
				Advancement advancement = Bukkit.getAdvancement(new NamespacedKey(advancedAchievements,
						AdvancementManager.getKey(achievement.getName())));
				// Matching advancement might not exist if user has not called /aach generate.
				if (advancement != null) {
					player.getAdvancementProgress(advancement).awardCriteria(AchievementAdvancement.CRITERIA_NAME);
				}
			}
		}
		databaseManager.registerAchievement(player.getUniqueId(), achievement.getName(), System.currentTimeMillis());

		achievement.getRewards().forEach(r -> r.getRewarder().accept(player));
		displayAchievement(player, achievement);

		if (cacheManager.getPlayerAchievements(player.getUniqueId()).size() == achievementMap.getAll().size()) {
			handleAllAchievementsReceived(player);
		}
	}

	/**
	 * Displays chat messages, screen title and launches a firework when a player receives an achievement.
	 *
	 * @param player
	 * @param achievement
	 */
	private void displayAchievement(Player player, Achievement achievement) {
		logger.info("Player " + player.getName() + " received the achievement: " + achievement.getDisplayName());

		String nameToShowUser = ChatColor.translateAlternateColorCodes('&', achievement.getDisplayName());
		String messageToShowUser = ChatColor.translateAlternateColorCodes('&', achievement.getMessage());

		if (configReceiverChatMessages || player.hasPermission("achievement.config.receiver.chat.messages")) {
			displayReceiverMessages(player, nameToShowUser, messageToShowUser, achievement.getRewards());
		}

		// Notify other online players that the player has received an achievement.
		for (Player p : advancedAchievements.getServer().getOnlinePlayers()) {
			// Notify other players only if NotifyOtherPlayers is enabled and player has not used /aach toggle, or if
			// NotifyOtherPlayers is disabled and player has used /aach toggle.
			if (!p.getName().equals(player.getName())
					&& (configNotifyOtherPlayers ^ toggleCommand.isPlayerToggled(p, achievement.getType()))) {
				displayNotification(player, nameToShowUser, p);
			}
		}

		if (configFirework) {
			displayFirework(player);
		} else if (configSimplifiedReception) {
			displaySimplifiedReception(player);
		}

		if (configTitleScreen || player.hasPermission("achievement.config.title.screen")) {
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
	 * @param rewards
	 */
	private void displayReceiverMessages(Player player, String nameToShowUser, String messageToShowUser,
			List<Reward> rewards) {
		List<String> chatMessages = rewards.stream()
				.map(Reward::getChatTexts)
				.flatMap(List::stream)
				.map(m -> StringHelper.replacePlayerPlaceholders(m, player))
				.collect(Collectors.toList());
		if (configHoverableReceiverChatText) {
			StringBuilder hover = new StringBuilder(messageToShowUser + "\n");
			chatMessages.forEach(t -> hover.append(ChatColor.translateAlternateColorCodes('&', t)).append("\n"));
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
		chatMessages.forEach(t -> player.sendMessage(pluginHeader + ChatColor.translateAlternateColorCodes('&', t)));
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
		Firework firework = player.getWorld().spawn(location, Firework.class);
		FireworkMeta fireworkMeta = firework.getFireworkMeta();
		FireworkEffect fireworkEffect = FireworkEffect.builder()
				.withColor(configColor)
				.withFade(mixColor)
				.with(getFireworkType())
				.build();
		fireworkMeta.addEffects(fireworkEffect);
		firework.setFireworkMeta(fireworkMeta);
		firework.setVelocity(location.getDirection().multiply(0));

		// Firework launched by plugin: damage will later be cancelled out.
		fireworkListener.addFirework(firework);
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
		if (serverVersion >= 9) {
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
		List<Reward> rewards = rewardParser.parseRewards("AllAchievementsReceivedRewards");
		rewards.forEach(r -> r.getRewarder().accept(player));
		player.sendMessage(langAllAchievementsReceived);
		rewards.stream()
				.map(Reward::getChatTexts)
				.flatMap(List::stream)
				.map(m -> StringHelper.replacePlayerPlaceholders(m, player))
				.forEach(t -> player.sendMessage(pluginHeader + ChatColor.translateAlternateColorCodes('&', t)));
	}
}
