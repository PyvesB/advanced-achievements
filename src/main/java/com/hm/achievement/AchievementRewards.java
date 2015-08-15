package com.hm.achievement;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class AchievementRewards {

	private AdvancedAchievements plugin;

	public AchievementRewards(AdvancedAchievements achievement) {
		this.plugin = achievement;
	}

	public Boolean checkAchievement(String ach) {
		String check = plugin.getConfig().getString(ach + ".Message", "null");
		if (check.equals("null")) {
			return false;
		} else {
			return true;
		}
	}

	public ItemStack getItemReward(Player player, String ach, int amount) {

		ItemStack item = new ItemStack(Material.getMaterial(plugin.getConfig()
				.getString(ach + ".Reward.Item.Type", "stone").toUpperCase()),
				amount);
		if (plugin.getLanguage().equals("fr"))
			player.sendMessage(ChatColor.GRAY + "[" + ChatColor.DARK_PURPLE
					+ plugin.getIcon() + ChatColor.GRAY + "] "
					+ "Vous avez reçu une récompense!");
		else
			player.sendMessage(ChatColor.GRAY + "[" + ChatColor.DARK_PURPLE
					+ plugin.getIcon() + ChatColor.GRAY + "] "
					+ "В награду за достижение получено:");
		return item;
	}

	@SuppressWarnings("deprecation")
	public void rewardMoney(Player player, Integer amount) {
		if (plugin.setupEconomy()) {
			String price = Integer.toString(amount);
			double amtd = Double.valueOf(price.trim());
			if (plugin.isRetroVault())
				plugin.getEconomy().depositPlayer(player.getName(), amtd);
			else
				plugin.getEconomy().depositPlayer(player, amtd);
			if (plugin.getLanguage().equals("fr"))
				player.sendMessage(ChatColor.GRAY + "[" + ChatColor.DARK_PURPLE
						+ plugin.getIcon() + ChatColor.GRAY + "] "
						+ "Vous avez gagné : " + ChatColor.DARK_PURPLE + amtd
						+ " " + plugin.getEconomy().currencyNamePlural() + "!");
			else
				player.sendMessage(ChatColor.GRAY + "[" + ChatColor.DARK_PURPLE
						+ plugin.getIcon() + ChatColor.GRAY + "] "
						+ "Получено: " + ChatColor.DARK_PURPLE + amtd + " "
						+ plugin.getEconomy().currencyNamePlural() + "!");
		}
	}

	public void checkConfig(Player player, String configAchievement) {

		int money = plugin.getConfig().getInt(
				configAchievement + ".Reward.Money.Amount", 0);
		int itemAmount = plugin.getConfig().getInt(
				configAchievement + ".Reward.Item.Amount", 0);
		String command = plugin.getConfig().getString(
				configAchievement + ".Reward.Command", "");

		if (money != 0) {
			this.rewardMoney(player, money);
		}
		if (itemAmount != 0) {
			ItemStack item = this.getItemReward(player, configAchievement,
					itemAmount);
			PlayerInventory inv = player.getInventory();
			inv.addItem(item);
		}
		if (!command.equals("")) {

			command = command.replace("PLAYER", player.getName());
			plugin.getServer().dispatchCommand(
					plugin.getServer().getConsoleSender(), command);
			if(! plugin.isRewardCommandNotif()) return;
			if (plugin.getLanguage().equals("fr"))
				player.sendMessage(ChatColor.GRAY + "[" + ChatColor.DARK_PURPLE
						+ plugin.getIcon() + ChatColor.GRAY + "] "
						+ "Commande de récompense exécutée !"	);
			else
				player.sendMessage(ChatColor.GRAY + "[" + ChatColor.DARK_PURPLE
						+ plugin.getIcon() + ChatColor.GRAY + "] "
						+ "Награда за достижение получена!");

		}

	}

}
