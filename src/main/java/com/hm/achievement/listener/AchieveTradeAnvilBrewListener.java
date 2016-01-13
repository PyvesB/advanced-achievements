package com.hm.achievement.listener;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.language.Lang;

public class AchieveTradeAnvilBrewListener implements Listener {

	private AdvancedAchievements plugin;

	public AchieveTradeAnvilBrewListener(AdvancedAchievements plugin) {

		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onInventoryClickEvent(InventoryClickEvent event) {

		// Not relevant for trade, anvil, or brewing events, but used for the
		// /aach list command to avoid adding an additional event handler.
		if (event.getInventory().getName().equals(Lang.LIST_GUI_TITLE.toString())) {
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
			int trades = plugin.getDb().incrementAndGetNormalAchievement(player, "trades");
			configAchievement = "Trades." + trades;

		} else if (player.hasPermission("achievement.count.anvilsused") && event.getRawSlot() == 2
				&& event.getInventory().getType().name().equals("ANVIL")) {
			int anvils = plugin.getDb().incrementAndGetNormalAchievement(player, "anvils");
			configAchievement = "AnvilsUsed." + anvils;

		} else if (player.hasPermission("achievement.count.brewing")
				&& event.getInventory().getType().name().equals("BREWING")) {
			int brewings = plugin.getDb().incrementAndGetNormalAchievement(player, "brewing");
			configAchievement = "Brewing." + brewings;

		} else
			return;

		if (plugin.getReward().checkAchievement(configAchievement)) {

			plugin.getAchievementDisplay().displayAchievement(player, configAchievement);
			plugin.getDb().registerAchievement(player, plugin.getConfig().getString(configAchievement + ".Name"),
					plugin.getConfig().getString(configAchievement + ".Message"));

			plugin.getReward().checkConfig(player, configAchievement);
		}
	}
}
