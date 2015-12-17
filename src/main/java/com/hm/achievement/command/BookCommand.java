package com.hm.achievement.command;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.language.Lang;
import com.hm.achievement.particle.ParticleEffect;

public class BookCommand {

	private AdvancedAchievements plugin;
	private HashMap<Player, Long> players;
	private int bookTime;
	private String bookSeparator;
	private boolean additionalEffects;
	private boolean sound;

	public BookCommand(AdvancedAchievements plugin) {

		this.plugin = plugin;
		players = new HashMap<Player, Long>();
		bookTime = plugin.getConfig().getInt("TimeBook", 0) * 1000;
		bookSeparator = plugin.getConfig().getString("BookSeparator", "");
		additionalEffects = plugin.getConfig().getBoolean("AdditionalEffects", true);
		sound = plugin.getConfig().getBoolean("Sound", true);
	}

	/**
	 * Check is player hasn't received a book too recently.
	 */
	private boolean timeAuthorisedBook(Player player) {

		if (player.isOp())
			return true;
		long currentTime = System.currentTimeMillis();
		long lastBookTime = 0;
		if (players.containsKey(player))
			lastBookTime = players.get(player);
		if (currentTime - lastBookTime < bookTime)
			return false;
		players.put(player, currentTime);
		return true;

	}

	/**
	 * Give an achievements book to the player.
	 */
	public void giveBook(Player player) {

		if (!player.isOp() && timeAuthorisedBook(player)) {

			// Play special effect when receiving the book.
			if (additionalEffects)
				try {
					ParticleEffect.ENCHANTMENT_TABLE.display(0, 2, 0, 1, 1000, player.getLocation(), 100);
				} catch (Exception ex) {
					plugin.getLogger().severe("Error while displaying additional particle effects.");
				}

			// Play special sound when receiving the book.
			if (sound)
				player.getWorld().playSound(player.getLocation(), Sound.LEVEL_UP, 1, 0);

			ArrayList<String> achievements = plugin.getDb().getPlayerAchievementsList(player);

			int i = 0;
			// Up to four achievement books can be generated (= 200
			// achievements received by a player).
			if (i == 0)
				i = fillBook(achievements, player, i, 150, 1);

			if (i > 149 && achievements.size() != 150)
				i = fillBook(achievements, player, i, 300, 2);

			if (i > 299 && achievements.size() != 300)
				i = fillBook(achievements, player, i, 450, 3);

			if (i > 449 && achievements.size() != 450)
				i = fillBook(achievements, player, i, 600, 4);

			player.sendMessage(plugin.getChatHeader() + Lang.BOOK_RECEIVED);
		} else {
			// The player has already received a book recently.
			player.sendMessage(plugin.getChatHeader()
					+ Lang.BOOK_DELAY.toString().replace("TIME", "" + bookTime / 1000));
		}
	}

	private int fillBook(ArrayList<String> achievements, Player player, int i, int max, int bookNumber) {

		ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
		ArrayList<String> pages = new ArrayList<String>();
		BookMeta bm = (BookMeta) book.getItemMeta();

		try {
			while (i < achievements.size() && i < max) {
				String currentAchievement = achievements.get(i) + "\n" + bookSeparator + "\n" + achievements.get(i + 1)
						+ "\n" + bookSeparator + "\n" + achievements.get(i + 2);
				currentAchievement = ChatColor.translateAlternateColorCodes('&', "&0" + currentAchievement);
				pages.add(currentAchievement);
				i = i + 3;
			}
		} catch (Exception e) {
			plugin.getLogger().severe("Error while creating book pages of book " + bookNumber + ".");
		}

		bm.setPages(pages);
		bm.setAuthor(player.getName());
		bm.setTitle(Lang.BOOK_NAME.toString());

		book.setItemMeta(bm);

		if (player.getInventory().firstEmpty() != -1)
			player.getInventory().addItem(new ItemStack[] { book });
		else
			for (ItemStack item : new ItemStack[] { book })
				player.getWorld().dropItem(player.getLocation(), item);

		return i;
	}

	public HashMap<Player, Long> getPlayers() {

		return players;
	}

}
