package com.hm.achievement.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.google.common.primitives.Ints;
import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.particle.ReflectionUtils.PackageType;
import com.hm.achievement.utils.YamlManager;

/**
 * Class in charge of handling the /aach list command, which displays interactive GUIs. The command displays the main
 * GUI, corresponding to all the different available categories and their names. Users will then be able to click on an
 * unlocked category to display another GUI with more specific details about the different achievements.
 * 
 * @author Pyves
 */
public class ListCommand extends AbstractCommand {

	private boolean hideNotReceivedCategories;
	private boolean obfuscateNotReceived;
	private boolean obfuscateProgressiveAchievements;
	private boolean hideRewardDisplay;
	private int listTime;
	private int version;

	// Corresponds to times at which players have entered list commands. Cooldown structure.
	private HashMap<String, Long> players;

	// Array of category names from language file.
	private String[] normalAchievementCategoryNames;
	private String[] multipleAchievementCategoryNames;

	// Array of item stacks for items displayed in the GUI.
	private ItemStack[] multipleAchievementCategoryItems;
	private ItemStack[] normalAchievementCategoryItems;

	// Pattern to delete colors if achievement not yet received.
	private static final Pattern REGEX_PATTERN = Pattern.compile("&([a-f]|[0-9]){1}");

	// Minecraft font, used to get size information in the progress bar.
	private static final MinecraftFont FONT = MinecraftFont.Font;

	public ListCommand(AdvancedAchievements plugin) {

		super(plugin);
		players = new HashMap<>();
		// Load configuration parameters.
		hideNotReceivedCategories = plugin.getPluginConfig().getBoolean("HideNotReceivedCategories", false);
		obfuscateNotReceived = plugin.getPluginConfig().getBoolean("ObfuscateNotReceived", true);
		obfuscateProgressiveAchievements = plugin.getPluginConfig().getBoolean("ObfuscateProgressiveAchievements",
				false);
		hideRewardDisplay = plugin.getPluginConfig().getBoolean("HideRewardDisplayInList", false);
		listTime = plugin.getPluginConfig().getInt("TimeList", 0) * 1000;
		// Get array of category names from configuration.
		normalAchievementCategoryNames = new String[] {
				plugin.getPluginLang().getString("list-connections", "Connections"),
				plugin.getPluginLang().getString("list-deaths", "Number of Deaths"),
				plugin.getPluginLang().getString("list-arrows", "Arrows Shot"),
				plugin.getPluginLang().getString("list-snowballs", "Snowballs Thrown"),
				plugin.getPluginLang().getString("list-eggs", "Eggs Thrown"),
				plugin.getPluginLang().getString("list-fish", "Fish Caught"),
				plugin.getPluginLang().getString("list-itembreaks", "Items Broken"),
				plugin.getPluginLang().getString("list-eatenitems", "Items Eaten"),
				plugin.getPluginLang().getString("list-shear", "Sheeps Sheared"),
				plugin.getPluginLang().getString("list-milk", "Cows Milked"),
				plugin.getPluginLang().getString("list-trades", "Number of Trades"),
				plugin.getPluginLang().getString("list-anvils", "Anvils Used"),
				plugin.getPluginLang().getString("list-enchantments", "Items Enchanted"),
				plugin.getPluginLang().getString("list-beds", "Beds Entered"),
				plugin.getPluginLang().getString("list-maxlevel", "Max Level Reached"),
				plugin.getPluginLang().getString("list-potions", "Potions Consumed"),
				plugin.getPluginLang().getString("list-playedtime", "Time Played"),
				plugin.getPluginLang().getString("list-itemdrops", "Items Dropped"),
				plugin.getPluginLang().getString("list-hoeplowings", "Surface Plowed"),
				plugin.getPluginLang().getString("list-fertilising", "Plants Fertilised"),
				plugin.getPluginLang().getString("list-taming", "Animals Tamed"),
				plugin.getPluginLang().getString("list-brewing", "Potions Brewed"),
				plugin.getPluginLang().getString("list-fireworks", "Fireworks Launched"),
				plugin.getPluginLang().getString("list-musicdiscs", "Music Discs Played"),
				plugin.getPluginLang().getString("list-enderpearls", "Teleportations with Ender Pearls"),
				plugin.getPluginLang().getString("list-distance-foot", "Distance Travelled by Foot"),
				plugin.getPluginLang().getString("list-distance-pig", "Distance Travelled on a Pig"),
				plugin.getPluginLang().getString("list-distance-horse", "Distance Travelled on a Horse"),
				plugin.getPluginLang().getString("list-distance-minecart", "Distance Travelled in a Minecart"),
				plugin.getPluginLang().getString("list-distance-boat", "Distance Travelled in a Boat"),
				plugin.getPluginLang().getString("list-distance-gliding", "Distance Travelled with Elytra"),
				plugin.getPluginLang().getString("list-commands", "Other Achievements") };

		multipleAchievementCategoryNames = new String[] {
				plugin.getPluginLang().getString("list-places", "Blocks Placed"),
				plugin.getPluginLang().getString("list-breaks", "Blocks Broken"),
				plugin.getPluginLang().getString("list-kills", "Entities Killed"),
				plugin.getPluginLang().getString("list-crafts", "Items Crafted") };

		// Simple and fast check to retrieve Minecraft version. Might need to be updated depending on how the
		// Minecraft versions change in the future.
		version = Integer.parseInt(PackageType.getServerVersion().split("_")[1]);

		// Get list of item stacks for items displayed in the GUI, for multiple achievements.
		multipleAchievementCategoryItems = new ItemStack[] { new ItemStack(Material.STONE, 1, (short) 6),
				new ItemStack(Material.SMOOTH_BRICK, 1, (short) 2), new ItemStack(Material.BONE),
				new ItemStack(Material.WORKBENCH) };

		// Get list of item stacks for items displayed in the GUI, for multiple achievements.
		// Elytra and Grass paths only available in Minecraft 1.9+, we construct the list depending on the game version.
		if (version >= 9)
			normalAchievementCategoryItems = new ItemStack[] { new ItemStack(Material.BOOK_AND_QUILL),
					new ItemStack(Material.SKULL_ITEM), new ItemStack(Material.ARROW),
					new ItemStack(Material.SNOW_BALL), new ItemStack(Material.EGG),
					new ItemStack(Material.RAW_FISH, 1, (short) 2), new ItemStack(Material.FLINT),
					new ItemStack(Material.MELON), new ItemStack(Material.SHEARS), new ItemStack(Material.MILK_BUCKET),
					new ItemStack(Material.EMERALD), new ItemStack(Material.ANVIL),
					new ItemStack(Material.ENCHANTMENT_TABLE), new ItemStack(Material.BED),
					new ItemStack(Material.EXP_BOTTLE), new ItemStack(Material.GLASS_BOTTLE),
					new ItemStack(Material.WATCH), new ItemStack(Material.HOPPER), new ItemStack(Material.GRASS_PATH),
					new ItemStack(Material.INK_SACK, 1, (short) 15), new ItemStack(Material.LEASH),
					new ItemStack(Material.BREWING_STAND_ITEM), new ItemStack(Material.FIREWORK),
					new ItemStack(Material.JUKEBOX), new ItemStack(Material.ENDER_PEARL),
					new ItemStack(Material.LEATHER_BOOTS), new ItemStack(Material.CARROT_STICK),
					new ItemStack(Material.SADDLE), new ItemStack(Material.MINECART), new ItemStack(Material.BOAT),
					new ItemStack(Material.ELYTRA), new ItemStack(Material.BOOKSHELF) };
		else
			normalAchievementCategoryItems = new ItemStack[] { new ItemStack(Material.BOOK_AND_QUILL),
					new ItemStack(Material.SKULL_ITEM), new ItemStack(Material.ARROW),
					new ItemStack(Material.SNOW_BALL), new ItemStack(Material.EGG),
					new ItemStack(Material.RAW_FISH, 1, (short) 2), new ItemStack(Material.FLINT),
					new ItemStack(Material.MELON), new ItemStack(Material.SHEARS), new ItemStack(Material.MILK_BUCKET),
					new ItemStack(Material.EMERALD), new ItemStack(Material.ANVIL),
					new ItemStack(Material.ENCHANTMENT_TABLE), new ItemStack(Material.BED),
					new ItemStack(Material.EXP_BOTTLE), new ItemStack(Material.GLASS_BOTTLE),
					new ItemStack(Material.WATCH), new ItemStack(Material.HOPPER), new ItemStack(Material.GRASS),
					new ItemStack(Material.INK_SACK, 1, (short) 15), new ItemStack(Material.LEASH),
					new ItemStack(Material.BREWING_STAND_ITEM), new ItemStack(Material.FIREWORK),
					new ItemStack(Material.JUKEBOX), new ItemStack(Material.ENDER_PEARL),
					new ItemStack(Material.LEATHER_BOOTS), new ItemStack(Material.CARROT_STICK),
					new ItemStack(Material.SADDLE), new ItemStack(Material.MINECART), new ItemStack(Material.BOAT),
					new ItemStack(Material.BEDROCK), new ItemStack(Material.BOOKSHELF) };
	}

	@Override
	protected void executeCommand(CommandSender sender, String[] args) {

		if (!(sender instanceof Player))
			return;

		Player player = (Player) sender;

		if (!player.hasPermission("achievement.list")) {
			player.sendMessage(plugin.getChatHeader()
					+ plugin.getPluginLang().getString("no-permissions", "You do not have the permission to do this."));
			return;
		}

		if (timeAuthorisedList(player)) {
			// Create a new chest-like inventory; make it as small as possible while still containing all elements.
			Inventory guiInv = Bukkit.createInventory(null,
					getClosestGreaterMultipleOf9(MultipleAchievements.values().length
							+ NormalAchievements.values().length - plugin.getDisabledCategorySet().size()),
					ChatColor.translateAlternateColorCodes('&',
							plugin.getPluginLang().getString("list-gui-title", "&5&lAchievements List")));

			// Total number of categories already displayed.
			int numberOfCategories = 0;

			YamlManager config = plugin.getPluginConfig();

			// Display categories with multiple sub-categories in GUI.
			for (MultipleAchievements category : MultipleAchievements.values()) {
				// Boolean corresponding to whether a player has received achievements in the current category. Used for
				// HideNotReceivedCategories config option.
				boolean hasReceivedInCategory = true;

				// Hide category if the user has defined an empty name for it.
				if (multipleAchievementCategoryNames[category.ordinal()].length() != 0) {
					// Retrieve the name of the category, defined by the user.
					String categoryName = category.toString();
					// Ignore this category if it's in the disabled list.
					if (plugin.getDisabledCategorySet().contains(categoryName)) {
						continue;
					}

					// Hide not yet unlocked categories: we must check whether the player has received at least one
					// achievement in the category.
					if (hideNotReceivedCategories) {
						hasReceivedInCategory = false;
						// Iterate through all sub-categories.
						for (String section : config.getConfigurationSection(categoryName).getKeys(false)) {
							// Iterate through all achievements in sub-category.
							for (String ach : config.getConfigurationSection(categoryName + '.' + section)
									.getKeys(false)) {
								// Check whether player has received achievement.
								if (plugin.getDb().hasPlayerAchievement(player,
										config.getString(categoryName + '.' + section + '.' + ach + ".Name", ""))) {
									// At least one achievement was received in the current category; it is unlocked,
									// can continue processing.
									hasReceivedInCategory = true;
									break;
								}
							}
							// No need to check next sub-category, break and move to next category.
							if (hasReceivedInCategory)
								break;
						}
					}

					ItemStack categoryItem;
					ItemMeta categoryMeta;

					if (hasReceivedInCategory || !hideNotReceivedCategories) {
						// Create item stack that will be displayed in the GUI, with its category name.
						categoryItem = multipleAchievementCategoryItems[category.ordinal()];
						categoryMeta = categoryItem.getItemMeta();
						categoryMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
								"&8" + config.getString("ListAchievementFormat", "%ICON% %NAME% %ICON%")
										.replaceAll("%ICON%", plugin.getIcon()).replaceAll("%NAME%",
												multipleAchievementCategoryNames[category.ordinal()])));
					} else {
						// The player has not unlocked any achievements in the category: display barrier item with
						// message.
						categoryItem = new ItemStack(Material.BARRIER);
						categoryMeta = categoryItem.getItemMeta();
						categoryMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
								"&8" + plugin.getPluginLang().getString("list-category-not-unlocked",
										"You have not yet unlocked this category.")));
					}

					// Set item in the GUI.
					categoryItem.setItemMeta(categoryMeta);
					guiInv.setItem(numberOfCategories, categoryItem);
					numberOfCategories++;
				}
			}

			// Display categories with normal achievements in GUI.
			for (NormalAchievements category : NormalAchievements.values()) {
				// Hide category if the user has defined an empty name for it.
				if (normalAchievementCategoryNames[category.ordinal()].length() != 0) {
					// Retrieve the name of the category, defined by the user.
					String categoryName = category.toString();
					// Ignore this category if it's in the disabled list.
					if (plugin.getDisabledCategorySet().contains(categoryName)) {
						continue;
					}

					ItemStack itemInMainGui = createNormalMainItem(categoryName, player, category.ordinal());
					guiInv.setItem(numberOfCategories, itemInMainGui);
					numberOfCategories++;
				}
			}

			// Hide Commands category if the user has defined an empty name for it or if it was disabled.
			if (normalAchievementCategoryNames[normalAchievementCategoryNames.length - 1].length() != 0
					&& !plugin.getDisabledCategorySet().contains("Commands")) {

				ItemStack itemInMainGui = createNormalMainItem("Commands", player,
						normalAchievementCategoryNames.length - 1);
				guiInv.setItem(numberOfCategories, itemInMainGui);
			}

			// Display GUI to the player.
			player.openInventory(guiInv);
		} else {
			// The player has already done a list command recently.
			player.sendMessage(plugin.getChatHeader() + plugin.getPluginLang()
					.getString("list-delay", "You must wait TIME seconds between each list command!")
					.replace("TIME", Integer.toString(listTime / 1000)));
		}
	}

	/**
	 * Create an ItemStack to be displayed in the main GUI for either normal or Commands categories.
	 * 
	 * @param categoryName
	 * @param player
	 * @param indexInItemStacksArray
	 * @param numberOfCategories
	 * @param guiInv
	 * @return
	 */
	private ItemStack createNormalMainItem(String categoryName, Player player, int indexInItemStacksArray) {

		YamlManager config = plugin.getPluginConfig();

		// Boolean corresponding to whether a player has received achievements in the current category. Used
		// for HideNotReceivedCategories config option.
		boolean hasReceivedInCategory = true;

		// Hide not yet unlocked categories: we must check whether the player has received at least one
		// achievement in the category.
		if (hideNotReceivedCategories) {
			hasReceivedInCategory = false;
			// Iterate through all achievements in category.
			for (String ach : config.getConfigurationSection(categoryName).getKeys(false)) {
				/// Check whether player has received achievement.
				if (plugin.getDb().hasPlayerAchievement(player,
						config.getString(categoryName + '.' + ach + ".Name", ""))) {
					// At least one achievement was received in the current category; it is unlocked,
					// can continue processing.
					hasReceivedInCategory = true;
					break;
				}
			}
		}

		ItemStack categoryItem;
		ItemMeta categoryMeta;

		if (hasReceivedInCategory) {
			// Create item stack that will be displayed in the GUI, with its category name.
			categoryItem = normalAchievementCategoryItems[indexInItemStacksArray];
			categoryMeta = categoryItem.getItemMeta();
			categoryMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
					"&8" + config.getString("ListAchievementFormat", "%ICON% %NAME% %ICON%")
							.replaceAll("%ICON%", plugin.getIcon()).replaceAll("%NAME%",
									normalAchievementCategoryNames[indexInItemStacksArray])));
		} else {
			// The player has not unlocked any achievements in the category: display barrier item with
			// message.
			categoryItem = new ItemStack(Material.BARRIER);
			categoryMeta = categoryItem.getItemMeta();
			categoryMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&8" + plugin.getPluginLang()
					.getString("list-category-not-unlocked", "You have not yet unlocked this category.")));
		}

		// Set item in the GUI.
		categoryItem.setItemMeta(categoryMeta);

		return categoryItem;
	}

	/**
	 * Check if player hasn't done a list command too recently (with "too recently" being defined in configuration
	 * file).
	 * 
	 * @param player
	 * @return whether a player is authorised to perform the list command
	 */
	private boolean timeAuthorisedList(Player player) {

		// Player bypasses cooldown if he has full plugin permissions.
		if (player.hasPermission("achievement.*") || listTime == 0)
			return true;
		long currentTime = System.currentTimeMillis();
		long lastListTime = 0;
		String uuid = player.getUniqueId().toString();
		if (players.containsKey(uuid))
			lastListTime = players.get(uuid);
		if (currentTime - lastListTime < listTime)
			return false;
		players.put(uuid, currentTime);
		return true;
	}

	/**
	 * Display a category GUI, containing all the achievements from a given category. This method is used for normal
	 * achievements.
	 * 
	 * @param clickedItem
	 * @param player
	 */
	public void createCategoryGUINormal(Material clickedItem, Player player) {

		NormalAchievements category;
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
			// Objects exclusive to Minecraft 1.9+ or Commands achievements.
			default:
				statistic = -1L;
				category = null;
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
					statistic = -1L;
					category = null;
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
		} else if (category != null) {
			statistic = plugin.getPoolsManager().getStatisticAmount(category, player);
		}

		YamlManager config = plugin.getPluginConfig();

		// Used to make the GUI as small as possible while still containing all achievements; limit to 99, above the GUI
		// is really messed up.
		int inventorySize = getClosestGreaterMultipleOf9(
				config.getConfigurationSection(categoryName).getKeys(false).size() + 1);

		// Create a new chest-like inventory.
		Inventory inventory = Bukkit.createInventory(null, inventorySize, ChatColor.translateAlternateColorCodes('&',
				plugin.getPluginLang().getString("list-gui-title", "&5&lAchievements List")));

		int positionInGUI = 0;

		String previousItemDate = null;
		Integer previousItemGoal = 0;
		// Populate the GUI with all of the achievements for the category.
		for (String ach : plugin.getPluginConfig().getConfigurationSection(categoryName).getKeys(false)) {

			// If the user specifies more than 98 achievements in the category do not display them.
			if (positionInGUI >= inventorySize - 1)
				break;

			// ach is the threshold for obtaining this achievement
			// Convert it to an integer
			Integer currentItemGoal = Ints.tryParse(ach);

			String achName = plugin.getPluginConfig().getString(categoryName + '.' + ach + ".Name", "");
			String displayName = plugin.getPluginConfig().getString(categoryName + '.' + ach + ".DisplayName", "");
			String nameToShowUser;

			if (Strings.isNullOrEmpty(displayName)) {
				// Use the achievement key name (this name is used in the achievements table in the database).
				nameToShowUser = achName;
			} else {
				// Display name is defined; use it.
				nameToShowUser = displayName;
			}

			String achMessage;
			String goal = config.getString(categoryName + '.' + ach + ".Goal", "");
			if (Strings.isNullOrEmpty(goal)) {
				// Show the achievement message below the achievement name.
				achMessage = config.getString(categoryName + '.' + ach + ".Message", "");
			} else {
				// Show the goal below the achievement name.
				achMessage = goal;
			}

			List<String> rewards = plugin.getReward().getRewardType(categoryName + '.' + ach);
			String date = plugin.getDb().getPlayerAchievementDate(player, achName);

			boolean inelligibleSeriesItem;

			if (statistic == -1L || positionInGUI == 0 || date != null || previousItemDate != null) {
				// Commands achievement or
				// first achievement in the category or
				// achievement has been completed or
				// previous achievement has been completed.
				inelligibleSeriesItem = false;
			} else {
				// Check whether this achievement cannot be completed until the previous one is completed.
				if (currentItemGoal > previousItemGoal)
					inelligibleSeriesItem = true;
				else
					inelligibleSeriesItem = false;
			}

			boolean playedTime = false;
			if (clickedItem == Material.WATCH)
				playedTime = true;

			createGUIItem(inventory, positionInGUI, ach, statistic, nameToShowUser, achMessage, rewards, date,
					inelligibleSeriesItem, playedTime);
			positionInGUI++;

			previousItemDate = date;
			previousItemGoal = currentItemGoal;
		}

		// Add "back button" item.
		ItemStack achItem = new ItemStack(Material.PAPER);
		ItemMeta connectionsMeta = achItem.getItemMeta();
		connectionsMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
				StringEscapeUtils.unescapeJava(plugin.getPluginLang().getString("list-back-message", "&7Back"))));
		achItem.setItemMeta(connectionsMeta);
		inventory.setItem(positionInGUI, achItem);

		// Display category GUI.
		player.openInventory(inventory);

	}

	/**
	 * Display the a category GUI, containing all the achievements from a given category. This method is used for
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
			// Default case cannot happen.
			default:
				category = null;
				break;
		}

		String categoryName = category.toString();

		YamlManager config = plugin.getPluginConfig();
		ConfigurationSection categoryConfig = config.getConfigurationSection(categoryName);

		int totalAchievementsInCategory = 0;

		// Retrieve total number of achievements in the sub-categories of the category.
		for (String section : categoryConfig.getKeys(false)) {
			ConfigurationSection subcategoryConfig = config.getConfigurationSection(categoryName + '.' + section);
			totalAchievementsInCategory += subcategoryConfig.getKeys(false).size();
		}

		// Used to make the GUI as small as possible while still containing all achievements; limit to 99, above the GUI
		// is really messed up.
		int inventorySize = getClosestGreaterMultipleOf9(totalAchievementsInCategory + 1);

		// Create a new chest-like inventory; make it as small as possible while still containing all achievements.
		Inventory inventory = Bukkit.createInventory(null, inventorySize, ChatColor.translateAlternateColorCodes('&',
				plugin.getPluginLang().getString("list-gui-title", "&5&lAchievements List")));

		int positionInGUI = 0;

		// Match the item the player clicked on with its database statistic.
		for (String section : categoryConfig.getKeys(false)) {

			// Retrieve statistic from category and subcategory.
			int statistic = plugin.getPoolsManager().getStatisticAmount(category, section, player);

			String previousItemDate = null;
			int previousItemGoal = 0;
			int subcategoryIndex = 0;

			// Populate GUI with all the achievements for the current sub-category.
			ConfigurationSection subcategoryConfig = config.getConfigurationSection(categoryName + '.' + section);
			for (String level : subcategoryConfig.getKeys(false)) {

				// If the user specifies more than 98 achievements in the category do not display them.
				if (positionInGUI >= inventorySize - 1)
					break;

				// level is the threshold for obtaining this achievement
				// Convert it to an integer.
				int currentItemGoal = Integer.parseInt(level);

				String achName = config.getString(categoryName + '.' + section + '.' + level + ".Name", "");
				String displayName = config.getString(categoryName + '.' + section + '.' + level + ".DisplayName", "");
				String nameToShowUser;

				if (Strings.isNullOrEmpty(displayName)) {
					// Use the achievement key name (this name is used in the achievements table in the database).
					nameToShowUser = achName;
				} else {
					// Display name is defined; use it.
					nameToShowUser = displayName;
				}

				String achMessage;
				String goal = config.getString(categoryName + '.' + section + '.' + level + ".Goal", "");
				if (Strings.isNullOrEmpty(goal)) {
					// Show the achievement message below the achievement name.
					achMessage = config.getString(categoryName + '.' + section + '.' + level + ".Message", "");
				} else {
					// Show the goal below the achievement name.
					achMessage = goal;
				}

				List<String> rewards = plugin.getReward().getRewardType(categoryName + '.' + section + '.' + level);
				String date = plugin.getDb().getPlayerAchievementDate(player, achName);

				boolean inelligibleSeriesItem;

				if (subcategoryIndex == 0 || date != null || previousItemDate != null) {
					// First achievement in the category or
					// achievement has been completed or
					// previous achievement has been completed.
					inelligibleSeriesItem = false;
				} else {
					// Check whether this achievement cannot be completed until the previous one is completed.
					if (currentItemGoal > previousItemGoal)
						inelligibleSeriesItem = true;
					else
						inelligibleSeriesItem = false;
				}

				createGUIItem(inventory, positionInGUI, level, statistic, nameToShowUser, achMessage, rewards, date,
						inelligibleSeriesItem, false);
				positionInGUI++;

				previousItemDate = date;
				previousItemGoal = currentItemGoal;
				subcategoryIndex++;
			}
		}

		// Add "back button" item.
		ItemStack achItem = new ItemStack(Material.PAPER);
		ItemMeta connectionsMeta = achItem.getItemMeta();
		connectionsMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
				StringEscapeUtils.unescapeJava(plugin.getPluginLang().getString("list-back-message", "&7Back"))));
		achItem.setItemMeta(connectionsMeta);
		inventory.setItem(positionInGUI, achItem);

		// Display category GUI.
		player.openInventory(inventory);
	}

	/**
	 * Create a GUI item for a given achievement.
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
	private void createGUIItem(Inventory inventory, int positionInGUI, String level, long statistic, String achName,
			String achMessage, List<String> rewards, String date, boolean inelligibleSeriesItem, boolean playedTime) {

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
		ItemMeta connectionsMeta = achItem.getItemMeta();
		if (date != null)
			connectionsMeta
					.setDisplayName(ChatColor.translateAlternateColorCodes('&',
							StringEscapeUtils.unescapeJava(
									plugin.getPluginLang().getString("list-achievement-received", "&a\u2713&f "))
									+ achName));
		else if (obfuscateNotReceived || (obfuscateProgressiveAchievements && inelligibleSeriesItem))
			connectionsMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
					StringEscapeUtils.unescapeJava(
							plugin.getPluginLang().getString("list-achievement-not-received", "&4\u2717&8 ")) + "&k"
							+ achName.replaceAll(REGEX_PATTERN.pattern(), "")));
		else
			connectionsMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
					StringEscapeUtils.unescapeJava(
							plugin.getPluginLang().getString("list-achievement-not-received", "&4\u2717&8 ") + "&o"
									+ achName.replaceAll(REGEX_PATTERN.pattern(), ""))));

		// Build the lore of the item.
		ArrayList<String> lore = buildLoreString(achMessage, level, rewards, date, statistic, inelligibleSeriesItem,
				playedTime);

		connectionsMeta.setLore(lore);
		achItem.setItemMeta(connectionsMeta);
		inventory.setItem(positionInGUI, achItem);
	}

	/**
	 * Create the lore for the current achievement, containing information about the progress, date of reception,
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

		// Set description of the achievement. The style depends whether it was received or not and whether the user has
		// set obfuscateNotReceived in the config.
		if (date != null)
			lore.add(ChatColor.translateAlternateColorCodes('&', "&r" + achMessage));
		else if (obfuscateNotReceived || (obfuscateProgressiveAchievements && inelligibleSeriesItem))
			lore.add(ChatColor.translateAlternateColorCodes('&',
					"&8&k" + achMessage.replaceAll(REGEX_PATTERN.pattern(), "")));
		else
			lore.add(ChatColor.translateAlternateColorCodes('&',
					"&8&o" + achMessage.replaceAll(REGEX_PATTERN.pattern(), "")));
		lore.add("");

		// Display date if the achievement was received, or progress bar otherwise; achievements with statistic -1
		// correspond to
		// Commands achievements, ignore if they weren't yet received.
		if (date != null) {
			lore.add(ChatColor.translateAlternateColorCodes('&', "&r" + date.replaceAll(REGEX_PATTERN.pattern(), "")));
			lore.add("");
		} else if (!obfuscateNotReceived && statistic >= 0) {
			StringBuilder barDisplay = new StringBuilder("&7[");
			// Length of the progress bar; we make it the same size as Goal/Message.
			int textSize;
			// MinecraftFont essentially supports latin alphabet characters. If invalid characters are found just use
			// number of chars.
			if (FONT.isValid(achMessage))
				textSize = FONT.getWidth(achMessage.replaceAll(REGEX_PATTERN.pattern(), ""));
			else
				textSize = (achMessage.replaceAll(REGEX_PATTERN.pattern(), "")).length() * 3;

			double statisticDouble;
			if (playedTime) {
				// Convert from millis to hours.
				statisticDouble = statistic / 3600000.0;
			} else {
				// Cast to double.
				statisticDouble = statistic;
			}

			for (int i = 1; i < textSize / 2; i++) {
				if (i < ((textSize / 2 - 1) * statisticDouble) / Integer.parseInt(level)) {
					barDisplay.append(plugin.getColor()).append('|');
				} else {
					barDisplay.append("&8|");
				}
			}

			barDisplay.append("&7]");
			lore.add(ChatColor.translateAlternateColorCodes('&', barDisplay.toString()));
			lore.add("");
		}

		// Add the rewards information.
		if (!rewards.isEmpty() && !hideRewardDisplay) {
			if (date != null) {
				lore.add(ChatColor.translateAlternateColorCodes('&',
						"&r" + plugin.getPluginLang().getString("list-reward", "Reward: ")));
				for (String reward : rewards)
					lore.add(ChatColor.translateAlternateColorCodes('&', "&r- " + reward));
			} else {
				lore.add(ChatColor.translateAlternateColorCodes('&',
						"&o&8" + plugin.getPluginLang().getString("list-reward", "Reward: ")));
				for (String reward : rewards)
					lore.add(ChatColor.translateAlternateColorCodes('&', "&o&8- " + reward));
			}
		}
		return lore;
	}

	/**
	 * Inventory GUIs need a number of slots that is a multiple of 9. This simple function gets the smallest multiple of
	 * 9 greater than its input value, in order for the GUI to contain all of its elements with minimum empty space.
	 * 
	 * @param value
	 * @return closest multiple of 9 greater than value
	 */
	private int getClosestGreaterMultipleOf9(int value) {

		int multipleOfNine = 9;
		while (multipleOfNine < value && multipleOfNine <= 99)
			multipleOfNine += 9;
		return multipleOfNine;
	}

	/**
	 * Retrieve cooldown structure.
	 * 
	 * @return list cooldown structure
	 */
	public Map<String, Long> getPlayers() {

		return players;
	}

}
