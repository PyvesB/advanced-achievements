package com.hm.achievement.command;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.particle.ParticleEffect;
import com.hm.achievement.particle.ReflectionUtils.PackageType;

/**
 * Class in charge of handling the /aach top command, which displays global, weekly and monthly player rankings.
 * 
 * @author Pyves
 */
public class TopCommand {

	private AdvancedAchievements plugin;
	private long lastTopTime;
	private long lastWeekTime;
	private long lastMonthTime;
	private int totalGlobalPlayers;
	private int totalWeeklyPlayers;
	private int totalMonthlyPlayers;
	private ArrayList<String> globalPlayersTop;
	private ArrayList<String> weeklyPlayersTop;
	private ArrayList<String> monthlyPlayersTop;
	private int topList;
	private boolean additionalEffects;
	private boolean sound;
	private int version;

	private final static int VALUES_EXPIRATION_DELAY = 60000;

	public TopCommand(AdvancedAchievements plugin) {

		this.plugin = plugin;
		// Timestamps, set to 0 initially.
		lastTopTime = 0L;
		lastWeekTime = 0L;
		lastMonthTime = 0L;
		// Load configuration parameters.
		topList = plugin.getPluginConfig().getInt("TopList", 5);
		additionalEffects = plugin.getPluginConfig().getBoolean("AdditionalEffects", true);
		sound = plugin.getPluginConfig().getBoolean("Sound", true);
		// Simple and fast check to compare versions. Might need to be updated in the future depending on how the
		// Minecraft versions change in the future.
		version = Integer.parseInt(PackageType.getServerVersion().split("_")[1]);
	}

	/**
	 * Get list of players with the most achievements and display player's rank. All time ranking.
	 * 
	 * @param sender
	 */
	public void getTop(CommandSender sender) {

		long currentTime = System.currentTimeMillis();
		int rank = Integer.MAX_VALUE;
		// Get the global rank of the player.
		if (sender instanceof Player)
			rank = plugin.getDb().getPlayerRank((Player) sender, 0L);
		// Update global top list if too old.
		if (currentTime - lastTopTime >= VALUES_EXPIRATION_DELAY)
			globalPlayersTop = plugin.getDb().getTopList(topList, 0L);

		sender.sendMessage(plugin.getChatHeader()
				+ plugin.getPluginLang().getString("top-achievement", "Top achievement owners:"));

		for (int i = 0; i < globalPlayersTop.size(); i += 2) {
			try {
				String playerName = Bukkit.getServer().getOfflinePlayer(UUID.fromString(globalPlayersTop.get(i)))
						.getName();
				// Color the name of the player if he is in the top list.
				if (sender instanceof Player && playerName.equals(((Player) sender).getName()))
					sender.sendMessage(ChatColor.GRAY + "[" + plugin.getColor() + ((i + 2) >> 1) + ChatColor.GRAY + "] "
							+ plugin.getColor() + playerName + " - " + globalPlayersTop.get(i + 1));
				else
					sender.sendMessage(ChatColor.GRAY + "[" + plugin.getColor() + ((i + 2) >> 1) + ChatColor.GRAY + "] "
							+ playerName + " - " + globalPlayersTop.get(i + 1));
			} catch (Exception ex) {
				plugin.getLogger().warning("Top command: name corresponding to UUID not found.");
			}
		}
		if (sender instanceof Player) {
			// Launch effect if player is in top list.
			if (rank <= topList)
				launchEffects((Player) sender);

			// Update global number of players if value is too old, and update timestamp.
			if (currentTime - lastTopTime >= VALUES_EXPIRATION_DELAY) {
				totalGlobalPlayers = plugin.getDb().getTotalPlayers(0L);
				lastTopTime = System.currentTimeMillis();
			}
			sender.sendMessage(plugin.getChatHeader() + plugin.getPluginLang().getString("player-rank", "Current rank:")
					+ " " + plugin.getColor() + rank + ChatColor.GRAY + "/" + plugin.getColor() + totalGlobalPlayers);
		}
	}

	/**
	 * Get list of players with the most achievements and display player's rank. Weekly ranking.
	 * 
	 * @param sender
	 */
	public void getWeek(CommandSender sender) {

		long currentTime = System.currentTimeMillis();
		int rank = Integer.MAX_VALUE;
		Calendar c = Calendar.getInstance();
		// Set calendar to the first day of the week.
		c.set(Calendar.DAY_OF_WEEK, c.getFirstDayOfWeek());
		// Get the weekly rank of the player.
		if (sender instanceof Player)
			rank = plugin.getDb().getPlayerRank((Player) sender, c.getTimeInMillis());
		// Update weekly top list if too old.
		if (currentTime - lastWeekTime >= VALUES_EXPIRATION_DELAY)
			weeklyPlayersTop = plugin.getDb().getTopList(topList, c.getTimeInMillis());

		sender.sendMessage(plugin.getChatHeader()
				+ plugin.getPluginLang().getString("week-achievement", "Weekly achievement rankings:"));

		for (int i = 0; i < weeklyPlayersTop.size(); i += 2) {
			try {
				String playerName = Bukkit.getServer().getOfflinePlayer(UUID.fromString(weeklyPlayersTop.get(i)))
						.getName();
				// Color the name of the player if he is in the top list.
				if (sender instanceof Player && playerName.equals(((Player) sender).getName()))
					sender.sendMessage(ChatColor.GRAY + "[" + plugin.getColor() + ((i + 2) >> 1) + ChatColor.GRAY + "] "
							+ plugin.getColor() + playerName + " - " + weeklyPlayersTop.get(i + 1));
				else
					sender.sendMessage(ChatColor.GRAY + "[" + plugin.getColor() + ((i + 2) >> 1) + ChatColor.GRAY + "] "
							+ playerName + " - " + weeklyPlayersTop.get(i + 1));
			} catch (Exception ex) {
				plugin.getLogger().warning("Week command: name corresponding to UUID not found.");
			}
		}
		if (sender instanceof Player) {
			// Launch effect if player is in top list.
			if (rank <= topList)
				launchEffects((Player) sender);

			// Update weekly number of players if value is too old, and update timestamp.
			if (currentTime - lastWeekTime >= VALUES_EXPIRATION_DELAY) {
				totalWeeklyPlayers = plugin.getDb().getTotalPlayers(c.getTimeInMillis());
				lastWeekTime = System.currentTimeMillis();
			}

			// If rank > totalWeeklyPlayers, player has not yet received an achievement this week, not ranked.
			if (rank <= totalWeeklyPlayers)
				sender.sendMessage(plugin.getChatHeader()
						+ plugin.getPluginLang().getString("player-rank", "Current rank:") + " " + plugin.getColor()
						+ rank + ChatColor.GRAY + "/" + plugin.getColor() + totalWeeklyPlayers);
			else
				sender.sendMessage(plugin.getChatHeader() + plugin.getPluginLang().getString("not-ranked",
						"You are currently not ranked for this period."));
		}
	}

	/**
	 * Get list of players with the most achievements and display player's rank. Monthly ranking.
	 * 
	 * @param sender
	 */
	public void getMonth(CommandSender sender) {

		long currentTime = System.currentTimeMillis();
		int rank = Integer.MAX_VALUE;
		Calendar c = Calendar.getInstance();
		// Set calendar to the first day of the month.
		c.set(Calendar.DAY_OF_MONTH, 1);
		// Get the monthly rank of the player.
		if (sender instanceof Player)
			rank = plugin.getDb().getPlayerRank((Player) sender, c.getTimeInMillis());
		// Update monthly top list if too old.
		if (currentTime - lastMonthTime >= VALUES_EXPIRATION_DELAY)
			monthlyPlayersTop = plugin.getDb().getTopList(topList, c.getTimeInMillis());

		sender.sendMessage(plugin.getChatHeader()
				+ plugin.getPluginLang().getString("month-achievement", "Monthly achievement rankings:"));

		for (int i = 0; i < monthlyPlayersTop.size(); i += 2) {
			try {
				String playerName = Bukkit.getServer().getOfflinePlayer(UUID.fromString(monthlyPlayersTop.get(i)))
						.getName();
				// Color the name of the player if he is in the top list.
				if (sender instanceof Player && playerName.equals(((Player) sender).getName()))
					sender.sendMessage(ChatColor.GRAY + "[" + plugin.getColor() + ((i + 2) >> 1) + ChatColor.GRAY + "] "
							+ plugin.getColor() + playerName + " - " + monthlyPlayersTop.get(i + 1));
				else
					sender.sendMessage(ChatColor.GRAY + "[" + plugin.getColor() + ((i + 2) >> 1) + ChatColor.GRAY + "] "
							+ playerName + " - " + monthlyPlayersTop.get(i + 1));
			} catch (Exception ex) {
				plugin.getLogger().warning("Month command: name corresponding to UUID not found.");
			}
		}
		if (sender instanceof Player) {
			// Launch effect if player is in top list.
			if (rank <= topList)
				launchEffects((Player) sender);

			// Update monthly number of players if value is too old, and update timestamp.
			if (currentTime - lastMonthTime >= VALUES_EXPIRATION_DELAY) {
				totalMonthlyPlayers = plugin.getDb().getTotalPlayers(c.getTimeInMillis());
				lastMonthTime = System.currentTimeMillis();
			}

			// If rank > totalMonthlyPlayers, player has not yet received an achievement this month, not ranked.
			if (rank <= totalMonthlyPlayers)
				sender.sendMessage(plugin.getChatHeader()
						+ plugin.getPluginLang().getString("player-rank", "Current rank:") + " " + plugin.getColor()
						+ rank + ChatColor.GRAY + "/" + plugin.getColor() + totalMonthlyPlayers);
			else
				sender.sendMessage(plugin.getChatHeader() + plugin.getPluginLang().getString("not-ranked",
						"You are currently not ranked for this period."));
		}
	}

	/**
	 * Launch sound and particle effects if player is in a top list.
	 * 
	 * @param sender
	 */
	private void launchEffects(Player player) {

		try {
			if (additionalEffects)
				// Play special effect when in top list.
				ParticleEffect.PORTAL.display(0, 1, 0, 0.5f, 1000, player.getLocation(), 1);
		} catch (Exception ex) {
			plugin.getLogger().severe("Error while displaying additional particle effects.");
		}

		// Play special sound when in top list.
		if (sound) {
			if (version < 9) {
				// Old enum for versions prior to Minecraft 1.9. Retrieving it by name as it does no longer exist in
				// newer versions.
				player.getWorld().playSound(player.getLocation(), Sound.valueOf("FIREWORK_BLAST"), 1, 0.6f);
			} else {
				// Play sound with enum for newer versions.
				player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_LARGE_BLAST, 1, 0.7f);
			}
		}
	}
}
