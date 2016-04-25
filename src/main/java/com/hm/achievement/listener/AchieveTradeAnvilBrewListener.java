package com.hm.achievement.listener;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.db.DatabasePools;

public class AchieveTradeAnvilBrewListener implements Listener {

	private AdvancedAchievements plugin;

	public AchieveTradeAnvilBrewListener(AdvancedAchievements plugin) {

		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent event) {

		// Not relevant for trade, anvil, or brewing events, but used for the
		// /aach list command to avoid adding an additional event handler.
		if (event.getInventory().getName()
				.equals(plugin.getPluginLang().getString("list-gui-title", "&5§lAchievements List"))) {
			event.setCancelled(true);
			return;
		}

		if (event.getRawSlot() != 0 && event.getRawSlot() != 1 && event.getRawSlot() != 2
				|| event.getCurrentItem().getType().name().equals("AIR"))
			return;

		Player player = (Player) event.getWhoClicked();
		if (plugin.isRestrictCreative() && player.getGameMode() == GameMode.CREATIVE || plugin.isInExludedWorld(player))
			return;

		String configAchievement;

		if (player.hasPermission("achievement.count.trades") && event.getRawSlot() == 2
				&& event.getInventory().getType().name().equals("MERCHANT")) {
			int trades;
			if (!DatabasePools.getTradeHashMap().containsKey(player.getUniqueId().toString()))
				trades = plugin.getDb().getNormalAchievementAmount(player, "trades") + 1;
			else
				trades = DatabasePools.getTradeHashMap().get(player.getUniqueId().toString()) + 1;

			DatabasePools.getTradeHashMap().put(player.getUniqueId().toString(), trades);
			configAchievement = "Trades." + trades;

		} else if (player.hasPermission("achievement.count.anvilsused") && event.getRawSlot() == 2
				&& event.getInventory().getType().name().equals("ANVIL")) {
			int anvils;
			if (!DatabasePools.getAnvilHashMap().containsKey(player.getUniqueId().toString()))
				anvils = plugin.getDb().getNormalAchievementAmount(player, "anvils") + 1;
			else
				anvils = DatabasePools.getAnvilHashMap().get(player.getUniqueId().toString()) + 1;

			DatabasePools.getAnvilHashMap().put(player.getUniqueId().toString(), anvils);
			configAchievement = "AnvilsUsed." + anvils;

		} else if (player.hasPermission("achievement.count.brewing")
				&& event.getInventory().getType().name().equals("BREWING")) {
			int brewings;
			if (!DatabasePools.getBrewingHashMap().containsKey(player.getUniqueId().toString()))
				brewings = plugin.getDb().getNormalAchievementAmount(player, "brewing") + 1;
			else
				brewings = DatabasePools.getBrewingHashMap().get(player.getUniqueId().toString()) + 1;

			DatabasePools.getBrewingHashMap().put(player.getUniqueId().toString(), brewings);
			configAchievement = "Brewing." + brewings;

		} else
			return;

		if (plugin.getReward().checkAchievement(configAchievement)) {

			plugin.getAchievementDisplay().displayAchievement(player, configAchievement);
			plugin.getDb().registerAchievement(player, plugin.getPluginConfig().getString(configAchievement + ".Name"),
					plugin.getPluginConfig().getString(configAchievement + ".Message"));

			plugin.getReward().checkConfig(player, configAchievement);
		}
	}
}
