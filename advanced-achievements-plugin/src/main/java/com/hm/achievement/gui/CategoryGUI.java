package com.hm.achievement.gui;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.hm.achievement.category.Category;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.config.AchievementMap;
import com.hm.achievement.db.AbstractDatabaseManager;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.domain.Achievement;
import com.hm.achievement.domain.Reward;
import com.hm.achievement.lifecycle.Reloadable;
import com.hm.achievement.utils.NumberHelper;
import com.hm.achievement.utils.StringHelper;

/**
 * Represents the main GUI, corresponding to more specific details about the different achievements.
 *
 * @author Pyves
 */
@Singleton
public class CategoryGUI implements Reloadable {

	public static final int ROW_SIZE = 9;

	private static final int MAX_ACHIEVEMENTS_PER_PAGE = 5 * ROW_SIZE;
	private static final long NO_STAT = -1L;
	private static final String NO_SUBCATEGORY = "";
	private static final int PROGRESS_BAR_SIZE = 90;
	private static final DecimalFormat TIME_FORMAT = new DecimalFormat("0");
	static {
		TIME_FORMAT.setMaximumFractionDigits(1);
	}

	private final YamlConfiguration mainConfig;
	private final YamlConfiguration langConfig;
	private final CacheManager cacheManager;
	private final AbstractDatabaseManager databaseManager;
	private final GUIItems guiItems;
	private final AchievementMap achievementMap;

	private boolean configObfuscateNotReceived;
	private boolean configObfuscateProgressiveAchievements;
	private boolean configHideProgressiveAchievements;
	private boolean configHideRewardDisplayInList;
	private boolean configEnrichedProgressBars;
	private boolean configNumberedItemsInList;
	private ChatColor configColor;
	private ChatColor configListColorNotReceived;
	private String configFormatNotReceived;
	private boolean configBackButtonIsCategoryItem;
	private String langListBackMessage;
	private String langListBackLore;
	private String langListGUITitle;
	private String langListAchievementReceived;
	private String langListAchievementNotReceived;
	private String langListDescription;
	private String langListReception;
	private String langListGoal;
	private String langListProgress;
	private String langListReward;
	private String langListRewards;

	@Inject
	public CategoryGUI(@Named("main") YamlConfiguration mainConfig, @Named("lang") YamlConfiguration langConfig,
			CacheManager cacheManager, AbstractDatabaseManager databaseManager, GUIItems guiItems,
			AchievementMap achievementMap) {
		this.mainConfig = mainConfig;
		this.langConfig = langConfig;
		this.cacheManager = cacheManager;
		this.databaseManager = databaseManager;
		this.guiItems = guiItems;
		this.achievementMap = achievementMap;
	}

	@Override
	public void extractConfigurationParameters() {
		configObfuscateNotReceived = mainConfig.getBoolean("ObfuscateNotReceived");
		configObfuscateProgressiveAchievements = mainConfig.getBoolean("ObfuscateProgressiveAchievements");
		configHideProgressiveAchievements = mainConfig.getBoolean("HideProgressiveAchievements");
		configHideRewardDisplayInList = mainConfig.getBoolean("HideRewardDisplayInList");
		configEnrichedProgressBars = mainConfig.getBoolean("EnrichedListProgressBars");
		configNumberedItemsInList = mainConfig.getBoolean("NumberedItemsInList");
		configColor = ChatColor.getByChar(mainConfig.getString("Color"));
		configListColorNotReceived = ChatColor.getByChar(mainConfig.getString("ListColorNotReceived"));
		configFormatNotReceived = mainConfig.getBoolean("ListItaliciseNotReceived") ? "&o" : "";
		configBackButtonIsCategoryItem = mainConfig.getBoolean("BackButtonIsCategoryItem");

		langListBackMessage = translateColorCodes(langConfig.getString("list-back-message"));
		langListBackLore = translateColorCodes(langConfig.getString("list-back-lore"));
		langListGUITitle = translateColorCodes(langConfig.getString("list-gui-title"));
		langListAchievementReceived = StringEscapeUtils.unescapeJava(langConfig.getString("list-achievement-received"));
		langListAchievementNotReceived = StringEscapeUtils
				.unescapeJava(langConfig.getString("list-achievement-not-received")) + configListColorNotReceived;
		String description = langConfig.getString("list-description");
		langListDescription = description.isEmpty() ? "" : translateColorCodes("&7&l" + description);
		String reception = langConfig.getString("list-reception");
		langListReception = reception.isEmpty() ? "" : translateColorCodes("&7&l" + reception);
		String goal = langConfig.getString("list-goal");
		langListGoal = goal.isEmpty() ? "" : translateColorCodes("&7&l" + goal);
		String progress = langConfig.getString("list-progress");
		langListProgress = progress.isEmpty() ? "" : translateColorCodes("&7&l" + progress);
		String reward = langConfig.getString("list-reward");
		langListReward = reward.isEmpty() ? "" : translateColorCodes("&7&l" + reward);
		String rewards = langConfig.getString("list-rewards");
		langListRewards = rewards.isEmpty() ? "" : translateColorCodes("&7&l" + rewards);
	}

	/**
	 * Displays a category GUI, containing all the achievements from a given category.
	 *
	 * @param item
	 * @param player
	 * @param requestedPage
	 */
	public void displayCategoryGUI(ItemStack item, Player player, int requestedPage) {
		for (Entry<OrderedCategory, ItemStack> achievementItem : guiItems.getOrderedAchievementItems().entrySet()) {
			if (achievementItem.getValue().getItemMeta().getDisplayName().equals(item.getItemMeta().getDisplayName())) {
				Category category = achievementItem.getKey().getCategory();
				Map<String, Long> subcategoriesToStatistics;
				List<Achievement> achievements = achievementMap.getForCategory(category);
				if (category instanceof MultipleAchievements) {
					subcategoriesToStatistics = getMultipleStatisticsMapping((MultipleAchievements) category, player);
				} else if (category instanceof NormalAchievements) {
					long statistic = getNormalStatistic((NormalAchievements) category, player);
					subcategoriesToStatistics = Collections.singletonMap(NO_SUBCATEGORY, statistic);
				} else {
					subcategoriesToStatistics = achievements.stream()
							.collect(Collectors.toMap(Achievement::getSubcategory, a -> NO_STAT));
				}
				displayPage(player, subcategoriesToStatistics, requestedPage, item, achievements);
				return;
			}
		}
	}

	/**
	 * Displays a category GUI page, containing up to MAX_PER_PAGE achievements from a given category.
	 *
	 * @param player
	 * @param subcategoriesToStatistics
	 * @param requestedIndex
	 * @param clickedItem
	 * @param achievements
	 */
	private void displayPage(Player player, Map<String, Long> subcategoriesToStatistics, int requestedIndex,
			ItemStack clickedItem, List<Achievement> achievements) {
		int pageIndex = getPageIndex(requestedIndex, achievements.size());
		int pageStart = MAX_ACHIEVEMENTS_PER_PAGE * pageIndex;
		int pageEnd = Math.min(MAX_ACHIEVEMENTS_PER_PAGE * (pageIndex + 1), achievements.size());

		// The inventory must be big enough to contain all page achievements and an entire row for the navigation items.
		int guiSize = Math.min(NumberHelper.nextMultipleOf9(achievements.size()), MAX_ACHIEVEMENTS_PER_PAGE) + ROW_SIZE;
		AchievementInventoryHolder inventoryHolder = new AchievementInventoryHolder(pageIndex, clickedItem);
		Inventory inventory = Bukkit.createInventory(inventoryHolder, guiSize, langListGUITitle);
		inventoryHolder.setInventory(inventory);

		String previousItemDate = null;
		String previousSubcategory = NO_SUBCATEGORY;
		int seriesStart = 0;
		if (pageStart > 0) {
			Achievement previousAchievement = achievements.get(pageStart - 1);
			previousItemDate = databaseManager.getPlayerAchievementDate(player.getUniqueId(), previousAchievement.getName());
			previousSubcategory = previousAchievement.getSubcategory();
			String currentSubcategory = achievements.get(pageStart).getSubcategory();
			if (!currentSubcategory.isEmpty()) {
				seriesStart = IntStream.range(0, achievements.size())
						.filter(i -> achievements.get(i).getSubcategory().equals(currentSubcategory))
						.findFirst()
						.getAsInt();
			}
		}
		// Populate the current GUI page with all of the achievements for the category.
		for (int index = pageStart; index < pageEnd; ++index) {
			// Path can either be a threshold (eg '10', or a subcategory and threshold (eg 'skeleton.10').
			Achievement achievement = achievements.get(index);
			long statistic = subcategoriesToStatistics.get(achievement.getSubcategory());
			String receptionDate = databaseManager.getPlayerAchievementDate(player.getUniqueId(), achievement.getName());

			boolean differentSubcategory = !previousSubcategory.equals(achievement.getSubcategory());
			if (differentSubcategory) {
				seriesStart = index;
			}
			boolean ineligibleSeriesItem = true;
			if (statistic == NO_STAT || receptionDate != null || previousItemDate != null
					|| index == pageStart && pageStart == 0 || differentSubcategory) {
				// Commands achievement OR achievement has been completed OR previous achievement has been completed OR
				// first achievement in the category OR different subcategory.
				ineligibleSeriesItem = false;
			}

			if (configHideProgressiveAchievements && ineligibleSeriesItem) {
				inventory.setItem(index - pageStart, guiItems.getAchievementLock());
			} else {
				List<String> lore = buildLore(achievement, receptionDate, statistic, ineligibleSeriesItem, player);
				insertAchievement(inventory, index - pageStart, statistic, achievement.getDisplayName(), receptionDate,
						ineligibleSeriesItem, index - seriesStart, lore, achievement.getType());
			}

			previousItemDate = receptionDate;
			previousSubcategory = achievement.getSubcategory();
		}

		// Add navigation items.
		if (configBackButtonIsCategoryItem) {
			ItemStack backButton = clickedItem.clone();
			ItemMeta backMeta = backButton.getItemMeta();
			backMeta.setDisplayName(langListBackMessage);
			if (StringUtils.isNotBlank(langListBackLore)) {
				backMeta.setLore(Collections.singletonList(langListBackLore));
			} else {
				backMeta.setLore(Collections.emptyList());
			}
			backButton.setItemMeta(backMeta);
			inventory.setItem(guiSize - (ROW_SIZE + 1) / 2, backButton);
		} else {
			inventory.setItem(guiSize - (ROW_SIZE + 1) / 2, guiItems.getBackButton());
		}
		if (pageIndex > 0) {
			inventory.setItem(guiSize - ROW_SIZE, guiItems.getPreviousButton());
		}
		if (achievements.size() > MAX_ACHIEVEMENTS_PER_PAGE * (pageIndex + 1)) {
			inventory.setItem(guiSize - 1, guiItems.getNextButton());
		}

		// Display page.
		player.openInventory(inventory);
	}

	/**
	 * Creates a GUI item for a given achievement.
	 *
	 * @param gui
	 * @param position
	 * @param statistic
	 * @param name
	 * @param date
	 * @param ineligibleSeriesItem
	 * @param seriesIndex
	 * @param lore
	 * @param type
	 */
	private void insertAchievement(Inventory gui, int position, long statistic, String name, String date,
			boolean ineligibleSeriesItem, int seriesIndex, List<String> lore, String type) {
		// Display an item depending on whether the achievement was received or not, or whether progress was started.
		// Clone in order to work with an independent set of metadata.
		ItemStack achItem;
		if (date != null) {
			achItem = guiItems.getAchievementReceived(type).clone();
		} else if (statistic > 0) {
			achItem = guiItems.getAchievementStarted(type).clone();
		} else {
			achItem = guiItems.getAchievementNotStarted(type).clone();
		}

		String displayName = date == null ? langListAchievementNotReceived + notReceivedStyle(name, ineligibleSeriesItem)
				: langListAchievementReceived + name;
		ItemMeta itemMeta = achItem.getItemMeta();
		itemMeta.setDisplayName(translateColorCodes(displayName));
		itemMeta.setLore(lore);
		achItem.setItemMeta(itemMeta);
		if (configNumberedItemsInList) {
			achItem.setAmount(seriesIndex + 1);
		}
		gui.setItem(position, achItem);
	}

	/**
	 * Gets the player's statistics for each subcategory in the Multiple category.
	 *
	 * @param category
	 * @param player
	 * @return the mapping from subcategory to player's statistics
	 */
	public Map<String, Long> getMultipleStatisticsMapping(MultipleAchievements category, Player player) {
		return achievementMap.getSubcategoriesForCategory(category).stream()
				.collect(Collectors.toMap(Function.identity(), subcategory -> cacheManager
						.getAndIncrementStatisticAmount(category, subcategory, player.getUniqueId(), 0)));
	}

	/**
	 * Gets the player's statistic for a Normal category.
	 *
	 * @param category
	 * @param player
	 * @return the player's statistic for the category
	 */
	public long getNormalStatistic(NormalAchievements category, Player player) {
		return cacheManager.getAndIncrementStatisticAmount(category, player.getUniqueId(), 0);
	}

	/**
	 * Extracts the achievement message/goals that should be shown in the item lore.
	 *
	 * @param achievement
	 * @param completed
	 * @return the description to display in the GUI
	 */
	private List<String> getDescriptionsToDisplay(Achievement achievement, boolean completed) {
		if (!completed) {
			return Arrays.asList(StringUtils.splitByWholeSeparator(achievement.getGoal(), "\\n"));
		}
		return Collections.singletonList(achievement.getMessage());
	}

	/**
	 * Computes the page index to display given a requested page and the total number of achievements in the category.
	 * The returned page index must be within a meaningful range, such that there are achievements to display on the
	 * page.
	 *
	 * @param requestedPage Index of requested page
	 * @param totalAchievements Number of achievements in the category
	 * @return the page index to display (start index is 0)
	 */
	private int getPageIndex(int requestedPage, int totalAchievements) {
		if (requestedPage <= 0) {
			return 0;
		} else if (totalAchievements <= MAX_ACHIEVEMENTS_PER_PAGE * requestedPage) {
			return requestedPage - 1;
		}
		return requestedPage;
	}

	/**
	 * Creates the lore for the current achievement, containing information about the progress, date of reception,
	 * description, rewards.
	 *
	 * @param achievement
	 * @param date
	 * @param statistic
	 * @param ineligibleSeriesItem
	 * @param player
	 * @return the list representing the lore of a category item
	 */
	private List<String> buildLore(Achievement achievement, String date, long statistic, boolean ineligibleSeriesItem,
			Player player) {
		List<String> descriptions = getDescriptionsToDisplay(achievement, date != null);
		List<String> lore = new ArrayList<>();
		lore.add("");

		if (date != null) {
			if (!langListDescription.isEmpty()) {
				lore.add(langListDescription);
			}
			descriptions.forEach(d -> lore.add(translateColorCodes("&r&f" + d)));
			lore.add("");
			if (!langListReception.isEmpty()) {
				lore.add(langListReception);
			}
			lore.add(translateColorCodes("&r&f" + date));
		} else {
			if (!langListGoal.isEmpty()) {
				lore.add(langListGoal);
			}
			descriptions.forEach(d -> lore.add(translateColorCodes(notReceivedStyle(d, ineligibleSeriesItem))));
			// Display progress if not Commands category.
			if (!configObfuscateNotReceived && statistic != NO_STAT) {
				lore.add("");
				boolean timeStat = NormalAchievements.PLAYEDTIME == achievement.getCategory();
				if (!langListProgress.isEmpty()) {
					lore.add(langListProgress);
				}
				lore.add(translateColorCodes(constructProgressBar(achievement.getThreshold(), statistic, timeStat)));
			}
		}

		List<Reward> rewards = achievement.getRewards();
		// Add the rewards information.
		if (!rewards.isEmpty() && !configHideRewardDisplayInList) {
			lore.add("");
			if (rewards.size() == 1 && !langListReward.isEmpty()) {
				lore.add(langListReward);
			} else if (rewards.size() > 1 && !langListRewards.isEmpty()) {
				lore.add(langListRewards);
			}
			String dot = StringEscapeUtils.unescapeJava(
					date == null ? configListColorNotReceived + "\u25CF " + configFormatNotReceived : "&r&f\u25CF ");
			for (Reward reward : rewards) {
				for (String listText : reward.getListTexts()) {
					lore.add(StringHelper.replacePlayerPlaceholders(translateColorCodes(dot + listText), player));
				}
			}
		}
		return lore;
	}

	/**
	 * Constructs the progress bar to be displayed in an achievement's item lore.
	 *
	 * @param threshold
	 * @param statistic
	 * @param time
	 * @return progress bar
	 */
	private String constructProgressBar(long threshold, long statistic, boolean time) {
		double statisticDouble;
		String statisticString;
		if (time) {
			// Convert millis to hours. Math.floor(X * 10) / 10 ensures that the value isn't rounded up when formatting.
			// This would lead to displaying values such as 2/2 even if the player hasn't yet reached the threshold.
			statisticDouble = Math.floor(statistic / 3600000.0 * 10) / 10;
			statisticString = TIME_FORMAT.format(statisticDouble);
		} else {
			statisticDouble = statistic; // Cast to double.
			statisticString = Long.toString(statistic);
		}
		String middleText = " " + configListColorNotReceived + configFormatNotReceived + statisticString + "/" + threshold
				+ " ";

		StringBuilder barDisplay = new StringBuilder().append(configListColorNotReceived).append("[").append(configColor);
		// Approximation: colours chars account for no size, spaces ~2 vertical bars, other chars ~3 vertical bars.
		int middleTextSize = configEnrichedProgressBars ? (middleText.length() - 6) * 3 + 4 : 0;
		boolean hasDisplayedMiddleText = false;
		boolean hasDisplayedNotReceivedColor = false;
		int i = 0;
		while (++i < PROGRESS_BAR_SIZE) {
			if (configEnrichedProgressBars && !hasDisplayedMiddleText && i >= (PROGRESS_BAR_SIZE - middleTextSize) / 2) {
				// Middle reached: append enriched statistic information.
				barDisplay.append(middleText);
				if (!hasDisplayedNotReceivedColor) {
					barDisplay.append(configColor);
				}
				// Do not display middleText again.
				hasDisplayedMiddleText = true;
				hasDisplayedNotReceivedColor = false;
				// Iterate a number of times equal to the number of iterations so far to have the same number of
				// vertical bars left and right from the middle text.
				i = PROGRESS_BAR_SIZE - i;
			} else {
				if (i >= PROGRESS_BAR_SIZE * statisticDouble / threshold && !hasDisplayedNotReceivedColor) {
					// Not received color: amount not yet reached by user.
					hasDisplayedNotReceivedColor = true;
					barDisplay.append(configListColorNotReceived);
				}
				barDisplay.append("|");
			}
		}
		return barDisplay.append(configListColorNotReceived).append("]").toString();
	}

	/**
	 * Randomises the contents of a string; preserves spaces.
	 * 
	 * @param text
	 * @return a string with randomised alphabetic characters
	 */
	private String randomiseParts(String text) {
		if (text.isEmpty()) {
			return "";
		}
		StringBuilder randomisedText = new StringBuilder();
		for (String part : StringUtils.split(text)) {
			randomisedText.append(RandomStringUtils.randomAlphabetic(part.length())).append(' ');
		}
		return randomisedText.substring(0, randomisedText.length() - 1);
	}

	private String notReceivedStyle(String input, boolean ineligibleSeriesItem) {
		if (configObfuscateNotReceived || (configObfuscateProgressiveAchievements && ineligibleSeriesItem)) {
			return configListColorNotReceived + "&k" + randomiseParts(StringHelper.removeFormattingCodes(input));
		} else {
			return configListColorNotReceived + configFormatNotReceived + StringHelper.removeFormattingCodes(input);
		}
	}

	private String translateColorCodes(String translate) {
		return ChatColor.translateAlternateColorCodes('&', translate);
	}

}
