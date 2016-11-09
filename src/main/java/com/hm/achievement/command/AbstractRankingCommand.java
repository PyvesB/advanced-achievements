package com.hm.achievement.command;

import java.util.List;
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
 * Abstract class in charge of factoring out common functionality for /aach top, week and month commands.
 * 
 * @author Pyves
 */
public abstract class AbstractRankingCommand extends AbstractCommand {

	protected String languageHeaderKey;
	protected String defaultHeaderMessage;

	private int topList;
	private boolean additionalEffects;
	private boolean sound;
	private int version;

	// Used for caching.
	private int totalPlayersInRanking;
	private List<String> playersRanking;

	// Caching cooldown.
	private long lastCommandTime;

	private final static int VALUES_EXPIRATION_DELAY = 60000;

	protected AbstractRankingCommand(AdvancedAchievements plugin) {

		super(plugin);
		lastCommandTime = 0L;
		// Load configuration parameters.
		topList = plugin.getPluginConfig().getInt("TopList", 5);
		additionalEffects = plugin.getPluginConfig().getBoolean("AdditionalEffects", true);
		sound = plugin.getPluginConfig().getBoolean("Sound", true);
		// Simple and fast check to compare versions. Might need to be updated in the future depending on how the
		// Minecraft versions change in the future.
		version = Integer.parseInt(PackageType.getServerVersion().split("_")[1]);
	}

	@Override
	public void executeCommand(CommandSender sender, String[] args) {

		long currentTime = System.currentTimeMillis();
		int rank = Integer.MAX_VALUE;

		long rankingStartTime = getRankingStartTime();

		if (sender instanceof Player)
			rank = plugin.getDb().getPlayerRank((Player) sender, rankingStartTime);
		// Update top list on given period if too old.
		if (currentTime - lastCommandTime >= VALUES_EXPIRATION_DELAY)
			playersRanking = plugin.getDb().getTopList(topList, rankingStartTime);

		sender.sendMessage(
				plugin.getChatHeader() + plugin.getPluginLang().getString(languageHeaderKey, defaultHeaderMessage));

		for (int i = 0; i < playersRanking.size(); i += 2) {
			try {
				String playerName = Bukkit.getServer().getOfflinePlayer(UUID.fromString(playersRanking.get(i)))
						.getName();
				// Color the name of the player if he is in the top list.
				if (sender instanceof Player && playerName.equals(((Player) sender).getName()))
					sender.sendMessage(ChatColor.GRAY + "[" + plugin.getColor() + ((i + 2) >> 1) + ChatColor.GRAY + "] "
							+ plugin.getColor() + playerName + " - " + playersRanking.get(i + 1));
				else
					sender.sendMessage(ChatColor.GRAY + "[" + plugin.getColor() + ((i + 2) >> 1) + ChatColor.GRAY + "] "
							+ playerName + " - " + playersRanking.get(i + 1));
			} catch (Exception e) {
				plugin.getLogger().warning("Ranking command: name corresponding to UUID not found.");
			}
		}
		if (sender instanceof Player) {
			// Launch effect if player is in top list.
			if (rank <= topList)
				launchEffects((Player) sender);

			// Update number of players for this period if value is too old, and update timestamp.
			if (currentTime - lastCommandTime >= VALUES_EXPIRATION_DELAY) {
				totalPlayersInRanking = plugin.getDb().getTotalPlayers(rankingStartTime);
				lastCommandTime = System.currentTimeMillis();
			}

			// If rank > totalPlayersInRanking, player has not yet received an achievement for this period, not ranked.
			if (rank <= totalPlayersInRanking)
				sender.sendMessage(plugin.getChatHeader()
						+ plugin.getPluginLang().getString("player-rank", "Current rank:") + " " + plugin.getColor()
						+ rank + ChatColor.GRAY + "/" + plugin.getColor() + totalPlayersInRanking);
			else
				sender.sendMessage(plugin.getChatHeader() + plugin.getPluginLang().getString("not-ranked",
						"You are currently not ranked for this period."));
		}
	}

	/**
	 * Returns start time for a specific ranking period.
	 * 
	 * @return time (epoch) in millis
	 */
	protected abstract long getRankingStartTime();

	/**
	 * Launches sound and particle effects if player is in a top list.
	 * 
	 * @param sender
	 */
	private void launchEffects(Player player) {

		try {
			if (additionalEffects)
				// Play special effect when in top list.
				ParticleEffect.PORTAL.display(0, 1, 0, 0.5f, 1000, player.getLocation(), 1);
		} catch (Exception e) {
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
