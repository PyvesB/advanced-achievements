package com.hm.achievement.listener.statistics;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.config.AchievementMap;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.utils.InventoryHelper;

/**
 * Listener class to deal with Crafts achievements.
 * 
 * @author Pyves
 *
 */
@Singleton
public class CraftsListener extends AbstractListener {

	@Inject
	public CraftsListener(@Named("main") YamlConfiguration mainConfig, AchievementMap achievementMap,
			CacheManager cacheManager) {
		super(MultipleAchievements.CRAFTS, mainConfig, achievementMap, cacheManager);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onCraftItem(CraftItemEvent event) {
		if (!(event.getWhoClicked() instanceof Player) || event.getAction() == InventoryAction.NOTHING
				|| event.getClick() == ClickType.NUMBER_KEY && event.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD
				|| isCraftingIngotFromBlock(event.getRecipe())) {
			return;
		}

		Player player = (Player) event.getWhoClicked();
		ItemStack item = event.getCurrentItem();
		String craftName = item.getType().name().toLowerCase();
		if (!player.hasPermission(category.toChildPermName(craftName))) {
			return;
		}

		Set<String> subcategories = new HashSet<>();
		addMatchingSubcategories(subcategories, craftName);

		int eventAmount = item.getAmount();
		if (event.isShiftClick()) {
			int maxAmount = event.getInventory().getMaxStackSize();
			ItemStack[] matrix = event.getInventory().getMatrix();
			for (ItemStack itemStack : matrix) {
				if (itemStack != null && itemStack.getType() != Material.AIR) {
					int itemStackAmount = itemStack.getAmount();
					if (itemStackAmount < maxAmount && itemStackAmount > 0) {
						maxAmount = itemStackAmount;
					}
				}
			}
			eventAmount *= maxAmount;
			eventAmount = Math.min(eventAmount, InventoryHelper.getAvailableSpace(player, item));
			if (eventAmount == 0) {
				return;
			}
		}

		updateStatisticAndAwardAchievementsIfAvailable(player, subcategories, eventAmount);
	}

	/**
	 * Metal blocks can be used for repeated crafts of ingots (e.g. iron block -> 9 iron ingots -> iron block -> ...).
	 * Detect and prevent this.
	 * 
	 * @param recipe
	 * @return true if the player is trying to craft ingots from a block of the same metal
	 */
	private boolean isCraftingIngotFromBlock(Recipe recipe) {
		Material ingredient = Material.AIR;
		if (recipe instanceof ShapelessRecipe) {
			ShapelessRecipe shapelessRecipe = (ShapelessRecipe) recipe;
			List<ItemStack> ingredientList = shapelessRecipe.getIngredientList();
			if (ingredientList.size() == 1) {
				ingredient = ingredientList.get(0).getType();
			}
		} else if (recipe instanceof ShapedRecipe) {
			ShapedRecipe shapedRecipe = (ShapedRecipe) recipe;
			Collection<ItemStack> ingredients = shapedRecipe.getIngredientMap().values();
			if (ingredients.size() == 1) {
				ingredient = ingredients.iterator().next().getType();
			}
		}
		return ingredient.name().endsWith("_BLOCK") && recipe.getResult().getType().name().endsWith("_INGOT");
	}
}
