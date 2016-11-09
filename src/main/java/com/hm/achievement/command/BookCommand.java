package com.hm.achievement.command;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.particle.ParticleEffect;
import com.hm.achievement.particle.ReflectionUtils.PackageType;

/**
 * Class in charge of handling the /aach book command, which creates and gives a book containing the player's
 * achievements.
 * 
 * @author Pyves
 */
public class BookCommand extends AbstractCommand {

	private int bookTime;
	private String bookSeparator;
	private boolean additionalEffects;
	private boolean sound;
	private int version;

	// Corresponds to times at which players have received their books. Cooldown structure.
	private HashMap<String, Long> players;

	public BookCommand(AdvancedAchievements plugin) {

		super(plugin);
		players = new HashMap<>();
		// Load configuration parameters.
		bookTime = plugin.getPluginConfig().getInt("TimeBook", 0) * 1000;
		bookSeparator = plugin.getPluginConfig().getString("BookSeparator", "");
		additionalEffects = plugin.getPluginConfig().getBoolean("AdditionalEffects", true);
		sound = plugin.getPluginConfig().getBoolean("Sound", true);
		// Simple and fast check to compare versions. Might need to be updated in the future depending on how the
		// Minecraft versions change in the future.
		version = Integer.parseInt(PackageType.getServerVersion().split("_")[1]);
	}

	@Override
	protected void executeCommand(CommandSender sender, String[] args) {

		if (!(sender instanceof Player))
			return;

		Player player = (Player) sender;

		if (timeAuthorisedBook(player)) {
			// Play special particle effect when receiving the book.
			if (additionalEffects) {
				try {
					ParticleEffect.ENCHANTMENT_TABLE.display(0, 2, 0, 1, 1000, player.getLocation(), 100);
				} catch (Exception e) {
					plugin.getLogger().severe("Error while displaying additional particle effects for books.");
				}
			}

			// Play special sound when receiving the book.
			if (sound) {
				if (version < 9) {
					// Old enum for versions prior to Minecraft 1.9. Retrieving it by name as it does no longer exist in
					// newer versions.
					player.getWorld().playSound(player.getLocation(), Sound.valueOf("LEVEL_UP"), 1, 0);
				} else {
					// Play sound with enum for newer versions.
					player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 0);
				}
			}

			List<String> achievements = plugin.getDb().getPlayerAchievementsList(player);

			fillBook(achievements, player);

			player.sendMessage(plugin.getChatHeader()
					+ plugin.getPluginLang().getString("book-received", "You received your achievements book!"));
		} else {
			// The player has already received a book recently.
			player.sendMessage(plugin.getChatHeader() + plugin.getPluginLang()
					.getString("book-delay", "You must wait TIME seconds between each book reception!")
					.replace("TIME", Integer.toString(bookTime / 1000)));
		}
	}

	/**
	 * Check if player hasn't received a book too recently (with "too recently" being defined in configuration file).
	 * 
	 * @param player
	 * @return whether a player is authorised to receive a book
	 */
	private boolean timeAuthorisedBook(Player player) {

		// Player bypasses cooldown if he has full plugin permissions.
		if (player.hasPermission("achievement.*"))
			return true;
		long currentTime = System.currentTimeMillis();
		long lastBookTime = 0;
		String uuid = player.getUniqueId().toString();
		if (players.containsKey(uuid))
			lastBookTime = players.get(uuid);
		if (currentTime - lastBookTime < bookTime)
			return false;
		players.put(uuid, currentTime);
		return true;
	}

	/**
	 * Construct the pages of a book.
	 * 
	 * @param achievements
	 * @param player
	 */
	private void fillBook(List<String> achievements, Player player) {

		ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
		ArrayList<String> pages = new ArrayList<>();
		BookMeta bm = (BookMeta) book.getItemMeta();

		try {
			// Elements in the array go by groups of 3: name, description and date.
			for (int i = 0; i < achievements.size(); i += 3) {
				String currentAchievement = "&0" + achievements.get(i) + "\n" + bookSeparator + "\n"
						+ achievements.get(i + 1) + "\n" + bookSeparator + "\n&r" + achievements.get(i + 2);
				currentAchievement = ChatColor.translateAlternateColorCodes('&', currentAchievement);
				pages.add(currentAchievement);
			}
		} catch (Exception e) {
			// Catch runtime exception (for instance ArrayIndexOutOfBoundsException); this should not happen unless
			// something went wrong in the database.
			plugin.getLogger().severe("Error while creating book pages of book.");
		}

		// Set the pages and other elements of the book (author, title and date of reception).
		bm.setPages(pages);
		bm.setAuthor(player.getName());
		bm.setTitle(plugin.getPluginLang().getString("book-name", "Achievements Book"));
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		bm.setLore(Arrays.asList(ChatColor.translateAlternateColorCodes('&', "&r&o" + plugin.getPluginLang()
				.getString("book-date", "Book created on DATE.").replace("DATE", format.format(new Date())))));

		book.setItemMeta(bm);

		// Check whether player has room in his inventory, else drop book on the ground.
		if (player.getInventory().firstEmpty() != -1)
			player.getInventory().addItem(book);
		else
			player.getWorld().dropItem(player.getLocation(), book);

	}

	/**
	 * Retrieve cooldown structure.
	 * 
	 * @return book cooldown structure
	 */
	public Map<String, Long> getPlayers() {

		return players;
	}

}
