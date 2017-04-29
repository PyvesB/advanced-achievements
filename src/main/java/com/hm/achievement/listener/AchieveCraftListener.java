package com.hm.achievement.listener;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.MultipleAchievements;

/**
 * Listener class to deal with Crafts achievements.
 * 
 * @author Pyves
 *
 */
public class AchieveCraftListener extends AbstractListener {

	public AchieveCraftListener(AdvancedAchievements plugin) {
		super(plugin);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onCraftItem(CraftItemEvent event) {
		if (!(event.getWhoClicked() instanceof Player) || event.getAction() == InventoryAction.NOTHING) {
			return;
		}

		Player player = (Player) event.getWhoClicked();
		if (!shouldEventBeTakenIntoAccountNoPermission(player)
				|| event.isShiftClick() && player.getInventory().firstEmpty() < 0) {
			return;
		}

		MultipleAchievements category = MultipleAchievements.CRAFTS;

		ItemStack item = event.getRecipe().getResult();
		String craftName = item.getType().name().toLowerCase();
		if (!player.hasPermission(category.toPermName() + '.' + craftName)) {
			return;
		}
		if (plugin.getPluginConfig().isConfigurationSection(category + "." + craftName + ':' + item.getDurability())) {
			craftName += ":" + item.getDurability();
		} else if (!plugin.getPluginConfig().isConfigurationSection(category + "." + craftName)) {
			return;
		}

		// Some items appear as having amount equal to 0, regardless of the amount crafted (for instance fireworks).
		// Probably a bug in Spigot.
		int eventAmount = item.getAmount() > 0 ? item.getAmount() : 1;
		if (event.isShiftClick()) {
			int max = event.getInventory().getMaxStackSize();
			ItemStack[] matrix = event.getInventory().getMatrix();
			for (ItemStack itemStack : matrix) {
				if (itemStack != null && itemStack.getType() != Material.AIR) {
					int itemStackAmount = itemStack.getAmount();
					if (itemStackAmount < max && itemStackAmount > 0) {
						max = itemStackAmount;
					}
				}
			}
			eventAmount *= max;
		}

		updateStatisticAndAwardAchievementsIfAvailable(player, category, craftName, eventAmount);
	}
}
