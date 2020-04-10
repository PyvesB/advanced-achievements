package com.hm.achievement.command.executable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.text.TextStringBuilder;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.command.pagination.SupplierCommandPagination;
import com.hm.achievement.db.AbstractDatabaseManager;
import com.hm.achievement.db.data.AwardedDBAchievement;
import com.hm.achievement.lang.LangHelper;
import com.hm.achievement.lang.command.CmdLang;
import com.hm.achievement.utils.StringHelper;
import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Class in charge of displaying recipients of an achievement (/aach inspect).
 *
 * @author Rsl1122
 */
@Singleton
@CommandSpec(name = "inspect", permission = "inspect", minArgs = 1, maxArgs = Integer.MAX_VALUE)
public class InspectCommand extends AbstractCommand {

	private static final int CACHE_EXPIRATION_DELAY = 60000;
	private static final int PER_PAGE = 16;

	private final AdvancedAchievements advancedAchievements;
	private final AbstractDatabaseManager databaseManager;
	private final Map<String, String> displayNamesToNames;

	private final Map<String, Long> lastCached;
	private final Map<String, SupplierCommandPagination> cachedPaginations;

	@Inject
	public InspectCommand(@Named("main") CommentedYamlConfiguration mainConfig,
			@Named("lang") CommentedYamlConfiguration langConfig, StringBuilder pluginHeader,
			AdvancedAchievements advancedAchievements, AbstractDatabaseManager databaseManager,
			@Named("dtn") Map<String, String> displayNamesToNames) {
		super(mainConfig, langConfig, pluginHeader);
		this.advancedAchievements = advancedAchievements;
		this.databaseManager = databaseManager;
		this.displayNamesToNames = displayNamesToNames;

		this.lastCached = new HashMap<>();
		this.cachedPaginations = new HashMap<>();
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();
	}

	private int getPage(String[] args) {
		boolean lastArgIsNumber = args.length > 1 && NumberUtils.isDigits(args[args.length - 1]);
		return lastArgIsNumber ? Integer.parseInt(args[args.length - 1]) : 1;
	}

	@Override
	void onExecute(CommandSender sender, String[] args) {
		// Argument Parsing
		String achievementDisplayName = parseAchievementName(args).toLowerCase();
		String achievementName = getAchievementName(achievementDisplayName);
		if (achievementName == null) {
			sender.sendMessage(pluginHeader + LangHelper.getEachReplaced(CmdLang.ACHIEVEMENT_NOT_RECOGNIZED, langConfig,
					new String[] { "NAME", "CLOSEST_MATCH" }, new String[] { achievementDisplayName, StringHelper
							.getClosestMatch(achievementDisplayName, displayNamesToNames.keySet()) }));
			return;
		}
		int page = getPage(args);

		advancedAchievements.getServer().getScheduler().runTaskAsynchronously(advancedAchievements, () -> {
			// Cleaning the cache & caching desired pagination
			cleanUpCache();
			checkAndCache(achievementName);

			// Send pagination
			SupplierCommandPagination pagination = cachedPaginations.get(achievementName);
			pagination.sendPage(page, sender);
		});
	}

	private String parseAchievementName(String[] args) {
		TextStringBuilder achName = new TextStringBuilder();

		boolean lastArgumentIsNumber = args.length > 1 && NumberUtils.isDigits(args[args.length - 1]);
		if (lastArgumentIsNumber) {
			List<String> endsRemoved = new ArrayList<>(Arrays.asList(args).subList(1, args.length - 1));
			achName.appendWithSeparators(endsRemoved, " ");
		} else {
			List<String> firstArgRemoved = new ArrayList<>(Arrays.asList(args).subList(1, args.length));
			achName.appendWithSeparators(firstArgRemoved, " ");
		}

		return achName.toString();
	}

	private String getAchievementName(String achievementDisplayName) {
		String achievementName = displayNamesToNames.get(achievementDisplayName);
		if (achievementName == null) {
			String whiteSpaceRemovedName = achievementDisplayName.replace(' ', '_');
			if (displayNamesToNames.containsValue(whiteSpaceRemovedName)) {
				achievementName = whiteSpaceRemovedName;
			}
		}
		return achievementName;
	}

	private void cleanUpCache() {
		long time = System.currentTimeMillis();
		Set<String> toRemove = new HashSet<>();
		for (Map.Entry<String, Long> entry : lastCached.entrySet()) {
			if (time - CACHE_EXPIRATION_DELAY > entry.getValue()) {
				toRemove.add(entry.getKey());
			}
		}
		for (String achievementName : toRemove) {
			cachedPaginations.remove(achievementName);
			lastCached.remove(achievementName);
		}

	}

	private void checkAndCache(String achievementName) {
		if (System.currentTimeMillis() - CACHE_EXPIRATION_DELAY > lastCached.getOrDefault(achievementName, 0L)) {
			List<AwardedDBAchievement> recipientList = databaseManager.getAchievementsRecipientList(achievementName);
			// Use Suppliers to avoid huge work on getting UUID - name relations.
			List<Supplier<String>> messages = recipientList.stream()
					.map(achievement -> (Supplier<String>) () -> {
						UUID uuid = achievement.getAwardedTo();
						OfflinePlayer player = advancedAchievements.getServer().getOfflinePlayer(uuid);
						String identifier = player.hasPlayedBefore() ? player.getName() : uuid.toString();
						return "  " + identifier + " (" + achievement.getFormattedDate() + ")";
					}).collect(Collectors.toList());

			SupplierCommandPagination pagination = new SupplierCommandPagination(messages, PER_PAGE, langConfig);
			cachedPaginations.put(achievementName, pagination);
			lastCached.put(achievementName, System.currentTimeMillis());
		}
	}
}
