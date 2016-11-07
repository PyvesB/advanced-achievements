package com.hm.achievement.listener;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
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
public class AchieveCraftListener implements Listener {

	private AdvancedAchievements plugin;

	public AchieveCraftListener(AdvancedAchievements plugin) {

		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onCraftItem(CraftItemEvent event) {

		if (!(event.getWhoClicked() instanceof Player) || event.getAction() == InventoryAction.NOTHING)
			return;

		Player player = (Player) event.getWhoClicked();
		if (plugin.isRestrictCreative() && player.getGameMode() == GameMode.CREATIVE || plugin.isInExludedWorld(player)
				|| event.isShiftClick() && player.getInventory().firstEmpty() < 0)
			return;

		ItemStack item = event.getRecipe().getResult();
		String craftName = item.getType().name().toLowerCase();
		if (player.hasPermission("achievement.count.crafts." + craftName + "." + item.getDurability())
				&& plugin.getPluginConfig().isConfigurationSection(
						MultipleAchievements.CRAFTS + "." + craftName + ":" + item.getDurability()))
			craftName += ":" + item.getDurability();
		else {
			if (!player.hasPermission("achievement.count.crafts." + craftName))
				return;
			if (!plugin.getPluginConfig().isConfigurationSection(MultipleAchievements.CRAFTS + "." + craftName))
				return;
		}

		int amount = item.getAmount();
		if (event.isShiftClick()) {
			int max = event.getInventory().getMaxStackSize();
			ItemStack[] matrix = event.getInventory().getMatrix();
			for (ItemStack itemStack : matrix) {
				if (itemStack != null && itemStack.getType() != Material.AIR) {
					int tmp = itemStack.getAmount();
					if (tmp < max && tmp > 0)
						max = tmp;
				}
			}
			amount *= max;
		}

		int times = plugin.getPoolsManager().getPlayerCraftAmount(player, craftName) + amount;

		plugin.getPoolsManager().getCraftHashMap().put(player.getUniqueId().toString() + craftName, times);

		String configAchievement;
		for (String threshold : plugin.getPluginConfig()
				.getConfigurationSection(MultipleAchievements.CRAFTS + "." + craftName).getKeys(false))
			if (times >= Integer.parseInt(threshold)
					&& !plugin.getDb().hasPlayerAchievement(player, plugin.getPluginConfig().getString(
							MultipleAchievements.CRAFTS + "." + craftName + '.' + threshold + '.' + "Name"))) {
				configAchievement = MultipleAchievements.CRAFTS + "." + craftName + '.' + threshold;
				if (plugin.getPluginConfig().getString(configAchievement + ".Message", null) != null) {

					plugin.getAchievementDisplay().displayAchievement(player, configAchievement);
					plugin.getDb().registerAchievement(player,
							plugin.getPluginConfig().getString(configAchievement + ".Name"),
							plugin.getPluginConfig().getString(configAchievement + ".Message"));
					plugin.getReward().checkConfig(player, configAchievement);

				}
			}
	}
}
