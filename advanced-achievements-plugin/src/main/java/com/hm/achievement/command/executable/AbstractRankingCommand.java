package com.hm.achievement.command.executable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Logger;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.hm.achievement.command.pagination.CommandPagination;
import com.hm.achievement.db.AbstractDatabaseManager;
import com.hm.achievement.lang.Lang;
import com.hm.achievement.lang.LangHelper;
import com.hm.achievement.lang.command.CmdLang;
import com.hm.achievement.utils.SoundPlayer;
import com.hm.mcshared.file.CommentedYamlConfiguration;
import com.hm.mcshared.particle.ParticleEffect;

/**
 * Abstract class in charge of factoring out common functionality for /aach top, week and month commands.
 *
 * @author Pyves
 */
public abstract class AbstractRankingCommand extends AbstractCommand {

	private static final int CACHE_EXPIRATION_DELAY = 60000;
	private static final int DECIMAL_CIRCLED_ONE = Integer.parseInt("2780", 16);
	private static final int DECIMAL_CIRCLED_ELEVEN = Integer.parseInt("246A", 16);
	private static final int DECIMAL_CIRCLED_TWENTY_ONE = Integer.parseInt("3251", 16);
	private static final int DECIMAL_CIRCLED_THIRTY_SIX = Integer.parseInt("32B1", 16);
	// 16 per page since two other messages are sent.
	private static final int PER_PAGE = 16;

	private final Logger logger;
	private final int serverVersion;
	private final Lang languageHeader;
	private final AbstractDatabaseManager databaseManager;
	private final SoundPlayer soundPlayer;

	private ChatColor configColor;
	private int configTopList;
	private boolean configAdditionalEffects;
	private boolean configSound;
	private String configSoundRanking;
	private String langPeriodAchievement;
	private String langPlayerRank;
	private String langNotRanked;
	// Used for caching.
	private Map<String, Integer> cachedSortedRankings;
	private List<Integer> cachedAchievementCounts;
	private long lastCacheUpdate = 0L;

	AbstractRankingCommand(CommentedYamlConfiguration mainConfig, CommentedYamlConfiguration langConfig,
			StringBuilder pluginHeader, Logger logger, int serverVersion, Lang languageHeader,
			AbstractDatabaseManager databaseManager, SoundPlayer soundPlayer) {
		super(mainConfig, langConfig, pluginHeader);
		this.logger = logger;
		this.serverVersion = serverVersion;
		this.languageHeader = languageHeader;
		this.databaseManager = databaseManager;
		this.soundPlayer = soundPlayer;
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		configColor = ChatColor.getByChar(mainConfig.getString("Color", "5"));
		configTopList = mainConfig.getInt("TopList", 5);
		configAdditionalEffects = mainConfig.getBoolean("AdditionalEffects", true);
		configSound = mainConfig.getBoolean("Sound", true);
		configSoundRanking = mainConfig.getString("SoundRanking", "ENTITY_FIREWORK_ROCKET_BLAST").toUpperCase();

		langPeriodAchievement = pluginHeader + LangHelper.get(languageHeader, langConfig);
		langPlayerRank = pluginHeader + LangHelper.get(CmdLang.PLAYER_RANK, langConfig) + " " + configColor;
		langNotRanked = pluginHeader + LangHelper.get(CmdLang.NOT_RANKED, langConfig);
	}

	@Override
	public void onExecute(CommandSender sender, String[] args) {
		if (System.currentTimeMillis() - lastCacheUpdate >= CACHE_EXPIRATION_DELAY) {
			// Update cached data structures.
			cachedSortedRankings = databaseManager.getTopList(getRankingStartTime());
			cachedAchievementCounts = new ArrayList<>(cachedSortedRankings.values());
			lastCacheUpdate = System.currentTimeMillis();
		}

		sender.sendMessage(langPeriodAchievement);

		List<String> rankingMessages = getRankingMessages(sender);

		// If config has top set at less than one page, don't use pagination.
		if (configTopList < PER_PAGE) {
			rankingMessages.forEach(sender::sendMessage);
		} else {
			int page = getPage(args);
			CommandPagination pagination = new CommandPagination(rankingMessages, PER_PAGE, langConfig);
			pagination.sendPage(page, sender);
		}

		if (sender instanceof Player) {
			Integer achievementsCount = cachedSortedRankings.get(((Player) sender).getUniqueId().toString());
			// If not entry in the map, player has not yet received an achievement for this period, not ranked.
			if (achievementsCount == null) {
				sender.sendMessage(langNotRanked);
			} else {
				// Rank is the first index in the list that has received as many achievements as the player.
				int playerRank = cachedAchievementCounts.indexOf(achievementsCount) + 1;
				// Launch effect if player is in top list.
				if (playerRank <= configTopList) {
					launchEffects((Player) sender);
				}
				sender.sendMessage(
						langPlayerRank + playerRank + ChatColor.GRAY + "/" + configColor + cachedSortedRankings.size());
			}
		}
	}

	private int getPage(String[] args) {
		return args.length > 1 && NumberUtils.isDigits(args[1]) ? Integer.parseInt(args[1]) : 1;
	}

	private List<String> getRankingMessages(CommandSender sender) {
		List<String> rankingMessages = new ArrayList<>();
		int currentRank = 1;
		for (Entry<String, Integer> ranking : cachedSortedRankings.entrySet()) {
			String playerName = Bukkit.getOfflinePlayer(UUID.fromString(ranking.getKey())).getName();
			if (playerName != null) {
				// Color the name of the player if he is in the top list.
				ChatColor color = playerName.equals(sender.getName()) ? configColor : ChatColor.GRAY;
				rankingMessages.add(color + " " + getRankingSymbol(currentRank) + " " + playerName + " - "
						+ ranking.getValue());
			} else {
				logger.warning("Ranking command: could not find player's name using a database UUID.");
			}

			++currentRank;
			if (currentRank > configTopList) {
				break;
			}
		}
		return rankingMessages;
	}

	/**
	 * Returns an UTF-8 circled number based on the player's rank.
	 *
	 * @param rank
	 * @return an UTF-8 string corresponding to the rank
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
	abstract long getRankingStartTime();

	/**
	 * Launches sound and particle effects if player is in a top list.
	 *
	 * @param player
	 */
	private void launchEffects(Player player) {
		if (configAdditionalEffects) {
			if (serverVersion >= 13) {
				player.spawnParticle(Particle.PORTAL, player.getLocation(), 100, 0, 1, 0, 0.5f);
			} else {
				try {
					ParticleEffect.PORTAL.display(0, 1, 0, 0.5f, 1000, player.getLocation(), 1);
				} catch (Exception e) {
					logger.warning("Failed to display additional particle effects for rankings.");
				}
			}
		}

		if (configSound) {
			soundPlayer.play(player, configSoundRanking, "ENTITY_FIREWORK_ROCKET_BLAST", "ENTITY_FIREWORK_LARGE_BLAST",
					"FIREWORK_BLAST");
		}
	}
}
