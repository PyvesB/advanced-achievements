package com.hm.achievement.gui;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.hm.achievement.category.Category;
import com.hm.achievement.category.CommandAchievements;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.lang.GuiLang;
import com.hm.achievement.lang.LangHelper;
import com.hm.achievement.utils.MaterialHelper;
import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Represents the main GUI, corresponding to all the different available categories and their names.
 *
 * @author Pyves
 */
@Singleton
public class MainGUI extends AbstractGUI {

	private static final int MAX_PER_PAGE = 54;

	private final Set<Category> disabledCategories;
	private final ItemStack lockedItem;

	private boolean configHideNotReceivedCategories;
	private boolean configHideNoPermissionCategories;

	private String langListGUITitle;

	@Inject
	public MainGUI(@Named("main") CommentedYamlConfiguration mainConfig,
			@Named("lang") CommentedYamlConfiguration langConfig, @Named("gui") CommentedYamlConfiguration guiConfig,
			CacheManager cacheManager, int serverVersion, Set<Category> disabledCategories, MaterialHelper materialHelper) {
		super(mainConfig, langConfig, guiConfig, cacheManager, materialHelper);
		this.disabledCategories = disabledCategories;
		lockedItem = new ItemStack(serverVersion < 8 ? Material.OBSIDIAN : Material.BARRIER);
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		configHideNotReceivedCategories = mainConfig.getBoolean("HideNotReceivedCategories", false);
		configHideNoPermissionCategories = mainConfig.getBoolean("HideNoPermissionCategories", false);

		langListGUITitle = translateColorCodes(LangHelper.get(GuiLang.GUI_TITLE, langConfig));

		ItemMeta itemMeta = lockedItem.getItemMeta();
		itemMeta.setDisplayName(translateColorCodes("&8" + LangHelper.get(GuiLang.CATEGORY_NOT_UNLOCKED, langConfig)));
		lockedItem.setItemMeta(itemMeta);
	}

	/**
	 * Displays the main GUI to a player.
	 *
	 * @param player
	 */
	public void displayMainGUI(Player player) {
		int totalEnabledCategories = MultipleAchievements.values().length + NormalAchievements.values().length + 1
				- disabledCategories.size();
		AchievementInventoryHolder inventoryHolder = new AchievementInventoryHolder();
		Inventory mainGUI = Bukkit.createInventory(inventoryHolder, nextMultipleOf9(totalEnabledCategories, MAX_PER_PAGE),
				langListGUITitle);
		inventoryHolder.setInventory(mainGUI);

		int displayedSoFar = 0;
		// Display Multiple categories in the GUI.
		for (MultipleAchievements category : MultipleAchievements.values()) {
			ItemStack item = multipleAchievementItems.get(category);
			if (shouldDisplayCategory(item, player, category)) {
				displayMultipleCategory(item, mainGUI, player, category.toString(), displayedSoFar);
				++displayedSoFar;
			}
		}
		// Display Normal categories in the GUI.
		for (NormalAchievements category : NormalAchievements.values()) {
			ItemStack item = normalAchievementItems.get(category);
			if (shouldDisplayCategory(item, player, category)) {
				displayNormalCategory(item, mainGUI, player, category.toString(), displayedSoFar);
				++displayedSoFar;
			}
		}
		// Display the Commands category.
		if (shouldDisplayCategory(commandsAchievementsItem, player, CommandAchievements.COMMANDS)) {
			displayNormalCategory(commandsAchievementsItem, mainGUI, player, CommandAchievements.COMMANDS.toString(),
					displayedSoFar);
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
	 * @return true if an item corresponding to the category should be added to the GUI
	 */
	private boolean shouldDisplayCategory(ItemStack item, Player player, Category category) {
		// Hide category if an empty name is defined for it, if it's disabled or if the player is missing permissions.
		return item.getItemMeta().getDisplayName().length() > 0 && !disabledCategories.contains(category)
				&& (!configHideNoPermissionCategories || player.hasPermission(category.toPermName()));
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
		for (String subcategory : mainConfig.getShallowKeys(category)) {
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
		for (String threshold : mainConfig.getShallowKeys(configPath)) {
			if (cacheManager.hasPlayerAchievement(player.getUniqueId(),
					mainConfig.getString(configPath + '.' + threshold + ".Name", ""))) {
				// At least one achievement was received in the current category: it is unlocked.
				return true;
			}
		}
		return false;
	}
}
