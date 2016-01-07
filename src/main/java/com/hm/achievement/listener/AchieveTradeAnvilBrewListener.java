package com.hm.achievement.listener;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.hm.achievement.event.PlayerAchievementEvent;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import com.hm.achievement.AdvancedAchievements;

public class AchieveTradeAnvilBrewListener implements Listener {

	private AdvancedAchievements plugin;

	public AchieveTradeAnvilBrewListener(AdvancedAchievements plugin) {

		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onInventoryClickEvent(InventoryClickEvent event) {

		if (event.getRawSlot() != 0 && event.getRawSlot() != 1 && event.getRawSlot() != 2
				|| event.getCurrentItem().getType().name().equals("AIR"))
			return;
		Player player = (Player) event.getWhoClicked();
		if (!player.hasPermission("achievement.get") || plugin.isRestrictCreative()
				&& player.getGameMode() == GameMode.CREATIVE || plugin.isInExludedWorld(player))
			return;
		String configAchievement = "";

		if (event.getRawSlot() == 2 && event.getInventory().getType().name().equals("MERCHANT")) {
			Integer trades = plugin.getDb().incrementAndGetNormalAchievement(player, "trades");
			configAchievement = "Trades." + trades;

		} else if (event.getRawSlot() == 2 && event.getInventory().getType().name().equals("ANVIL")) {
			Integer anvils = plugin.getDb().incrementAndGetNormalAchievement(player, "anvils");
			configAchievement = "AnvilsUsed." + anvils;

		} else if (event.getInventory().getType().name().equals("BREWING")) {
			Integer brewings = plugin.getDb().incrementAndGetNormalAchievement(player, "brewing");
			configAchievement = "Brewing." + brewings;

		} else
			return;

		if (plugin.getReward().checkAchievement(configAchievement)) {

			plugin.getAchievementDisplay().displayAchievement(player, configAchievement);
			SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
			plugin.getDb().registerAchievement(player, plugin.getConfig().getString(configAchievement + ".Name"),
					plugin.getConfig().getString(configAchievement + ".Message"), format.format(new Date()));

			plugin.getServer().getPluginManager().callEvent(new PlayerAchievementEvent(player, configAchievement));

			plugin.getReward().checkConfig(player, configAchievement);
		}
	}
}
