package com.hm.achievement.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.map.MinecraftFont;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;

/**
 * Represents the main GUI, corresponding to more specific details about the different achievements.
 * 
 * @author Pyves
 */
public class CategoryGUI extends AbstractGUI {

	private static final int MAX_PER_PAGE = 50;
	private static final long NO_STAT = -1L;
	private static final String NO_SUBCATEGORY = "";
	// Pattern to delete colors if achievement not yet received.
	private static final Pattern REGEX_PATTERN = Pattern.compile("&([a-f]|[0-9]){1}");
	// Minecraft font, used to get size information in the progress bar.
	private static final MinecraftFont FONT = MinecraftFont.Font;

	private boolean configObfuscateNotReceived;
	private boolean configObfuscateProgressiveAchievements;
	private boolean configHideRewardDisplayInList;
	private boolean configEnrichedProgressBars;
	private boolean configNumberedItemsInList;
	private ChatColor configColor;
	private String langListGUITitle;
	private String langListAchievementReceived;
	private String langListAchievementNotReceived;
	private String langListDescription;
	private String langListReception;
	private String langListGoal;
	private String langListProgress;
	private String langListReward;

	// Various item stacks displayed in the GUI.
	private ItemStack previousButton;
	private ItemStack nextButton;
	private ItemStack backButton;
	private ItemStack achievementNotStarted;
	private ItemStack achievementStarted;
	private ItemStack achievementReceived;

	public CategoryGUI(AdvancedAchievements plugin) {
		super(plugin);
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		configObfuscateNotReceived = plugin.getPluginConfig().getBoolean("ObfuscateNotReceived", true);
		configObfuscateProgressiveAchievements = plugin.getPluginConfig().getBoolean("ObfuscateProgressiveAchievements",
				false);
		configHideRewardDisplayInList = plugin.getPluginConfig().getBoolean("HideRewardDisplayInList", false);
		configEnrichedProgressBars = plugin.getPluginConfig().getBoolean("EnrichedListProgressBars", true);
		configNumberedItemsInList = plugin.getPluginConfig().getBoolean("NumberedItemsInList", false);
		configColor = ChatColor.getByChar(plugin.getPluginConfig().getString("Color", "5").charAt(0));

		langListGUITitle = ChatColor.translateAlternateColorCodes('&',
				plugin.getPluginLang().getString("list-gui-title", "&5&lAchievements List"));
		langListAchievementReceived = StringEscapeUtils
				.unescapeJava(plugin.getPluginLang().getString("list-achievement-received", "&a\u2714&f "));
		langListAchievementNotReceived = StringEscapeUtils
				.unescapeJava(plugin.getPluginLang().getString("list-achievement-not-received", "&4\u2718&8 "));
		langListDescription = ChatColor.translateAlternateColorCodes('&',
				"&7&l" + plugin.getPluginLang().getString("list-description", "Description:"));
		langListReception = ChatColor.translateAlternateColorCodes('&',
				"&7&l" + plugin.getPluginLang().getString("list-reception", "Reception date:"));
		langListGoal = ChatColor.translateAlternateColorCodes('&',
				"&7&l" + plugin.getPluginLang().getString("list-goal", "Goal:"));
		langListProgress = ChatColor.translateAlternateColorCodes('&',
				"&7&l" + plugin.getPluginLang().getString("list-progress", "Progress:"));
		langListReward = ChatColor.translateAlternateColorCodes('&',
				"&7&l" + plugin.getPluginLang().getString("list-reward", "Reward(s):"));

		achievementNotStarted = createItemStack("AchievementNotStarted");
		achievementStarted = createItemStack("AchievementStarted");
		achievementReceived = createItemStack("AchievementReceived");
		previousButton = createItemStack("PreviousButton");
		ItemMeta previousMeta = previousButton.getItemMeta();
		previousMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', StringEscapeUtils
				.unescapeJava(plugin.getPluginLang().getString("list-previous-message", "&7Previous"))));
		previousButton.setItemMeta(previousMeta);
		nextButton = createItemStack("NextButton");
		ItemMeta nextMeta = nextButton.getItemMeta();
		nextMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
				StringEscapeUtils.unescapeJava(plugin.getPluginLang().getString("list-next-message", "&7Next"))));
		nextButton.setItemMeta(nextMeta);
		backButton = createItemStack("BackButton");
		ItemMeta backMeta = backButton.getItemMeta();
		backMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
				StringEscapeUtils.unescapeJava(plugin.getPluginLang().getString("list-back-message", "&7Back"))));
		backButton.setItemMeta(backMeta);
	}

	/**
	 * Displays a category GUI, containing all the achievements from a given category.
	 * 
	 * @param item
	 * @param player
	 * @param requestedPage
	 */
	public void displayCategoryGUI(ItemStack item, Player player, int requestedPage) {
		for (Entry<MultipleAchievements, ItemStack> entry : multipleAchievementItems.entrySet()) {
			if (entry.getValue().getType() == item.getType()
					&& entry.getValue().getDurability() == item.getDurability()) {
				String categoryName = entry.getKey().toString();
				List<String> achievementPaths = getSortedMultipleAchievementPaths(categoryName);
				Map<String, Long> subcategoriesToStatistics = getMultipleStatisticsMapping(entry.getKey(), player);
				displayPage(categoryName, player, subcategoriesToStatistics, requestedPage, item, achievementPaths);
				return;
			}
		}
		for (Entry<NormalAchievements, ItemStack> entry : normalAchievementItems.entrySet()) {
			if (entry.getValue().getType() == item.getType()
					&& entry.getValue().getDurability() == item.getDurability()) {
				String categoryName = entry.getKey().toString();
				List<String> achievementThresholds = getSortedNormalAchievementThresholds(categoryName);
				long statistic = getNormalStatistic(entry.getKey(), player);
				displayPage(categoryName, player, Collections.singletonMap(NO_SUBCATEGORY, statistic), requestedPage,
						item, achievementThresholds);
				return;
			}
		}
		List<String> achievementPaths = new ArrayList<>(
				plugin.getPluginConfig().getConfigurationSection("Commands").getKeys(false));
		displayPage("Commands", player, Collections.singletonMap(NO_SUBCATEGORY, NO_STAT), requestedPage, item,
				achievementPaths);
	}

	/**
	 * Displays a category GUI page, containing up to MAX_PER_PAGE achievements from a given category.
	 * 
	 * @param categoryName
	 * @param player
	 * @param subcategoriesToStatistics
	 * @param requestedPage
	 * @param clickedItem
	 * @param achievementPaths
	 */
	private void displayPage(String categoryName, Player player, Map<String, Long> subcategoriesToStatistics,
			int requestedPage, ItemStack clickedItem, List<String> achievementPaths) {
		int pageToDisplay = getPageToDisplay(requestedPage, achievementPaths.size());
		int pageStart = MAX_PER_PAGE * (pageToDisplay - 1);
		int pageEnd = Math.min(MAX_PER_PAGE * pageToDisplay, achievementPaths.size());

		// Create a new chest-like inventory as small as possible whilst still containing all page achievements and
		// navigation items.
		int guiSize = nextMultipleOf9(achievementPaths.size() + 4, MAX_PER_PAGE);
		Inventory inventory = Bukkit.createInventory(null, guiSize, langListGUITitle + " " + pageToDisplay);
		// Persist clicked item (ie. category's item in the main GUI) as first item in the category GUI.
		inventory.setItem(0, clickedItem);

		String previousItemDate = null;
		String previousSubcategory = NO_SUBCATEGORY;
		if (pageStart > 0) {
			String previousAchievement = achievementPaths.get(pageStart - 1);
			String achName = plugin.getPluginConfig().getString(categoryName + '.' + previousAchievement + ".Name", "");
			previousItemDate = plugin.getDatabaseManager().getPlayerAchievementDate(player.getUniqueId(), achName);
			if (previousAchievement.contains(".")) {
				previousSubcategory = previousAchievement.split("\\.")[0];
			}
		}
		// Populate the current GUI page with all of the achievements for the category.
		for (int index = pageStart; index < pageEnd; ++index) {
			// Path can either be a threshold (eg '10', or a subcategory and threshold (eg 'skeleton.10').
			String path = achievementPaths.get(index);
			String subcategory = path.contains(".") ? path.split("\\.")[0] : NO_SUBCATEGORY;
			long statistic = subcategoriesToStatistics.get(subcategory);
			String achName = plugin.getPluginConfig().getString(categoryName + '.' + path + ".Name", "");
			String receptionDate = plugin.getDatabaseManager().getPlayerAchievementDate(player.getUniqueId(), achName);

			boolean inelligibleSeriesItem = true;
			if (statistic == NO_STAT || receptionDate != null || previousItemDate != null
					|| index == pageStart && pageStart == 0 || !previousSubcategory.equals(subcategory)) {
				// Commands achievement OR achievement has been completed OR previous achievement has been completed OR
				// first achievement in the category OR different subcategory.
				inelligibleSeriesItem = false;
			}

			String nameToDisplay = getNameToDisplay(categoryName, path, achName);
			String descriptionToDisplay = getDescriptionToDisplay(categoryName, path, receptionDate != null);
			List<String> lore = buildLore(categoryName, descriptionToDisplay, path, receptionDate, statistic,
					inelligibleSeriesItem);
			insertAchievement(inventory, index - pageStart + 1, statistic, nameToDisplay, receptionDate,
					inelligibleSeriesItem, lore);

			previousItemDate = receptionDate;
			previousSubcategory = subcategory;
		}
		// Add navigation items.
		inventory.setItem(pageEnd - pageStart + 1, previousButton);
		inventory.setItem(pageEnd - pageStart + 2, nextButton);
		inventory.setItem(pageEnd - pageStart + 3, backButton);

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
	 * @param inelligibleSeriesItem
	 * @param lore
	 */
	private void insertAchievement(Inventory gui, int position, long statistic, String name, String date,
			boolean inelligibleSeriesItem, List<String> lore) {
		// Display an item depending on whether the achievement was received or not, or whether progress was started.
		// Clone in order to work with an independent set of metadata.
		ItemStack achItem;
		if (date != null) {
			achItem = achievementReceived.clone();
		} else if (statistic > 0) {
			achItem = achievementStarted.clone();
		} else {
			achItem = achievementNotStarted.clone();
		}

		// Set name of the achievement. The style depends whether it was received or not and whether the user has set
		// obfuscateNotReceived and/or obfuscateProgressiveAchievements in the config.
		ItemMeta itemMeta = achItem.getItemMeta();
		if (date != null) {
			itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', langListAchievementReceived + name));
		} else if (configObfuscateNotReceived || (configObfuscateProgressiveAchievements && inelligibleSeriesItem)) {
			itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
					langListAchievementNotReceived + "&k" + REGEX_PATTERN.matcher(name).replaceAll("")));
		} else {
			itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', StringEscapeUtils
					.unescapeJava(langListAchievementNotReceived + "&o" + REGEX_PATTERN.matcher(name).replaceAll(""))));
		}

		itemMeta.setLore(lore);
		achItem.setItemMeta(itemMeta);
		if (configNumberedItemsInList) {
			achItem.setAmount(position);
		}
		gui.setItem(position, achItem);
	}

	/**
	 * Gets a sorted list of paths (subcategory + threshold) for a Multiple category.
	 * 
	 * @param categoryName
	 * @return the list of paths for the Multiple category
	 */
	public List<String> getSortedMultipleAchievementPaths(String categoryName) {
		List<String> paths = new ArrayList<>();
		// Populate the achievements from all the sub-categories in the category.
		for (String subcategory : plugin.getPluginConfig().getConfigurationSection(categoryName).getKeys(false)) {
			List<String> subcategoryAchievements = new ArrayList<>();
			for (long threshold : plugin.getSortedThresholds().get(categoryName + "." + subcategory)) {
				subcategoryAchievements.add(subcategory + "." + threshold);
			}
			paths.addAll(subcategoryAchievements);
		}
		return paths;
	}

	/**
	 * Gets a sorted list of thresholds for a Normal category.
	 * 
	 * @param categoryName
	 * @return the list of paths for the Normal category
	 */
	public List<String> getSortedNormalAchievementThresholds(String categoryName) {
		return plugin.getSortedThresholds().get(categoryName).stream().map(i -> Long.toString(i))
				.collect(Collectors.toList());
	}

	/**
	 * Gets the player's statistics for each subcategory in the Multiple category.
	 * 
	 * @param category
	 * @param player
	 * @return the mapping from subcategory to player's statistics
	 */
	public Map<String, Long> getMultipleStatisticsMapping(MultipleAchievements category, Player player) {
		Map<String, Long> subcategoriesToStatistics = new HashMap<>();
		for (String subcategory : plugin.getPluginConfig().getConfigurationSection(category.toString())
				.getKeys(false)) {
			long statistic = plugin.getCacheManager().getAndIncrementStatisticAmount(category, subcategory,
					player.getUniqueId(), 0);
			subcategoriesToStatistics.put(subcategory, statistic);
		}
		return subcategoriesToStatistics;
	}

	/**
	 * Gets the player's statistic for a Normal category.
	 * 
	 * @param category
	 * @param player
	 * @return the player's statistic for the category
	 */
	public long getNormalStatistic(NormalAchievements category, Player player) {
		if (category == NormalAchievements.CONNECTIONS) {
			return plugin.getDatabaseManager().getConnectionsAmount(player.getUniqueId());
		}
		return plugin.getCacheManager().getAndIncrementStatisticAmount(category, player.getUniqueId(), 0);
	}

	/**
	 * Extracts the achievement name that should be shown in the item lore.
	 * 
	 * @param category
	 * @param path
	 * @param achName
	 * @return the name to display in the GUI
	 */
	private String getNameToDisplay(String category, String path, String achName) {
		String displayName = plugin.getPluginConfig().getString(category + '.' + path + ".DisplayName", "");
		if (StringUtils.isNotBlank(displayName)) {
			// Display name is defined; use it.
			return displayName;
		}
		return achName;
	}

	/**
	 * Extracts the achievement message that should be shown in the item lore.
	 * 
	 * @param category
	 * @param path
	 * @param completed
	 * @return the description to display in the GUI
	 */
	private String getDescriptionToDisplay(String category, String path, boolean completed) {
		String goal = plugin.getPluginConfig().getString(category + '.' + path + ".Goal", "");
		if (StringUtils.isNotBlank(goal) && !completed) {
			// Show the goal below the achievement name.
			return goal;
		}
		// Show the achievement message below the achievement name.
		return plugin.getPluginConfig().getString(category + '.' + path + ".Message", "");
	}

	/**
	 * Computes the page number to display given a requested page anf the total number of achievements in the category.
	 * The returned page number must be within a meaningful range, such that there are achievements to display on the
	 * page.
	 * 
	 * @param requestedPage
	 * @param totalAchievements
	 * @return the page number to display (start index is 1)
	 */
	private int getPageToDisplay(int requestedPage, int totalAchievements) {
		if (totalAchievements <= MAX_PER_PAGE * (requestedPage - 1)) {
			return requestedPage - 1;
		} else if (requestedPage < 1) {
			return 1;
		}
		return requestedPage;
	}

	/**
	 * Creates the lore for the current achievement, containing information about the progress, date of reception,
	 * description, rewards.
	 * 
	 * @param categoryName
	 * @param description
	 * @param path
	 * @param date
	 * @param statistic
	 * @param inelligibleSeriesItem
	 * @return the list representing the lore of a category item
	 */
	private List<String> buildLore(String categoryName, String description, String path, String date, long statistic,
			boolean inelligibleSeriesItem) {
		List<String> lore = new ArrayList<>();
		lore.add("");

		if (date != null) {
			lore.add(langListDescription);
			lore.add(ChatColor.translateAlternateColorCodes('&', "&r" + description));
			lore.add("");
			lore.add(langListReception);
			lore.add(ChatColor.translateAlternateColorCodes('&', "&r" + date));
			lore.add("");
		} else {
			lore.add(langListGoal);
			String strippedAchMessage = REGEX_PATTERN.matcher(description).replaceAll("");
			if (configObfuscateNotReceived || (configObfuscateProgressiveAchievements && inelligibleSeriesItem)) {
				lore.add(ChatColor.translateAlternateColorCodes('&', "&8&k" + strippedAchMessage));
			} else {
				lore.add(ChatColor.translateAlternateColorCodes('&', "&8&o" + strippedAchMessage));
			}
			lore.add("");
			// Display progress if not COmmands category.
			if (!configObfuscateNotReceived && statistic != NO_STAT) {
				String threshold = path.contains(".") ? path.split("\\.")[1] : path;
				boolean timeStat = NormalAchievements.PLAYEDTIME.toString().equals(categoryName);
				lore.add(langListProgress);
				lore.add(ChatColor.translateAlternateColorCodes('&',
						constructProgressBar(strippedAchMessage, threshold, statistic, timeStat)));
				lore.add("");
			}
		}

		List<String> rewards = plugin.getRewardParser().getRewardListing(categoryName + '.' + path);
		// Add the rewards information.
		if (!rewards.isEmpty() && !configHideRewardDisplayInList) {
			lore.add(langListReward);
			String dot;
			if (date != null) {
				dot = StringEscapeUtils.unescapeJava("&r\u25CF ");
			} else {
				dot = StringEscapeUtils.unescapeJava("&8\u25CF &o");
			}
			for (String reward : rewards) {
				lore.add(ChatColor.translateAlternateColorCodes('&', dot + reward));
			}
		}
		return lore;
	}

	/**
	 * Constructs the progress bar to be displayed in an achievement's item lore.
	 * 
	 * @param achMessage
	 * @param level
	 * @param statistic
	 * @param time
	 * @return progress bar
	 */
	private String constructProgressBar(String achMessage, String level, long statistic, boolean time) {
		StringBuilder barDisplay = new StringBuilder("&8[");
		// Length of the progress bar; we make it the same size as Goal/Message.
		int textSize;
		// MinecraftFont essentially supports latin alphabet characters. If invalid characters are found just use
		// number of chars.
		if (FONT.isValid(achMessage)) {
			textSize = FONT.getWidth(REGEX_PATTERN.matcher(achMessage).replaceAll(""));
		} else {
			textSize = achMessage.length() * 3;
		}

		long levelInt = Long.parseLong(level);
		String middleText;
		double statisticDouble;
		if (time) {
			// Convert from millis to hours.
			statisticDouble = statistic / 3600000.0;
			// Display one floating digit in the progress bar.
			middleText = " " + String.format("%.1f", statisticDouble) + "/" + levelInt + " ";
		} else {
			middleText = " " + statistic + "/" + levelInt + " ";
			// Cast to double.
			statisticDouble = statistic;
		}
		int middleTextSize = FONT.getWidth(middleText);
		middleText = "&8&o" + middleText;

		boolean hasDisplayedMiddleText = false;
		int i = 1;
		while (i < textSize / 2) {
			if (configEnrichedProgressBars && !hasDisplayedMiddleText && i >= (textSize - middleTextSize) / 4) {
				// Middle reached: append enriched statistic information.
				barDisplay.append(middleText);
				// Do not display middleText again.
				hasDisplayedMiddleText = true;
				// Iterate a number of times equal to the number of iterations so far to have the same number of
				// vertical bars left and right from the middle text.
				i = textSize / 2 + 1 - i;
			} else if (i < (((double) textSize / 2 - 1) * statisticDouble) / levelInt) {
				// Color: progress by user.
				barDisplay.append(configColor).append('|');
				i++;
			} else {
				// Grey: amount not yet reached by user.
				barDisplay.append("&8|");
				i++;
			}
		}
		return barDisplay.append("&8]").toString();
	}

	public ItemStack getPreviousButton() {
		return previousButton;
	}

	public ItemStack getNextButton() {
		return nextButton;
	}

	public ItemStack getBackButton() {
		return backButton;
	}
}
