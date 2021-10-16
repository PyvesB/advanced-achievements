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
import org.bukkit.Sound;
import org.bukkit.advancement.Advancement;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.metadata.FixedMetadataValue;

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
import com.hm.achievement.utils.StringHelper;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * Listener class to deal with achievement receptions: rewards, display and database operations.
 *
 * @author Pyves
 */
@Singleton
public class PlayerAdvancedAchievementListener implements Listener, Reloadable {

	private static final Random RANDOM = new Random();
	private static final String ADVANCED_ACHIEVEMENTS_FIREWORK = "advanced_achievements_firework";
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
	private final Logger logger;
	private final StringBuilder pluginHeader;
	private final CacheManager cacheManager;
	private final AdvancedAchievements advancedAchievements;
	private final RewardParser rewardParser;
	private final AchievementMap achievementMap;
	private final AbstractDatabaseManager databaseManager;
	private final ToggleCommand toggleCommand;
	private final FancyMessageSender fancyMessageSender;

	private String configFireworkStyle;
	private boolean configFirework;
	private Color configColor;
	private Color mixColor;
	private BarColor barColor;
	private boolean configSimplifiedReception;
	private boolean configTitleScreen;
	private boolean configNotifyOtherPlayers;
	private boolean configActionBarNotify;
	private boolean configHoverableReceiverChatText;
	private boolean configReceiverChatMessages;
	private boolean configBossBarProgress;

	private String langAchievementReceived;
	private String langAchievementNew;
	private String langAllAchievementsReceived;
	private String langBossBarProgress;

	@Inject
	public PlayerAdvancedAchievementListener(@Named("main") YamlConfiguration mainConfig,
			@Named("lang") YamlConfiguration langConfig, Logger logger, StringBuilder pluginHeader,
			CacheManager cacheManager, AdvancedAchievements advancedAchievements, RewardParser rewardParser,
			AchievementMap achievementMap, AbstractDatabaseManager databaseManager, ToggleCommand toggleCommand,
			FancyMessageSender fancyMessageSender) {
		this.mainConfig = mainConfig;
		this.langConfig = langConfig;
		this.logger = logger;
		this.pluginHeader = pluginHeader;
		this.cacheManager = cacheManager;
		this.advancedAchievements = advancedAchievements;
		this.rewardParser = rewardParser;
		this.achievementMap = achievementMap;
		this.databaseManager = databaseManager;
		this.toggleCommand = toggleCommand;
		this.fancyMessageSender = fancyMessageSender;
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
		configNotifyOtherPlayers = mainConfig.getBoolean("NotifyOtherPlayers");
		configActionBarNotify = mainConfig.getBoolean("ActionBarNotify");
		configHoverableReceiverChatText = mainConfig.getBoolean("HoverableReceiverChatText");
		configBossBarProgress = mainConfig.getBoolean("BossBarProgress");
		configReceiverChatMessages = mainConfig.getBoolean("ReceiverChatMessages");
		ChatColor chatColor = ChatColor.getByChar(mainConfig.getString("Color"));
		configColor = ColorHelper.convertChatColorToColor(chatColor);
		mixColor = Color.WHITE.mixColors(ColorHelper.convertChatColorToColor(FIREWORK_COLOR_MIX.get(chatColor)));
		barColor = ColorHelper.convertChatColorToBarColor(chatColor);

		langAchievementReceived = langConfig.getString("achievement-received") + " " + ChatColor.WHITE;
		langAchievementNew = pluginHeader + langConfig.getString("achievement-new") + " " + ChatColor.WHITE;
		langAllAchievementsReceived = pluginHeader + langConfig.getString("all-achievements-received");
		langBossBarProgress = langConfig.getString("boss-bar-progress");
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		// Cancel damage if the firework was launched by the plugin.
		event.setCancelled(event.getEntity().hasMetadata(ADVANCED_ACHIEVEMENTS_FIREWORK));
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerAdvancedAchievementReception(PlayerAdvancedAchievementEvent event) {
		Achievement achievement = event.getAchievement();
		Player player = event.getPlayer();
		// Achievement could have already been received if MultiCommand is set to true in the configuration.
		if (!cacheManager.hasPlayerAchievement(player.getUniqueId(), achievement.getName())) {
			cacheManager.registerNewlyReceivedAchievement(player.getUniqueId(), achievement.getName());

			Advancement advancement = Bukkit.getAdvancement(new NamespacedKey(advancedAchievements,
					AdvancementManager.getKey(achievement.getName())));
			// Matching advancement might not exist if user has not called /aach generate.
			if (advancement != null) {
				player.getAdvancementProgress(advancement).awardCriteria(AchievementAdvancement.CRITERIA_NAME);
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
			player.sendTitle(nameToShowUser, messageToShowUser, 10, 70, 20);
		}

		if (configBossBarProgress) {
			int receivedAmount = cacheManager.getPlayerAchievements(player.getUniqueId()).size();
			int totalAmount = achievementMap.getAll().size();
			double progress = ((double) receivedAmount) / totalAmount;
			String message = StringUtils.replaceOnce(langBossBarProgress, "AMOUNT", receivedAmount + "/" + totalAmount);
			BossBar bossBar = Bukkit.getServer().createBossBar(message, barColor, BarStyle.SOLID);
			bossBar.setProgress(progress);
			Bukkit.getScheduler().scheduleSyncDelayedTask(advancedAchievements, () -> bossBar.addPlayer(player), 110);
			Bukkit.getScheduler().scheduleSyncDelayedTask(advancedAchievements, () -> bossBar.removePlayer(player), 240);
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
		String message = langAchievementNew.contains("ACH")
				? StringUtils.replaceOnce(langAchievementNew, "ACH", nameToShowUser)
				: langAchievementNew + nameToShowUser;
		if (configHoverableReceiverChatText) {
			StringBuilder hover = new StringBuilder(messageToShowUser + "\n");
			chatMessages.forEach(t -> hover.append(ChatColor.translateAlternateColorCodes('&', t)).append("\n"));
			fancyMessageSender.sendHoverableMessage(player, message, hover.substring(0, hover.length() - 1), "white");
			return;
		}
		player.sendMessage(message);
		player.sendMessage(pluginHeader.toString() + ChatColor.WHITE + messageToShowUser);
		chatMessages.forEach(t -> player.sendMessage(pluginHeader + ChatColor.translateAlternateColorCodes('&', t)));
	}

	/**
	 * Displays an action bar message or chat notification to another player.
	 *
	 * @param receiver
	 * @param nameToShowUser
	 * @param otherPlayer
	 */
	private void displayNotification(Player receiver, String nameToShowUser, Player otherPlayer) {
		String message = langAchievementReceived.contains("ACH")
				? StringUtils.replaceEach(langAchievementReceived, new String[] { "PLAYER", "ACH" },
						new String[] { receiver.getName(), nameToShowUser })
				: StringUtils.replaceOnce(langAchievementReceived, "PLAYER", receiver.getName()) + nameToShowUser;
		if (configActionBarNotify) {
			otherPlayer.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("&o" + message));
		} else {
			otherPlayer.sendMessage(pluginHeader + message);
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
		firework.setMetadata(ADVANCED_ACHIEVEMENTS_FIREWORK, new FixedMetadataValue(advancedAchievements, true));
		firework.setVelocity(location.getDirection().multiply(0));
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
		player.playSound(player.getLocation(), Sound.valueOf("ENTITY_PLAYER_LEVELUP"), 1, 0.7f);
		player.spawnParticle(Particle.FIREWORKS_SPARK, player.getLocation(), 500, 0, 3, 0, 0.1f);
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
