package com.hm.achievement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import com.hm.achievement.language.Lang;

public class AchievementBookGiver {

	private AdvancedAchievements plugin;
	private HashMap<Player, Long> players;

	public AchievementBookGiver(AdvancedAchievements plugin) {

		this.plugin = plugin;
		players = new HashMap<Player, Long>();
	}

	/**
	 * Check is player hasn't received a book too recently.
	 */
	private boolean timeAuthorisedBook(Player player) {

		long currentTime = System.currentTimeMillis();
		long lastBookTime = 0;
		if (players.containsKey(player))
			lastBookTime = players.get(player);
		if (currentTime - lastBookTime < plugin.getBookTime())
			return false;
		players.put(player, currentTime);
		return true;

	}

	/**
	 * Give an achievements book to the player.
	 */
	public void giveBook(Player player, String name) {

		if (timeAuthorisedBook(player)) {

			// Play special sound when receiving the book.
			if (plugin.isSound())
				player.getWorld().playSound(player.getLocation(), Sound.LEVEL_UP, 1, 0);

			// Up to four achievement books can be generated (= 200
			// achievements received by a player).
			ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
			ItemStack book2 = new ItemStack(Material.WRITTEN_BOOK);
			ItemStack book3 = new ItemStack(Material.WRITTEN_BOOK);
			ItemStack book4 = new ItemStack(Material.WRITTEN_BOOK);
			ArrayList<String> achievements = plugin.getDb().getAchievements(player);
			ArrayList<String> pages = new ArrayList<String>();
			ArrayList<String> pages2 = new ArrayList<String>();
			ArrayList<String> pages3 = new ArrayList<String>();
			ArrayList<String> pages4 = new ArrayList<String>();

			int i = 0;
			while (i < achievements.size() && i < 150) {
				try {
					String currentAchievement = achievements.get(i) + "\n" + plugin.getBookSeparator() + "\n"
							+ achievements.get(i + 1) + "\n" + plugin.getBookSeparator() + "\n"
							+ achievements.get(i + 2);
					currentAchievement = ChatColor.translateAlternateColorCodes('&', "&0" + currentAchievement);
					pages.add(currentAchievement);
					i = i + 3;
				} catch (Exception e) {
					Logger log = plugin.getLogger();
					log.severe("Error while creating book pages of book 1.");
				}

			}

			BookMeta bm = (BookMeta) book.getItemMeta();

			bm.setPages(pages);

			bm.setAuthor(name);

			bm.setTitle(Lang.BOOK_NAME.toString());

			book.setItemMeta(bm);
			if (player.getInventory().firstEmpty() != -1)
				player.getInventory().addItem(new ItemStack[] { book });
			else
				for (ItemStack item : new ItemStack[] { book })
					player.getWorld().dropItem(player.getLocation(), item);

			if (i > 149 && achievements.size() != 150) {
				while (i < achievements.size() && i < 300) {
					try {
						String currentAchievement = achievements.get(i) + "\n" + plugin.getBookSeparator() + "\n"
								+ achievements.get(i + 1) + "\n" + plugin.getBookSeparator() + "\n"
								+ achievements.get(i + 2);
						currentAchievement = ChatColor.translateAlternateColorCodes('&', "&0" + currentAchievement);
						pages2.add(currentAchievement);
						i = i + 3;
					} catch (Exception e) {
						Logger log = plugin.getLogger();
						log.severe("Error while creating book pages of book 2.");
					}

				}
				BookMeta bm2 = (BookMeta) book2.getItemMeta();

				bm2.setPages(pages2);

				bm2.setAuthor(name);

				bm2.setTitle(Lang.BOOK_NAME + " 2");

				book2.setItemMeta(bm2);
				if (player.getInventory().firstEmpty() != -1)
					player.getInventory().addItem(new ItemStack[] { book2 });
				else
					for (ItemStack item : new ItemStack[] { book2 })
						player.getWorld().dropItem(player.getLocation(), item);
			}

			if (i > 299 && achievements.size() != 300) {
				while (i < achievements.size() && i < 450) {
					try {
						String currentAchievement = achievements.get(i) + "\n" + plugin.getBookSeparator() + "\n"
								+ achievements.get(i + 1) + "\n" + plugin.getBookSeparator() + "\n"
								+ achievements.get(i + 2);
						currentAchievement = ChatColor.translateAlternateColorCodes('&', "&0" + currentAchievement);
						pages3.add(currentAchievement);
						i = i + 3;
					} catch (Exception e) {
						Logger log = plugin.getLogger();
						log.severe("Error while creating book pages of book 3.");
					}

				}
				BookMeta bm3 = (BookMeta) book3.getItemMeta();

				bm3.setPages(pages3);

				bm3.setAuthor(name);

				bm3.setTitle(Lang.BOOK_NAME + " 3");

				book3.setItemMeta(bm3);
				if (player.getInventory().firstEmpty() != -1)
					player.getInventory().addItem(new ItemStack[] { book3 });
				else
					for (ItemStack item : new ItemStack[] { book3 })
						player.getWorld().dropItem(player.getLocation(), item);
			}

			if (i > 449 && achievements.size() != 450) {
				while (i < achievements.size() && i < 600) {
					try {
						String currentAchievement = achievements.get(i) + "\n" + plugin.getBookSeparator() + "\n"
								+ achievements.get(i + 1) + "\n" + plugin.getBookSeparator() + "\n"
								+ achievements.get(i + 2);
						currentAchievement = ChatColor.translateAlternateColorCodes('&', "&0" + currentAchievement);
						pages4.add(currentAchievement);
						i = i + 3;
					} catch (Exception e) {
						Logger log = plugin.getLogger();
						log.severe("Error while creating book pages of book 4.");
					}

				}
				BookMeta bm4 = (BookMeta) book4.getItemMeta();

				bm4.setPages(pages4);

				bm4.setAuthor(name);

				bm4.setTitle(Lang.BOOK_NAME + " 4");

				book4.setItemMeta(bm4);
				if (player.getInventory().firstEmpty() != -1)
					player.getInventory().addItem(new ItemStack[] { book4 });
				else
					for (ItemStack item : new ItemStack[] { book4 })
						player.getWorld().dropItem(player.getLocation(), item);
			}

			player.sendMessage(ChatColor.GRAY + "[" + ChatColor.DARK_PURPLE + plugin.getIcon() + ChatColor.GRAY + "] "
					+ Lang.BOOK_RECEIVED);
		} else {
			// The player has already received a book recently.
			player.sendMessage(ChatColor.GRAY + "[" + ChatColor.DARK_PURPLE + plugin.getIcon() + ChatColor.GRAY + "] "
					+ Lang.BOOK_DELAY.toString().replace("TIME", "" + plugin.getBookTime() / 1000));
		}
	}

	public HashMap<Player, Long> getPlayers() {

		return players;
	}

}
