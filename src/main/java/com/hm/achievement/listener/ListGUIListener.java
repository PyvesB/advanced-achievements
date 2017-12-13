package com.hm.achievement.listener;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;

/**
 * Listener class to deal with the GUIs from the /aach list command.
 * 
 * @author Pyves
 *
 */
public class ListGUIListener extends AbstractListener {

	private static final int MAIN_GUI_PAGE = 0;

	private final Material lockedMaterial;

	private String langListGUITitle;

	public ListGUIListener(AdvancedAchievements plugin) {
		super(plugin);

		lockedMaterial = version < 8 ? Material.OBSIDIAN : Material.BARRIER;
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		langListGUITitle = ChatColor.translateAlternateColorCodes('&',
				plugin.getPluginLang().getString("list-gui-title", "&5&lAchievements List"));
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent event) {
		String inventoryName = event.getInventory().getName();
		if (!inventoryName.startsWith(langListGUITitle) || event.getRawSlot() < 0) {
			return;
		}

		// Prevent players from taking items out of the GUI.
		event.setCancelled(true);

		int page = getCurrentCategoryPage(inventoryName);
		Player player = (Player) event.getWhoClicked();
		if (page == MAIN_GUI_PAGE) {
			// Main GUI, check whether player can interact with the selected item.
			if (event.getCurrentItem().getType() != lockedMaterial && event.getRawSlot() < getMainGUIItemCount()) {
				plugin.getCategoryGUI().displayCategoryGUI(event.getCurrentItem(), player, 1);
			}
			return;
		}

		ItemStack categoryItem = event.getInventory().getItem(0);
		// Check whether a navigation button was clicked in a category GUI.
		if (isButtonClicked(event, plugin.getCategoryGUI().getBackButton())) {
			plugin.getMainGUI().displayMainGUI(player);
		} else if (isButtonClicked(event, plugin.getCategoryGUI().getPreviousButton())) {
			plugin.getCategoryGUI().displayCategoryGUI(categoryItem, player, page - 1);
		} else if (isButtonClicked(event, plugin.getCategoryGUI().getNextButton())) {
			plugin.getCategoryGUI().displayCategoryGUI(categoryItem, player, page + 1);
		}
	}

	/**
	 * Verifies whether the user has clicked on the given navigation button.
	 * 
	 * @param event
	 * @param button
	 * @return true if the button is clicked, false otherwise
	 */
	private boolean isButtonClicked(InventoryClickEvent event, ItemStack button) {
		if (event.getCurrentItem().getDurability() == button.getDurability()
				&& event.getCurrentItem().getType() == button.getType()) {
			// Clicked item seems to be the button. But player could have clicked on item in his personal inventory that
			// matches the properties of the button used by Advanced Achievements. The first item matching the
			// properties of the button is the real one, check that this is indeed the clicked one.
			Map<Integer, ItemStack> backButtonCandidates = new TreeMap<>(
					event.getInventory().all(event.getCurrentItem().getType()));
			for (Entry<Integer, ItemStack> entry : backButtonCandidates.entrySet()) {
				if (entry.getValue().getDurability() == event.getCurrentItem().getDurability()) {
					// Found real button. Did the player click on it?
					if (entry.getKey() == event.getRawSlot()) {
						return true;
					}
					break;
				}
			}
		}
		return false;
	}

	/**
	 * Gets the current page index, by parsing the inventory title.
	 * 
	 * @param name
	 * @return the current page number (start index is 1)
	 */
	private int getCurrentCategoryPage(String name) {
		String pageNumber = StringUtils.replaceOnce(name, langListGUITitle + " ", "");
		if (StringUtils.isNumeric(pageNumber)) {
			return Integer.parseInt(pageNumber);
		}
		return MAIN_GUI_PAGE;
	}

	/**
	 * Returns the number of items in the main GUI.
	 * 
	 * @return the count of non disabled categories
	 */
	private int getMainGUIItemCount() {
		return NormalAchievements.values().length + MultipleAchievements.values().length
				- plugin.getDisabledCategorySet().size() + 1;
	}
}
