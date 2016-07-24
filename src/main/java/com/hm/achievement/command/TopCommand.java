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

public class TopCommand {

	private AdvancedAchievements plugin;
	private long lastTopTime;
	private long lastWeekTime;
	private long lastMonthTime;
	private long lastTotalGlobalPlayersTime;
	private long lastTotalWeeklyPlayersTime;
	private long lastTotalMonthlyPlayersTime;
	private int totalPlayersGlobal;
	private int totalPlayersWeekly;
	private int totalPlayersMonthly;
	private ArrayList<String> achievementsTop;
	private ArrayList<String> achievementsWeek;
	private ArrayList<String> achievementsMonth;
	private int topList;
	private boolean additionalEffects;
	private boolean sound;
	private int version;

	public TopCommand(AdvancedAchievements plugin) {

		this.plugin = plugin;
		lastTopTime = 0L;
		lastWeekTime = 0L;
		lastMonthTime = 0L;
		lastTotalGlobalPlayersTime = 0L;
		lastTotalWeeklyPlayersTime = 0L;
		lastTotalMonthlyPlayersTime = 0L;
		topList = plugin.getPluginConfig().getInt("TopList", 5);
		additionalEffects = plugin.getPluginConfig().getBoolean("AdditionalEffects", true);
		sound = plugin.getPluginConfig().getBoolean("Sound", true);
		// Simple and fast check to compare versions. Might need to
		// be updated in the future depending on how the Minecraft
		// versions change in the future.
		version = Integer.parseInt(PackageType.getServerVersion().split("_")[1]);
	}

	/**
	 * Get list of players with the most achievements and display player's rank. All time ranking.
	 */
	public void getTop(CommandSender sender) {

		long currentTime = System.currentTimeMillis();
		int rank = Integer.MAX_VALUE;
		if (sender instanceof Player)
			rank = plugin.getDb().getPlayerRank((Player) sender, 0L);
		if (currentTime - lastTopTime >= 60000) {

			achievementsTop = plugin.getDb().getTopList(topList, 0L);
			lastTopTime = System.currentTimeMillis();
		}

		sender.sendMessage(plugin.getChatHeader()
				+ plugin.getPluginLang().getString("top-achievement", "Top achievement owners:"));

		for (int i = 0; i < achievementsTop.size(); i += 2) {
			try {
				String playerName = Bukkit.getServer().getOfflinePlayer(UUID.fromString(achievementsTop.get(i)))
						.getName();
				// Name in plugin color if player in top list.
				if (sender instanceof Player && playerName.equals(((Player) sender).getName()))
					sender.sendMessage(ChatColor.GRAY + "[" + plugin.getColor() + ((i + 2) >> 1) + ChatColor.GRAY + "] "
							+ plugin.getColor() + playerName + " - " + achievementsTop.get(i + 1));
				else
					sender.sendMessage(ChatColor.GRAY + "[" + plugin.getColor() + ((i + 2) >> 1) + ChatColor.GRAY + "] "
							+ playerName + " - " + achievementsTop.get(i + 1));
			} catch (Exception ex) {
				plugin.getLogger().warning("Top command: name corresponding to UUID not found.");
			}
		}
		if (sender instanceof Player) {

			if (rank <= topList) {
				launchEffects(sender);
			}
			if (currentTime - lastTotalGlobalPlayersTime >= 60000) {
				totalPlayersGlobal = plugin.getDb().getTotalPlayers(0L);
				lastTotalGlobalPlayersTime = System.currentTimeMillis();
			}
			sender.sendMessage(plugin.getChatHeader() + plugin.getPluginLang().getString("player-rank", "Current rank:")
					+ " " + plugin.getColor() + rank + ChatColor.GRAY + "/" + plugin.getColor() + totalPlayersGlobal);
		}
	}

	/**
	 * Get list of players with the most achievements and display player's rank. Weekly ranking.
	 */
	public void getWeek(CommandSender sender) {

		long currentTime = System.currentTimeMillis();
		int rank = Integer.MAX_VALUE;
		Calendar c = Calendar.getInstance(); // this takes current date
		c.set(Calendar.DAY_OF_WEEK, c.getFirstDayOfWeek());
		if (sender instanceof Player)
			rank = plugin.getDb().getPlayerRank((Player) sender, c.getTimeInMillis());
		if (currentTime - lastWeekTime >= 60000) {

			achievementsWeek = plugin.getDb().getTopList(topList, c.getTimeInMillis());
			lastWeekTime = System.currentTimeMillis();
		}

		sender.sendMessage(plugin.getChatHeader()
				+ plugin.getPluginLang().getString("week-achievement", "Weekly achievement rankings:"));

		for (int i = 0; i < achievementsWeek.size(); i += 2) {
			try {
				String playerName = Bukkit.getServer().getOfflinePlayer(UUID.fromString(achievementsWeek.get(i)))
						.getName();
				// Name in plugin color if player in top list.
				if (sender instanceof Player && playerName.equals(((Player) sender).getName()))
					sender.sendMessage(ChatColor.GRAY + "[" + plugin.getColor() + ((i + 2) >> 1) + ChatColor.GRAY + "] "
							+ plugin.getColor() + playerName + " - " + achievementsWeek.get(i + 1));
				else
					sender.sendMessage(ChatColor.GRAY + "[" + plugin.getColor() + ((i + 2) >> 1) + ChatColor.GRAY + "] "
							+ playerName + " - " + achievementsWeek.get(i + 1));
			} catch (Exception ex) {
				plugin.getLogger().warning("Week command: name corresponding to UUID not found.");
			}
		}
		if (sender instanceof Player) {

			if (rank <= topList) {
				launchEffects(sender);
			}

			if (currentTime - lastTotalWeeklyPlayersTime >= 60000) {
				totalPlayersWeekly = plugin.getDb().getTotalPlayers(c.getTimeInMillis());
				lastTotalWeeklyPlayersTime = System.currentTimeMillis();
			}

			if (rank <= totalPlayersWeekly)
				sender.sendMessage(plugin.getChatHeader()
						+ plugin.getPluginLang().getString("player-rank", "Current rank:") + " " + plugin.getColor()
						+ rank + ChatColor.GRAY + "/" + plugin.getColor() + totalPlayersWeekly);
			else
				sender.sendMessage(plugin.getChatHeader() + plugin.getPluginLang().getString("not-ranked",
						"You are currently not ranked for this period."));
		}
	}

	/**
	 * Get list of players with the most achievements and display player's rank. Monthly ranking.
	 */
	public void getMonth(CommandSender sender) {

		long currentTime = System.currentTimeMillis();
		int rank = Integer.MAX_VALUE;
		Calendar c = Calendar.getInstance(); // this takes current date
		c.set(Calendar.DAY_OF_MONTH, 1);
		if (sender instanceof Player)
			rank = plugin.getDb().getPlayerRank((Player) sender, c.getTimeInMillis());
		if (currentTime - lastMonthTime >= 60000) {

			achievementsMonth = plugin.getDb().getTopList(topList, c.getTimeInMillis());
			lastMonthTime = System.currentTimeMillis();
		}

		sender.sendMessage(plugin.getChatHeader()
				+ plugin.getPluginLang().getString("month-achievement", "Monthly achievement rankings:"));

		for (int i = 0; i < achievementsMonth.size(); i += 2) {
			try {
				String playerName = Bukkit.getServer().getOfflinePlayer(UUID.fromString(achievementsMonth.get(i)))
						.getName();
				// Name in plugin color if player in top list.
				if (sender instanceof Player && playerName.equals(((Player) sender).getName()))
					sender.sendMessage(ChatColor.GRAY + "[" + plugin.getColor() + ((i + 2) >> 1) + ChatColor.GRAY + "] "
							+ plugin.getColor() + playerName + " - " + achievementsMonth.get(i + 1));
				else
					sender.sendMessage(ChatColor.GRAY + "[" + plugin.getColor() + ((i + 2) >> 1) + ChatColor.GRAY + "] "
							+ playerName + " - " + achievementsMonth.get(i + 1));
			} catch (Exception ex) {
				plugin.getLogger().warning("Month command: name corresponding to UUID not found.");
			}
		}
		if (sender instanceof Player) {

			if (rank <= topList) {
				launchEffects(sender);
			}

			if (currentTime - lastTotalMonthlyPlayersTime >= 60000) {
				totalPlayersMonthly = plugin.getDb().getTotalPlayers(c.getTimeInMillis());
				lastTotalMonthlyPlayersTime = System.currentTimeMillis();
			}

			if (rank <= totalPlayersMonthly)
				sender.sendMessage(plugin.getChatHeader()
						+ plugin.getPluginLang().getString("player-rank", "Current rank:") + " " + plugin.getColor()
						+ rank + ChatColor.GRAY + "/" + plugin.getColor() + totalPlayersMonthly);
			else
				sender.sendMessage(plugin.getChatHeader() + plugin.getPluginLang().getString("not-ranked",
						"You are currently not ranked for this period."));
		}
	}

	private void launchEffects(CommandSender sender) {

		try {
			if (additionalEffects)
				// Play special effect when in top list.
				ParticleEffect.PORTAL.display(0, 1, 0, 0.5f, 1000, ((Player) sender).getLocation(), 1);

		} catch (Exception ex) {
			plugin.getLogger().severe("Error while displaying additional particle effects.");
		}

		// Play special sound when in top list.
		if (sound) {
			if (version < 9) // Old enum for versions prior to Minecraft
								// 1.9.
				((Player) sender).getWorld().playSound(((Player) sender).getLocation(), Sound.valueOf("FIREWORK_BLAST"),
						1, 0.6f);
			else
				((Player) sender).getWorld().playSound(((Player) sender).getLocation(),
						Sound.ENTITY_FIREWORK_LARGE_BLAST, 1, 0.7f);
		}
	}

}
