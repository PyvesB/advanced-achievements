package com.hm.achievement.listener;

import static com.hm.achievement.gui.AchievementInventoryHolder.MAIN_GUI_PAGE;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.hm.achievement.category.Category;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.gui.AchievementInventoryHolder;
import com.hm.achievement.gui.CategoryGUI;
import com.hm.achievement.gui.GUIItems;
import com.hm.achievement.gui.MainGUI;

/**
 * Listener class to deal with the GUIs from the /aach list command.
 *
 * @author Pyves
 */
@Singleton
public class ListGUIListener implements Listener {

	private final Set<Category> disabledCategories;
	private final MainGUI mainGUI;
	private final CategoryGUI categoryGUI;
	private final GUIItems guiItems;

	@Inject
	public ListGUIListener(Set<Category> disabledCategories, MainGUI mainGUI, CategoryGUI categoryGUI, GUIItems guiItems) {
		this.disabledCategories = disabledCategories;
		this.mainGUI = mainGUI;
		this.categoryGUI = categoryGUI;
		this.guiItems = guiItems;
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent event) {
		Inventory inventory = event.getInventory();
		if (!(inventory.getHolder() instanceof AchievementInventoryHolder) || event.getRawSlot() < 0) {
			return;
		}

		// Prevent players from taking items out of the GUI.
		event.setCancelled(true);

		// Clicking empty slots should do nothing
		if (event.getCurrentItem() == null) {
			return;
		}

		int currentPage = ((AchievementInventoryHolder) inventory.getHolder()).getPageIndex();
		Player player = (Player) event.getWhoClicked();
		if (currentPage == MAIN_GUI_PAGE) {
			// Main GUI, check whether player can interact with the selected item.
			if (!event.getCurrentItem().isSimilar(guiItems.getCategoryLock())
					&& event.getRawSlot() < getMainGUIItemCount()) {
				categoryGUI.displayCategoryGUI(event.getCurrentItem(), player, 0);
			}
			return;
		}

		ItemStack categoryItem = inventory.getItem(0);
		// Check whether a navigation button was clicked in a category GUI.
		if (isButtonClicked(event, guiItems.getBackButton())) {
			mainGUI.displayMainGUI(player);
		} else if (isButtonClicked(event, guiItems.getPreviousButton())) {
			categoryGUI.displayCategoryGUI(categoryItem, player, currentPage - 1);
		} else if (isButtonClicked(event, guiItems.getNextButton())) {
			categoryGUI.displayCategoryGUI(categoryItem, player, currentPage + 1);
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
		if (event.getCurrentItem().isSimilar(button)) {
			// Clicked item seems to be the button. But player could have clicked on item in his personal inventory that
			// matches the properties of the button used by Advanced Achievements. The first item matching the
			// properties of the button is the real one, check that this is indeed the clicked one.
			Map<Integer, ItemStack> backButtonCandidates = new TreeMap<>(
					event.getInventory().all(event.getCurrentItem().getType()));
			for (Entry<Integer, ItemStack> entry : backButtonCandidates.entrySet()) {
				if (event.getCurrentItem().isSimilar(entry.getValue())) {
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
	 * Returns the number of items in the main GUI.
	 *
	 * @return the count of non disabled categories
	 */
	private int getMainGUIItemCount() {
		return NormalAchievements.values().length + MultipleAchievements.values().length - disabledCategories.size() + 1;
	}
}
