package com.hm.achievement.command;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.language.Lang;
import com.hm.achievement.particle.ParticleEffect;

public class TopCommand {

	private AdvancedAchievements plugin;
	private long lastTopTime;
	private ArrayList<String> achievementsTop;
	private int topList;
	private boolean additionalEffects;
	private boolean sound;

	public TopCommand(AdvancedAchievements plugin) {

		this.plugin = plugin;
		lastTopTime = 0L;
		topList = plugin.getConfig().getInt("TopList", 5);
		additionalEffects = plugin.getConfig().getBoolean("AdditionalEffects", true);
		sound = plugin.getConfig().getBoolean("Sound", true);
	}

	/**
	 * Get list of players with the most achievements and diplay player's rank.
	 */
	public void getTop(CommandSender sender) {

		long currentTime = System.currentTimeMillis();
		int rank = Integer.MAX_VALUE;
		if (sender instanceof Player)
			rank = plugin.getDb().getRank(plugin.getDb().getPlayerAchievementsAmount((Player) sender));
		if (currentTime - lastTopTime >= 60000) {

			achievementsTop = plugin.getDb().getTopList(topList);
			lastTopTime = System.currentTimeMillis();
		}

		sender.sendMessage(plugin.getChatHeader() + Lang.TOP_ACHIEVEMENT);

		for (int i = 0; i < achievementsTop.size(); i += 2) {
			try {
				String playerName = Bukkit.getServer().getOfflinePlayer(UUID.fromString(achievementsTop.get(i)))
						.getName();
				// Name in purple if player in top list.
				if (sender instanceof Player && playerName.equals(((Player) sender).getName()))
					sender.sendMessage(ChatColor.GRAY + "[" + ChatColor.DARK_PURPLE + (i + 2) / 2 + ChatColor.GRAY
							+ "] " + ChatColor.DARK_PURPLE + playerName + " - " + achievementsTop.get(i + 1));
				else
					sender.sendMessage(ChatColor.GRAY + "[" + ChatColor.DARK_PURPLE + (i + 2) / 2 + ChatColor.GRAY
							+ "] " + playerName + " - " + achievementsTop.get(i + 1));
			} catch (Exception ex) {
				plugin.getLogger().warning("Top command: name corresponding to UUID not found.");
			}
		}
		if (sender instanceof Player) {

			if (rank <= topList) {
				try {
					if (additionalEffects)
						// Play special effect when in top list.
						ParticleEffect.PORTAL.display(0, 1, 0, 0.1f, 500, ((Player) sender).getLocation(), 1);

				} catch (Exception ex) {
					plugin.getLogger().severe("Error while displaying additional particle effects.");
				}

				if (sound)
					// Play special sound when in top list.
					((Player) sender).getWorld().playSound(((Player) sender).getLocation(), Sound.FIREWORK_BLAST, 1,
							0.6f);
			}

			int totalPlayers = plugin.getDb().getTotalPlayers();
			sender.sendMessage(plugin.getChatHeader() + Lang.PLAYER_RANK + " " + ChatColor.DARK_PURPLE + rank
					+ ChatColor.GRAY + "/" + ChatColor.DARK_PURPLE + totalPlayers);
		}
	}

}
