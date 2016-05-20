package com.hm.achievement.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.hm.achievement.AdvancedAchievements;

public class ListCommand {

	private AdvancedAchievements plugin;
	private boolean hideNotReceivedCategories;
	private boolean obfuscateNotReceived;
	private boolean hideRewardDisplay;
	private String itemSeparator;
	private int listTime;

	// Corresponds to times at which players have entered list commands.
	private HashMap<Player, Long> players;

	// Messages for language file.
	private String[] normalAchievementTypesLanguage;
	private String[] multipleAchievementTypesLanguage;

	// Get lists of item stacks for items displayed in the GUI.
	private ItemStack[] multipleAchievementsTypesItems;
	private ItemStack[] normalAchievementsTypesItems;

	// Pattern to delete colors if achievement not yet received.
	private static final Pattern REGEX_PATTERN = Pattern.compile("&([a-f]|[0-9]){1}");

	public ListCommand(AdvancedAchievements plugin) {

		this.plugin = plugin;
		players = new HashMap<Player, Long>();
		hideNotReceivedCategories = plugin.getPluginConfig().getBoolean("HideNotReceivedCategories", false);
		obfuscateNotReceived = plugin.getPluginConfig().getBoolean("ObfuscateNotReceived", true);
		hideRewardDisplay = plugin.getPluginConfig().getBoolean("HideRewardDisplayInList", false);
		itemSeparator = StringEscapeUtils
				.unescapeJava(plugin.getPluginConfig().getString("ListItemSeparator", " \u2192 "));
		listTime = plugin.getPluginConfig().getInt("TimeList", 0) * 1000;

		normalAchievementTypesLanguage = new String[] {
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

		multipleAchievementTypesLanguage = new String[] {
				plugin.getPluginLang().getString("list-places", "Blocks Placed"),
				plugin.getPluginLang().getString("list-breaks", "Blocks Broken"),
				plugin.getPluginLang().getString("list-kills", "Entities Killed"),
				plugin.getPluginLang().getString("list-crafts", "Items Crafted") };

		// Get lists of item stacks for items displayed in the GUI.
		multipleAchievementsTypesItems = new ItemStack[] { new ItemStack(Material.SMOOTH_BRICK),
				new ItemStack(Material.SMOOTH_BRICK, 1, (short) 2), new ItemStack(Material.BONE),
				new ItemStack(Material.WORKBENCH) };

		// Simple and fast check to compare versions. Might need to
		// be updated in the future depending on how the Minecraft
		// versions change in the future.
		int version = Integer.valueOf(Bukkit.getBukkitVersion().charAt(2) + "");

		if (version >= 9)
			normalAchievementsTypesItems = new ItemStack[] { new ItemStack(Material.BOOK_AND_QUILL),
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
			normalAchievementsTypesItems = new ItemStack[] { new ItemStack(Material.BOOK_AND_QUILL),
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

	/**
	 * Check is player hasn't done a list command too recently (with
	 * "too recently" being defined in configuration file).
	 */
	private boolean timeAuthorisedList(Player player) {

		if (player.hasPermission("achievement.*") || listTime == 0)
			return true;
		long currentTime = System.currentTimeMillis();
		long lastListTime = 0;
		if (players.containsKey(player))
			lastListTime = players.get(player);
		if (currentTime - lastListTime < listTime)
			return false;
		players.put(player, currentTime);
		return true;

	}

	/**
	 * Display name of received achievements and name of missing achievements
	 * (goes through the entire config file) in a GUI. Does not display category
	 * is language file string is empty.
	 */
	public void getList(Player player) {

		if (timeAuthorisedList(player)) {

			// Create a new chest-like inventory.
			Inventory guiInv = Bukkit.createInventory(null, 36, ChatColor.translateAlternateColorCodes('&',
					plugin.getPluginLang().getString("list-gui-title", "&5&lAchievements List")));

			// Number of achievements in current category.
			int numberInCategory = 0;
			// Total number of categories displayed.
			int numberOfCategories = 0;

			// Build list of achievements with multiple sub-categories in GUI.
			for (int i = 0; i < AdvancedAchievements.MULTIPLE_ACHIEVEMENTS.length; i++) {
				if (multipleAchievementTypesLanguage[i].length() != 0) {
					// Create item stack that will be displayed in the GUI.
					ItemStack connections = multipleAchievementsTypesItems[i];
					ItemMeta connectionsMeta = connections.getItemMeta();
					ArrayList<String> lore = new ArrayList<String>();
					// Iterate through all sub-categories in achievement
					// category
					String cat = AdvancedAchievements.MULTIPLE_ACHIEVEMENTS[i];

					// Ignore this category if it's in the disabled list.
					if (plugin.getDisabledCategorySet().contains(cat)) {
						continue;
					}

					for (String section : plugin.getPluginConfig().getConfigurationSection(cat).getKeys(false)) {
						// Iterate through all achievements in sub-category.
						for (String ach : plugin.getPluginConfig().getConfigurationSection(cat + '.' + section)
								.getKeys(false)) {
							// Check if player has received achievement and
							// build message accordingly.
							String achname = plugin.getPluginConfig()
									.getString(cat + '.' + section + '.' + ach + ".Name", "");
							String reward = plugin.getReward().getRewardType(cat + '.' + section + '.' + ach);
							if (plugin.getDb().hasPlayerAchievement(player, achname)) {
								numberInCategory++;
								lore.add(buildLoreString(achname, ach, reward, i, true));
							} else
								lore.add(buildLoreString(achname, ach, reward, i, false));
						}
					}
					// Set lore for the current category item in GUI.
					if (lore.size() > 0 && (numberInCategory != 0 || !hideNotReceivedCategories)) {
						connectionsMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
								"&8" + plugin.getPluginConfig()
										.getString("ListAchievementFormat", "%ICON% %NAME% %ICON%")
										.replaceAll("%ICON%", plugin.getIcon())
										.replaceAll("%NAME%", multipleAchievementTypesLanguage[i])));
						connectionsMeta.setLore(lore);
						connections.setItemMeta(connectionsMeta);

						guiInv.setItem(numberOfCategories, connections);
						numberOfCategories++;

					}
					lore.clear();
					numberInCategory = 0;
				}
			}

			// Build list of normal achievements in GUI.
			for (int i = 0; i < AdvancedAchievements.NORMAL_ACHIEVEMENTS.length; i++) {
				if (normalAchievementTypesLanguage[i].length() != 0) {
					// Create item stack that will be displayed in the GUI.
					ItemStack connections = normalAchievementsTypesItems[i];
					ItemMeta connectionsMeta = connections.getItemMeta();
					ArrayList<String> lore = new ArrayList<String>();
					// Iterate through all achievements in category.
					String cat = AdvancedAchievements.NORMAL_ACHIEVEMENTS[i];

					// Ignore this category if it's in the disabled list
					List<String> disabled = plugin.getConfig().getStringList("DisabledCategories");
					if ((disabled != null) && (disabled.contains(cat))) {
						continue;
					}

					for (String ach : plugin.getPluginConfig().getConfigurationSection(cat).getKeys(false)) {
						// Check if player has received achievement and
						// build message accordingly.
						String achname = plugin.getPluginConfig().getString(cat + '.' + ach + ".Name", "");
						String reward = plugin.getReward().getRewardType(cat + '.' + ach);
						if (plugin.getDb().hasPlayerAchievement(player, achname)) {
							numberInCategory++;
							lore.add(buildLoreString(achname, ach, reward, i, true));
						} else
							lore.add(buildLoreString(achname, ach, reward, i, false));
					}
					// Set lore for the current category item in GUI.
					if (lore.size() > 0 && (numberInCategory != 0 || !hideNotReceivedCategories)) {
						connectionsMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
								"&8" + plugin.getPluginConfig()
										.getString("ListAchievementFormat", "%ICON% %NAME% %ICON%")
										.replaceAll("%ICON%", plugin.getIcon())
										.replaceAll("%NAME%", normalAchievementTypesLanguage[i])));
						connectionsMeta.setLore(lore);
						connections.setItemMeta(connectionsMeta);

						guiInv.setItem(numberOfCategories, connections);
						numberOfCategories++;

					}
					lore.clear();
					numberInCategory = 0;
				}
			}
			// Display GUI to the player.
			player.openInventory(guiInv);
		} else {
			// The player has already done a list command recently.
			player.sendMessage(plugin.getChatHeader() + plugin.getPluginLang()
					.getString("list-delay", "You must wait TIME seconds between each list command!")
					.replace("TIME", "" + listTime / 1000));
		}
	}

	/**
	 * Create achievement line for item lore.
	 */
	public String buildLoreString(String name, String level, String reward, int number, boolean received) {

		StringBuilder lore = new StringBuilder(100);

		// Set achievement text color and style based on whether the achievement
		// has been received
		if (received)
			lore.append(StringEscapeUtils
					.unescapeJava(plugin.getPluginLang().getString("list-achievement-received", "&a\u2713&f ")));
		else if (obfuscateNotReceived)
			lore.append(StringEscapeUtils
					.unescapeJava(plugin.getPluginLang().getString("list-achievement-not-received", "&4\u2717&8 ")))
					.append("&k");
		else
			lore.append(StringEscapeUtils
					.unescapeJava(plugin.getPluginLang().getString("list-achievement-not-received", "&4\u2717&8 ")))
					.append("&o");

		// Apply regex pattern if not received: get rid of coulours defined by
		// the user if achievement not yet received.
		if (!received)
			lore.append(name.replaceAll(REGEX_PATTERN.pattern(), ""));
		else
			lore.append(name);

		// For achievements with levels (not command achievements)
		if (number != (normalAchievementTypesLanguage.length - 1)) {
			lore.append(itemSeparator);
			lore.append(plugin.getPluginLang().getString("list-amount", "Lvl: "));
			lore.append(level);
		}

		// Add the reward
		if (!reward.isEmpty() && !hideRewardDisplay) {
			lore.append(itemSeparator);
			lore.append(plugin.getPluginLang().getString("list-reward", "Reward: "));
			lore.append(reward);
		}

		return ChatColor.translateAlternateColorCodes('&', lore.toString());
	}

	public HashMap<Player, Long> getPlayers() {

		return players;
	}

}
