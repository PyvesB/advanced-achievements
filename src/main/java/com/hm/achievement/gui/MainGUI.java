package com.hm.achievement.gui;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.lang.GuiLang;
import com.hm.achievement.lang.Lang;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Represents the main GUI, corresponding to all the different available categories and their names.
 *
 * @author Pyves
 */
public class MainGUI extends AbstractGUI {

	private static final int MAX_PER_PAGE = 54;

	private final ItemStack lockedItem;

	private boolean configHideNotReceivedCategories;
	private boolean configHideNoPermissionCategories;

	private String langListGUITitle;
	private String langListCategoryNotUnlocked;

	public MainGUI(AdvancedAchievements plugin) {
		super(plugin);

		lockedItem = new ItemStack(plugin.getServerVersion() < 8 ? Material.OBSIDIAN : Material.BARRIER);
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		configHideNotReceivedCategories = plugin.getPluginConfig().getBoolean("HideNotReceivedCategories", false);
		configHideNoPermissionCategories = plugin.getPluginConfig().getBoolean("HideNoPermissionCategories", false);

		langListGUITitle = translateColorCodes(Lang.get(GuiLang.GUI_TITLE, plugin));
		langListCategoryNotUnlocked = translateColorCodes("&8" + Lang.get(GuiLang.CATEGORY_NOT_UNLOCKED, plugin));

		ItemMeta itemMeta = lockedItem.getItemMeta();
		itemMeta.setDisplayName(langListCategoryNotUnlocked);
		lockedItem.setItemMeta(itemMeta);
	}

	/**
	 * Displays the main GUI to a player.
	 *
	 * @param player
	 */
	public void displayMainGUI(Player player) {
		int totalNonDisabledCategories = MultipleAchievements.values().length + NormalAchievements.values().length + 1
				- plugin.getDisabledCategorySet().size();
		Inventory mainGUI = Bukkit.createInventory(null, nextMultipleOf9(totalNonDisabledCategories, MAX_PER_PAGE),
				langListGUITitle);

		int displayedSoFar = 0;
		// Display Multiple categories in the GUI.
		for (MultipleAchievements category : MultipleAchievements.values()) {
			ItemStack item = multipleAchievementItems.get(category);
			if (shouldDisplayCategory(item, player, category.toString(), category.toPermName())) {
				displayMultipleCategory(item, mainGUI, player, category.toString(), displayedSoFar);
				++displayedSoFar;
			}
		}
		// Display Normal categories in the GUI.
		for (NormalAchievements category : NormalAchievements.values()) {
			ItemStack item = normalAchievementItems.get(category);
			if (shouldDisplayCategory(item, player, category.toString(), category.toPermName())) {
				displayNormalCategory(item, mainGUI, player, category.toString(), displayedSoFar);
				++displayedSoFar;
			}
		}
		// Display the Commands category.
		if (shouldDisplayCategory(commandsAchievementsItem, player, "Commands", null)) {
			displayNormalCategory(commandsAchievementsItem, mainGUI, player, "Commands", displayedSoFar);
		}

		// Display the main GUI to the player.
		player.openInventory(mainGUI);
	}

	/**
	 * Determines whether the category should be displayed in the GUI.
	 *
	 * @param item
	 * @param player
	 * @param category
	 * @param permName
	 * @return true if an item corresponding to the category should be added to the GUI
	 */
	private boolean shouldDisplayCategory(ItemStack item, Player player, String category, String permName) {
		// Hide category if an empty name is defined for it, if it's disabled or if the player is missing permissions.
		return item.getItemMeta().getDisplayName().length() > 0 && !plugin.getDisabledCategorySet().contains(category)
				&& (!configHideNoPermissionCategories || permName == null || player.hasPermission(permName));
	}

	/**
	 * Displays an item corresponding to a Multiple category, or a barrier if the category should be hidden.
	 *
	 * @param item
	 * @param gui
	 * @param player
	 * @param category
	 * @param position
	 */
	private void displayMultipleCategory(ItemStack item, Inventory gui, Player player, String category, int position) {
		for (String subcategory : plugin.getPluginConfig().getShallowKeys(category)) {
			if (!configHideNotReceivedCategories || hasReceivedInCategory(player, category + "." + subcategory)) {
				gui.setItem(position, item);
				return;
			}
		}
		gui.setItem(position, lockedItem);
	}

	/**
	 * Displays an item corresponding to a Normal category, or a barrier if the category should be hidden.
	 *
	 * @param item
	 * @param gui
	 * @param player
	 * @param category
	 * @param position
	 */
	private void displayNormalCategory(ItemStack item, Inventory gui, Player player, String category, int position) {
		if (configHideNotReceivedCategories && !hasReceivedInCategory(player, category)) {
			gui.setItem(position, lockedItem);
		} else {
			gui.setItem(position, item);
		}
	}

	/**
	 * Determines whether the player has received at least one achievement in the category or subcategory.
	 *
	 * @param player
	 * @param configPath
	 * @return true if the player has received at least one achievement in the category, false otherwise
	 */
	private boolean hasReceivedInCategory(Player player, String configPath) {
		for (String threshold : plugin.getPluginConfig().getShallowKeys(configPath)) {
			if (plugin.getCacheManager().hasPlayerAchievement(player.getUniqueId(),
					plugin.getPluginConfig().getString(configPath + '.' + threshold + ".Name", ""))) {
				// At least one achievement was received in the current category: it is unlocked.
				return true;
			}
		}
		return false;
	}
}
