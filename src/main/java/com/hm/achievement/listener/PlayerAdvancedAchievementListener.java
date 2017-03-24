package com.hm.achievement.listener;

import java.util.Random;
import java.util.logging.Level;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;

import com.google.common.base.Strings;
import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.PlayerAdvancedAchievementEvent;
import com.hm.mcshared.particle.PacketSender;
import com.hm.mcshared.particle.ParticleEffect;

/**
 * Listener class to deal with achievement receptions: rewards, display and database operations.
 * 
 * @author Pyves
 *
 */
public class PlayerAdvancedAchievementListener extends AbstractListener implements Listener {

	private static final Random RANDOM = new Random();

	private boolean rewardCommandNotif;
	private String fireworkStyle;
	private boolean fireworks;
	private boolean simplifiedReception;
	private boolean titleScreen;

	public PlayerAdvancedAchievementListener(AdvancedAchievements plugin) {
		super(plugin);
		extractParameters();
	}

	public void extractParameters() {
		fireworkStyle = plugin.getPluginConfig().getString("FireworkStyle", "BALL_LARGE");
		fireworks = plugin.getPluginConfig().getBoolean("Firework", true);
		simplifiedReception = plugin.getPluginConfig().getBoolean("SimplifiedReception", false);
		titleScreen = plugin.getPluginConfig().getBoolean("TitleScreen", true);
		// No longer available in default config, kept for compatibility with versions prior to 2.1; defines whether
		// a player is notified in case of a command reward.
		rewardCommandNotif = plugin.getPluginConfig().getBoolean("RewardCommandNotif", true);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerAdvancedAchievementReception(PlayerAdvancedAchievementEvent event) {
		Player player = event.getPlayer();
		plugin.getDb().registerAchievement(player.getUniqueId(), event.getName(), event.getMessage());
		String uuid = player.getUniqueId().toString();
		plugin.getPoolsManager().getReceivedAchievementsCache().put(uuid, event.getName());
		plugin.getPoolsManager().getNotReceivedAchievementsCache().remove(uuid, event.getName());
		plugin.getPoolsManager().getTotalPlayerAchievementsCache().put(uuid,
				plugin.getPoolsManager().getPlayerTotalAchievements(player.getUniqueId()) + 1);
		displayAchievement(player, event.getName(), event.getDisplayName(), event.getMessage());
		if (event.getMoneyReward() > 0) {
			rewardMoney(player, event.getMoneyReward());
		}
		if (event.getItemReward() != null) {
			rewardItem(player, event.getItemReward());
		}
		if (event.getCommandRewards() != null && event.getCommandRewards().length > 0) {
			rewardCommands(player, event.getCommandRewards());
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
		String rewardMsg = plugin.getPluginLang().getString("command-reward", "Reward command carried out!");
		if (!rewardCommandNotif || rewardMsg.length() == 0) {
			return;
		}
		player.sendMessage(plugin.getChatHeader() + rewardMsg);
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
		player.sendMessage(plugin.getChatHeader()
				+ plugin.getPluginLang().getString("item-reward-received", "You received an item reward:") + " "
				+ plugin.getReward().getItemName(item));
	}

	/**
	 * Gives a money reward to a player.
	 * 
	 * @param player
	 * @param amount
	 */
	@SuppressWarnings("deprecation")
	private void rewardMoney(Player player, int amount) {
		if (plugin.setUpEconomy(true)) {
			try {
				plugin.getEconomy().depositPlayer(player, amount);
			} catch (NoSuchMethodError e) {
				// Deprecated method, but was the only one existing prior to Vault 1.4.
				plugin.getEconomy().depositPlayer(player.getName(), amount);
			}

			String currencyName = plugin.getReward().getCurrencyName(amount);

			player.sendMessage(plugin.getChatHeader() + ChatColor.translateAlternateColorCodes('&',
					StringUtils.replaceOnce(
							plugin.getPluginLang().getString("money-reward-received", "You received: AMOUNT!"),
							"AMOUNT", amount + " " + currencyName)));
		}
	}

	/**
	 * Displays chat messages, screen title and launches a firework when a player receives an achievement.
	 * 
	 * @param player
	 * @param configAchievement
	 */
	private void displayAchievement(Player player, String name, String displayName, String description) {
		String nameToShowUser;
		if (Strings.isNullOrEmpty(displayName)) {
			// Use the achievement key name (this name is used in the achievements table in the database).
			nameToShowUser = ChatColor.translateAlternateColorCodes('&', name);
			plugin.getLogger().info("Player " + player.getName() + " received the achievement: " + name);
		} else {
			// Display name is defined; use it.
			nameToShowUser = ChatColor.translateAlternateColorCodes('&', displayName);
			plugin.getLogger().info(
					"Player " + player.getName() + " received the achievement: " + name + " (" + displayName + ")");
		}

		String msg = ChatColor.translateAlternateColorCodes('&', description);

		player.sendMessage(
				plugin.getChatHeader() + plugin.getPluginLang().getString("achievement-new", "New Achievement:") + " "
						+ ChatColor.WHITE + nameToShowUser);

		// Notify other online players that the player has received an achievement.
		for (Player p : plugin.getServer().getOnlinePlayers()) {
			// Notify other players only if chatNotify is enabled and player has not used /aach toggle, or if
			// chatNotify is disabled and player has used /aach toggle.
			if ((plugin.isChatNotify() ^ plugin.getToggleCommand().isPlayerToggled(p))
					&& !p.getName().equals(player.getName())) {
				p.sendMessage(plugin.getChatHeader()
						+ StringUtils.replaceOnce(plugin.getPluginLang().getString("achievement-received",
								"PLAYER received the achievement:"), "PLAYER", player.getName())
						+ " " + ChatColor.WHITE + nameToShowUser);
			}
		}
		player.sendMessage(plugin.getChatHeader() + ChatColor.WHITE + msg);

		if (fireworks) {
			displayFirework(player);
		} else if (simplifiedReception) {
			displaySimplifiedReception(player);
		}

		if (titleScreen) {
			displayTitle(player, nameToShowUser, msg);
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
			if ("RANDOM".equalsIgnoreCase(fireworkStyle)) {
				effectBuilder.with(getRandomFireworkType());
			} else {
				try {
					// Firework style must be one of the following: BALL_LARGE, BALL, BURST, CREEPER or STAR.
					effectBuilder.with(Type.valueOf(fireworkStyle.toUpperCase()));
				} catch (Exception e) {
					effectBuilder.with(Type.BALL_LARGE);
					plugin.getLogger().warning(
							"Error while loading FireworkStyle. Please check your config. Loading default style.");
				}
			}
			fireworkMeta.addEffects(effectBuilder.build());
			firework.setVelocity(player.getLocation().getDirection().multiply(0));
			firework.setFireworkMeta(fireworkMeta);
			// Firework launched by plugin: damage will later be cancelled out.
			plugin.getFireworkListener().addFirework(firework);
		} catch (Exception e) {
			// Particle effect workaround to handle bug in early Spigot 1.9 and 1.11 releases. We try to simulate a
			// firework.
			player.getWorld().playSound(location, Sound.ENTITY_FIREWORK_LAUNCH, 1, 0.6f);
			ParticleEffect.FIREWORKS_SPARK.display(0, 3, 0, 0.1f, 500, location, 1);
			player.getWorld().playSound(location, Sound.ENTITY_FIREWORK_BLAST, 1, 0.6f);
			player.getWorld().playSound(location, Sound.ENTITY_FIREWORK_TWINKLE, 1, 0.6f);
		}
	}

	/**
	 * Returns a random firework type.
	 * 
	 * @return
	 */
	private Type getRandomFireworkType() {
		Type[] fireworkTypes = Type.values();
		return fireworkTypes[RANDOM.nextInt(fireworkTypes.length)];
	}

	/**
	 * Displays a simplified particle effect and calm sound when receiving an achievement. Is used instead of
	 * displayFirework.
	 * 
	 * @param player
	 */
	private void displaySimplifiedReception(Player player) {
		Location location = player.getLocation();
		player.getWorld().playSound(location, Sound.ENTITY_PLAYER_LEVELUP, 1, 0.9f);
		ParticleEffect.FIREWORKS_SPARK.display(0, 3, 0, 0.1f, 500, location, 1);
	}
}
