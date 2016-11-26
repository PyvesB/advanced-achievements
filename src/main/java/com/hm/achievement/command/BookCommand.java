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

/**
 * Class in charge of handling the /aach book command, which creates and gives a book containing the player's
 * achievements.
 * 
 * @author Pyves
 */
public class BookCommand extends AbstractCommand {

	private final int bookTime;
	private final String bookSeparator;
	private final boolean additionalEffects;
	private final boolean sounds;

	// Corresponds to times at which players have received their books. Cooldown structure.
	private final HashMap<String, Long> playersBookTime;

	public BookCommand(AdvancedAchievements plugin) {

		super(plugin);
		playersBookTime = new HashMap<>();
		// Load configuration parameters.
		bookTime = plugin.getPluginConfig().getInt("TimeBook", 0) * 1000;
		bookSeparator = plugin.getPluginConfig().getString("BookSeparator", "");
		additionalEffects = plugin.getPluginConfig().getBoolean("AdditionalEffects", true);
		sounds = plugin.getPluginConfig().getBoolean("Sound", true);
	}

	@Override
	protected void executeCommand(CommandSender sender, String[] args) {

		if (!(sender instanceof Player)) {
			return;
		}

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
			if (sounds) {
				Sound sound;
				if (version < 9) {
					// Old enum for versions prior to Minecraft 1.9. Retrieving it by name as it does no longer exist in
					// newer versions.
					sound = Sound.valueOf("LEVEL_UP");
				} else {
					// Play sound with enum for newer versions.
					sound = Sound.ENTITY_PLAYER_LEVELUP;
				}
				player.getWorld().playSound(player.getLocation(), sound, 1.0f, 0.0f);
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
	 * Checks if player hasn't received a book too recently (with "too recently" being defined in configuration file).
	 * 
	 * @param player
	 * @return whether a player is authorised to receive a book
	 */
	private boolean timeAuthorisedBook(Player player) {

		// Player bypasses cooldown if he has full plugin permissions.
		if (player.hasPermission("achievement.*")) {
			return true;
		}
		long currentTime = System.currentTimeMillis();
		long lastBookTime = 0;
		String uuid = player.getUniqueId().toString();
		if (playersBookTime.containsKey(uuid)) {
			lastBookTime = playersBookTime.get(uuid);
		}
		if (currentTime - lastBookTime < bookTime) {
			return false;
		}
		playersBookTime.put(uuid, currentTime);
		return true;
	}

	/**
	 * Constructs the pages of a book.
	 * 
	 * @param achievements
	 * @param player
	 */
	private void fillBook(List<String> achievements, Player player) {

		ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
		ArrayList<String> pages = new ArrayList<>(achievements.size() / 3);
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
		if (player.getInventory().firstEmpty() != -1) {
			player.getInventory().addItem(book);
		} else {
			player.getWorld().dropItem(player.getLocation(), book);
		}
	}

	public Map<String, Long> getPlayersBookTime() {

		return playersBookTime;
	}
}
