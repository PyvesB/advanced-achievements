package com.hm.achievement.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.map.MinecraftFont;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Ints;
import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.utils.AchievementCommentedYamlConfiguration;

/**
 * Class in charge of handling the /aach list command, which displays interactive GUIs. The command displays the main
 * GUI, corresponding to all the different available categories and their names. Users will then be able to click on an
 * unlocked category to display another GUI with more specific details about the different achievements.
 * 
 * @author Pyves
 */
public class ListCommand extends AbstractCommand {

	// Pattern to delete colors if achievement not yet received.
	private static final Pattern REGEX_PATTERN = Pattern.compile("&([a-f]|[0-9]){1}");

	// Minecraft font, used to get size information in the progress bar.
	private static final MinecraftFont FONT = MinecraftFont.Font;

	private final boolean hideNotReceivedCategories;
	private final boolean obfuscateNotReceived;
	private final boolean obfuscateProgressiveAchievements;
	private final boolean hideRewardDisplay;
	private final boolean enrichedProgressBars;
	private final boolean numberedItemsInList;

	// Array of item stacks for items displayed in the GUI.
	private final ItemStack[] multipleAchievementCategoryItems;
	private final ItemStack[] normalAchievementCategoryItems;

	private final ItemStack backButton = new ItemStack(Material.PAPER);

	public ListCommand(AdvancedAchievements plugin) {

		super(plugin);
		// Load configuration parameters.
		hideNotReceivedCategories = plugin.getPluginConfig().getBoolean("HideNotReceivedCategories", false);
		obfuscateNotReceived = plugin.getPluginConfig().getBoolean("ObfuscateNotReceived", true);
		obfuscateProgressiveAchievements = plugin.getPluginConfig().getBoolean("ObfuscateProgressiveAchievements",
				false);
		hideRewardDisplay = plugin.getPluginConfig().getBoolean("HideRewardDisplayInList", false);
		enrichedProgressBars = plugin.getPluginConfig().getBoolean("EnrichedListProgressBars", true);
		numberedItemsInList = plugin.getPluginConfig().getBoolean("NumberedItemsInList", false);

		// Get list of item stacks for items displayed in the GUI, for multiple achievements.
		multipleAchievementCategoryItems = new ItemStack[] { new ItemStack(Material.STONE, 1, (short) 6),
				new ItemStack(Material.SMOOTH_BRICK, 1, (short) 2), new ItemStack(Material.BONE),
				new ItemStack(Material.WORKBENCH), new ItemStack(Material.PAPER) };

		// Get list of item stacks for items displayed in the GUI, for multiple achievements.
		// Elytra and Grass paths only available in Minecraft 1.9+, we construct the list depending on the game version.
		if (version >= 9) {
			normalAchievementCategoryItems = new ItemStack[] { new ItemStack(Material.BOOK_AND_QUILL),
					new ItemStack(Material.SKULL_ITEM), new ItemStack(Material.ARROW),
					new ItemStack(Material.SNOW_BALL), new ItemStack(Material.EGG),
					new ItemStack(Material.RAW_FISH, 1, (short) 2), new ItemStack(Material.FISHING_ROD),
					new ItemStack(Material.FLINT), new ItemStack(Material.MELON), new ItemStack(Material.SHEARS),
					new ItemStack(Material.MILK_BUCKET), new ItemStack(Material.LAVA_BUCKET),
					new ItemStack(Material.WATER_BUCKET), new ItemStack(Material.EMERALD),
					new ItemStack(Material.ANVIL), new ItemStack(Material.ENCHANTMENT_TABLE),
					new ItemStack(Material.BED), new ItemStack(Material.EXP_BOTTLE),
					new ItemStack(Material.GLASS_BOTTLE), new ItemStack(Material.WATCH), new ItemStack(Material.HOPPER),
					new ItemStack(Material.CHEST), new ItemStack(Material.GRASS_PATH),
					new ItemStack(Material.INK_SACK, 1, (short) 15), new ItemStack(Material.LEASH),
					new ItemStack(Material.BREWING_STAND_ITEM), new ItemStack(Material.FIREWORK),
					new ItemStack(Material.JUKEBOX), new ItemStack(Material.ENDER_PEARL),
					new ItemStack(Material.GOLD_BARDING), new ItemStack(Material.IRON_BARDING),
					new ItemStack(Material.FURNACE), new ItemStack(Material.LEATHER_BOOTS),
					new ItemStack(Material.CARROT_STICK), new ItemStack(Material.SADDLE),
					new ItemStack(Material.MINECART), new ItemStack(Material.BOAT), new ItemStack(Material.ELYTRA),
					new ItemStack(Material.CARPET, 1, (short) 9), new ItemStack(Material.BOOKSHELF) };
		} else {
			normalAchievementCategoryItems = new ItemStack[] { new ItemStack(Material.BOOK_AND_QUILL),
					new ItemStack(Material.SKULL_ITEM), new ItemStack(Material.ARROW),
					new ItemStack(Material.SNOW_BALL), new ItemStack(Material.EGG),
					new ItemStack(Material.RAW_FISH, 1, (short) 2), new ItemStack(Material.FISHING_ROD),
					new ItemStack(Material.FLINT), new ItemStack(Material.MELON), new ItemStack(Material.SHEARS),
					new ItemStack(Material.MILK_BUCKET), new ItemStack(Material.LAVA_BUCKET),
					new ItemStack(Material.WATER_BUCKET), new ItemStack(Material.EMERALD),
					new ItemStack(Material.ANVIL), new ItemStack(Material.ENCHANTMENT_TABLE),
					new ItemStack(Material.BED), new ItemStack(Material.EXP_BOTTLE),
					new ItemStack(Material.GLASS_BOTTLE), new ItemStack(Material.WATCH), new ItemStack(Material.HOPPER),
					new ItemStack(Material.CHEST), new ItemStack(Material.GRASS),
					new ItemStack(Material.INK_SACK, 1, (short) 15), new ItemStack(Material.LEASH),
					new ItemStack(Material.BREWING_STAND_ITEM), new ItemStack(Material.FIREWORK),
					new ItemStack(Material.JUKEBOX), new ItemStack(Material.ENDER_PEARL),
					new ItemStack(Material.GOLD_BARDING), new ItemStack(Material.IRON_BARDING),
					new ItemStack(Material.FURNACE), new ItemStack(Material.LEATHER_BOOTS),
					new ItemStack(Material.CARROT_STICK), new ItemStack(Material.SADDLE),
					new ItemStack(Material.MINECART), new ItemStack(Material.BOAT), new ItemStack(Material.BEDROCK),
					new ItemStack(Material.CARPET, 1, (short) 9), new ItemStack(Material.BOOKSHELF) };
		}

		ItemMeta backMeta = backButton.getItemMeta();
		backMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
				StringEscapeUtils.unescapeJava(plugin.getPluginLang().getString("list-back-message", "&7Back"))));
		backButton.setItemMeta(backMeta);
	}

	@Override
	protected void executeCommand(CommandSender sender, String[] args) {

		if (!(sender instanceof Player)) {
			return;
		}

		Player player = (Player) sender;

		if (!player.hasPermission("achievement.list")) {
			player.sendMessage(plugin.getChatHeader()
					+ plugin.getPluginLang().getString("no-permissions", "You do not have the permission to do this."));
			return;
		}

		// Create a new chest-like inventory; make it as small as possible while still containing all elements.
		Inventory guiInv = Bukkit.createInventory(null,
				getClosestGreaterMultipleOf9(MultipleAchievements.values().length + NormalAchievements.values().length
						- plugin.getDisabledCategorySet().size() + 1),
				ChatColor.translateAlternateColorCodes('&',
						plugin.getPluginLang().getString("list-gui-title", "&5&lAchievements List")));

		// Total number of categories already displayed.
		int numberOfCategories = 0;
		numberOfCategories += populateMainGUIWithMultipleAchievements(player, guiInv);
		numberOfCategories += populateMainGUIWithNormalAchievements(player, guiInv, numberOfCategories);
		populateMainGUIWithCommandsAchievements(player, guiInv, numberOfCategories);

		// Display GUI to the player.
		player.openInventory(guiInv);
	}

	/**
	 * Displays the a category GUI, containing all the achievements from a given category. This method is used for
	 * multiple achievements, in other words those based on sub-categories.
	 * 
	 * @param clickedItem
	 * @param player
	 */
	public void createCategoryGUIMultiple(Material clickedItem, Player player) {

		MultipleAchievements category;

		// Match the item the player clicked on with a category.
		switch (clickedItem) {
			case STONE:
				category = MultipleAchievements.PLACES;
				break;
			case SMOOTH_BRICK:
				category = MultipleAchievements.BREAKS;
				break;
			case BONE:
				category = MultipleAchievements.KILLS;
				break;
			case WORKBENCH:
				category = MultipleAchievements.CRAFTS;
				break;
			case PAPER:
				category = MultipleAchievements.PLAYERCOMMANDS;
				break;
			// Default case cannot happen.
			default:
				return;
		}

		String categoryName = category.toString();

		AchievementCommentedYamlConfiguration config = plugin.getPluginConfig();
		ConfigurationSection categoryConfig = config.getConfigurationSection(categoryName);

		int totalAchievementsInCategory = 0;

		// Retrieve total number of achievements in the sub-categories of the category.
		for (String section : categoryConfig.getKeys(false)) {
			totalAchievementsInCategory += config.getConfigurationSection(categoryName + '.' + section).getKeys(false)
					.size();
		}

		// Used to make the GUI as small as possible while still containing all achievements.
		int inventorySize = getClosestGreaterMultipleOf9(totalAchievementsInCategory + 1);

		// Create a new chest-like inventory; make it as small as possible while still containing all achievements.
		Inventory inventory = Bukkit.createInventory(null, inventorySize, ChatColor.translateAlternateColorCodes('&',
				plugin.getPluginLang().getString("list-gui-title", "&5&lAchievements List")));

		int positionInGUI = 0;

		// Match the item the player clicked on with its database statistic.
		for (String section : categoryConfig.getKeys(false)) {
			// Retrieve statistic from category and subcategory.
			int statistic = plugin.getPoolsManager().getAndIncrementStatisticAmount(category, section, player, 0);

			String previousItemDate = null;
			int previousItemGoal = 0;
			int subcategoryIndex = 0;

			// Populate GUI with all the achievements for the current sub-category.
			ConfigurationSection subcategoryConfig = config.getConfigurationSection(categoryName + '.' + section);
			for (String level : subcategoryConfig.getKeys(false)) {
				// If the user specifies more than 98 achievements in the category they will not be displayed.
				if (positionInGUI >= inventorySize - 1) {
					break;
				}

				// Convert threshold to an integer.
				int currentItemGoal = Integer.parseInt(level);

				String achName = config.getString(categoryName + '.' + section + '.' + level + ".Name", "");
				String nameToShowUser = getAchievementNameToDisplay(categoryName, '.' + section, achName, level);
				String achMessage = getAchievementMessageToDisplay(categoryName, '.' + section, level);

				List<String> rewards = plugin.getReward().getRewardListing(categoryName + '.' + section + '.' + level);
				String date = plugin.getDb().getPlayerAchievementDate(player, achName);

				boolean inelligibleSeriesItem;
				if (subcategoryIndex == 0 || date != null || previousItemDate != null) {
					// First achievement in the category OR achievement has been completed OR previous achievement has
					// been completed.
					inelligibleSeriesItem = false;
				} else {
					// Check whether this achievement cannot be completed until the previous one is completed.
					if (currentItemGoal > previousItemGoal) {
						inelligibleSeriesItem = true;
					} else {
						inelligibleSeriesItem = false;
					}
				}

				createCategoryGUIItem(inventory, positionInGUI, level, statistic, nameToShowUser, achMessage, rewards,
						date, inelligibleSeriesItem, false);
				positionInGUI++;

				previousItemDate = date;
				previousItemGoal = currentItemGoal;
				subcategoryIndex++;
			}
		}

		// Add "back button" item.
		inventory.setItem(positionInGUI, backButton);

		// Display category GUI.
		player.openInventory(inventory);
	}

	/**
	 * Displays a category GUI, containing all the achievements from a given category. This method is used for normal
	 * achievements.
	 * 
	 * @param clickedItem
	 * @param player
	 */
	public void createCategoryGUINormal(Material clickedItem, Player player) {

		NormalAchievements category = null;
		long statistic = 0L;

		// Match the item the player clicked on with a category and its database statistic.
		switch (clickedItem) {
			case BOOK_AND_QUILL:
				category = NormalAchievements.CONNECTIONS;
				break;
			case SKULL_ITEM:
				category = NormalAchievements.DEATHS;
				break;
			case ARROW:
				category = NormalAchievements.ARROWS;
				break;
			case SNOW_BALL:
				category = NormalAchievements.SNOWBALLS;
				break;
			case EGG:
				category = NormalAchievements.EGGS;
				break;
			case RAW_FISH:
				category = NormalAchievements.FISH;
				break;
			case FLINT:
				category = NormalAchievements.ITEMBREAKS;
				break;
			case MELON:
				category = NormalAchievements.EATENITEMS;
				break;
			case SHEARS:
				category = NormalAchievements.SHEARS;
				break;
			case MILK_BUCKET:
				category = NormalAchievements.MILKS;
				break;
			case LAVA_BUCKET:
				category = NormalAchievements.LAVABUCKETS;
				break;
			case WATER_BUCKET:
				category = NormalAchievements.WATERBUCKETS;
				break;
			case EMERALD:
				category = NormalAchievements.TRADES;
				break;
			case ANVIL:
				category = NormalAchievements.ANVILS;
				break;
			case ENCHANTMENT_TABLE:
				category = NormalAchievements.ENCHANTMENTS;
				break;
			case BED:
				category = NormalAchievements.BEDS;
				break;
			case EXP_BOTTLE:
				category = NormalAchievements.LEVELS;
				break;
			case GLASS_BOTTLE:
				category = NormalAchievements.CONSUMEDPOTIONS;
				break;
			case WATCH:
				category = NormalAchievements.PLAYEDTIME;
				break;
			case HOPPER:
				category = NormalAchievements.DROPS;
				break;
			case CHEST:
				category = NormalAchievements.PICKUPS;
				break;
			case GRASS:
				category = NormalAchievements.HOEPLOWING;
				break;
			case INK_SACK:
				category = NormalAchievements.FERTILISING;
				break;
			case LEASH:
				category = NormalAchievements.TAMES;
				break;
			case BREWING_STAND_ITEM:
				category = NormalAchievements.BREWING;
				break;
			case FIREWORK:
				category = NormalAchievements.FIREWORKS;
				break;
			case JUKEBOX:
				category = NormalAchievements.MUSICDISCS;
				break;
			case ENDER_PEARL:
				category = NormalAchievements.ENDERPEARLS;
				break;
			case LEATHER_BOOTS:
				category = NormalAchievements.DISTANCEFOOT;
				break;
			case CARROT_STICK:
				category = NormalAchievements.DISTANCEPIG;
				break;
			case SADDLE:
				category = NormalAchievements.DISTANCEHORSE;
				break;
			case MINECART:
				category = NormalAchievements.DISTANCEMINECART;
				break;
			case BOAT:
				category = NormalAchievements.DISTANCEBOAT;
				break;
			case BEDROCK:
				category = NormalAchievements.DISTANCEGLIDING;
				break;
			case IRON_BARDING:
				category = NormalAchievements.PETMASTERRECEIVE;
				break;
			case GOLD_BARDING:
				category = NormalAchievements.PETMASTERGIVE;
				break;
			case CARPET:
				category = NormalAchievements.DISTANCELLAMA;
				break;
			case FURNACE:
				category = NormalAchievements.SMELTING;
				break;
			case FISHING_ROD:
				category = NormalAchievements.TREASURES;
				break;
			// Objects exclusive to Minecraft 1.9+ or Commands achievements.
			default:
				statistic = -1L;
				break;
		}

		// Items used for 1.9+ version of Minecraft. This corresponds to the default case of the above switch.
		if (category == null && version >= 9) {
			switch (clickedItem) {
				case ELYTRA:
					category = NormalAchievements.DISTANCEGLIDING;
					break;
				case GRASS_PATH:
					category = NormalAchievements.HOEPLOWING;
					break;
				// Default case: Commands achievements.
				default:
					break;
			}
		}

		String categoryName;
		if (category == null) {
			categoryName = "Commands";
		} else {
			categoryName = category.toString();
		}

		if (category == NormalAchievements.PLAYEDTIME) {
			statistic = plugin.getPoolsManager().getPlayerPlayTimeAmount(player);
		} else if (category == NormalAchievements.CONNECTIONS) {
			statistic = plugin.getDb().getConnectionsAmount(player);
		} else if (category != null) {
			statistic = plugin.getPoolsManager().getAndIncrementStatisticAmount(category, player, 0);
		}

		// Used to make the GUI as small as possible while still containing all achievements.
		int inventorySize = getClosestGreaterMultipleOf9(
				plugin.getPluginConfig().getConfigurationSection(categoryName).getKeys(false).size() + 1);

		// Create a new chest-like inventory.
		Inventory inventory = Bukkit.createInventory(null, inventorySize, ChatColor.translateAlternateColorCodes('&',
				plugin.getPluginLang().getString("list-gui-title", "&5&lAchievements List")));

		int positionInGUI = 0;

		String previousItemDate = null;
		Integer previousItemGoal = 0;
		// Populate the GUI with all of the achievements for the category.
		for (String level : plugin.getPluginConfig().getConfigurationSection(categoryName).getKeys(false)) {
			// If the user specifies more than 98 achievements in the category they will not be displayed.
			if (positionInGUI >= inventorySize - 1) {
				break;
			}

			// Convert threshold to an integer.
			Integer currentItemGoal = Ints.tryParse(level);

			String achName = plugin.getPluginConfig().getString(categoryName + '.' + level + ".Name", "");
			String nameToShowUser = getAchievementNameToDisplay(categoryName, "", achName, level);
			String achMessage = getAchievementMessageToDisplay(categoryName, "", level);

			List<String> rewards = plugin.getReward().getRewardListing(categoryName + '.' + level);
			String date = plugin.getDb().getPlayerAchievementDate(player, achName);

			boolean inelligibleSeriesItem;
			if (statistic == -1L || positionInGUI == 0 || date != null || previousItemDate != null) {
				// Commands achievement OR first achievement in the category OR achievement has been completed OR
				// previous achievement has been completed.
				inelligibleSeriesItem = false;
			} else {
				// Check whether this achievement cannot be completed until the previous one is completed.
				if (currentItemGoal > previousItemGoal) {
					inelligibleSeriesItem = true;
				} else {
					inelligibleSeriesItem = false;
				}
			}

			boolean playedTime = false;
			if (clickedItem == Material.WATCH) {
				playedTime = true;
			}

			createCategoryGUIItem(inventory, positionInGUI, level, statistic, nameToShowUser, achMessage, rewards, date,
					inelligibleSeriesItem, playedTime);
			positionInGUI++;

			previousItemDate = date;
			previousItemGoal = currentItemGoal;
		}

		// Add "back button" item.
		inventory.setItem(positionInGUI, backButton);

		// Display category GUI.
		player.openInventory(inventory);
	}

	/**
	 * Extracts the achievement name that should be shown in the item lore.
	 * 
	 * @param categoryName
	 * @param subcategory
	 * @param achName
	 * @param level
	 * @return
	 */
	private String getAchievementNameToDisplay(String categoryName, String subcategory, String achName, String level) {

		String nameToShowUser;
		String displayName = plugin.getPluginConfig()
				.getString(categoryName + subcategory + '.' + level + ".DisplayName", "");
		if (Strings.isNullOrEmpty(displayName)) {
			// Use the achievement key name (this name is used in the achievements table in the database).
			nameToShowUser = achName;
		} else {
			// Display name is defined; use it.
			nameToShowUser = displayName;
		}
		return nameToShowUser;
	}

	/**
	 * Extracts the achievement message that should be shown in the item lore.
	 * 
	 * @param categoryName
	 * @param subcategory
	 * @param level
	 * @return
	 */
	private String getAchievementMessageToDisplay(String categoryName, String subcategory, String level) {

		String achMessage;
		String goal = plugin.getPluginConfig().getString(categoryName + subcategory + '.' + level + ".Goal", "");
		if (Strings.isNullOrEmpty(goal)) {
			// Show the achievement message below the achievement name.
			achMessage = plugin.getPluginConfig().getString(categoryName + subcategory + '.' + level + ".Message", "");
		} else {
			// Show the goal below the achievement name.
			achMessage = goal;
		}
		return achMessage;
	}

	/**
	 * Populates the main GUI with MultipleAchievement categories.
	 * 
	 * @param player
	 * @param guiInv
	 * @return
	 */
	private int populateMainGUIWithMultipleAchievements(Player player, Inventory guiInv) {

		int categoriesProcessed = 0;

		// Display categories with multiple sub-categories in GUI.
		for (MultipleAchievements category : MultipleAchievements.values()) {
			// Boolean corresponding to whether a player has received achievements in the current category. Used for
			// HideNotReceivedCategories config option.
			boolean hasReceivedInCategory = !hideNotReceivedCategories;

			String displayName = plugin.getPluginLang().getString(category.toLangName(), category.toLangDefault());
			String categoryName = category.toString();
			// Hide category if the user has defined an empty name for it or if it's in the disabled list.
			if (displayName.length() == 0 || plugin.getDisabledCategorySet().contains(categoryName)) {
				continue;
			}

			int totalAchievementsInCategory = 0;

			// Iterate through all sub-categories.
			for (String section : plugin.getPluginConfig().getConfigurationSection(categoryName).getKeys(false)) {
				Set<String> configSubcategory = plugin.getPluginConfig()
						.getConfigurationSection(categoryName + '.' + section).getKeys(false);
				totalAchievementsInCategory += configSubcategory.size();
				// No need to check next sub-category, continue and move to next sub-category.
				if (hasReceivedInCategory) {
					continue;
				}
				// Hide not yet unlocked categories: we must check whether the player has received at least one
				// achievement
				// in the category. Iterate through all achievements in sub-category.
				for (String level : configSubcategory) {
					// Check whether player has received achievement.
					if (plugin.getPoolsManager().hasPlayerAchievement(player, plugin.getPluginConfig()
							.getString(categoryName + '.' + section + '.' + level + ".Name", ""))) {
						// At least one achievement was received in the current category; it is unlocked, can
						// continue processing.
						hasReceivedInCategory = true;
						break;
					}
				}
			}
			ItemStack categoryItem = getItemStack(multipleAchievementCategoryItems, displayName, category.ordinal(),
					hasReceivedInCategory, totalAchievementsInCategory);
			guiInv.setItem(categoriesProcessed, categoryItem);
			categoriesProcessed++;
		}
		return categoriesProcessed;
	}

	/**
	 * Populates the main GUI with NormalAchievement categories.
	 * 
	 * @param player
	 * @param guiInv
	 * @return
	 */
	private int populateMainGUIWithNormalAchievements(Player player, Inventory guiInv,
			int categoriesPreviouslyProcessed) {

		int categoriesProcessed = 0;

		// Display categories with normal achievements in GUI.
		for (NormalAchievements category : NormalAchievements.values()) {
			String displayName = plugin.getPluginLang().getString(category.toLangName(), category.toLangDefault());
			String categoryName = category.toString();
			// Hide category if the user has defined an empty name for it or if it's in the disabled list.
			if (displayName.length() == 0 || plugin.getDisabledCategorySet().contains(categoryName)) {
				continue;
			}

			// Boolean corresponding to whether a player has received achievements in the current category. Used for
			// HideNotReceivedCategories config option.
			boolean hasReceivedInCategory = true;

			Set<String> configCategory = plugin.getPluginConfig().getConfigurationSection(categoryName).getKeys(false);

			// Hide not yet unlocked categories: we must check whether the player has received at least one achievement
			// in the category.
			if (hideNotReceivedCategories) {
				hasReceivedInCategory = false;
				// Iterate through all achievements in category.

				for (String level : configCategory) {
					/// Check whether player has received achievement.
					if (plugin.getPoolsManager().hasPlayerAchievement(player,
							plugin.getPluginConfig().getString(categoryName + '.' + level + ".Name", ""))) {
						// At least one achievement was received in the current category; it is unlocked, can continue
						// processing.
						hasReceivedInCategory = true;
						break;
					}
				}
			}
			ItemStack itemInMainGui = getItemStack(normalAchievementCategoryItems, displayName, category.ordinal(),
					hasReceivedInCategory, configCategory.size());
			guiInv.setItem(categoriesPreviouslyProcessed + categoriesProcessed, itemInMainGui);
			categoriesProcessed++;
		}
		return categoriesProcessed;
	}

	/**
	 * Populates the main GUI with Commands category.
	 * 
	 * @param player
	 * @param guiInv
	 * @return
	 */
	private void populateMainGUIWithCommandsAchievements(Player player, Inventory guiInv,
			int categoriesPreviouslyProcessed) {

		String commandsDisplayName = plugin.getPluginLang().getString("list-commands", "Other Achievements");
		// Hide Commands category if the user has defined an empty name for it or if it was disabled.
		if (commandsDisplayName.length() == 0 || plugin.getDisabledCategorySet().contains("Commands")) {
			return;
		}
		// Boolean corresponding to whether a player has received achievements in the current category. Used for
		// HideNotReceivedCategories config option.
		boolean hasReceivedInCategory = true;

		Set<String> configCategory = plugin.getPluginConfig().getConfigurationSection("Commands").getKeys(false);

		// Hide not yet unlocked categories: we must check whether the player has received at least one achievement in
		// the category.
		if (hideNotReceivedCategories) {
			hasReceivedInCategory = false;
			// Iterate through all achievements in category.
			for (String ach : configCategory) {
				/// Check whether player has received achievement.
				if (plugin.getPoolsManager().hasPlayerAchievement(player,
						plugin.getPluginConfig().getString("Commands." + ach + ".Name", ""))) {
					// At least one achievement was received in the current category; it is unlocked, can continue
					// processing.
					hasReceivedInCategory = true;
					break;
				}
			}
		}
		ItemStack itemInMainGui = getItemStack(normalAchievementCategoryItems, commandsDisplayName,
				NormalAchievements.values().length, hasReceivedInCategory, configCategory.size());
		guiInv.setItem(categoriesPreviouslyProcessed, itemInMainGui);
	}

	/**
	 * Retrieves correct item in the ItemStack lists and set its metadata.
	 * 
	 * @param itemStacks
	 * @param displayName
	 * @param indexInItemStacksArray
	 * @param hasReceivedInCategory
	 * @param i
	 * @return
	 */
	private ItemStack getItemStack(ItemStack[] itemStacks, String displayName, int indexInItemStacksArray,
			boolean hasReceivedInCategory, int totalAchievementsInCategory) {

		ItemStack categoryItem;
		ItemMeta categoryMeta;
		if (hasReceivedInCategory) {
			// Create item stack that will be displayed in the GUI, with its category name.
			categoryItem = itemStacks[indexInItemStacksArray];
			categoryMeta = categoryItem.getItemMeta();
			categoryMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
					"&8" + plugin.getPluginConfig().getString("ListAchievementFormat", "%ICON% %NAME% %ICON%")
							.replace("%ICON%", plugin.getIcon()).replace("%NAME%", "&l" + displayName + "&8")));
		} else {
			// The player has not unlocked any achievements in the category: display barrier item with message.
			categoryItem = new ItemStack(Material.BARRIER);
			categoryMeta = categoryItem.getItemMeta();
			categoryMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&8" + plugin.getPluginLang()
					.getString("list-category-not-unlocked", "You have not yet unlocked this category.")));
		}
		String loreAmountMessage;
		if (totalAchievementsInCategory > 1) {
			loreAmountMessage = plugin.getPluginLang().getString("list-achievements-in-category-plural",
					"AMOUNT achievements");

		} else {
			loreAmountMessage = plugin.getPluginLang().getString("list-achievements-in-category-singular",
					"AMOUNT achievement");
		}
		// Set item lore.
		categoryMeta.setLore(ImmutableList.of(ChatColor.translateAlternateColorCodes('&',
				"&8" + loreAmountMessage.replace("AMOUNT", Integer.toString(totalAchievementsInCategory)))));
		// Set item meta.
		categoryItem.setItemMeta(categoryMeta);

		return categoryItem;
	}

	/**
	 * Creates a GUI item for a given achievement.
	 * 
	 * @param inventory
	 * @param positionInGUI
	 * @param level
	 * @param statistic
	 * @param achName
	 * @param achMessage
	 * @param rewards
	 * @param date
	 * @param inelligibleSeriesItem
	 */
	private void createCategoryGUIItem(Inventory inventory, int positionInGUI, String level, long statistic,
			String achName, String achMessage, List<String> rewards, String date, boolean inelligibleSeriesItem,
			boolean playedTime) {

		// Display a clay block in the GUI, with a color depending on whether it was received or not, or whether
		// progress was started.
		ItemStack achItem;
		if (date != null) {
			// Achievement has been received.
			achItem = new ItemStack(Material.STAINED_CLAY, 1, (short) 5);
		} else if (statistic > 0) {
			// Player is making progress toward the achievement.
			achItem = new ItemStack(Material.STAINED_CLAY, 1, (short) 4);
		} else {
			// Player has not started progress.
			achItem = new ItemStack(Material.STAINED_CLAY, 1, (short) 14);
		}

		// Set name of the achievement. The style depends whether it was received or not and whether the user has set
		// obfuscateNotReceived and/or obfuscateProgressiveAchievements in the config.
		ItemMeta itemMeta = achItem.getItemMeta();
		if (date != null) {
			itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
							StringEscapeUtils.unescapeJava(
									plugin.getPluginLang().getString("list-achievement-received", "&a\u2714&f "))
									+ achName));
		} else if (obfuscateNotReceived || (obfuscateProgressiveAchievements && inelligibleSeriesItem)) {
			itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
					StringEscapeUtils.unescapeJava(
							plugin.getPluginLang().getString("list-achievement-not-received", "&4\u2718&8 ")) + "&k"
							+ REGEX_PATTERN.matcher(achName).replaceAll("")));
		} else {
			itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
					StringEscapeUtils.unescapeJava(
							plugin.getPluginLang().getString("list-achievement-not-received", "&4\u2718&8 ") + "&o"
									+ REGEX_PATTERN.matcher(achName).replaceAll(""))));
		}

		// Build the lore of the item.
		ArrayList<String> lore = buildLoreString(achMessage, level, rewards, date, statistic, inelligibleSeriesItem,
				playedTime);

		itemMeta.setLore(lore);
		achItem.setItemMeta(itemMeta);
		if (numberedItemsInList) {
			achItem.setAmount(positionInGUI % 64 + 1);
		}
		inventory.setItem(positionInGUI, achItem);
	}

	/**
	 * Creates the lore for the current achievement, containing information about the progress, date of reception,
	 * description, rewards.
	 * 
	 * @param achMessage
	 * @param level
	 * @param rewards
	 * @param date
	 * @param statistic
	 * @param inelligibleSeriesItem
	 * @return
	 */
	private ArrayList<String> buildLoreString(String achMessage, String level, List<String> rewards, String date,
			long statistic, boolean inelligibleSeriesItem, boolean playedTime) {

		ArrayList<String> lore = new ArrayList<>();
		lore.add("");

		if (date != null) {
			// Display description.
			lore.add(ChatColor.translateAlternateColorCodes('&',
					"&7&l" + plugin.getPluginLang().getString("list-description", "Description:")));
			lore.add(ChatColor.translateAlternateColorCodes('&', "&r" + achMessage));
			lore.add("");
			// Display reception date.
			lore.add(ChatColor.translateAlternateColorCodes('&',
					"&7&l" + plugin.getPluginLang().getString("list-reception", "Reception date:")));
			lore.add(ChatColor.translateAlternateColorCodes('&', "&r" + date));
			lore.add("");
		} else {
			// Display goal. The style depends whether it was received or not and whether the user has set
			// obfuscateNotReceived in the config.
			lore.add(ChatColor.translateAlternateColorCodes('&',
					"&7&l" + plugin.getPluginLang().getString("list-goal", "Goal:")));
			String strippedAchMessage = REGEX_PATTERN.matcher(achMessage).replaceAll("");
			if (obfuscateNotReceived || (obfuscateProgressiveAchievements && inelligibleSeriesItem)) {
				lore.add(ChatColor.translateAlternateColorCodes('&', "&8&k" + strippedAchMessage));
			} else {
				lore.add(ChatColor.translateAlternateColorCodes('&', "&8&o" + strippedAchMessage));
			}
			lore.add("");
			// Display progress bar. Achievements with statistic -1 correspond to Commands achievements, ignore.
			if (!obfuscateNotReceived && statistic >= 0) {
				lore.add(ChatColor.translateAlternateColorCodes('&',
						"&7&l" + plugin.getPluginLang().getString("list-progress", "Progress:")));
				lore.add(ChatColor.translateAlternateColorCodes('&',
						constructProgressBar(strippedAchMessage, level, statistic, playedTime)));
				lore.add("");
			}
		}

		// Add the rewards information.
		if (!rewards.isEmpty() && !hideRewardDisplay) {
			lore.add(ChatColor.translateAlternateColorCodes('&',
					"&7&l" + plugin.getPluginLang().getString("list-reward", "Reward(s):")));
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
	 * @param playedTime
	 * @return progress bar
	 */
	private String constructProgressBar(String achMessage, String level, long statistic, boolean playedTime) {

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

		int levelInt = Integer.parseInt(level);
		String middleText;
		double statisticDouble;
		if (playedTime) {
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
			if (enrichedProgressBars && !hasDisplayedMiddleText && i >= (textSize - middleTextSize) / 4) {
				// Middle reached: append enriched statistic information.
				barDisplay.append(middleText);
				// Do not display middleText again.
				hasDisplayedMiddleText = true;
				// Iterate a number of times equal to the number of iterations so far to have the same number of
				// vertical bars left and right from the middle text.
				i = textSize / 2 + 1 - i;
			} else if (i < ((textSize / 2 - 1) * statisticDouble) / levelInt) {
				// Color: progress by user.
				barDisplay.append(plugin.getColor()).append('|');
				i++;
			} else {
				// Grey: amount not yet reached by user.
				barDisplay.append("&8|");
				i++;
			}
		}

		barDisplay.append("&8]");
		return barDisplay.toString();
	}

	/**
	 * Inventory GUIs need a number of slots that is a multiple of 9. This simple function gets the smallest multiple of
	 * 9 greater than its input value, in order for the GUI to contain all of its elements with minimum empty space.
	 * Limit to 99, above the GUI is really messed up.
	 * 
	 * @param value
	 * @return closest multiple of 9 greater than value
	 */
	private int getClosestGreaterMultipleOf9(int value) {

		int multipleOfNine = 9;
		while (multipleOfNine < value && multipleOfNine <= 99) {
			multipleOfNine += 9;
		}
		return multipleOfNine;
	}
}
