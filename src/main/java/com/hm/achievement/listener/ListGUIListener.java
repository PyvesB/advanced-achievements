package com.hm.achievement.listener;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;

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

	private String langListGUITitle;

	public ListGUIListener(AdvancedAchievements plugin) {
		super(plugin);
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		langListGUITitle = ChatColor.translateAlternateColorCodes('&',
				plugin.getPluginLang().getString("list-gui-title", "&5&lAchievements List"));
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent event) {
		if (!event.getInventory().getName().startsWith(langListGUITitle) || event.getRawSlot() < 0) {
			return;
		}

		// Prevent players from taking items out of the GUI.
		event.setCancelled(true);

		// Back button; display main GUI again.
		if (event.getCurrentItem().getType() == Material.PAPER) {
			plugin.getAchievementListCommand().executeCommand(event.getWhoClicked(), null, "list");
		}

		// GUI corresponding to the achievement listing of a given category. Do not let the player interact with it.
		if (event.getInventory().getItem(0).getType() == Material.STAINED_CLAY) {
			return;
		}

		// Main GUI displaying all the categories. Do not let players interact with locked categories or slots not
		// corresponding to a category item.
		if (event.getCurrentItem().getType() == Material.BARRIER || event.getCurrentItem().getType() == Material.BEDROCK
				|| event.getRawSlot() > NormalAchievements.values().length + MultipleAchievements.values().length
						- plugin.getDisabledCategorySet().size()) {
			return;
		}

		plugin.getAchievementListCommand().createCategoryGUI(event.getCurrentItem(), (Player) event.getWhoClicked());
	}
}
