package com.hm.achievement.command;

import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.hm.achievement.AdvancedAchievements;
import com.hm.mcshared.particle.ParticleEffect;

/**
 * Abstract class in charge of factoring out common functionality for /aach top, week and month commands.
 * 
 * @author Pyves
 */
public abstract class AbstractRankingCommand extends AbstractCommand {

	private static final int VALUES_EXPIRATION_DELAY = 60000;
	private static final int DECIMAL_CIRCLED_ONE = Integer.parseInt("2780", 16);
	private static final int DECIMAL_CIRCLED_ELEVEN = Integer.parseInt("246A", 16);
	private static final int DECIMAL_CIRCLED_TWENTY_ONE = Integer.parseInt("3251", 16);
	private static final int DECIMAL_CIRCLED_THIRTY_SIX = Integer.parseInt("32B1", 16);

	private final String languageHeaderKey;
	private final String defaultHeaderMessage;

	private int configTopList;
	private boolean configAdditionalEffects;
	private boolean configSound;
	private String langPeriodAchievement;
	private String langPlayerRank;
	private String langNotRanked;
	// Used for caching.
	private int totalPlayersInRanking;
	private List<String> playersRanking;
	private long lastCommandTime;

	protected AbstractRankingCommand(AdvancedAchievements plugin, String languageHeaderKey,
			String defaultHeaderMessage) {
		super(plugin);

		this.languageHeaderKey = languageHeaderKey;
		this.defaultHeaderMessage = defaultHeaderMessage;
		lastCommandTime = 0L;
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		configTopList = plugin.getPluginConfig().getInt("TopList", 5);
		configAdditionalEffects = plugin.getPluginConfig().getBoolean("AdditionalEffects", true);
		configSound = plugin.getPluginConfig().getBoolean("Sound", true);

		langPeriodAchievement = plugin.getChatHeader()
				+ plugin.getPluginLang().getString(languageHeaderKey, defaultHeaderMessage);
		langPlayerRank = plugin.getChatHeader() + plugin.getPluginLang().getString("player-rank", "Current rank:") + " "
				+ configColor;
		langNotRanked = plugin.getChatHeader()
				+ plugin.getPluginLang().getString("not-ranked", "You are currently not ranked for this period.");
	}

	@Override
	public void executeCommand(CommandSender sender, String[] args) {
		long currentTime = System.currentTimeMillis();
		int rank = Integer.MAX_VALUE;

		long rankingStartTime = getRankingStartTime();

		if (sender instanceof Player) {
			rank = plugin.getDatabaseManager().getPlayerRank(((Player) sender).getUniqueId(), rankingStartTime);
		}
		// Update top list on given period if too old.
		if (currentTime - lastCommandTime >= VALUES_EXPIRATION_DELAY) {
			playersRanking = plugin.getDatabaseManager().getTopList(configTopList, rankingStartTime);
		}

		sender.sendMessage(langPeriodAchievement);

		for (int i = 0; i < playersRanking.size(); i += 2) {
			String playerName = Bukkit.getServer().getOfflinePlayer(UUID.fromString(playersRanking.get(i))).getName();
			if (playerName != null) {
				// Color the name of the player if he is in the top list.
				String color = "";
				if (sender instanceof Player && playerName.equals(((Player) sender).getName())) {
					color = configColor.toString();
				}
				sender.sendMessage(ChatColor.GRAY + " " + color + getRankingSymbol((i + 2) >> 1) + ' ' + playerName
						+ " - " + playersRanking.get(i + 1));
			} else {
				plugin.getLogger().warning("Ranking command: name corresponding to UUID not found.");
			}
		}
		// Launch effect if player is in top list.
		if (rank <= configTopList) {
			launchEffects((Player) sender);
		}

		// Update number of players for this period if value is too old, and update timestamp.
		if (currentTime - lastCommandTime >= VALUES_EXPIRATION_DELAY) {
			totalPlayersInRanking = plugin.getDatabaseManager().getTotalPlayers(rankingStartTime);
			lastCommandTime = System.currentTimeMillis();
		}

		// If rank > totalPlayersInRanking, player has not yet received an achievement for this period, not ranked.
		if (rank <= totalPlayersInRanking) {
			sender.sendMessage(langPlayerRank + rank + ChatColor.GRAY + "/" + configColor + totalPlayersInRanking);
		} else if (sender instanceof Player) {
			sender.sendMessage(langNotRanked);
		}
	}

	/**
	 * Returns an UTF-8 circled number based on the player's rank.
	 * 
	 * @param rank
	 * @return
	 */
	private String getRankingSymbol(int rank) {
		int decimalRankSymbol;
		if (rank <= 10) {
			decimalRankSymbol = DECIMAL_CIRCLED_ONE + rank - 1;
		} else if (rank <= 20) {
			decimalRankSymbol = DECIMAL_CIRCLED_ELEVEN + rank - 11;
		} else if (rank <= 35) {
			decimalRankSymbol = DECIMAL_CIRCLED_TWENTY_ONE + rank - 21;
		} else {
			decimalRankSymbol = DECIMAL_CIRCLED_THIRTY_SIX + rank - 36;
		}
		return StringEscapeUtils.unescapeJava("\\u" + Integer.toHexString(decimalRankSymbol));
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
			// Play special effect when in top list.
			if (configAdditionalEffects) {
				ParticleEffect.PORTAL.display(0, 1, 0, 0.5f, 1000, player.getLocation(), 1);
			}
		} catch (Exception e) {
			plugin.getLogger().severe("Error while displaying additional particle effects.");
		}

		// Play special sound when in top list.
		if (configSound) {
			playFireworkSound(player);
		}
	}
}
